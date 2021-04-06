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

import it.polimi.tiw.dao.UserDAO;
/**
 * Servlet implementation class SubmitLogin
 */
@WebServlet("/SubmitLogin")
public class SubmitLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

       
  
    public SubmitLogin() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect("Templates/Login.html");
			return;
		}
		
		response.sendRedirect("GetHomePage");
	
		

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		int idUser = 0;
		boolean isPasswordCorrect = false;
		UserDAO userDAO = new UserDAO(connection);
		try {
			idUser = userDAO.findIdOfUser(username);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Userid: "+ idUser);
		if (idUser == -1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User not found");
		}
		
		try {
			isPasswordCorrect = userDAO.isPasswordCorrect(idUser, password);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!isPasswordCorrect) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Password is not correct");
		}
		
		System.out.println("idUser: "+idUser);
		System.out.println("Username: " + username);
		System.out.println("Password: "+ password);
		
		HttpSession session = request.getSession(true);
		session.setAttribute("idUser", idUser);
		
		response.sendRedirect(getServletContext().getContextPath() + "/GetHomePage");
		
		
		
		
		
	}

}
