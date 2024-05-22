package services;

import database.DatabaseManager;
import models.Artist;
import models.Playlist;
import models.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class PlaylistService {

    ArtistService artistService = new ArtistService();

    SongService songService = new SongService();


    public Playlist getPlaylistByTitle(String playlistTitle) {
        Playlist playlist = null;
        String query = "SELECT playlist.id, playlist.title, playlist.numSongs, song.id AS song_id, song.title AS song_title, song.artist_id, song.year, song.time " +
                "FROM playlist " +
                "INNER JOIN playlist_songs ON playlist.id = playlist_songs.playlist_id " +
                "INNER JOIN song ON playlist_songs.song_id = song.id " +
                "WHERE playlist.title = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, playlistTitle);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Song> songs = new ArrayList<>();

                while (resultSet.next()) {
                    int songId = resultSet.getInt("song_id");
                    String songTitle = resultSet.getString("song_title");
                    int artistId = resultSet.getInt("artist_id");
                    int year = resultSet.getInt("year");
                    int time = resultSet.getInt("time");

                    Artist artist = artistService.getArtistById(artistId);

                    Song song = new Song();
                    song.setSongTitle(songTitle);
                    song.setSongArtist(artist);
                    song.setSongYear(year);
                    song.setSongTime(time);
                    songs.add(song);

                    if (playlist == null) {
                        int id = resultSet.getInt("id");
                        String title = resultSet.getString("title");
                        int numSongs = resultSet.getInt("numSongs");
                        playlist = new Playlist(new Song[numSongs], numSongs);
                        playlist.setPlaylistId(id);  // Set the playlist ID
                        playlist.setPlaylistName(title);
                    }
                }

                if (playlist != null) {
                    Song[] songsArray = songs.toArray(new Song[0]);
                    playlist.setSongs(songsArray);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlist;
    }
    public int getPlaylistIdFromTitle(String playlistTitle) {
        int playlistId = -1; //initialize with a default value in case of playlist not found

        String selectQuery = "SELECT id FROM Playlist WHERE title = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, playlistTitle);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    playlistId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve playlist ID from title.", e);
        }

        return playlistId;
    }
    public Playlist createPlaylist() {
        Scanner sc = new Scanner(System.in);

        //prompt for title
        String title = " ";
        boolean validName = false;
        while (!validName) {
            try {
                System.out.println("Introduce the title of the playlist: ");
                title = sc.nextLine();
                if (!title.isBlank()) {
                    validName = true;
                } else System.out.println("Invalid title. Please enter a valid title (not empty).");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a title.");
                sc.nextLine();
            }
        }

        ///validate and prompt for number of songs
        int numSongs = -1;
        boolean validNumSongs = false;
        while (!validNumSongs) {
            try {
                System.out.print("Introduce the number of songs in the playlist: ");
                numSongs = sc.nextInt();
                if (numSongs > 0) {
                    validNumSongs = true;
                } else {
                    System.out.println("Invalid number of songs. Please enter a positive value.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value for the number of songs.");
                sc.nextLine(); //clear the input buffer
            }
        }

        sc.nextLine();
        Song[] playlistSongs = new Song[numSongs];

        for (int i = 0; i < numSongs; i++) {
            System.out.println("Details for playlist song #" + (i + 1) + ":");
            System.out.println("Title: ");
            String songTitle = sc.nextLine();

            //search for the song in the db
            Song foundSong = null;
            try (Connection connection = DatabaseManager.getConnection()) {
                String query = "SELECT s.title, s.artist_id, a.nickname AS artist_nickname, s.time FROM Song s JOIN Artist a ON s.artist_id = a.id WHERE s.title = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, songTitle);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            String foundTitle = resultSet.getString("title");
                            int artistId = resultSet.getInt("artist_id");
                            String foundArtistNickname = resultSet.getString("artist_nickname");
                            int foundDuration = resultSet.getInt("time");

                            //create artist obj
                            Artist foundArtist = new Artist();
                            foundArtist.setArtistNickname(foundArtistNickname);
                            foundArtist.setId(artistId);

                            //create song obj
                            foundSong = new Song();
                            foundSong.setSongTitle(foundTitle);
                            foundSong.setSongTime(foundDuration);
                            foundSong.setSongArtist(foundArtist);
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (foundSong != null) {
                playlistSongs[i] = foundSong;
            } else {
                System.out.println("No match found for a song with this title.");
                i--; //to reprompt for the same song if it was not found (may be a mistake from the user)
            }
        }

        //insert playlist into the database
        try (Connection connection = DatabaseManager.getConnection()) {
            String insertQuery = "INSERT INTO Playlist (title, numSongs) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, title);
                statement.setInt(2, numSongs);
                statement.executeUpdate();


                Playlist newPlaylist = new Playlist(playlistSongs, numSongs);
                newPlaylist.setPlaylistName(title);

                //inserting into playlist_songs using vector playlistSongs
                //retreiving the id of the playlist from the db
                //to insert the songs in playlist_songs table
                int playlistId = getPlaylistIdFromTitle(title);
                String insertSongQuery = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
                try(PreparedStatement songstatement = connection.prepareStatement(insertSongQuery)){
                    for(Song song : playlistSongs){
                        songstatement.setInt(1, playlistId);
                        int id = songService.getSongIdByTitle(song.getTitle());
                        songstatement.setInt(2,id);
                        songstatement.executeUpdate();
                    }
                }
                AuditService.getInstance().logAction("addPlaylist");
                return newPlaylist;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean deletePlaylist() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the title of the playlist you want to delete: ");
        String playlistTitle = sc.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            //get the playlist ID from the title
            int playlistId = getPlaylistIdFromTitle(playlistTitle);
            if (playlistId == -1) {
                System.out.println("Playlist not found.");
                return false;
            }

            //delete rows from playlist_songs
            String deleteSongsQuery = "DELETE FROM playlist_songs WHERE playlist_id = ?";
            try (PreparedStatement songsStatement = connection.prepareStatement(deleteSongsQuery)) {
                songsStatement.setInt(1, playlistId);
                songsStatement.executeUpdate();
            }

            //delete playlist from Playlist table
            String deleteQuery = "DELETE FROM Playlist WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setInt(1, playlistId);
                int rowsDeleted = statement.executeUpdate();
                AuditService.getInstance().logAction("deletePlaylist");
                return rowsDeleted > 0; //returns true if at least one row was deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //return false if an exception occurred
        }
    }
    public String getPlaylistTitleById(int playlistId) {
        String playlistTitle = null;
        String selectQuery = "SELECT title FROM playlist WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, playlistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    //retrieve playlist title from the result set
                    playlistTitle = resultSet.getString("title");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlistTitle;
    }
    public void displayAllPlaylists() {
        System.out.println("List of Playlists: ");
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, title FROM playlist")) {

            while (resultSet.next()) {
                int playlistId = resultSet.getInt("id");
                String playlistTitle = resultSet.getString("title");

                System.out.println("Playlist: " + playlistTitle + " ID: " + playlistId);

                //fetch songs associated with this playlist from the db
                String selectQuery = "SELECT s.title, s.time, s.year, a.nickname AS artist_nickname " +
                        "FROM playlist_songs AS PSongs " +
                        "JOIN Song AS s ON PSongs.song_id = s.id " +
                        "JOIN Artist AS a ON s.artist_id = a.id " +
                        "WHERE PSongs.playlist_id = ?";
                try (PreparedStatement songStatement = connection.prepareStatement(selectQuery)) {
                    songStatement.setInt(1, playlistId);
                    ResultSet songResultSet = songStatement.executeQuery();
                    while (songResultSet.next()) {
                        String songTitle = songResultSet.getString("title");
                        int duration = songResultSet.getInt("time");
                        String artistNickname = songResultSet.getString("artist_nickname");
                        int year = songResultSet.getInt("year");
                        System.out.println("Song: '" + songTitle + "' - by " + artistNickname + " from " + year + " with duration " + duration + " seconds");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("------------------------------------");
            }
            AuditService.getInstance().logAction("displayPlaylists");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
