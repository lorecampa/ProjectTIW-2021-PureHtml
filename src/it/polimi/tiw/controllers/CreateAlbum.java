package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Genre;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.SessionControlHandler;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/CreateAlbum")
@MultipartConfig
public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String imagePath = null;
	private String audioPath = null;
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
		//nothing
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}



	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(!SessionControlHandler.isSessionValidate(request, response))	return;
		
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
			// year limit
			if (yearInteger < 0 || yearInteger > 3000) {
				forwardToErrorPage(request, response, ErrorType.CREATE_ALBUM_BAD_PARAMETERS.getMessage());
				return;
			}
			year = yearInteger.shortValue();
		} catch (NumberFormatException e) {
			forwardToErrorPage(request, response, ErrorType.CREATE_ALBUM_BAD_PARAMETERS.getMessage());
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
		
		Album album = new Album(title, interpreter, year, genre, user.getId(), null);
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		int created;
		try {
			//if albumCreated is 0 then it was already present in our database (title, interpreter, year, genre, idCreator) is a unique constraint
			created = albumDAO.createAlbum(album, imageExt);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, ErrorType.CREATING_ALBUM_ERROR.getMessage());
			return;
		}
		if(created == 0) {
			//album was already created
			redirectToHomePage(session, response, ErrorType.ALBUM_ALREADY_PRESENT.getMessage());
			return;
		}
		
		//setting album id
		try {
			album.setId(albumDAO.findAlbumId(album));
		} catch (SQLException e1) {
			e1.printStackTrace();
			redirectToHomePage(session, response, ErrorType.FINDING_ALBUM_ERROR.getMessage());
			return;
		}
		
		
		
		//control type and then make some controls
		String imageId = "" + album.getId() + "-" + album.getIdCreator() + imageExt;
		
		//imagePath refers to the path initialized in the init part
		String imageOutputPath = imagePath + imageId;
		
		File imageFile = new File(imageOutputPath);
		
		try (InputStream fileContent = imagePart.getInputStream()) {
			
			Files.copy(fileContent, imageFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			forwardToErrorPage(request, response, ErrorType.INTERNAL_SERVER_ERROR.getMessage());
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