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
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parametres");
			return;
		}
		
		Short year;
		try {
			Integer yearInteger = Integer.parseInt(yearString);
			// year limit
			if (yearInteger < 0 || yearInteger > 3000) {
				//return error to home page
				String msg = "The year must be bewteween 0 and 3000";
				String path = "GetHomePage";
				response.sendRedirect(path + "?errorCreateAlbum="+msg);
				return;
			}
			year = yearInteger.shortValue();
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing year when creating song");
			return;
		}
		
		Genre genre = Genre.fromString(genreString);
		if (genre.getDisplayName().equals("Not found")){
			//return error to home page
			String msg = "Genre is not from list";
			String path = "GetHomePage";
			response.sendRedirect(path + "?errorCreateAlbum="+msg);
			return;
		}
		
		Part imagePart = request.getPart("picture");
		
		// We first check the parameter needed is present
		if (imagePart == null || imagePart.getSize() <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file in request!");
			return;
		}
		
		String contentType = imagePart.getContentType();
		//check if the file is an image
		if (!contentType.startsWith("image")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image file format not permitted");
			return;
		}
		
		
		
		Album album = new Album(title, interpreter, year, genre, user.getId(), "LAZY_LOADING");
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		int albumCreated;
		try {
			//if albumCreated is 0 then it was already present in our database (title, interpreter, year, genre, idCreator) is a unique constraint
			albumCreated = albumDAO.createAlbum(album);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating album");
			return;
		}
		if(albumCreated == 0) {
			//album was already created
			String msg = "Album already present in your list";
			String path = "GetHomePage";
			response.sendRedirect(path + "?errorCreateAlbum="+msg);
			return;
		}
		
			
		//now that album is created
		try {
			//find id of album
			album.setId(albumDAO.findAlbumId(album));
			
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding album");
			return;
		}
		
		
		//control type and then make some controls
		
		String imageFileName = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
		int indexImage = imageFileName.lastIndexOf('.');
		String imageExt = "";
		imageExt = imageFileName.substring(indexImage);
		String imageId = "" + album.getIdCreator() + "-" + album.getId() + imageExt;
		
		//imagePath and audioPath refers to the path initialized in the init part
		String imageOutputPath = imagePath + imageId;
		
		System.out.println("ImageOutputPath: " + imageOutputPath);

		
		
		
		File imageFile = new File(imageOutputPath);
		
		try (InputStream fileContent = imagePart.getInputStream()) {
			
			Files.copy(fileContent, imageFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			try {
				albumDAO.removeInitialAlbum(album.getId());
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue removing initial album after error in saving image");
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue saving album image in: " + imagePath);
			return;
			
		}
		
		album.setImageUrl(imageId);
		
		
		int updateAlbum;
		try {
			updateAlbum  = albumDAO.updateAlbum(album);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue updating song path files");
			return;
		}
		
		
		String msg;
		if (updateAlbum == 0) {
			msg = "Album: " + album.getId() + " image was not update. No images in database";
		}else {
			msg = "Album " + album.getTitle() + " created succesfully";
		}
		
		String path = "GetHomePage";
		response.sendRedirect(path + "?errorCreateAlbum="+msg);
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}