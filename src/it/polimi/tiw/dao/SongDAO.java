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
	
	
	//return 0 if song is already present (title, idAlbum) unique constraint
	public int createSong(Song song, String imageExt) throws SQLException {
		int code = 0;
		String query = "INSERT IGNORE INTO `MusicPlaylistdb`.`Song` (`id`, `title`, `songUrl`, `idAlbum`)\n"
				+ "VALUES (\n"
				+ "(SELECT (coalesce(MAX(s2.id), 0) + 1) FROM MusicPlaylistdb.Song as s2),\n"
				+ "?,\n"
				+ "(SELECT CONCAT ( (SELECT (coalesce(MAX(s2.id), 0) + 1) FROM MusicPlaylistdb.Song as s2), ?)),\n"
				+ "?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, song.getTitle());
			pstatement.setString(2, "-" + song.getIdAlbum() + imageExt);
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
		
	//return -1 if song is not present
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
