package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.polimi.tiw.beans.Album;


public class AlbumDAO {
	private Connection con = null;
	
	public AlbumDAO(Connection con) {
		this.con = con;
	}
	
	
	//come si fa a gestire i casi in cui uno crea un album e subito uno sta facendo il find Id ma non lo trova
	//cioè come si gestisce la concorrenza in questi casi?
	//ragionarci con calma
	public int findAlbumId(Album album) throws SQLException {
		int id = -1;
		String query = "SELECT id FROM MusicPlaylistdb.Album WHERE title = ? && interpreter = ? && year = ? && genre = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, album.getTitle());
			pstatement.setString(2, album.getInterpreter());
			pstatement.setShort(3, album.getYear());
			pstatement.setString(4, album.getGenre().getDisplayName());
			result = pstatement.executeQuery();
			
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
	
	public int createAlbum(Album album) throws SQLException {
		int code = 0;
		String query = "INSERT INTO `MusicPlaylistdb`.`Album` (`title`, `interpreter`, `year`, `genre`) VALUES (?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, album.getTitle());
			pstatement.setString(2, album.getInterpreter());
			pstatement.setShort(3,  album.getYear());
			pstatement.setString(4, album.getGenre().getDisplayName());
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
		
	
}
