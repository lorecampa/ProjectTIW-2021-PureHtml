package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Playlist;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.MatchDAO;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.SessionControlHandler;
import it.polimi.tiw.utils.TymeleafHandler;
import it.polimi.tiw.utils.*;


@WebServlet("/GetPlaylist")
public class GetPlaylist extends HttpServlet {
	private Connection connection = null;
	private TemplateEngine templateEngine;
	public final int NUM_SLIDE_SONG = 5;

	   
    
    public GetPlaylist() {
        super();
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
	}
    

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionControlHandler.isSessionValidate(request, response))	return;
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		int idUser = user.getId();
		
		//getting id playlist selected
		String idPlaylistString = request.getParameter("idPlaylist");
		String currentSlideString = request.getParameter("currentSlide");
		
		int idPlaylist;
		Integer currentSlide;
		try {
			idPlaylist = Integer.parseInt(idPlaylistString);
			currentSlide = Integer.parseInt(currentSlideString);
		}catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing idPlaylist or currentSlide query string parameter");
			return;
		}
		
		
		
				 
		//finding playlist
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		Playlist playlist;
		try {
			playlist = playlistDAO.findPlaylistById(idPlaylist);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue getting playlist information");
			return;
		}
		
		//control that the playlist belongs to the user session
		if(playlist == null || (playlist.getIdCreator() != user.getId()) || currentSlide < 0) {
			session.invalidate();
			String path = "SubmitLogin";
			String msg = "You are trying to access wrong information. Login again to identify yourself ";
			response.sendRedirect(path+"?logout=" + msg);
			return;
		}
		
		
		
		
		MatchDAO matchDAO = new MatchDAO(connection);
		//list of song id present on the playlist
		ArrayList<Integer> playlistSongIds = new ArrayList<>();
		
		try {
			playlistSongIds = matchDAO.findAllSongIdOfPlaylist(idPlaylist, idUser);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue retrieving playlist song information");
			return;
		}
		
		SongDAO songDAO = new SongDAO(connection);
		AlbumDAO albumDAO = new AlbumDAO(connection);

		ArrayList<Song> songs = new ArrayList<>();
		ArrayList<Album> albums = new ArrayList<>();
		Song song;
		Album album;
		//find all songs in playlist selected
		for(int id: playlistSongIds) {
			try {
				//return null if song is not present
				song = songDAO.findSongById(id);
				if (song != null) {
					album = albumDAO.findAlumById(song.getIdAlbum());
					if (album != null) {
						songs.add(song);
						albums.add(album);
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving playlist song or album information");
				return;
			}
			
			
		}
		
		
		//lists of song that the user can select and add to the playlist
		ArrayList<Song> userSongsSelection = new ArrayList<>();
		try {
			ArrayList<Song> tempSelection = new ArrayList<>();
			//return empty list if there are no songs
			tempSelection = songDAO.findAllSongByUserId(idUser);
			for (Song songTemp: tempSelection) {
				if(!playlistSongIds.contains(songTemp.getId())) {
					userSongsSelection.add(songTemp);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding user's songs");
			return;
		}		
		
		
		int sizeSongs = songs.size();
		//control currentSlide correctness
		int div = (sizeSongs / NUM_SLIDE_SONG);
		if (currentSlide < 0 || currentSlide > div) {
			session.invalidate();
			String path = "SubmitLogin";
			String msg = "You are trying to access wrong information. Login again to identify yourself ";
			response.sendRedirect(path+"?logout=" + msg);
			return;
		}
		
		int fromIndex = currentSlide * NUM_SLIDE_SONG;
		int toIndex = fromIndex + 5;
		boolean isPrevActive = false;
		boolean isNextActive = false;
		
		if (sizeSongs <= toIndex) {
			toIndex = sizeSongs;
			isNextActive = false;
		}else {
			isNextActive = true;
		}
	
		if(currentSlide != 0) {
			isPrevActive = true;
		}
		
		System.out.println("From Index: "+ fromIndex);
		System.out.println("To Index: "+ toIndex);
		System.out.println("prevActive: "+ isPrevActive);
		System.out.println("nextActive: "+ isNextActive);
		System.out.println("songsSize: "+ songs.size());


		
		
		
		String path = "/WEB-INF/Templates/PlaylistPage";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("playlist", playlist);
		ctx.setVariable("userSongsSelection", userSongsSelection);
		ctx.setVariable("songs", songs.subList(fromIndex, toIndex));
		ctx.setVariable("albums", albums.subList(fromIndex, toIndex));
		ctx.setVariable("isNextActive", isNextActive);
		ctx.setVariable("isPrevActive", isPrevActive);
		ctx.setVariable("currentSlide", currentSlide);

		


		
	
		
		templateEngine.process(path, ctx, response.getWriter());
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
