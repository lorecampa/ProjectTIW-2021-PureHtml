package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/ShowFile/*")
public class ShowFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	  

	String imagePath = "";
	String audioPath = "";

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
		// get folder path from webapp init parameters inside web.xml
		imagePath = getServletContext().getInitParameter("imagePath");
		audioPath = getServletContext().getInitParameter("audioPath");

		
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String pathInfo = request.getPathInfo();
		
		if (pathInfo == null || pathInfo.equals("/")) {
			forwardToErrorPage(request, response, ErrorType.FILE_BAD_PARAMETER.getMessage());
			return;
		}
		
		int indexExt = pathInfo.indexOf(".");
		String ext = pathInfo.substring(indexExt);
		String[] fileNameSplitted = pathInfo.substring(1, indexExt).split("_");
				
		String fileType = fileNameSplitted[0];
		String fileName = fileNameSplitted[1];
		
		int idAlbum;
		int idSong;
		try {
			if (fileNameSplitted.length == 3) {
				idSong = -1;
				idAlbum = Integer.parseInt(fileNameSplitted[2]);
				fileName +="_"+idAlbum;
			}else if(fileNameSplitted.length == 4){
				idSong = Integer.parseInt(fileNameSplitted[2]);
				idAlbum = Integer.parseInt(fileNameSplitted[3]);
				fileName += "_" + idSong + "_" + idAlbum;
			}else {
				throw new Exception();
			}
			fileName += ext;
		}catch(Exception e) {
			forwardToErrorPage(request, response, ErrorType.FILE_BAD_PARAMETER.getMessage());
			return;
		}
		
				
		//finding song bean
		SongDAO songDAO = new SongDAO(connection);
		Song song = null;
		try {
			if (idSong != -1) {
				song = songDAO.findSongById(idSong);
			}
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}		

		//finding album bean
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album = null;
		try {
			album = albumDAO.findAlumById(idAlbum);			
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		//control show file authenticity
		if ((song == null && idSong != -1) || album == null ||
				(idSong != -1 && song.getIdAlbum() != idAlbum) ||
				(album.getIdCreator() != user.getId())) {
			
			forwardToErrorPage(request, response, ErrorType.FILE_BAD_PARAMETER.getMessage());
			return;
		}
		
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
		
		
		URLDecoder.decode(fileName, "UTF-8");
		File file = new File(filePath + fileName); 

		if (!file.exists() || file.isDirectory()) {
			forwardToErrorPage(request, response, ErrorType.FILE_NOT_EXIST.getMessage());
			return;
		}

		// set headers for browser
		response.setHeader("Content-Type", getServletContext().getMimeType(fileName));
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
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
