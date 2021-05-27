package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import it.polimi.tiw.beans.Song;

public class SongDAO {
	private Connection con;
	
	public SongDAO(Connection con) {
		this.con = con;
		
	}
	
	
	
	public int createSong(Song song) throws SQLException {
		int result = 0;
		PreparedStatement pstm1 = null;
		PreparedStatement pstm2 = null;
		
		// for the new songId
        ResultSet rs = null;
		try {
			// set auto commit to false
            con.setAutoCommit(false);
            
            String query1 = "INSERT IGNORE INTO `MusicPlaylistDb`.`Song` (`title`, `idAlbum`) VALUES (?, ?);";
			pstm1 = con.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
			pstm1.setString(1, song.getTitle());
			pstm1.setInt(2, song.getIdAlbum());
			int rowAffected = pstm1.executeUpdate();
			
			// get song id
            rs = pstm1.getGeneratedKeys();
            int songId = 0;
            if (rs.next()) {
            	songId = rs.getInt(1);
            	song.setId(songId);
            }
            
			if (rowAffected == 1) {
				//update songUrl
				String query2 = "UPDATE MusicPlaylistDb.Song as s\n"
						+ "SET s.songUrl = ?\n"
						+ "WHERE s.id = ?;";
				
				pstm2 = con.prepareStatement(query2);
				String songUrl = "songAudio_" + songId + "_" + song.getIdAlbum()+""+song.getSongUrl();
				song.setSongUrl(songUrl);
				pstm2.setString(1, songUrl);
				pstm2.setInt(2, songId);
				int rowUpdated = pstm2.executeUpdate();
				
				if (rowUpdated == 1) {
					con.commit();
					result = 1;
					
				}else {
					con.rollback();
				}
				
			}else {
				con.rollback();
			}
			
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if(rs != null)  rs.close();
				if(pstm1 != null) pstm1.close();
                if(pstm2 != null) pstm2.close();
			} catch (Exception e1) {

			}
		}
		return result;
		
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
