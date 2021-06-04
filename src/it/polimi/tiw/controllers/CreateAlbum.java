package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Genre;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/CreateAlbum")
@MultipartConfig
public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String imagePath = null;
	private TemplateEngine templateEngine;
	private Connection connection = null;

       
    public CreateAlbum() {
        super();
    }
    
    
    public void init() throws UnavailableException {
    	
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
		//starting path for saving images and audio files
    	imagePath = getServletContext().getInitParameter("imagePath");
    	
    }
    

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}



	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		String title = request.getParameter("title");
		String interpreter = request.getParameter("interpreter");
		String yearString = request.getParameter("year");
		String genreString = request.getParameter("genre");

		
		if (title == null || title.isEmpty() || 
				interpreter  == null || interpreter.isEmpty() ||
				yearString == null || yearString.isEmpty() ||
				genreString == null || genreString.isEmpty()) {
			
			forwardToErrorPage(request, response, ErrorType.CREATE_ALBUM_BAD_PARAMETERS.getMessage());
			return;
		}
		
		Short year;
		try {
			Integer yearInteger = Integer.parseInt(yearString);
			// year limit [0 - currentYear + 1]
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			if (yearInteger < 0 || yearInteger > currentYear + 1) {
				String msg = "Year must be beetween [0 - " + (currentYear + 1) + "]";
				redirectToHomePage(session, response, msg);
				return;
			}
			year = yearInteger.shortValue();
		} catch (NumberFormatException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		Genre genre = Genre.fromString(genreString);
		if (genre.getDisplayName().equals("Not found")){
			forwardToErrorPage(request, response, ErrorType.CREATE_ALBUM_BAD_PARAMETERS.getMessage());
			return;
		}
		
		
		Part imagePart = request.getPart("picture");
		// We first check the parameter needed is present
		if (imagePart == null || imagePart.getSize() <= 0) {
			forwardToErrorPage(request, response, ErrorType.CREATE_ALBUM_BAD_PARAMETERS.getMessage());
			return;
		}
		
		String contentType = imagePart.getContentType();
		//check if the file is an image
		if (!contentType.startsWith("image")) {
			redirectToHomePage(session, response, ErrorType.IMAGE_TYPE_NOT_PERMITTED.getMessage());
			return;
		}
		
		//get image file extension
		String imageFileName = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
		int indexImage = imageFileName.lastIndexOf('.');
		String imageExt = imageFileName.substring(indexImage);
		
		
		Album album = new Album(title, interpreter, year, genre, user.getId(), imageExt);
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		int created;
		try {
			//return 0 if already present in our database (title, interpreter, year, genre, idCreator) is a unique constraint
			created = albumDAO.createAlbum(album);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		if(created == 0) {
			redirectToHomePage(session, response, ErrorType.ALBUM_ALREADY_PRESENT.getMessage());
			return;
		}
				
		
		//imagePath refers to the path initialized in the init part
		String imageOutputPath = imagePath + album.getImageUrl();
		
		//save image
		File imageFile = new File(imageOutputPath);
		try (InputStream fileContent = imagePart.getInputStream()) {
			
			Files.copy(fileContent, imageFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			forwardToErrorPage(request, response, e.getMessage());
			return;
			
		}
		
		
		String msg = "Album " + album.getTitle() + " created succesfully";	
		redirectToHomePage(session, response, msg);
		
	}
	
	private void redirectToHomePage(HttpSession session, HttpServletResponse response, String message) throws ServletException, IOException{
		session.setAttribute("albumWarning", message);
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.HOME_SERVLET);
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