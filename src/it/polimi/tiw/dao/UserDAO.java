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
	
	
	//return -1 if user is not present
	public int findIdOfUserByEmail(String email) throws SQLException {
		int id = -1;
		String query = "SELECT id FROM MusicPlaylistdb.user WHERE email = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, email);
			result = pstatement.executeQuery();
			if (result.next()) {
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
	
	
	public boolean isPasswordCorrect(int idUser, String password) throws SQLException {
		boolean queryResult = false;
		String query = "SELECT EXISTS (SELECT * FROM user WHERE id = ? and password = ?)";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idUser);
			pstatement.setString(2, password);
			result = pstatement.executeQuery();
			result.next();
			
			if(result.getInt(1) == 1) {
				queryResult = true;
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
	
	
	
	//return 0 if user is already present (IGNORE STATEMENT) email unique constant
	public int createUser(User user) throws SQLException {
		int code = 0;
		String query = "INSERT IGNORE INTO MusicPlaylistdb.user (username, email, password, name, surname)   VALUES(?, ?, ?, ?, ?);";
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
	
	//return null if user is not present
	public User findUserById(int idUser) throws SQLException {
		User user = null;
		String query = "SELECT * FROM MusicPlaylistdb.User WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idUser);
			result = pstatement.executeQuery();
			if(result.next()) {
				int id = result.getInt("id");
				String username = result.getString("username");
				String email = result.getString("email");
				String password = result.getString("password");
				String name = result.getString("name");
				String surname = result.getString("surname");
				user = new User(id, username, email, password, name, surname);
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
