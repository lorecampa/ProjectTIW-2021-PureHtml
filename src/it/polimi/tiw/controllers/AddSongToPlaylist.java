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
/**
 * Servlet implementation class AddSongToPlaylist
 */
@WebServlet("/AddSongToPlaylist")
public class AddSongToPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddSongToPlaylist() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			System.out.println("Session's over");
			response.sendRedirect("SubmitLogin");
		}
		int idUser = (int) session.getAttribute("idUser");
		
		String idSongString = request.getParameter("songs");
		String idPlaylistString = request.getParameter("idPlaylist");
		
		int idSong = -1;
		int idPlaylist = -1;
		
		try {
			idSong = Integer.parseInt(idSongString);
			idPlaylist = Integer.parseInt(idPlaylistString);
			
		}catch(NumberFormatException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error parsing when getting match id");
			return;
		}
		
		
		Match match = new Match(idSong, idPlaylist);
		MatchDAO matchDAO = new MatchDAO(connection);
		int created = 0;
		try {
			created = matchDAO.createMatch(match);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating match");
			return;
		}
		if (created == 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Match was not created");
			return;
		}
		
		response.sendRedirect("/MusicPlaylist/GetPlaylist?idPlaylist="+idPlaylist);
		
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
