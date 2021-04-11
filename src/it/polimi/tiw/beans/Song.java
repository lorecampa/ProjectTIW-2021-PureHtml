package it.polimi.tiw.beans;

public class Song{
	private Integer id;
	private String title;
	private String songUrl;
	private Integer idAlbum;
	
	
	public Song() {
		super();
	}
	
	public Song(String title, String songUrl, Integer idAlbum) {
		super();
		this.title = title;
		this.songUrl = songUrl;
		this.idAlbum = idAlbum;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	

	public String getSongUrl() {
		return songUrl;
	}

	public void setSongUrl(String songUrl) {
		this.songUrl = songUrl;
	}


	public Integer getIdAlbum() {
		return idAlbum;
	}

	public void setIdAlbum(Integer idAlbum) {
		this.idAlbum = idAlbum;
	}
	
	
	
	
}
