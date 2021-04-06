package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Playlist {
	private int id;
	private String title;
	private int idCreator;
	private Timestamp timestamp;

	public Playlist() {
		super();
	}
	
	public Playlist(int id, String title, int idCreator) {
		super();
		this.id = id;
		this.title = title;
		this.idCreator = idCreator;
	}
	
	public Playlist(String title, int idCreator) {
		super();
		this.title = title;
		this.idCreator = idCreator;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getIdCreator() {
		return idCreator;
	}
	public void setIdCreator(int idCreator) {
		this.idCreator = idCreator;
	}
	
	
	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	
	

}
