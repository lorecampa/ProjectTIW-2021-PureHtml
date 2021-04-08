package it.polimi.tiw.beans;

public class Album {
	private int id;
	private String title;
	private String interpreter;
	private short year;
	private Genre genre;
	
	public Album() {
		super();
	}

	public Album(int id, String title, String interpreter, short year, Genre genre) {
		super();
		this.id = id;
		this.title = title;
		this.interpreter = interpreter;
		this.year = year;
		this.genre = genre;
	}
	public Album(String title, String interpreter, short year, Genre genre) {
		super();
		this.title = title;
		this.interpreter = interpreter;
		this.year = year;
		this.genre = genre;
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

	public String getInterpreter() {
		return interpreter;
	}

	public void setInterpreter(String interpeter) {
		this.interpreter = interpeter;
	}

	public short getYear() {
		return year;
	}

	public void setYear(short year) {
		this.year = year;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	
	

}
