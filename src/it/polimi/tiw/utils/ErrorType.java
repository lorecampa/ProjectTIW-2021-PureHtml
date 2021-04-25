package it.polimi.tiw.utils;

public enum ErrorType {
	//song errors
	SONG_BAD_PARAMETERS("Bad Song parameters"),
	SONG_ALREADY_PRESENT("Song is already present in your list"),
	CREATING_SONG_ERROR("Issue creating song"),
	FINDING_SONG_ERROR("Error finding song information"),
	AUDIO_TYPE_NOT_PERMITTED("Audio file type is not permitted"),
	UPDATE_SONG_ERROR("Issue updating song audio database identifier"),
	SONG_NOT_EXSIST("Song not exist or not yours"),

	
	//playlist errors
	PLAYLIST_BAD_PARAMETERS("Bad Create Playlist parameters"),
	FINDING_PLAYLIST_ERROR("Error finding playlists information"),
	CREATING_PLAYLIST_ERROR("Issue creating playlist"),
	PLAYLIST_ALREADY_PRESENT("Playlist is already present in your list"),
	PLAYLIST_NOT_EXSIST("Playlist not exist or not yours"),


	
	//album errors
	CREATE_ALBUM_BAD_PARAMETERS("Bad Create Album parameters"),
	FINDING_ALBUM_ERROR("Error finding album information"),
	CREATING_ALBUM_ERROR("Issue creating album"),
	ALBUM_ALREADY_PRESENT("Album is already present in your list"),
	IMAGE_TYPE_NOT_PERMITTED("Image file type is not permitted"),
	UPDATE_ALBUM_ERROR("Issue updating album image database identifier"),
	ALBUM_NOT_EXIST("Album not exist or not yours"),

	
	//MATCH ERROR
	ADDING_SONG_ERROR("Issue adding song to playlist"),
	
	//login errors
	LOGIN_BAD_PARAMETERS("Bad Login parameters"),
	
	//registration errors
	REGISTRATION_BAD_PARAMATERS("Bad registration parameters"),
	ALREADY_REGISTRED("Email is already registred"),
	PASSWORD_LENGTH_ERROR("Password length must be greater or equals than 4"),
	
	//user error
	FINDING_USER_ERROR("Error retreiving user information"),
	FINDING_USER_SONG_ERROR("Error retreiving user songs"),

	CREATING_USER_ERROR("Issue creating user"),
	
	
	//database errors
	INTERNAL_SERVER_ERROR("Internal error in server database"),
	
	
	SESSION_OVER("Session's over");
	
	private final String message;
	
	ErrorType(String message){
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
