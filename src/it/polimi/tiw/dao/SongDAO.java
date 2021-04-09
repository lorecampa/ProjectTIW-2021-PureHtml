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
	
	//bisogna in futuro controllare che non vengano inserite due canzoni con lo stesso titolo dallo stesso autore
	//stessa cisa per gli album
	
	public int findSongId(Song song) throws SQLException {
		int id = -1;
		String query = "SELECT id FROM MusicPlaylistdb.Song WHERE title = ? && idCreator = ? && idAlbum = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getTitle());
			pstatement.setInt(2, song.getIdCreator());
			pstatement.setInt(3, song.getIdAlbum());
			result = pstatement.executeQuery();
			
			//andrebbe controllato che il risultato della query sia unico?? bho
			while(result.next()) {
				id = result.getInt("id");		
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
		return id;
	
	}
	
	//return null if it finds no song with this id
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
				song.setImageUrl(result.getString("imageUrl"));
				song.setSongUrl(result.getString("songUrl"));
				song.setIdCreator(result.getInt("idCreator"));
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
	
	public int createSong(Song song) throws SQLException {
		int code = 0;
		String query = "INSERT IGNORE INTO `MusicPlaylistdb`.`Song` (`title`, `imageUrl`, `songUrl`, `idCreator`, `idAlbum`) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getTitle());
			pstatement.setString(2, "LAZY_LOADING");
			pstatement.setString(3,  "LAZY_LOADING");
			pstatement.setInt(4, song.getIdCreator());
			pstatement.setInt(5, song.getIdAlbum());
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
	
	public int updateSongPath(Song song) throws SQLException {
		int code = 0;
		String query = "UPDATE MusicPlaylistdb.Song SET imageUrl = ? , songUrl = ? WHERE id = ?";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getImageUrl());
			pstatement.setString(2, song.getSongUrl());
			pstatement.setInt(3, song.getId());
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
	
	public ArrayList<Song> findAllSongByUserId(int userId) throws SQLException{
		ArrayList<Song> songs = new ArrayList<>();
		String query = "SELECT * FROM MusicPlaylistdb.Song WHERE idCreator = ?";
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
				song.setImageUrl(result.getString("imageUrl"));
				song.setSongUrl(result.getString("songUrl"));
				song.setIdCreator(userId);
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
