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
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.PathUtils;
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
		forward(request, response, PathUtils.REGISTER_PAGE);	
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
				surname == null || surname.isEmpty()) {
			forwardToErrorPage(request, response, ErrorType.REGISTRATION_BAD_PARAMATERS.getMessage());
			return;
		}
		
		if (password.length() < 4) {
			//return error to Registration.html
			request.setAttribute("registrationWarning", ErrorType.PASSWORD_LENGTH_ERROR.getMessage());
			forward(request, response, PathUtils.REGISTER_PAGE);
			return;			
		}
		
		UserDAO userDAO = new UserDAO(connection);
		User user = new User(username, email.toLowerCase(), password, name, surname);
		
		//creation user
		int created = 0;
		try {
			//return 0 if the user is already creted (email is unique identifier in db)
			created = userDAO.createUser(user);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		if (created == 0) {
			String msg = "Email: " + user.getEmail() + " is already registred";
			request.setAttribute("registrationWarning", msg);
			forward(request, response, PathUtils.REGISTER_PAGE);
			return;
		}
		
		//find and set user id just created
		try {
			user.setId(userDAO.findIdOfUserByEmail(user.getEmail()));
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		
		//adding user to session
		HttpSession session = request.getSession(true);
		session.setAttribute("user", user);
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.HOME_SERVLET);
				
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
