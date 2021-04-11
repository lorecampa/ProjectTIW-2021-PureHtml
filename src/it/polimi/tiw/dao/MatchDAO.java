package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import it.polimi.tiw.beans.Match;
import it.polimi.tiw.beans.Playlist;

public class MatchDAO {
	private Connection con;
	
	public MatchDAO(Connection con) {
		this.con = con;
	}
	
	public int createMatch(Match match) throws SQLException {
		int code = 0;
		String query = "INSERT INTO `MusicPlaylistdb`.`Match` (`idSong`, `idPlaylist`) VALUES (?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, match.getIdSong());
			pstatement.setInt(2, match.getIdPlaylist());
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
	
	public ArrayList<Integer> findAllSongIdOfPlaylist(int idPlaylist, int userId) throws SQLException{
		ArrayList<Integer> songIds = new ArrayList<>();
		String query = "SELECT m.idSong\n"
				+ "FROM MusicPlaylistdb.Match AS m, MusicPlaylistdb.Song AS s, MusicPlaylistdb.Album AS a\n"
				+ "WHERE m.idPlaylist = ? && m.idSong = s.id && s.idAlbum = a.id && a.idCreator = ?\n"
				+ "ORDER BY a.year DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idPlaylist);
			pstatement.setInt(2,  userId);
			result = pstatement.executeQuery();
			
			int id;
			while(result.next()) {
				id = result.getInt("idSong");
				songIds.add(id);
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
		return songIds;
		
		
	}

}
