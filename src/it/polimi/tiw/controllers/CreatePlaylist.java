package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.exceptions.PlaylistException;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.SessionControlHandler;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/CreatePlaylist")
public class CreatePlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
       
   
    public CreatePlaylist() {
        super();
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//nothing
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//session control
		if(!SessionControlHandler.isSessionValidate(request, response))	return;
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		String playlistName = request.getParameter("name");
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		
		//check if the user has already created a playlist with this name
		boolean isPlaylistAlreadyCreated = true;
		try {
			isPlaylistAlreadyCreated = playlistDAO.isPlaylistAlreadyCreated(playlistName, user.getId());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if (playlistName == null || playlistName.isEmpty() || isPlaylistAlreadyCreated) {
			String msg;
			if (isPlaylistAlreadyCreated) msg = "Playlist already present";
			else msg = "Missing playlist name";
			
			String path = "GetHomePage";
			response.sendRedirect(path + "?errorCreatePlaylist="+msg);
			
			return;
		}
		
		
		int created = 0;
		try {
			//return 0 if it update nothing
			created = playlistDAO.createPlaylist(playlistName, user.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating playlist");
			return;
		}
		
		if (created == 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Playlist was not created");
			return;
		}
		
		response.sendRedirect("GetHomePage");
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
