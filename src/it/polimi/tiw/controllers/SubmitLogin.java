package it.polimi.tiw.controllers;

import java.io.IOException;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.SessionControlHandler;
import it.polimi.tiw.utils.TymeleafHandler;

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

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;

@WebServlet("/SubmitLogin")
public class SubmitLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

       
  
    public SubmitLogin() {
        super();

    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String path;
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			String msg = request.getParameter("logout");
			//session's over
			path = "/WEB-INF/Templates/Login.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("logout", msg);
			templateEngine.process(path, ctx, response.getWriter());
		}else {
			//otherwise go to home page
			path = getServletContext().getContextPath() + "/GetHomePage";
			response.sendRedirect(path);
		}
		
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if (username == null || username.isEmpty() ||
				password == null || password.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Login parametres");
			return;
		}
		
		//we want to control if the password is not correct or the user is still not registred
		int idUser = 0;
		UserDAO userDAO = new UserDAO(connection);
		try {
			idUser = userDAO.findIdOfUserByUsername(username);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error finding userId information");
			return;

		}
		
		
		
		if (idUser == -1) {
			//username not present in db
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("logout", "Incorrect username");
			String path = "/WEB-INF/Templates/Login.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
			
		}
		
		boolean isPasswordCorrect = false;
		try {
			isPasswordCorrect = userDAO.isPasswordCorrect(idUser, password);
			
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error validating user: "+username+" password");
			return;
		}
		if(!isPasswordCorrect) {
			//password not correct
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("logout", "Incorrect password");
			String path = "/WEB-INF/Templates/Login.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		//retrieve user bean
		User user = null;
		try {
			user = userDAO.findUserById(idUser);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving user information");
			return;
		}
		
		if (user == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot create user bean from db");
			
		}else {
			//adding user to session
			HttpSession session = request.getSession(true);
			session.setAttribute("user", user);
			response.sendRedirect("GetHomePage");
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
