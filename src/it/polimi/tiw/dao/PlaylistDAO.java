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
	
	//return 0 if playlist already present (IGNORE STATEMENT)
	public int createPlaylist(String title, int idCreator) throws SQLException {
		int code = 0;
		String query = "INSERT IGNORE INTO MusicPlaylistdb.Playlist (title, idCreator)   VALUES(?, ?)";
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
	
	//return empty array if there aren't playlist created by the user
	public ArrayList<Playlist> findAllPlaylistByUserId(int userId) throws SQLException{
		ArrayList<Playlist> playlists = new ArrayList<>();
		String query = "SELECT * FROM MusicPlaylistdb.Playlist WHERE idCreator = ? ORDER BY dateCreation DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, userId);
			result = pstatement.executeQuery();
			
			while(result.next()) {
				Playlist playlist = new Playlist();
				playlist.setId(result.getInt("id"));
				playlist.setTitle(result.getString("title"));
				playlist.setIdCreator(result.getInt("idCreator"));
				playlist.setDate(result.getTimestamp("dateCreation"));
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
	

	
	//return null if there is not a playlist with this id
	public Playlist findPlaylistById(int playlistId) throws SQLException {
		Playlist playlist = null;
		String query = "SELECT * FROM MusicPlaylistdb.Playlist WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, playlistId);
			result = pstatement.executeQuery();
			
			while(result.next()) {
				playlist = new Playlist();
				playlist.setId(playlistId);
				playlist.setTitle(result.getString("title"));
				playlist.setIdCreator(result.getInt("idCreator"));
				playlist.setDate(result.getTimestamp("dateCreation"));
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
		return playlist;
	}
}
