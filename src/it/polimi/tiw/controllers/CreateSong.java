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
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.SongDAO;

/**
 * Servlet implementation class CreateSong
 */
@WebServlet("/CreateSong")
@MultipartConfig
public class CreateSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String imagePath = null;
	private String audioPath = null;
	private TemplateEngine templateEngine;
	private Connection connection = null;

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateSong() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    
    public void init() throws UnavailableException {
    	
    	//starting path for saving images and audio files
    	imagePath = getServletContext().getInitParameter("imagePath");
    	audioPath = getServletContext().getInitParameter("audioPath");
    	
    	ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
    }
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String title = request.getParameter("title");
		String albumTitle = request.getParameter("album");
		String interpreter = request.getParameter("interpreter");
		String yearString = request.getParameter("year");
		String genreString = request.getParameter("genre");
		
		System.out.println("Title: "+ title);
		System.out.println("Album: "+ albumTitle);
		System.out.println("Interpeter: "+ interpreter);
		System.out.println("Year: "+ yearString);
		System.out.println("Genre: "+ genreString);
		
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
			// We assume in this example that a course with more than 50 cannot be created
			if (year < 0 || year > 3000) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The year must be bewteween 0 and 3000");
				return;
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing year...");
			return;
		}
		Genre genre = Genre.fromString(genreString);
		
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
		int idAlbum = 0;
		
		try {
			idAlbum = albumDAO.findAlbumId(album);
		} catch (SQLException e1) {
			e1.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding album");
			return;
		}
		
		if (idAlbum == -1) {
			//if album still not present
			//creation album
			int created = 0;
			try {
				created = albumDAO.createAlbum(album);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating album");
				return;
			}
			
			if (created == 0) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Album was not created");
				return;
			}
			
			//now that album is present in db
			//finding idAlbum
			try {
				idAlbum = albumDAO.findAlbumId(album);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding album");
				return;
			}
		}
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			//session's over
			response.sendRedirect("/MusicPlaylist/SubmitLogin");
			return;
		}
		int idCreator = (int) session.getAttribute("idUser");
		
		Song song = new Song(title, "NOT_YET", "NOT_YET", idCreator, idAlbum);
		SongDAO songDAO = new SongDAO(connection);
		
		//control if the song already exsist
		int idSong = 0;
		try {
			idSong = songDAO.findSongId(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding song");
			return;
		}
		if (idSong != -1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Song is already present in db");
			return;
		}
		
		int songCreated = 0;
		try {
			songCreated = songDAO.createInitialSong(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating initial song");
			return;
		}
		
		if (songCreated == 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Initial Song was not created");
			return;
		}
		
		try {
			idSong = songDAO.findSongId(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding song");
			return;
		}
		song.setId(idSong);
		
		String imageContentType = imagePart.getContentType();
		String audioContentType = audioPart.getContentType();
		System.out.println("Type image: " + imageContentType);
		System.out.println("TypeAudio: " + audioContentType);
		
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
		String imageOutputPath = imagePath + "/" + idCreator + "_" + idSong + "_" + idAlbum + "_" + new Timestamp(System.currentTimeMillis()) + imageExt;
		String audioOutputPath = audioPath + "/" + idCreator + "_" + idSong + "_" + idAlbum + "_" + new Timestamp(System.currentTimeMillis()) + audioExt;
		
		System.out.println("ImageOutputPath: " + imageOutputPath);
		System.out.println("AudioOutputPath: " + audioOutputPath);
		
		
		
		File imageFile = new File(imageOutputPath);
		File audioFile = new File(audioOutputPath);
		
		try (InputStream fileContent = imagePart.getInputStream()) {
			
			Files.copy(fileContent, imageFile.toPath());
			System.out.println("Image saved correctly!");

		} catch (Exception e) {
			e.printStackTrace();
			try {
				songDAO.removeInitialSong(song);
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue removing initial song");
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while saving image");
			
			return;
		}
		
		try (InputStream fileContent = audioPart.getInputStream()) {

			Files.copy(fileContent, audioFile.toPath());
			System.out.println("Audio saved correctly!");

		} catch (Exception e) {
			e.printStackTrace();
			try {
				songDAO.removeInitialSong(song);
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue removing initial song");
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while saving audio");
			return;
		}
		
		song.setImageUrl(imageOutputPath);
		song.setSongUrl(audioOutputPath);
		
		int updateSong = 0;
		
		try {
			updateSong = songDAO.updateSongPath(song);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue updating song image and audio path");
			return;
		}
		
		if(updateSong == 0) {
			try {
				songDAO.removeInitialSong(song);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error removing initial song after"
						+ "failing image and audio path updating");
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Song image and audio path was not updated");
			return;
		}
		
		
		
		
		response.sendRedirect("/MusicPlaylist/GetHomePage");
		
		
		
		
			
		
	}

}