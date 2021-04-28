package it.polimi.tiw.controllers;

import java.io.IOException;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ErrorType;
import it.polimi.tiw.utils.TymeleafHandler;
import it.polimi.tiw.utils.PathUtils;
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
		forward(request, response, PathUtils.LOGIN_PAGE);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email").toLowerCase();
		String password = request.getParameter("password");
		
		if (email == null || email.isEmpty() ||
				password == null || password.isEmpty()) {
			forwardToErrorPage(request, response, ErrorType.LOGIN_BAD_PARAMETERS.getMessage());
			return;
		}
		
		//finding
		int idUser = 0;
		UserDAO userDAO = new UserDAO(connection);
		try {
			//return -1 if email is not present
			idUser = userDAO.findIdOfUserByEmail(email);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;

		}
		
		if (idUser == -1) {
			request.setAttribute("loginWarnings", "Incorrect username");
			forward(request, response, PathUtils.LOGIN_PAGE);
			return;
			
		}
		
		boolean isPasswordCorrect = false;
		try {
			//return false if password is not correct
			isPasswordCorrect = userDAO.isPasswordCorrect(idUser, password);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		if(!isPasswordCorrect) {
			request.setAttribute("loginWarnings", "Incorrect password");
			forward(request, response, PathUtils.LOGIN_PAGE);
			return;
		}
		
		//retrieve user bean
		User user;
		try {
			//return null if user is not present
			user = userDAO.findUserById(idUser);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		if (user == null) {
			forwardToErrorPage(request, response, ErrorType.FINDING_USER_ERROR.getMessage());
			
		}else {
			//adding user to session
			HttpSession session = request.getSession(true);
			session.setAttribute("user", user);
			response.sendRedirect(getServletContext().getContextPath() + PathUtils.HOME_SERVLET);
		}
		
		
		
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
