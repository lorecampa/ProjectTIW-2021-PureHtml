package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

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


@WebServlet("/GetPlaylist")
public class GetPlaylist extends HttpServlet {
	private Connection connection = null;
	private TemplateEngine templateEngine;
	   
    
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
		int idPlaylist;
		try {
			idPlaylist = Integer.parseInt(idPlaylistString);
		}catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing idPlaylist query string parameter");
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
		ArrayList<Song> songs = new ArrayList<>();
		
		for(int id: playlistSongIds) {
			try {
				songs.add(songDAO.findSongById(id));
				
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving playlist song information");
				return;
			}
		}
		
		
		//lists of song that the user can select and add to the playlist
		ArrayList<Song> userSongsSelection = new ArrayList<>();
		try {
			ArrayList<Song> tempSelection = new ArrayList<>();
			tempSelection = songDAO.findAllSongByUserId(idUser);
			for (Song song: tempSelection) {
				if(!playlistSongIds.contains(song.getId())) {
					userSongsSelection.add(song);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding user's songs");
			return;
		}		
		
		
		
		
		
		String path = "/WEB-INF/Templates/PlaylistPage";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("playlist", playlist);
		ctx.setVariable("userSongsSelection", userSongsSelection);
		ctx.setVariable("songs", songs);
		
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
