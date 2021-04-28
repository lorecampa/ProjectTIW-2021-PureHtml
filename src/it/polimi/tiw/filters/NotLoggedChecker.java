package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.utils.PathUtils;


@WebFilter("/NotLoggedChecker")
public class NotLoggedChecker implements Filter {

    public NotLoggedChecker() {
    }

	
	public void destroy() {
	}

	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.println("NotLoggedChecker filter executing ...\n");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			res.sendRedirect(req.getServletContext().getContextPath() + PathUtils.LOGIN_SERVLET);
			return;
		}

		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
