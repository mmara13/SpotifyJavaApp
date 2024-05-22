package services;

import database.DatabaseManager;
import models.Artist;
import models.Song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SongService {

    ArtistService artistService = new ArtistService();

    public Song createSong() throws IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            //prompt for title
            System.out.println("Introduce the title of the song: ");
            String title = reader.readLine().trim();

            //ask the user for the artist name that plays the song
            System.out.println("Enter the name of the artist: ");
            String artistName = reader.readLine().trim();

            //find the object artist with this name
            Artist artist = artistService.getArtistByName(artistName);
            if (artist == null) {
                System.out.println("Artist not found.");
                return null;
            }

            //validate and prompt for release year
            int year = -1;
            boolean validYear = false;
            while (!validYear) {
                try {
                    System.out.print("Introduce the release year of the song: ");
                    year = Integer.parseInt(reader.readLine().trim());
                    if (year >= 1900 && year <= 2024) {
                        validYear = true;
                    } else {
                        System.out.println("Invalid release year. Please enter a valid year (1900-2024).");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a numeric value for the release year.");
                }
            }

            //validate and prompt for song duration
            int time = -1;
            boolean validTime = false;
            while (!validTime) {
                try {
                    System.out.print("Introduce the duration of the song (in seconds): ");
                    time = Integer.parseInt(reader.readLine().trim());
                    if (time > 0) {
                        validTime = true;
                    } else {
                        System.out.println("Invalid song duration. Please enter a positive value.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a numeric value for the song duration.");
                }
            }
            //insert song into the database
            try (Connection connection = DatabaseManager.getConnection()) {
                String insertQuery = "INSERT INTO song (title, artist_id, year, time) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, title);
                    statement.setInt(2, artist.getId());
                    statement.setInt(3, year);
                    statement.setInt(4, time);
                    statement.executeUpdate();

                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        Song newSong = new Song();
                        newSong.setSongTitle(title);
                        newSong.setSongArtist(artist);
                        newSong.setSongTime(time);
                        newSong.setSongYear(year);
                        newSong.setSongId(id);
                        AuditService.getInstance().logAction("addSong");
                        return newSong;
                    } else {
                        throw new SQLException("Failed to get the auto-generated ID for the song.");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public int getSongIdByTitle(String songTitle) {
        String query = "SELECT id FROM song WHERE title = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, songTitle);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    throw new SQLException("Song with title '" + songTitle + "' not found.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while getting song ID by title", e);
        }
    }
    public Song[] fetchSongsByAlbumId(int albumId) {
        List<Song> songs = new ArrayList<>();

        String selectQuery = "SELECT * FROM album_songs " +
                "JOIN Song ON album_songs.song_id = Song.id " +
                "WHERE album_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, albumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    //retrieve song details from the result set
                    int songId = resultSet.getInt("song_id");
                    String title = resultSet.getString("title");
                    int year = resultSet.getInt("year");
                    int artist = resultSet.getInt("artist_id");
                    int time = resultSet.getInt("time");

                    //add the song to the list
                    Song song = new Song();
                    song.setSongTitle(title);
                    song.setSongYear(year);
                    song.setSongArtist(artistService.getArtistById(artist));
                    song.setSongTime(time);
                    songs.add(song);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //convert the list of songs to an array
        return songs.toArray(new Song[0]);
    }
    public boolean deleteSong() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the name of the song you want to delete: ");
        String songTitle = sc.nextLine();
        System.out.println("Enter the name of the artist who plays the song: ");
        String artistName = sc.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            String deleteQuery = "DELETE FROM Song WHERE title = ? AND artist_id = (SELECT id FROM Artist WHERE nickname = ?)";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, songTitle);
                statement.setString(2, artistName);
                int rowsDeleted = statement.executeUpdate();
                AuditService.getInstance().logAction("deleteSong");
                return rowsDeleted > 0; //returns true if at least one row was deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //return false if an exception occurred
        }
    }
    public void displayAllSongs() {
        System.out.println("List of Songs: ");
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT s.id, s.title, s.time, a.nickname AS artist_nickname, s.year " +
                     "FROM Song AS s " +
                     "JOIN Artist AS a ON s.artist_id = a.id")) {

            while (resultSet.next()) {
                int songId = resultSet.getInt("id");
                String songTitle = resultSet.getString("title");
                int duration = resultSet.getInt("time");
                String artistNickname = resultSet.getString("artist_nickname");
                int year = resultSet.getInt("year");

                System.out.println("Song ID " + songId + ": " + songTitle + "- by " + artistNickname +
                        " from " + year + " with duration " + duration + " seconds");
            }
            System.out.println("------------------------------------");
            AuditService.getInstance().logAction("displaySongs");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void displayAllSongsAlph() {
        System.out.println("List of Songs (Alphabetical Order): ");
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT s.title, s.time, a.nickname AS artist_nickname, s.year " +
                     "FROM Song AS s " +
                     "JOIN Artist AS a ON s.artist_id = a.id " +
                     "ORDER BY s.title")) {

            while (resultSet.next()) {
                String songTitle = resultSet.getString("title");
                int duration = resultSet.getInt("time");
                String artistNickname = resultSet.getString("artist_nickname");
                int year = resultSet.getInt("year");

                System.out.println("Song: '" + songTitle + "'  - by " + artistNickname +
                        " from " + year + " with duration " + duration + " seconds");
            }
            System.out.println("------------------------------------");
            AuditService.getInstance().logAction("displaySongsAlphabetically");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Song getSongByTitleAndArtist(String title, String artistName) {
        try (Connection connection = DatabaseManager.getConnection()){
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT s.id AS song_id, s.title AS song_title, s.year AS song_year, s.time AS song_duration, " +
                            "a.id AS artist_id, a.nickname AS artist_nickname " +
                            "FROM Song AS s " +
                            "JOIN Artist AS a ON s.artist_id = a.id " +
                            "WHERE s.title = ? AND a.nickname = ?");

            statement.setString(1, title);
            statement.setString(2, artistName);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                //retrieve song details
                int songId = resultSet.getInt("song_id");
                String songTitle = resultSet.getString("song_title");
                int songYear = resultSet.getInt("song_year");
                int songDuration = resultSet.getInt("song_duration");

                //retrieve artist details
                int artistId = resultSet.getInt("artist_id");
                String artistNickname = resultSet.getString("artist_nickname");

                //create an Artist object
                Artist artist = new Artist();
                artist.setId(artistId);
                artist.setArtistNickname(artistNickname);

                //create the Song object
                Song song = new Song();
                song.setSongTitle(songTitle);
                song.setSongYear(songYear);
                song.setSongTime(songDuration);
                song.setSongArtist(artist);

                return song;
            }
            return null; //song not found
        }  catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public String getSongTitleById(int songId) {
        String songTitle = null;
        String selectQuery = "SELECT title FROM Song WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, songId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    //retrieve song title from the result set
                    songTitle = resultSet.getString("title");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songTitle;
    }

}
