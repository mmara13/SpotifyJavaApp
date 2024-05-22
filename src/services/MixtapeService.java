package services;

import database.DatabaseManager;
import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class MixtapeService {

    AlbumService albumService = new AlbumService();
    ArtistService artistService = new ArtistService();
    PlaylistService playlistService = new PlaylistService();
    public Mixtape createMixtape() {
        Scanner sc = new Scanner(System.in);

        //album title from user
        System.out.println("Enter the title of the album: ");
        String albumTitle = sc.nextLine();
        //ask the user for the artist name (for the album)
        System.out.println("Enter the name of the artist: ");
        String artistName = sc.nextLine();

        //find the object artist with this name
        Artist artist = artistService.getArtistByName(artistName);
        if (artist == null) {
            System.out.println("Artist not found.");
            return null;
        }

        //find the album object by title and artist
        Album album = albumService.getAlbumByTitleAndArtist(albumTitle, artistName);
        if (album == null) {
            System.out.println("Album not found.");
            return null;
        }

        //prompt for title
        String title = " ";
        boolean validName = false;
        while (!validName) {
            try {
                System.out.println("Introduce the title of the mixtape: ");
                title = sc.nextLine();
                if (!title.isBlank()) {
                    validName = true;
                } else System.out.println("Invalid title. Please enter a valid title (not empty).");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a title.");
                sc.nextLine();
            }
        }

        //validate and prompt for release year
        int year = -1;
        boolean validYear = false;
        while (!validYear) {
            try {
                System.out.print("Introduce the release year of the mixtape: ");
                year = sc.nextInt();
                if (year >= 1900 && year <= 2024) {
                    validYear = true;
                } else {
                    System.out.println("Invalid release year. Please enter a valid year (1900-2024).");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value for the release year.");
                sc.nextLine(); //clear the input buffer
            }
        }

        sc.nextLine(); //to consume the newline char left in the buffer
        //search for album songs
        Song[] albumSongs = album.getAlbumSongs();
        int nrAlbum = albumSongs.length;

        System.out.println("Enter the title of the playlist: ");
        String playlistTitle = sc.nextLine();

        //find the playlist object by title
        Playlist playlist = playlistService.getPlaylistByTitle(playlistTitle);
        if (playlist == null) {
            System.out.println("Playlist not found");
            return null;
        }

        Song[] playlistSongs = playlist.getPlaylistSongs();
        int nrPlaylist = playlistSongs.length;


        //insert into the db
        try (Connection connection = DatabaseManager.getConnection()) {
            String insertQuery = "INSERT INTO Mixtape (title, album_id, playlist_id) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, title);
                statement.setInt(2, album.getId());
                statement.setInt(3, playlist.getId());
                statement.executeUpdate();

            }
            AuditService.getInstance().logAction("addMixtape");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public boolean deleteMixtape() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the name of the mixtape you want to delete: ");
        String mixtapeTitle = sc.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            String deleteQuery = "DELETE FROM Mixtape WHERE title = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, mixtapeTitle);
                int rowsDeleted = statement.executeUpdate();
                AuditService.getInstance().logAction("deleteMixtape");
                return rowsDeleted > 0; //returns true if at least one row was deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //return false if an exception occurred
        }
    }
    public void displayAllMixtapes() {
        System.out.println("List of Mixtapes: \n");
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, title, album_id, playlist_id FROM mixtape")) {

            while (resultSet.next()) {
                int mixtapeId = resultSet.getInt("id");
                String mixtapeTitle = resultSet.getString("title");
                int albumId = resultSet.getInt("album_id");
                int playlistId = resultSet.getInt("playlist_id");

                System.out.println("Mixtape: " + mixtapeTitle + " ID: " + mixtapeId);

                //fetch songs from the album
                String albumQuery = "SELECT s.title, s.time, a.nickname AS artist_nickname, s.year " +
                        "FROM album_songs AS ASongs " +
                        "JOIN Song AS s ON ASongs.song_id = s.id " +
                        "JOIN Artist AS a ON s.artist_id = a.id " +
                        "WHERE ASongs.album_id = ?";
                try (PreparedStatement albumStatement = connection.prepareStatement(albumQuery)) {
                    albumStatement.setInt(1, albumId);
                    try (ResultSet albumResultSet = albumStatement.executeQuery()) {
                        while (albumResultSet.next()) {
                            String songTitle = albumResultSet.getString("title");
                            int duration = albumResultSet.getInt("time");
                            String artistNickname = albumResultSet.getString("artist_nickname");
                            int year = albumResultSet.getInt("year");
                            System.out.println("Album Song: '" + songTitle + "' - by " + artistNickname + " from " + year + " with duration " + duration + " seconds");
                        }
                    }
                }

                //fetch songs from the playlist
                String playlistQuery = "SELECT s.title, s.time, a.nickname AS artist_nickname, s.year " +
                        "FROM playlist_songs AS PSongs " +
                        "JOIN Song AS s ON PSongs.song_id = s.id " +
                        "JOIN Artist AS a ON s.artist_id = a.id " +
                        "WHERE PSongs.playlist_id = ?";
                try (PreparedStatement playlistStatement = connection.prepareStatement(playlistQuery)) {
                    playlistStatement.setInt(1, playlistId);
                    try (ResultSet playlistResultSet = playlistStatement.executeQuery()) {
                        while (playlistResultSet.next()) {
                            String songTitle = playlistResultSet.getString("title");
                            int duration = playlistResultSet.getInt("time");
                            String artistNickname = playlistResultSet.getString("artist_nickname");
                            int year = playlistResultSet.getInt("year");
                            System.out.println("Playlist Song: '" + songTitle + "' - by " + artistNickname + " from " + year + " with duration " + duration + " seconds");
                        }
                    }
                }
                System.out.println("---------------------------------------------------------------------------------------------");
            }
            AuditService.getInstance().logAction("displayMixtapes");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Mixtape getMixtapeByTitle(String mixtapeTitle) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT m.id AS mixtape_id, m.title AS mixtape_title, " +
                             "a.id AS artist_id, a.nickname AS artist_nickname, a.age AS artist_age, " +
                             "s.id AS song_id, s.title AS song_title, s.time AS song_duration, s.year AS song_year " +
                             "FROM Mixtapes AS m " +
                             "JOIN Artist AS a ON m.artist_id = a.id " +
                             "JOIN Mixtapes_Songs AS ms ON m.id = ms.mixtape_id " +
                             "JOIN Songs AS s ON ms.song_id = s.id " +
                             "WHERE m.title = ?")) {

            statement.setString(1, mixtapeTitle);
            ResultSet resultSet = statement.executeQuery();

            Mixtape mixtape = null;
            Artist artist = null; //create an Artist object outside the loop

            if (resultSet.next()) {
                int mixtapeId = resultSet.getInt("mixtape_id");
                String title = resultSet.getString("mixtape_title");

                //retrieve artist details
                int artistId = resultSet.getInt("artist_id");
                String artistNickname = resultSet.getString("artist_nickname");
                int artistAge = resultSet.getInt("artist_age");

                //create artist obj and set details
                artist = new Artist();
                artist.setId(artistId);
                artist.setArtistNickname(artistNickname);
                artist.setArtistAge(artistAge);

                //retrieve songs for the mixtape
                List<Song> songs = new ArrayList<>();
                do {
                    int songId = resultSet.getInt("song_id");
                    String songTitle = resultSet.getString("song_title");
                    int duration = resultSet.getInt("song_duration");
                    int year = resultSet.getInt("song_year");

                    //create song object and add to list
                    Song song = new Song();
                    song.setSongArtist(artist); // Set the artist for each song
                    song.setSongTitle(songTitle);
                    song.setSongYear(year);
                    song.setSongTime(duration);
                    songs.add(song);
                } while (resultSet.next());

                //create mixtape object
                mixtape = new Mixtape(title, artist, songs.size(), songs.toArray(new Song[0]), 0, 0, new Song[0], mixtapeId);
            }

            return mixtape;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public String getMixtapeTitleById(int mixtapeId) {
        String mixtapeTitle = null;
        String selectQuery = "SELECT title FROM mixtape WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, mixtapeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    //retrieve mixtape title from the result set
                    mixtapeTitle = resultSet.getString("title");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mixtapeTitle;
    }

}
