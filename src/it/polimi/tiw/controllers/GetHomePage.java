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
import it.polimi.tiw.beans.Playlist;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
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
		
		
		//Playlists information
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		ArrayList<Playlist> playlists = null;
		try {
			//return empty array if there are not playlists created
			playlists = playlistDAO.findAllPlaylistByUserId(user.getId());
			
		} catch (SQLException e) {
			//debug
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue finding playlist");

		}
		
		
		String errorCreatePlaylist = request.getParameter("errorCreatePlaylist");
		String errorCreateSong = request.getParameter("errorCreateSong");

		
		
		String path = "/WEB-INF/Templates/HomePage";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("playlists", playlists);
		if (errorCreatePlaylist != null) {
			ctx.setVariable("errorCreatePlaylist", errorCreatePlaylist);
		}
		if (errorCreateSong != null) {
			ctx.setVariable("errorCreateSong", errorCreateSong);
		}
		

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
