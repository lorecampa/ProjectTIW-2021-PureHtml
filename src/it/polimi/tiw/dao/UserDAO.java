package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;


public class UserDAO {
	private Connection con;
	
	public UserDAO(Connection con) {
		this.con = con;
	}
	
	
	public int findIdOfUser(String username) throws SQLException {
		int id;
		String query = "SELECT id FROM MusicPlaylistdb.user WHERE username = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			if (result.next()) {
				id = result.getInt("id");
			}else {
				id = -1;
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
	
	public boolean isPasswordCorrect(int id, String password) throws SQLException {
		boolean queryResult;
		String query = "SELECT EXISTS (SELECT * FROM user WHERE id = ? and password = ?)";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, id);
			pstatement.setString(2, password);
			result = pstatement.executeQuery();
			result.next();
			
			if(result.getInt(1) == 1) {
				queryResult = true;
			}else {
				queryResult = false;
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
		
		return queryResult;
	}
	
	public int createUser(User user) throws SQLException {
		int code = 0;
		String query = "INSERT into MusicPlaylistdb.user (username, email, password, name, surname)   VALUES(?, ?, ?, ?, ?);";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, user.getUsername());
			pstatement.setString(2, user.getEmail());
			pstatement.setString(3, user.getPassword());
			pstatement.setString(4, user.getName());
			pstatement.setString(5, user.getSurname());

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
	
	public User findUserById(int idUser) throws SQLException {
		User user;
		String query = "SELECT * FROM MusicPlaylistdb.User WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idUser);
			result = pstatement.executeQuery();
			if(result.next()) {
				String username = result.getString("username");
				String email = result.getString("email");
				String password = result.getString("password");
				String name = result.getString("name");
				String surname = result.getString("surname");
				user = new User(username, email, password, name, surname);
			}else {
				user = null;
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
		
		return user;
		
	}
	
}
