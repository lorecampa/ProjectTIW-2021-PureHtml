package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/ShowFile/*")
public class ShowFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	   

	String imagePath = "";
	String audioPath = "";

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
		// get folder path from webapp init parameters inside web.xml
		imagePath = getServletContext().getInitParameter("imagePath");
		audioPath = getServletContext().getInitParameter("audioPath");

		
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String pathInfo = request.getPathInfo();
		
		if (pathInfo == null || pathInfo.equals("/")) {
			forwardToErrorPage(request, response, ErrorType.FILE_BAD_PARAMETER.getMessage());
			return;
		}
		
		int index = pathInfo.lastIndexOf("_");
		String fileType = pathInfo.substring(1, index);
		String fileId = pathInfo.substring(index + 1);
		
		//control file type
		String filePath;
		if (fileType.equals("image")) {
			filePath = imagePath;
		}else if(fileType.equals("audio")) {
			filePath = audioPath;
		}
		else {
			forwardToErrorPage(request, response, ErrorType.FILE_BAD_PARAMETER.getMessage());
			return;
		}
		
		
		URLDecoder.decode(fileId, "UTF-8");
		File file = new File(filePath + fileId); 

		if (!file.exists() || file.isDirectory()) {
			forwardToErrorPage(request, response, ErrorType.FILE_NOT_EXIST.getMessage());
			return;
		}

		// set headers for browser
		response.setHeader("Content-Type", getServletContext().getMimeType(fileId));
		response.setHeader("Content-Length", String.valueOf(file.length()));
		
		response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
																									
		// copy file to output stream
		Files.copy(file.toPath(), response.getOutputStream());

	}
	
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
		
	}
	
	private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{
		request.setAttribute("error", error);
		forward(request, response, PathUtils.ERROR_PAGE);
		return;
	}
}
