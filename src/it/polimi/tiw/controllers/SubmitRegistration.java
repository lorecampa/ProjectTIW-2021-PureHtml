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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.TymeleafHandler;


@WebServlet("/SubmitRegistration")
public class SubmitRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	

    public SubmitRegistration() {
        super();
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//forward to register page
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String path = "/WEB-INF/Templates/Register.html";
		templateEngine.process(path, ctx, response.getWriter());
		
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		
		
		//need to control if they are correct
		if(username == null || username.isEmpty() || email == null || email.isEmpty() ||
				password == null|| password.isEmpty() || name == null || name.isEmpty() ||
				surname == null || name.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing registration parameters");
			return;
		}
		
		if (password.length() < 4) {
			//return error to Registration.html
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			String msg = "Password must be greater or equals than 4";
			ctx.setVariable("registrationError", msg);
			String path = "/WEB-INF/Templates/Register.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
			
			//guardare quando fare il return durante il forward dei template
		}
		
		UserDAO userDAO = new UserDAO(connection);
		User user = new User(username, email, password, name, surname);
		
		//creation user
		int created = 0;
		try {
			//return 0 if the user is already creted (email is unique identifier)
			created = userDAO.createUser(user);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Issue creating user");
			return;
		}
		
		//redirect to submit login with message if we are registred
		String path = "SubmitLogin";		
		String msg;
		if (created == 0) {
			msg = "You are already registred with the email: " + user.getEmail() + ". Login now!";			
		}else {
			msg = "Now you are registred! Login with the same credential";
		}
		
		response.sendRedirect(path+"?logout=" + msg);
		
		
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
