package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Playlist;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/GetPlayer")
public class GetPlayer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	   
    
    public GetPlayer() {
        super();
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		String idSongString = request.getParameter("idSong");
		String idPlaylistString = request.getParameter("idPlaylist");
		int idSong;
		int idPlaylist;
		try {
			idSong = Integer.parseInt(idSongString);
			idPlaylist = Integer.parseInt(idPlaylistString);
		}catch(NumberFormatException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		//finding playlist bean
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		Playlist playlist;
		try {
			playlist = playlistDAO.findPlaylistById(idPlaylist);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		//finding song bean
		SongDAO songDAO = new SongDAO(connection);
		Song song;
		try {
			song = songDAO.findSongById(idSong);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		//finding album bean
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album = null;
		try {
			if (song != null) {
				album = albumDAO.findAlumById(song.getIdAlbum());
			}
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}

		
		//control that the song belongs to the user session
		if(playlist == null || song == null || album == null ||
				(playlist.getIdCreator() != user.getId() ||
				(album.getIdCreator() != user.getId())) ) {
			
			forwardToErrorPage(request, response, ErrorType.SONG_NOT_EXSIST.getMessage());
			return;
		}	
		
		
		request.setAttribute("song", song);
		request.setAttribute("album", album);
		request.setAttribute("playlist", playlist);
		
		forward(request, response, PathUtils.PLAYER_PAGE);
		
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
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
