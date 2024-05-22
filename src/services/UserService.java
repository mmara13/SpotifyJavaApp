package services;

import database.DatabaseManager;
import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import services.*;

public class UserService {

    ArtistService artistService = new ArtistService();

    public User createUser() {
        Scanner sc = new Scanner(System.in);

        //prompt for username
        String username = " ";
        boolean validUsername = false;
        boolean passwordvalid = false;
        User newUser = null;
        while (!validUsername) {
            try {
                System.out.println("Introduce a username: ");
                username = sc.nextLine();

                //db connection
                Connection connection = DatabaseManager.getConnection();

                Statement statement = connection.createStatement();

                //query to fetch all usernames from the database
                String query = "SELECT username FROM user";
                ResultSet resultSet = statement.executeQuery(query);

                boolean isDuplicate = false;

                //iterate through the result set to check for duplicates
                while (resultSet.next()) {
                    String dbUsername = resultSet.getString("username");
                    if (dbUsername.equals(username)) {
                        System.out.println("This username already exists. Please choose a different username");
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate && !username.isBlank()) {
                    System.out.println("Introduce the name of this user:  ");
                    String name = sc.nextLine();
                    while(!passwordvalid) {
                        validUsername = true;
                        System.out.println("Introduce the password of this user: ");
                        String password = sc.nextLine();
                        System.out.println("Repeat the password for verification: ");
                        String passwordd = sc.nextLine();
                        if (passwordd.equals(password)) {
                            newUser = new User(username, password, name);
                            AuditService.getInstance().logAction("addUser");
                            String insertQuery = "INSERT INTO User (name, username, password) VALUES (?, ?, ?)";
                            try (PreparedStatement statementuser = connection.prepareStatement(insertQuery)) {
                                statementuser.setString(1, newUser.getName());
                                statementuser.setString(2, newUser.getUsername());
                                statementuser.setString(3, newUser.getPassword());
                                statementuser.executeUpdate();
                            }
                            break;
                        }
                        else {
                            System.out.println("The passwords you introduced do not match! Please try again.");
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return newUser;
    }
    public boolean deleteUser() {
        Scanner sc = new Scanner(System.in);

        System.out.println("Introduce the username of the account you want to delete: ");
        String username = sc.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            //check if the user exists
            String selectQuery = "SELECT password FROM User WHERE username = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setString(1, username);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("password");

                        //prompt for the password
                        boolean passwordValid = false;
                        while (!passwordValid) {
                            System.out.println("Introduce the password for this user: ");
                            String password = sc.nextLine();
                            if (storedPassword.equals(password)) {
                                passwordValid = true;

                                //confirm deletion
                                System.out.println("Are you sure you want to delete the account " + username + "? (yes/no)");
                                String confirmation = sc.nextLine();
                                if (confirmation.equalsIgnoreCase("yes")) {
                                    //dlete the user
                                    String deleteQuery = "DELETE FROM User WHERE username = ?";
                                    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                                        deleteStatement.setString(1, username);
                                        deleteStatement.executeUpdate();
                                        AuditService.getInstance().logAction("deleteUser");
                                        System.out.println("User " + username + " has been deleted successfully.");
                                        return true;
                                    }
                                } else {
                                    System.out.println("User deletion cancelled.");
                                    return false;
                                }
                            } else {
                                System.out.println("The password you introduced is incorrect. Please try again.");
                            }
                        }
                    } else {
                        System.out.println("Username not found. Please try again.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public void displayAllUsers() {
        System.out.println("List of users: ");
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT username FROM User")) {

            int i = 1;
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                System.out.print(i + ") ");
                System.out.println(username);
                i++;
            }
            System.out.println("------------------------------------");
            AuditService.getInstance().logAction("displayUser");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean updateUserPasswordInDatabase(String username, String newPassword) {
        String updateQuery = "UPDATE user SET password = ? WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, newPassword);
            statement.setString(2, username);
            int rowsUpdated = statement.executeUpdate();
            AuditService.getInstance().logAction("changingUserPassword");
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private int getUserId(User user, Connection connection) throws SQLException {
        String userIdQuery = "SELECT id FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement userPs = connection.prepareStatement(userIdQuery)) {
            userPs.setString(1, user.getUsername());
            userPs.setString(2, user.getPassword());
            try (ResultSet userRs = userPs.executeQuery()) {
                if (userRs.next()) {
                    return userRs.getInt("id");
                }
            }
        }
        return -1;
    }
    public User findUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String password = resultSet.getString("password");
                    //create and return the user object
                    return new User(username, password, name);
                }
            }
        }
        //if no user is found return null
        return null;
    }

    //library functions
    public void addToUserLibrary(User user) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the type of the media you want to add (song, playlist, mixtape, album): ");
        String option = sc.nextLine().trim();

        //validate option
        if (!option.equalsIgnoreCase("song") && !option.equalsIgnoreCase("mixtape") && !option.equalsIgnoreCase("playlist") && !option.equalsIgnoreCase("album")) {
            System.out.println("Invalid option.");
            return;
        }

        System.out.println("Enter the title of the " + option.toLowerCase() + " that you want to add: ");
        String mediaTitle = sc.nextLine().trim();

        //validate title
        if (mediaTitle.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        String artistName = null;
        if (option.equalsIgnoreCase("song") || option.equalsIgnoreCase("album")) {
            System.out.println("Enter the artist of this " + option + ": ");
            artistName = sc.nextLine().trim();

            //validate artist name
            if (artistName.isEmpty()) {
                System.out.println("Artist name cannot be empty.");
                return;
            }
        }

        try {
            if (addMediaToLibrary(user, option.toLowerCase(), mediaTitle, artistName)) {
                System.out.println("Successfully added " + mediaTitle + " to the library!");
            } else {
                System.out.println(option.substring(0, 1).toUpperCase() + option.substring(1) + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    private boolean addMediaToLibrary(User user, String mediaType, String mediaTitle, String artistName) throws SQLException {

        String mediaIdQuery = "";
        try (Connection connection = DatabaseManager.getConnection()) {

            //retrieve the user_id from the database
            String userIdQuery = "SELECT id FROM user WHERE username = ? AND password = ?";
            try (PreparedStatement userPs = connection.prepareStatement(userIdQuery)) {
                userPs.setString(1, user.getUsername());
                userPs.setString(2, user.getPassword());
                try (ResultSet userRs = userPs.executeQuery()) {
                    if (!userRs.next()) {
                        System.out.println("User not found or authentication failed.");
                        return false;
                    }

                    PreparedStatement ps;
                    switch (mediaType) {
                        case "song":
                            mediaIdQuery = "SELECT id FROM song WHERE title = ? AND artist_id = (SELECT id FROM artist WHERE nickname = ?)";
                            ps = connection.prepareStatement(mediaIdQuery);
                            ps.setString(1, mediaTitle);
                            ps.setString(2, artistName);
                            AuditService.getInstance().logAction("addSongInUserLibrary");
                            break;
                        case "playlist":
                            mediaIdQuery = "SELECT id FROM playlist WHERE title = ?";
                            ps = connection.prepareStatement(mediaIdQuery);
                            ps.setString(1, mediaTitle);
                            AuditService.getInstance().logAction("addPlaylistInUserLibrary");
                            break;
                        case "album":
                            mediaIdQuery = "SELECT id FROM album WHERE title = ? AND artist_id = (SELECT id FROM artist WHERE nickname = ?)";
                            ps = connection.prepareStatement(mediaIdQuery);
                            ps.setString(1, mediaTitle);
                            ps.setString(2, artistName);
                            AuditService.getInstance().logAction("addInUserLibrary");
                            break;
                        case "mixtape":
                            mediaIdQuery = "SELECT id FROM mixtape WHERE title = ?";
                            ps = connection.prepareStatement(mediaIdQuery);
                            ps.setString(1, mediaTitle);
                            AuditService.getInstance().logAction("addMixtapeInUserLibrary");
                            break;
                        default:
                            return false;
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int mediaId = rs.getInt("id");
                            String insertQuery = "INSERT INTO user_library (user_id, media_id, media_type) VALUES (?, ?, ?)";
                            try (PreparedStatement insertPs = connection.prepareStatement(insertQuery)) {
                                insertPs.setInt(1, getUserId(user, connection));
                                insertPs.setInt(2, mediaId);
                                insertPs.setString(3, mediaType);
                                insertPs.executeUpdate();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    private boolean isMediaInUserLibrary(int userId, int mediaId, String mediaType) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM user_library WHERE user_id = ? AND " + mediaType + "_id = ?";
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, mediaId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        return count > 0;
                    }
                }
            }
        }
        return false;
    }
    public void displayUserLibrary(User user) {
        String mediaQuery = "SELECT * FROM user_library WHERE user_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(mediaQuery)) {
            //retrieve the users ID using their username and password
            int userId = getUserId(user, connection);
            if (userId == -1) {
                System.out.println("User not found or authentication failed.");
                return;
            }

            //set the user ID parameter in the prepared statement
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                AuditService.getInstance().logAction("displayUserLibrary");
                System.out.println("Library of user: " + user.getName() + ":");
                int index = 1;
                while (resultSet.next()) {
                    //retrieve details of each media item in the user's library
                    int mediaId = resultSet.getInt("media_id");
                    String mediaType = resultSet.getString("media_type");
                    String mediaDetails = getMediaDetails(mediaId, mediaType, connection);

                    //display the details of the media item
                    if (mediaDetails != null) {
                        System.out.println(index + ") " + mediaDetails);
                        index++;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching the user's library: " + e.getMessage());
        }
    }
    private String getMediaDetails(int mediaId, String mediaType, Connection connection) throws SQLException {
        String detailsQuery = "";
        switch (mediaType.toLowerCase()) {
            case "song":
                detailsQuery = "SELECT s.title, a.nickname FROM song s INNER JOIN artist a ON s.artist_id = a.id WHERE s.id = ?";
                break;
            case "playlist":
                detailsQuery = "SELECT title FROM playlist WHERE id = ?";
                break;
            case "album":
                detailsQuery = "SELECT a.title, ar.nickname FROM album a INNER JOIN artist ar ON a.artist_id = ar.id WHERE a.id = ?";
                break;
            case "mixtape":
                detailsQuery = "SELECT title FROM mixtape WHERE id = ?";
                break;
            default:
                return null; //unkn media types
        }

        try (PreparedStatement detailsPs = connection.prepareStatement(detailsQuery)) {
            detailsPs.setInt(1, mediaId);
            try (ResultSet detailsRs = detailsPs.executeQuery()) {
                if (detailsRs.next()) {
                    switch (mediaType.toLowerCase()) {
                        case "song":
                            String songTitle = detailsRs.getString("s.title");
                            String artistName = detailsRs.getString("a.nickname");
                            return "Song: " + songTitle + " - Artist: " + artistName;
                        case "playlist":
                            return "Playlist: " + detailsRs.getString("title");
                        case "album":
                            String albumTitle = detailsRs.getString("a.title");
                            String albumArtist = detailsRs.getString("ar.nickname");
                            return "Album: " + albumTitle + " - Artist: " + albumArtist;
                        case "mixtape":
                            return "Mixtape: " + detailsRs.getString("title");
                        default:
                            return null; //unkn media types
                    }
                }
            }
        }
        return null;
    }
    private Media getMediaDetailsFromDatabase(String mediaType, int mediaId) throws SQLException {
        String query;
        switch (mediaType) {
            case "song":
                query = "SELECT * FROM song WHERE song_id = ?";
                break;
            case "playlist":
                query = "SELECT * FROM playlist WHERE playlist_id = ?";
                break;
            case "album":
                query = "SELECT * FROM album WHERE album_id = ?";
                break;
            case "mixtape":
                query = "SELECT * FROM mixtape WHERE mixtape_id = ?";
                break;
            default:
                return null;
        }
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, mediaId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    switch (mediaType) {
                        case "song":
                            Artist artist = artistService.getArtistById(rs.getInt("artist_id"));
                            String title = rs.getString("title");
                            int year = rs.getInt("year");
                            int time = rs.getInt("time");
                            Song song = new Song();
                            song.setSongTime(time);
                            song.setSongYear(year);
                            song.setSongArtist(artist);
                            song.setSongTitle(title);
                            return song;
                        case "playlist":
                            String playlistName = rs.getString("title");
                            int playlistId = mediaId;
                            List<Song> songs = new ArrayList<>();

                            //query to fetch song details associated with the playlist
                            String songQuery = "SELECT s.title AS song_title, s.artist_id, s.year, s.time " +
                                    "FROM song s INNER JOIN playlist_songs ps ON s.id = ps.song_id " +
                                    "WHERE ps.playlist_id = ?";

                            //prepare statement
                            try (PreparedStatement songStatement = connection.prepareStatement(songQuery)) {
                                songStatement.setInt(1, playlistId);

                                try (ResultSet songResultSet = songStatement.executeQuery()) {
                                    //process result set to create Song objects
                                    while (songResultSet.next()) {
                                        String songTitle = songResultSet.getString("title");
                                        int artistId = songResultSet.getInt("artist_id");
                                        int yearsong = songResultSet.getInt("year");
                                        int timesong = songResultSet.getInt("time");

                                        Artist artist_song = artistService.getArtistById(artistId);

                                        //create Song object and add to the list
                                        Song song1 = new Song();
                                        song1.setSongTitle(songTitle);
                                        song1.setSongArtist(artistService.getArtistById(artistId));
                                        song1.setSongYear(yearsong);
                                        song1.setSongTime(timesong);
                                        songs.add(song1);
                                    }
                                }
                            }

                            //create Playlist object with songs
                            Song[] songsArray = songs.toArray(new Song[0]);
                            return new Playlist(songsArray, songs.size());
                        case "album":
                            String albumTitle = rs.getString("title");
                            int albumId = mediaId; // Assuming mediaId corresponds to album_id
                            int artist_id = rs.getInt("artist_id");
                            int album_year = rs.getInt("year");

                            List<Song> albumSongs = new ArrayList<>();

                            //query to fetch song details associated with the album
                            String songAQuery = "SELECT s.title AS song_title, s.artist_id, s.year, s.time " +
                                    "FROM song s INNER JOIN album_songs as ON s.id = as.song_id " +
                                    "WHERE as.album_id = ?";

                            try (PreparedStatement songAStatement = connection.prepareStatement(songAQuery)) {
                                songAStatement.setInt(1, albumId);

                                try (ResultSet songAResultSet = songAStatement.executeQuery()) {
                                    //process result set to create Song objects
                                    while (songAResultSet.next()) {
                                        String songTitle = songAResultSet.getString("song_title");
                                        int artistId = songAResultSet.getInt("artist_id");
                                        int songyear = songAResultSet.getInt("year");
                                        int songtime = songAResultSet.getInt("time");

                                        Artist artist_song = artistService.getArtistById(artistId);

                                        //create Song object and add to the list
                                        Song song2 = new Song();
                                        song2.setSongTime(songtime);
                                        song2.setSongYear(songyear);
                                        song2.setSongArtist(artist_song);
                                        song2.setSongTitle(songTitle);
                                        albumSongs.add(song2);
                                    }
                                }
                            }

                            //convert list to array
                            Song[] albumSongsArray = albumSongs.toArray(new Song[0]);

                            //create Album object with songs
                            return new Album(albumTitle, albumSongsArray, albumSongs.size(), artistService.getArtistById(artist_id), album_year, mediaId);
                        case "mixtape":
                            String mixtapeName = rs.getString("title");
                            int mixtapeId = mediaId; //assuming mediaId corresponds to mixtape_id
                            int album_id = rs.getInt("album_id");
                            int playlist_id = rs.getInt("playlist_id");
                            int numSongs = 0; //nr of songs in the album
                            int artist_album =0;
                            int nrSongs = 0; //nr of songs in the playlist
                            int year_album =0;
                            //first query to retrieve the number of songs for the album
                            String query_nr_albumsongs = "SELECT COUNT(*) AS numSongs, artist_id FROM album_songs WHERE album_id = ?";
                            PreparedStatement statement_nr_album_songs = connection.prepareStatement(query_nr_albumsongs);
                            //set the album_id parameter
                            statement_nr_album_songs.setInt(1, album_id);

                            //2nd query to retrieve the number of songs for the playlist
                            String query_nr_playlistsongs = "SELECT COUNT(*) AS nrSongs FROM playlist_songs WHERE playlist_id = ?";
                            PreparedStatement statement_nr_playlist_songs = connection.prepareStatement(query_nr_playlistsongs);
                            //set the playlist_id parameter
                            statement_nr_playlist_songs.setInt(1, playlist_id);

                            //execute the first query
                            try (ResultSet resultSet_album_songs = statement_nr_album_songs.executeQuery()) {
                                //process the result set for album songs count
                                if (resultSet_album_songs.next()) {
                                    numSongs = resultSet_album_songs.getInt("numSongs");
                                    artist_album = resultSet_album_songs.getInt("artist_id");
                                    year_album = resultSet_album_songs.getInt("year");
                                    //now we have the number of songs for the album
                                }
                            }

                            //execute the second query
                            try (ResultSet resultSet_playlist_songs = statement_nr_playlist_songs.executeQuery()) {
                                //process the result set for playlist songs count
                                if (resultSet_playlist_songs.next()) {
                                    nrSongs = resultSet_playlist_songs.getInt("nrSongs");
                                    //now we have the number of songs for the playlist
                                }
                            }
                            List<Song> mixtapeSongs = new ArrayList<>();

                            //query to fetch song details associated with the mixtape from the album
                            String albumSongQuery = "SELECT s.title AS song_title, s.artist_id, s.year, s.time " +
                                    "FROM song s INNER JOIN album_songs as ON s.id = as.song_id " +
                                    "WHERE as.album_id = ?";

                            try (PreparedStatement albumSongStatement = connection.prepareStatement(albumSongQuery)) {
                                albumSongStatement.setInt(1, mixtapeId);

                                try (ResultSet albumSongResultSet = albumSongStatement.executeQuery()) {
                                    //process result set to create Song objects
                                    while (albumSongResultSet.next()) {
                                        String songTitle = albumSongResultSet.getString("song_title");
                                        int artistId = albumSongResultSet.getInt("artist_id");
                                        int Ayear = albumSongResultSet.getInt("year");
                                        int Atime = albumSongResultSet.getInt("time");

                                        Artist artist_album_Mixtape= artistService.getArtistById(artistId);

                                        //create song and add it to the list
                                        Song song_album_mixtape = new Song();
                                        song_album_mixtape.setSongTitle(songTitle);
                                        song_album_mixtape.setSongArtist(artist_album_Mixtape);
                                        song_album_mixtape.setSongYear(Ayear);
                                        song_album_mixtape.setSongTime(Atime);
                                        mixtapeSongs.add(song_album_mixtape);
                                    }
                                }
                            }

                            //query to fetch song details associated with the mixtape from the playlist
                            String playlistSongQuery = "SELECT s.title AS song_title, s.artist_id, s.year, s.time " +
                                    "FROM song s INNER JOIN playlist_songs ps ON s.id = ps.song_id " +
                                    "WHERE ps.playlist_id = ?";

                            try (PreparedStatement playlistSongStatement = connection.prepareStatement(playlistSongQuery)) {
                                playlistSongStatement.setInt(1, mixtapeId);

                                try (ResultSet playlistSongResultSet = playlistSongStatement.executeQuery()) {
                                    //create song objs
                                    while (playlistSongResultSet.next()) {
                                        String songTitle = playlistSongResultSet.getString("song_title");
                                        int artistId = playlistSongResultSet.getInt("artist_id");
                                        int Pyear = playlistSongResultSet.getInt("year");
                                        int Ptime = playlistSongResultSet.getInt("time");

                                        Artist artist_playlist_mixtape = artistService.getArtistById(artistId);

                                        Song song_playlist_mixtape = new Song();
                                        song_playlist_mixtape.setSongTitle(songTitle);
                                        song_playlist_mixtape.setSongArtist(artist_playlist_mixtape);
                                        song_playlist_mixtape.setSongYear(Pyear);
                                        song_playlist_mixtape.setSongTime(Ptime);
                                        mixtapeSongs.add(song_playlist_mixtape);
                                    }
                                }
                            }

                            //convert list to array
                            Song[] mixtapeSongsArray = mixtapeSongs.toArray(new Song[0]);
                            //break it into two parts one for album one for playlist
                            Song[] songs_of_album = Arrays.copyOfRange(mixtapeSongsArray, 0, numSongs-1);
                            Song[] songs_of_playlist = Arrays.copyOfRange(mixtapeSongsArray, numSongs, numSongs+nrSongs-1);

                            //create Mixtape object with songs
                            return new Mixtape(mixtapeName,
                                    artistService.getArtistById(artist_album),
                                    numSongs,
                                    songs_of_album,
                                    year_album,
                                    nrSongs,
                                    songs_of_playlist,
                                    mixtapeId);
                    }
                }
            }
        }
        return null;
    }
}
