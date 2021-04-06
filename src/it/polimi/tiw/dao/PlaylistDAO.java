package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.Playlist;

public class PlaylistDAO {
	private Connection con = null;
	
	public PlaylistDAO(Connection con) {
		this.con = con;
	}
	
	public int createPlaylist(String title, int idCreator) throws SQLException {
		int code = 0;
		String query = "INSERT into MusicPlaylistdb.Playlist (title, idCreator)   VALUES(?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, title);
			pstatement.setInt(2, idCreator);
			code = pstatement.executeUpdate();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {

			}
		}
		
		return code;
	}
	
	public ArrayList<Playlist> findAllPlaylistById(int userId) throws SQLException{
		ArrayList<Playlist> playlists = new ArrayList<>();
		String query = "SELECT * FROM MusicPlaylistdb.Playlist";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			result = pstatement.executeQuery();
			
			while(result.next()) {
				Playlist playlist = new Playlist();
				playlist.setId(result.getInt("id"));
				playlist.setTitle(result.getString("title"));
				playlist.setIdCreator(result.getInt("idCreator"));
				playlist.setTimestamp(result.getTimestamp("dateCreation"));
				playlists.add(playlist);
				
			}

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close result");
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		
		return playlists;
	}
}
