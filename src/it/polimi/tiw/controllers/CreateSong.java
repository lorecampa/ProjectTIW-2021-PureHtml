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


@WebServlet("/CreateSong")
@MultipartConfig
public class CreateSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String imagePath = null;
	private String audioPath = null;
	private TemplateEngine templateEngine;
	private Connection connection = null;

       
    
    public CreateSong() {
        super();
    }
    
    
    public void init() throws UnavailableException {
    	
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
		//starting path for saving images and audio files
    	imagePath = getServletContext().getInitParameter("imagePath");
    	audioPath = getServletContext().getInitParameter("audioPath");
    	
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
		String albumTitle = request.getParameter("album");
		String interpreter = request.getParameter("interpreter");
		String yearString = request.getParameter("year");
		String genreString = request.getParameter("genre");

		
		if (title == null || title.isEmpty() || 
				albumTitle == null || albumTitle.isEmpty() ||
				interpreter  == null || interpreter.isEmpty() ||
				yearString == null || yearString.isEmpty() ||
				genreString == null || genreString.isEmpty()) {
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parametres");
			return;
		}
		
		Short year = -1;
		try {
			year = Short.parseShort(yearString);
			// year limit
			if (year < 0 || year > 3000) {
				//return error to home page
				String msg = "The year must be bewteween 0 and 3000";
				String path = "GetHomePage";
				response.sendRedirect(path + "?errorCreateSong="+msg);
				return;
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing year when creating song");
			return;
		}
		Genre genre = Genre.fromString(genreString);
		if (genre.getDisplayName().equals("Not found")){
			//return error to home page
			String msg = "Genre is not from list";
			String path = "GetHomePage";
			response.sendRedirect(path + "?errorCreateSong="+msg);
			return;
		}
		
		Part imagePart = request.getPart("picture");
		Part audioPart = request.getPart("audio");
		
		// We first check the parameter needed is present
		if (imagePart == null || imagePart.getSize() <= 0 ||
			audioPart == null || imagePart.getSize() <= 0){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file in request!");
			return;
		}
		
		
		
		Album album = new Album(albumTitle, interpreter, year, genre);
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		int created = 0;
		try {
			created = albumDAO.createAlbum(album);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating album");
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
		
		//create initial song		
		Song song = new Song(title, user.getId(), album.getId());
		SongDAO songDAO = new SongDAO(connection);
		
		
		//si può fare anche la creazione come nell'album se si vuole -> pensarci
		//control if the song already exsist
		created = 0;
		try {
			created = songDAO.createSong(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating initial song");
			return;
		}
		
		if (created == 0) {
			//return error to home page
			String msg = "Song already present in your list";
			String path = "GetHomePage";
			response.sendRedirect(path + "?errorCreateSong="+msg);
			return;
		}
		
		
		
		try {
			song.setId(songDAO.findSongId(song));
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding song");
			return;
		}
		
		
		
		//control type and then make some controls
		
		String imageFileName = Paths.get(imagePart.getSubmittedFileName()).getFileName().toString();
		int indexImage = imageFileName.lastIndexOf('.');
		String imageExt = "";
		
		String audioFileName = Paths.get(audioPart.getSubmittedFileName()).getFileName().toString();
		int indexAudio = audioFileName.lastIndexOf('.');
		String audioExt = "";
		
		imageExt = imageFileName.substring(indexImage);
		audioExt = audioFileName.substring(indexAudio);
		
		
		
		//imagePath and audioPath refers to the path initialized in the init part
		String imageOutputPath = imagePath + "/" + user.getId() + "_" + song.getId() + "_" + album.getId() + "_" + new Timestamp(System.currentTimeMillis()) + imageExt;
		String audioOutputPath = audioPath + "/" + user.getId() + "_" + song.getId() + "_" + album.getId() + "_" + new Timestamp(System.currentTimeMillis()) + audioExt;
		
		
		File imageFile = new File(imageOutputPath);
		File audioFile = new File(audioOutputPath);
		
		try (InputStream fileContent = imagePart.getInputStream()) {
			
			Files.copy(fileContent, imageFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			try {
				songDAO.removeInitialSong(song);
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue removing initial song");
				return;
			}
			
		}
		
		try (InputStream fileContent = audioPart.getInputStream()) {

			Files.copy(fileContent, audioFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				songDAO.removeInitialSong(song);
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue removing initial song");
				return;
			}
			
		}
		
		song.setImageUrl(imageOutputPath);
		song.setSongUrl(audioOutputPath);
		
		int update = 0;
		try {
			update = songDAO.updateSongPath(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue updating song path files");
			return;
		}
		
		
		
		response.sendRedirect("GetHomePage");
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}