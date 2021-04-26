package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
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
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
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
		doPost(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//session control
		if(!SessionControlHandler.isSessionValidate(request, response))	return;
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		String title = request.getParameter("title");
		String albumIdString = request.getParameter("albums");
		

		
		if (title == null || title.isEmpty() || 
				albumIdString == null || albumIdString.isEmpty()) {
			
			forwardToErrorPage(request, response, ErrorType.SONG_BAD_PARAMETERS.getMessage());
			return;
		}
		
		int albumId;
		try {
			albumId = Integer.parseInt(albumIdString);
		} catch (NumberFormatException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		
		//find album bean by id
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		try {
			//return null if not present
			album = albumDAO.findAlumById(albumId);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		//if idAlbum is doesn't belong to the user
		if (album == null || (album.getIdCreator() != user.getId())) {
			forwardToErrorPage(request, response, ErrorType.ALBUM_NOT_EXIST.getMessage());
			return;
		}
		
	
		
		Part audioPart = request.getPart("audio");
		
		// We first check the parameter needed is present
		if (audioPart == null || audioPart.getSize() <= 0){
			forwardToErrorPage(request, response, ErrorType.SONG_BAD_PARAMETERS.getMessage());
			return;
		}
		
		String contentType = audioPart.getContentType();
		//check if the file is an audio
		if (!contentType.startsWith("audio")) {
			forwardToErrorPage(request, response, ErrorType.AUDIO_TYPE_NOT_PERMITTED.getMessage());
			return;
		}
		
		//find audio extension
		String audioFileName = Paths.get(audioPart.getSubmittedFileName()).getFileName().toString();
		int indexAudio = audioFileName.lastIndexOf('.');
		String audioExt = "";
		audioExt = audioFileName.substring(indexAudio);
	
		//create song (audioUrl is set by the database)
		Song song = new Song(title, null, album.getId());
		SongDAO songDAO = new SongDAO(connection);
		int created;
		try {
			//return 0 if the song was already present (title, albumId) are a unique constraint
			created = songDAO.createSong(song, audioExt);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		if (created == 0) {
			//return error to home page
			redirectToHomePage(session, response, ErrorType.SONG_ALREADY_PRESENT.getMessage());
			return;
		}
		
		
		//find song id that is just created
		try {
			song.setId(songDAO.findSongId(song));
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
			
		
		String audioId = "" + song.getId() + "-" + song.getIdAlbum() + audioExt;
		
		//audioPath refers to the path initialized in the init part
		String audioOutputPath = audioPath + audioId;
						
		File audioFile = new File(audioOutputPath);
		
		
		try (InputStream fileContent = audioPart.getInputStream()) {

			Files.copy(fileContent, audioFile.toPath());

		} catch (Exception e) {
			e.printStackTrace();
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
	
		String msg = "Song " + song.getTitle() + " created succesfully";
		redirectToHomePage(session, response, msg);
		
		
	}
	
	private void redirectToHomePage(HttpSession session, HttpServletResponse response, String message) throws ServletException, IOException{
		session.setAttribute("songWarning", message);
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