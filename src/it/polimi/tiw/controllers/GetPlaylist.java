package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Playlist;
import it.polimi.tiw.beans.Song;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.AlbumDAO;
import it.polimi.tiw.dao.MatchDAO;
import it.polimi.tiw.dao.PlaylistDAO;
import it.polimi.tiw.dao.SongDAO;
import it.polimi.tiw.utils.*;


@WebServlet("/GetPlaylist")
public class GetPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	public final int NUM_SLIDE_SONG = 5;

	   
    
    public GetPlaylist() {
        super();
    }
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionHandler.getConnection(servletContext);
		this.templateEngine = TymeleafHandler.getTemplateEngine(servletContext);
		
	}
    

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		int idUser = user.getId();
		
		String addSongWarning = (String) session.getAttribute("addSongToPlaylistWarning");
		if (addSongWarning != null) {
			session.removeAttribute("addSongToPlaylistWarning");
			request.setAttribute("addSongWarning", addSongWarning);
		}
		
		String idPlaylistString = request.getParameter("idPlaylist");
		String currentSlideString = request.getParameter("currentSlide");
		
		int idPlaylist;
		int currentSlide;
		try {
			idPlaylist = Integer.parseInt(idPlaylistString);
			currentSlide = Integer.parseInt(currentSlideString);
		}catch (NumberFormatException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}

		
		//finding playlist bean
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		Playlist playlist;
		try {
			playlist = playlistDAO.findPlaylistById(idPlaylist);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		//control that the playlist belongs to the user session
		if(playlist == null || (playlist.getIdCreator() != user.getId())) {
			forwardToErrorPage(request, response, ErrorType.PLAYLIST_NOT_EXSIST.getMessage());
			return;
		}
		
		
		MatchDAO matchDAO = new MatchDAO(connection);
		//list of song id present on the playlist
		ArrayList<Integer> playlistSongIds = new ArrayList<>();
		try {
			playlistSongIds = matchDAO.findAllSongIdOfPlaylist(idPlaylist, idUser);
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}
		
		SongDAO songDAO = new SongDAO(connection);
		AlbumDAO albumDAO = new AlbumDAO(connection);

		ArrayList<Song> songs = new ArrayList<>();
		ArrayList<Album> albums = new ArrayList<>();
		Song song;
		Album album;
		//find all songs and albums bean in playlist selected
		for(int id: playlistSongIds) {
			try {
				//return null if song is not present
				song = songDAO.findSongById(id);
				if (song != null) {
					//return null if album is not present
					album = albumDAO.findAlumById(song.getIdAlbum());
					if (album != null) {
						songs.add(song);
						albums.add(album);
					}
				}
			} catch (SQLException e) {
				forwardToErrorPage(request, response, e.getMessage());
				return;
			}
		}
		
		
		//lists of song that the user can select and add to the playlist
		ArrayList<Song> userSongsSelection = new ArrayList<>();
		try {
			ArrayList<Song> allUserSong = new ArrayList<>();
			//return empty list if there are no songs
			allUserSong = songDAO.findAllSongByUserId(idUser);
			
			for (Song songTemp: allUserSong) {
				if(!playlistSongIds.contains(songTemp.getId())) {
					userSongsSelection.add(songTemp);
				}
			}
			
		} catch (SQLException e) {
			forwardToErrorPage(request, response, e.getMessage());
			return;
		}		
		
		
		int sizeSongs = songs.size();
		//control currentSlide correctness
		int div = (sizeSongs / NUM_SLIDE_SONG);
		if (currentSlide < 0 || currentSlide > div) {
			forwardToErrorPage(request, response, ErrorType.PLAYLIST_NOT_EXSIST.getMessage());
			return;
		}
		
		int fromIndex = currentSlide * NUM_SLIDE_SONG;
		int toIndex = fromIndex + 5;
		boolean isPrevActive = false;
		boolean isNextActive = false;
		
		if (sizeSongs <= toIndex) {
			toIndex = sizeSongs;
			isNextActive = false;
		}else {
			isNextActive = true;
		}
	
		if(currentSlide > 0) {
			isPrevActive = true;
		}
		
		
		request.setAttribute("playlist", playlist);
		request.setAttribute("userSongsSelection", userSongsSelection);
		request.setAttribute("songs", songs.subList(fromIndex, toIndex));
		request.setAttribute("albums", albums.subList(fromIndex, toIndex));
		request.setAttribute("isNextActive", isNextActive);
		request.setAttribute("isPrevActive", isPrevActive);
		request.setAttribute("currentSlide", currentSlide);
		
		forward(request, response, PathUtils.PLAYLIST_PAGE);
		
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
