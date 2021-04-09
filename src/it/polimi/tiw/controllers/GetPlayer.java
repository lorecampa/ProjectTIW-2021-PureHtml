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
import it.polimi.tiw.utils.SessionControlHandler;
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
		if(!SessionControlHandler.isSessionValidate(request, response))	return;
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		String idSongString = request.getParameter("idSong");
		String idPlayerString = request.getParameter("idPlaylist");
		int idSong;
		int idPlaylist;
		try {
			idSong = Integer.parseInt(idSongString);
			idPlaylist = Integer.parseInt(idPlayerString);
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing ids");
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
		if(playlist == null || !(playlist.getIdCreator() == user.getId())) {
			session.invalidate();
			String path = "SubmitLogin";
			String msg = "You are trying to access wrong information. Login again to identify yourself ";
			response.sendRedirect(path+"?logout=" + msg);
			return;
		}		
		
		SongDAO songDAO = new SongDAO(connection);
		Song song;
		try {
			song = songDAO.findSongById(idSong);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error finding song");
			return;
		}
		
		//control that the song belongs to the user session
		//song could be null even if someone delete the song in db
		if(song == null || !(song.getIdCreator() == user.getId())) {
			session.invalidate();
			String path = "SubmitLogin";
			String msg = "You are trying to access wrong information. Login again to identify yourself ";
			response.sendRedirect(path+"?logout=" + msg);
			return;
		}		
		
		
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		
		try {
			album = albumDAO.findAlumById(song.getIdAlbum());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error finding album");
			return;		
		}
		
		//if someone delete the song meanwhile i click to it
		if (album == null) {
			//in future i can add an error to display
			String path = "GetPlaylistPage";
			response.sendRedirect(path);
			return;
		}
		
		//forward
		String path = "/WEB-INF/Templates/Player";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("song", song);
		ctx.setVariable("album", album);
		ctx.setVariable("idPlaylist", idPlaylist);

		
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
