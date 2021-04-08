package it.polimi.tiw.beans;



public class User{
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", email=" + email + ", password=" + password + ", name="
				+ name + ", surname=" + surname + "]";
	}

	private Integer id;
	private String username;
	private String email;
	private String password;
	private String name;
	private String surname;

	
	public User(Integer id, String username, String email, String password,
			String name, String surname) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.name = name;
		this.surname = surname;
	}
	
	public User(String username, String email, String password,
			String name, String surname) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.name = name;
		this.surname = surname;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	
}
