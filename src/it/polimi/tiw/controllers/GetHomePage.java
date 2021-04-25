package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
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
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.SessionControlHandler;
import it.polimi.tiw.utils.TymeleafHandler;

@WebServlet("/GetHomePage")
public class GetHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
 
    public GetHomePage() {
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
		
		
		String playlistWarning = (String) session.getAttribute("playlistWarning");
		if (playlistWarning != null) {
			session.removeAttribute("playlistWarning");
			request.setAttribute("createPlaylistWarning", playlistWarning);
		}
		
		String albumWarning = (String) session.getAttribute("albumWarning");
		if (albumWarning != null) {
			session.removeAttribute("albumWarning");
			request.setAttribute("createAlbumWarning", albumWarning);
		}
		
		String songWarning = (String) session.getAttribute("songWarning");
		if (songWarning != null) {
			session.removeAttribute("songWarning");
			request.setAttribute("createSongWarning", songWarning);
		}
		
		
		//Playlists information
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		ArrayList<Playlist> playlists = null;
		try {
			//return empty array if there are not playlists created
			playlists = playlistDAO.findAllPlaylistByUserId(user.getId());
		} catch (SQLException e) {
			forwardToErrorPage(request, response, ErrorType.FINDING_PLAYLIST_ERROR.getMessage());
			return;
		}
		
		AlbumDAO albumDAO = new AlbumDAO(connection);
		ArrayList<Album> albums = new ArrayList<>();
		try {
			albums = albumDAO.findAllUserAlbumsById(user.getId());
		} catch (SQLException e) {
			forwardToErrorPage(request, response, ErrorType.FINDING_ALBUM_ERROR.getMessage());
			return;
		}
		
		
		request.setAttribute("playlists", playlists);
		request.setAttribute("userAlbums", albums);
		
		forward(request, response, PathUtils.HOME_PAGE);

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
