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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error parsing song or playlist ids");
			return;
		}
		
		//finding playlist
		SongDAO songDAO = new SongDAO(connection);
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Playlist playlist;
		Song song;
		Album album = new Album();
		
		try {
			playlist = playlistDAO.findPlaylistById(idPlaylist);
			song = songDAO.findSongById(idSong);
			if (song != null) {
				album = albumDAO.findAlumById(song.getIdAlbum());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue getting song information");
			return;
		}
		

		
		//control that the playlist belongs to the user session
		if(playlist == null || song == null || album == null ||
				(playlist.getIdCreator() != user.getId() ||
				(album.getIdCreator() != user.getId())) ) {
			
			session.invalidate();
			String path = "SubmitLogin";
			String msg = "You are trying to access wrong information. Login again to identify yourself ";
			response.sendRedirect(path+"?logout=" + msg);
			return;
		}	
		
		System.out.println("Userid: " + user.getId());
		System.out.println("Playlist id creator: " + playlist.getIdCreator());
		System.out.println("Album id creator: " + album.getIdCreator());
		
	


		
	
	
		
		//forward
		String path = "/WEB-INF/Templates/Player.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("song", song);
		ctx.setVariable("album", album);
		ctx.setVariable("playlist", playlist);

		
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
