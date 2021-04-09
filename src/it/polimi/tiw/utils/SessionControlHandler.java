package it.polimi.tiw.utils;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.context.WebContext;

public class SessionControlHandler {
	
	public static boolean isSessionValidate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			//session's over
			String path = "SubmitLogin";
			String msg = "Session is expired, please login again";
			response.sendRedirect(path+"?logout=" + msg);
			return false;
		}
		return true;
	}

}