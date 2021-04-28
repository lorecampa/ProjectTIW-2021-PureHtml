package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.Genre;


public class AlbumDAO {
	private Connection con = null;
	
	public AlbumDAO(Connection con) {
		this.con = con;
	}
	
	
	//return 0 if album was already present (IGNORE statement)
	public int createAlbum(Album album) throws SQLException {
		int code = 0;
		String query = "INSERT IGNORE INTO `MusicPlaylistdb`.`Album` (`id`, `title`, `interpreter`, `year`, `genre`, `idCreator`, `imageUrl`)\n"
				+ "VALUES (\n"
				+ "(SELECT (coalesce(MAX(a1.id), 0) + 1) FROM MusicPlaylistdb.Album as a1),\n"
				+ "?,\n"
				+ "?,\n"
				+ "?,\n"
				+ "?,\n"
				+ "?,\n"
				+ "?\n"
				+ ")";
		
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, album.getTitle());
			pstatement.setString(2, album.getInterpreter());
			pstatement.setShort(3,  album.getYear());
			pstatement.setString(4, album.getGenre().getDisplayName());
			pstatement.setInt(5, album.getIdCreator());
			pstatement.setString(6, album.getImageUrl());
			
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
	
	
	//return -1 if there's not album
	public int findAlbumId(Album album) throws SQLException {
		int idResult = -1;
		String query = "SELECT id FROM MusicPlaylistdb.Album WHERE title = ? && interpreter = ? && year = ? && genre = ? && idCreator = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, album.getTitle());
			pstatement.setString(2, album.getInterpreter());
			pstatement.setShort(3, album.getYear());
			pstatement.setString(4, album.getGenre().getDisplayName());
			pstatement.setInt(5, album.getIdCreator());
			

			result = pstatement.executeQuery();
			
			if (result.next()) {
				idResult = result.getInt("id");
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
	
	//return null if there is not album
	public Album findAlumById(int albumId) throws SQLException {
		Album album = null;
		String query = "SELECT * FROM MusicPlaylistdb.Album WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, albumId);
			result = pstatement.executeQuery();
			
			if(result.next()) {
				album = new Album();
				album.setId(albumId);
				album.setTitle(result.getString("title"));
				album.setInterpreter(result.getString("interpreter"));
				album.setYear(result.getShort("year"));
				Genre genre = Genre.fromString(result.getString("genre"));
				album.setGenre(genre);
				album.setIdCreator(result.getInt("idCreator"));
				album.setImageUrl(result.getString("imageUrl"));
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
		return album;
	}
	

	//return empty array if user has not albums
	public ArrayList<Album> findAllUserAlbumsById(int userId) throws SQLException{
		ArrayList<Album> albums = new ArrayList<Album>();
		String query = "SELECT * FROM MusicPlaylistdb.Album WHERE idCreator = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, userId);
			result = pstatement.executeQuery();
			
			while(result.next()) {
				Album album = new Album();
				album.setId(result.getInt(1));
				album.setTitle(result.getString(2));
				album.setInterpreter(result.getString(3));
				album.setYear(result.getShort(4));
				String genreString = result.getString(5);
				album.setGenre(Genre.fromString(genreString));
				album.setIdCreator(result.getInt(6));
				album.setImageUrl(result.getString(7));
				albums.add(album);
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
		return albums;
	}
	


	
}
