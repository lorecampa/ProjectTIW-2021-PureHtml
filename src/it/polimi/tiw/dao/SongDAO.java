package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.Song;

public class SongDAO {
	private Connection con;
	
	public SongDAO(Connection con) {
		this.con = con;
		
	}
	
	
	//create initial song, return 0 if song is already present (title, idAlbum) unique constraint
	public int createSong(Song song) throws SQLException {
		int code = 0;
		String query = "INSERT IGNORE INTO `MusicPlaylistdb`.`Song` (`title`, `songUrl`, `idAlbum`) VALUES (?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getTitle());
			pstatement.setString(2, song.getSongUrl());
			pstatement.setInt(3, song.getIdAlbum());
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
	
	public int findSongId(Song song) throws SQLException {
		int idResult = -1;
		String query = "SELECT id FROM MusicPlaylistdb.Song WHERE title = ? && idAlbum = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getTitle());
			pstatement.setInt(2, song.getIdAlbum());
			result = pstatement.executeQuery();
			
			if(result.next()) {
				idResult = result.getInt(1);
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
		return idResult;
	
	}
	
	public int removeInitialSong(Song song) throws SQLException {
		int code = 0;
		String query = "DELETE FROM MusicPlaylistdb.Song WHERE id = ?";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, song.getId());
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
	
	
	public int updateSong(Song song) throws SQLException {
		int code = 0;
		String query = "UPDATE MusicPlaylistdb.Song SET songUrl = ? WHERE id = ?";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getSongUrl());
			pstatement.setInt(2, song.getId());
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
	
	//return null if song is not present
	public Song findSongById(int songId) throws SQLException {
		Song song = null;
		String query = "SELECT * FROM MusicPlaylistdb.Song WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, songId);
			result = pstatement.executeQuery();
			
			if (result.next()) {
				song = new Song();
				song.setId(songId);
				song.setTitle(result.getString("title"));
				song.setSongUrl(result.getString("songUrl"));
				song.setIdAlbum(result.getInt("idAlbum"));
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
		return song;
	}
	
	//return empty array if user does not have any songs
	public ArrayList<Song> findAllSongByUserId(int userId) throws SQLException{
		ArrayList<Song> songs = new ArrayList<>();
		String query = "SELECT * \n"
				+ "FROM MusicPlaylistdb.Song \n"
				+ "WHERE idAlbum IN\n"
				+ "(SELECT id\n"
				+ "FROM MusicPlaylistdb.Album\n"
				+ "WHERE idCreator = ?)";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, userId);
			result = pstatement.executeQuery();
			
			while(result.next()) {
				Song song = new Song();
				song.setId(result.getInt("id"));
				song.setTitle(result.getString("title"));
				song.setSongUrl(result.getString("songUrl"));
				song.setIdAlbum(result.getInt("idAlbum"));
				
				songs.add(song);
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
		
		return songs;
	}
	


}
