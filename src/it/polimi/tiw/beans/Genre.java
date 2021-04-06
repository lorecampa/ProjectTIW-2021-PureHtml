package it.polimi.tiw.beans;

public enum Genre {
	ROCK("Rock"),
	POP("Pop"),
	ELECTRONIC("Electronic"),
	SOUL("Soul"),
	FUNK("Funk"),
	COUNTRY("Country"),
	LATIN("Latin"),
	REGGAE("Reggae"),
	HIPHOP("Hip Hop"),
	PUNK("Punk"),
	POLKA("Polka");
	
	private String displayName;

	private Genre(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public static Genre fromString(String text) {
		for (Genre genre: Genre.values()) {
			if(genre.getDisplayName().equalsIgnoreCase(text)) {
				return genre;
			}
		}
		return null;
	}
	

}
