package it.polimi.tiw.utils;

public enum ErrorType {
	//song errors
	SONG_BAD_PARAMETERS("Bad song parameters"),
	SONG_ALREADY_PRESENT("Song is already present in your list"),
	AUDIO_TYPE_NOT_PERMITTED("Audio file type is not permitted"),
	SONG_NOT_EXSIST("Song does not exist or not yours"),

	
	//playlist errors
	PLAYLIST_BAD_PARAMETERS("Bad playlist parameters"),
	PLAYLIST_ALREADY_PRESENT("Playlist is already present in your list"),
	PLAYLIST_NOT_EXSIST("Playlist not exist or not yours"),


	
	//album errors
	CREATE_ALBUM_BAD_PARAMETERS("Bad album parameters"),
	ALBUM_ALREADY_PRESENT("Album is already present in your list"),
	IMAGE_TYPE_NOT_PERMITTED("Image file type is not permitted"),
	ALBUM_NOT_EXIST("Album does not exist or not yours"),

	
	//FILE
	FILE_BAD_PARAMETER("Bad file parameter"),
	FILE_NOT_EXIST("File does not exist or not yours"),
	
	//login errors
	LOGIN_BAD_PARAMETERS("Bad login parameters"),
	
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
