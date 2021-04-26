package it.polimi.tiw.utils;

public enum ErrorType {
	//song errors
	SONG_BAD_PARAMETERS("Bad Song parameters"),
	SONG_ALREADY_PRESENT("Song is already present in your list"),
	AUDIO_TYPE_NOT_PERMITTED("Audio file type is not permitted"),
	SONG_NOT_EXSIST("Song not exist or not yours"),

	
	//playlist errors
	PLAYLIST_BAD_PARAMETERS("Bad Create Playlist parameters"),
	PLAYLIST_ALREADY_PRESENT("Playlist is already present in your list"),
	PLAYLIST_NOT_EXSIST("Playlist not exist or not yours"),


	
	//album errors
	CREATE_ALBUM_BAD_PARAMETERS("Bad Create Album parameters"),
	ALBUM_ALREADY_PRESENT("Album is already present in your list"),
	IMAGE_TYPE_NOT_PERMITTED("Image file type is not permitted"),
	ALBUM_NOT_EXIST("Album not exist or not yours"),

	
	//FILE
	FILE_BAD_PARAMETER("Bad File parameter"),
	FILE_NOT_EXIST("File not exist or not yours"),
	
	//login errors
	LOGIN_BAD_PARAMETERS("Bad Login parameters"),
	
	//registration errors
	REGISTRATION_BAD_PARAMATERS("Bad registration parameters"),
	PASSWORD_LENGTH_ERROR("Password length must be greater or equals than 4"),
	
	//user error
	FINDING_USER_ERROR("Error retreiving user information");
	
	
	private final String message;
	
	ErrorType(String message){
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
