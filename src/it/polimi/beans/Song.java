package it.polimi.beans;

public class Song{
	private Integer id;
	private String title;
	private String imageUrl;
	private String songUrl;
	private Integer idCreator;
	private Integer idAlbum;
	
	
	
	public Song(String title, String imageUrl, String songUrl, Integer idCreator, Integer idAlbum) {
		super();
		this.title = title;
		this.imageUrl = imageUrl;
		this.songUrl = songUrl;
		this.idCreator = idCreator;
		this.idAlbum = idAlbum;
	}
	
	public Song(Integer id, String title, String imageUrl, String songUrl, Integer idCreator, Integer idAlbum) {
		super();
		this.id = id;
		this.title = title;
		this.imageUrl = imageUrl;
		this.songUrl = songUrl;
		this.idCreator = idCreator;
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

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getSongUrl() {
		return songUrl;
	}

	public void setSongUrl(String songUrl) {
		this.songUrl = songUrl;
	}

	public Integer getIdCreator() {
		return idCreator;
	}

	public void setIdCreator(Integer idCreator) {
		this.idCreator = idCreator;
	}

	public Integer getIdAlbum() {
		return idAlbum;
	}

	public void setIdAlbum(Integer idAlbum) {
		this.idAlbum = idAlbum;
	}
	
	
	
	
}
