package it.polimi.tiw.beans;

import java.sql.Timestamp;

public class Match {
	private int idSong;
	private int idPlaylist;
	private Timestamp dateAdding;
	
	
	public Match(int idSong, int idPlaylist) {
		this.idSong = idSong;
		this.idPlaylist = idPlaylist;
		this.dateAdding = null;
	}

	public int getIdSong() {
		return idSong;
	}

	public void setIdSong(int idSong) {
		this.idSong = idSong;
	}

	public int getIdPlaylist() {
		return idPlaylist;
	}

	public void setIdPlaylist(int idPlaylist) {
		this.idPlaylist = idPlaylist;
	}

	public Timestamp getDateAdding() {
		return dateAdding;
	}

	public void setDateAdding(Timestamp dateAdding) {
		this.dateAdding = dateAdding;
	}
	
	
	
}
