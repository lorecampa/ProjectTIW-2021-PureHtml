package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

import it.polimi.tiw.beans.Match;
import it.polimi.tiw.dao.MatchDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.SessionControlHandler;
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
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//session control
		if(!SessionControlHandler.isSessionValidate(request, response))	return;
		HttpSession session = request.getSession();
		
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
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error parsing idSong or idPlaylist selected");
			return;
		}
		
		
		Match match = new Match(idSong, idPlaylist);
		MatchDAO matchDAO = new MatchDAO(connection);
		int matchCreated;
		try {
			//return zero if the match is already present
			matchCreated = matchDAO.createMatch(match);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating match");
			return;
		}
		
		if (matchCreated == 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Match was already present");
		}else {
			response.sendRedirect("GetPlaylist?idPlaylist="+idPlaylist+"&currentSlide="+currentSlide);
		}
		
		
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
