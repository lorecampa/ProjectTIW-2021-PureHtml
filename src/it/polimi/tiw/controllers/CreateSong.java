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
		String albumIdString = request.getParameter("albums");
		

		
		if (title == null || title.isEmpty() || 
				albumIdString == null || albumIdString.isEmpty()) {
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parametres");
			return;
		}
		
		int albumId;
		try {
			albumId = Integer.parseInt(albumIdString);
			
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing album when creating song");
			return;
		}
		
		System.out.println("Albumid: " + albumId);
		
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		try {
			album = albumDAO.findAlumById(albumId);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error finding album in database");
			return;
		}
		//if idAlbum is doesn't belong to the user
		if (album == null || (album.getIdCreator() != user.getId())) {
			session.invalidate();
			String path = "SubmitLogin";
			String msg = "You are trying to access wrong information. Login again to identify yourself ";
			response.sendRedirect(path+"?logout=" + msg);
			return;
		}
	
		
		Part audioPart = request.getPart("audio");
		
		// We first check the parameter needed is present
		if (audioPart == null || audioPart.getSize() <= 0){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file in request!");
			return;
		}
		
		String contentType = audioPart.getContentType();
		//check if the file is an image
		if (!contentType.startsWith("audio")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Audio file format not permitted");
			return;
		}
		
		
	
		//create initial song		
		Song song = new Song(title, "LAZY_LOADING", album.getId());
		SongDAO songDAO = new SongDAO(connection);
		
		
		//create initial song
		int songCreated;
		try {
			//return 0 if the song was already present (title, albumId) are a unique constraint
			songCreated = songDAO.createSong(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating initial song");
			return;
		}
		
		if (songCreated == 0) {
			//return error to home page
			String msg = "Song already present in your list";
			String path = "GetHomePage";
			response.sendRedirect(path + "?errorCreateSong="+msg);
			return;
		}
		
		
		//find song id that was already created
		try {
			song.setId(songDAO.findSongId(song));
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding song after creation");
			return;
		}
		
		
		
		//control type and then make some controls
		
		String audioFileName = Paths.get(audioPart.getSubmittedFileName()).getFileName().toString();
		int indexAudio = audioFileName.lastIndexOf('.');
		String audioExt = "";
		
		audioExt = audioFileName.substring(indexAudio);
		
		String audioId = "" + song.getIdAlbum() + "-" + song.getId() + audioExt;
		
		//imagePath and audioPath refers to the path initialized in the init part
		String audioOutputPath = audioPath + audioId;
		
		System.out.println("AudioOutputPath: " + audioOutputPath);

				
		File audioFile = new File(audioOutputPath);
		
		
		try (InputStream fileContent = audioPart.getInputStream()) {

			Files.copy(fileContent, audioFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				songDAO.removeInitialSong(song);
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue removing initial song after error in saving audio");
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error saving audio in: " + audioPath);
			return;
			
		}
	
		
		song.setSongUrl(audioId);
		int updateSong;
		try {
			updateSong = songDAO.updateSong(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue updating song audio path files");
			return;
		}
		
		String msg;
		if (updateSong == 0) {
			msg = "Song: " + song.getId() + " audio was not update. No audio in database";
		}else {
			msg = "Song " + song.getTitle() + " created succesfully";
		}
		
		String path = "GetHomePage";
		response.sendRedirect(path + "?errorCreateSong="+msg);
		
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}