package it.polimi.tiw.utils;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class SessionControlHandler {
	
	public static boolean isSessionValidate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			//session's over
			response.sendRedirect(request.getServletContext().getContextPath() + PathUtils.LOGIN_PAGE);
			return false;
		}
		return true;
	}

}
