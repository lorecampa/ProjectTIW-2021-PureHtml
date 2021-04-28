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
import it.polimi.tiw.beans.Match;
import it.polimi.tiw.beans.Playlist;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.MatchDAO;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TymeleafHandler;

@WebServlet("/AddSongToPlaylist")
public class AddSongToPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	   
    
    public AddSongToPlaylist() {
        super();
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		String idSongString = request.getParameter("songs");
		String idPlaylistString = request.getParameter("idPlaylist");
		String currentSlideString = request.getParameter("currentSlide");

		int idSong;
		int idPlaylist;
		Integer currentSlide;

		try {
			idSong = Integer.parseInt(idSongString);
			idPlaylist = Integer.parseInt(idPlaylistString);
			currentSlide = Integer.parseInt(currentSlideString);
			
		}catch(NumberFormatException e) {
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
		Album album;
		try {
			album = albumDAO.findAlumById(song.getIdAlbum());
		} catch (SQLException e) {
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
		
		//control autentity
		if (playlist.getIdCreator() != user.getId() || album.getIdCreator() != user.getId()) {
			forwardToErrorPage(request, response, ErrorType.SONG_NOT_EXSIST.getMessage());
			return;
		}
		
		Match match = new Match(idSong, idPlaylist);
		MatchDAO matchDAO = new MatchDAO(connection);
		int matchCreated;
		try {
			//return 0 if the song is already present in the playlist
			matchCreated = matchDAO.createMatch(match);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		//if song already present in playlist
		if (matchCreated == 0) {
			session.setAttribute("addSongToPlaylistWarning", ErrorType.SONG_ALREADY_PRESENT);
		}
		response.sendRedirect("GetPlaylist?idPlaylist="+idPlaylist+"&currentSlide="+currentSlide);
				
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
