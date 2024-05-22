package services;

import database.DatabaseManager;
import models.Album;
import models.Artist;
import models.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;


public class AlbumService {

    ArtistService artistService = new ArtistService();
    SongService songService = new SongService();

    public int getAlbumIdFromTitle(String albumTitle) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String query = "SELECT id FROM Album WHERE title = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, albumTitle);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    } else {
                        //album with the given title not found
                        return -1;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get album ID from title.", e);
        }
    }
    public Album createAlbum(Artist artist, Song[] songs, int numSongs) {
        Scanner sc = new Scanner(System.in);

        //prompt for title
        String title = " ";
        boolean validName = false;
        while (!validName) {
            try {
                System.out.println("Introduce the title of the album: ");
                title = sc.nextLine(); //to eat the newline chr
                if (!title.isBlank()) {
                    validName = true;
                } else System.out.println("Invalid title. Please enter a valid title (not empty).");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a title.");
                sc.nextLine();
            }
        }

        //validate and prompt for number of songs
        boolean validNumSongs = false;
        while (!validNumSongs) {
            try {
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

        //validate and prompt for release year
        int year = -1;
        boolean validYear = false;
        while (!validYear) {
            try {
                System.out.print("Introduce the release year of the album: ");
                year = sc.nextInt();
                if (year >= 1900 && year <= 2100) {
                    validYear = true;
                } else {
                    System.out.println("Invalid release year. Please enter a valid year (1900-2100).");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric value for the release year.");
                sc.nextLine(); //clear the input buffer
            }
        }

        try (Connection connection = DatabaseManager.getConnection()) {
            String insertQuery = "INSERT INTO Album (title, numSongs, artist_id, year, price, duration) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, title);
                statement.setInt(2, numSongs);
                statement.setInt(3, artist.getId());
                statement.setInt(4, year);

                Album newAlbum = new Album(title, songs, numSongs, artist, year, 0); //assuming null for songs and 0 for ID for now

                int price = newAlbum.Discount(); //Discount() =price
                statement.setInt(5, price);
                int duration = newAlbum.calculateDuration();
                statement.setInt(6,duration);
                statement.executeUpdate();

                //retreiving the id of the album from the db - didnt work with JDBC's method
                //to insert the songs in album_songs table
                int albumId = getAlbumIdFromTitle(title);
                String insertSongQuery = "INSERT INTO album_songs (album_id, song_id) VALUES (?, ?)";
                try(PreparedStatement songstatement = connection.prepareStatement(insertSongQuery)){
                    for(Song song : songs){
                        songstatement.setInt(1, albumId);
                        int id = songService.getSongIdByTitle(song.getTitle());
                        songstatement.setInt(2,id);
                        songstatement.executeUpdate();
                    }
                }
                AuditService.getInstance().logAction("addAlbum");
                return newAlbum;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert album into the database.", e);
        }
    }
    public void addNewAlbum(Scanner scanner) {
        //ask the user for the artist name
        System.out.println("Enter the name of the artist: ");
        String artistName = scanner.nextLine();

        //find the object artist with this name
        Artist artist = artistService.getArtistByName(artistName);
        if (artist == null) {
            System.out.println("Artist not found.");
            return;
        }
        //nr of songs
        System.out.print("Enter the number of songs in the album: ");
        int numSongs = Integer.parseInt(scanner.nextLine());
        Song[] albumSongs = new Song[numSongs];

        for (int i = 0; i < numSongs; i++) {
            System.out.println("Details for song number " + (i + 1) + ":");
            System.out.println("Title: ");
            String songTitle = scanner.nextLine();

            Song song = songService.getSongByTitleAndArtist(songTitle, artistName);
            if (song == null) {
                System.out.println("Song '" + songTitle + "' by artist '" + artistName + "' not found.");
                i--;
            } else
                albumSongs[i] = song;
        }
        createAlbum(artist, albumSongs, numSongs);
    }
    public Album getAlbumByTitleAndArtist(String albumTitle, String artistName) {
        Album album = null;
        String selectQuery = "SELECT * FROM album " +
                "JOIN Artist ON album.artist_id = Artist.id " +
                "WHERE album.title = ? AND Artist.nickname = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, albumTitle);
            statement.setString(2, artistName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    //retrieve album details from the result set
                    int id = resultSet.getInt("id");
                    int numSongs = resultSet.getInt("numSongs");
                    int artist_id = resultSet.getInt("artist_id");
                    int year = resultSet.getInt("year");
                    String title = resultSet.getString("title");
                    int price = resultSet.getInt("price");
                    int duration = resultSet.getInt("duration");

                    //create an Album object with the retrieved details
                    album = new Album(title, null, numSongs, artistService.getArtistById(artist_id), year, id);

                    //fetch songs associated with the album from the database
                    Song[] albumSongs = songService.fetchSongsByAlbumId(id);

                    //populating the albumSongs array
                    album.setAlbumSongs(albumSongs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return album;
    }
    public Song[] getSongsForAlbum(Connection connection, int albumId) {
        List<Song> songList = new ArrayList<>();
        String selectQuerySong = "SELECT s.title, s.time, a.nickname AS artist_nickname, s.year " +
                "FROM album_songs AS ASongs " +
                "JOIN Song AS s ON ASongs.song_id = s.id " +
                "JOIN Artist AS a ON s.artist_id = a.id " +
                "WHERE ASongs.album_id = ?";
        try (PreparedStatement songStatement = connection.prepareStatement(selectQuerySong)) {
            songStatement.setInt(1, albumId);
            try (ResultSet resultSetS = songStatement.executeQuery()) {
                while (resultSetS.next()) {
                    String songTitle = resultSetS.getString("title");
                    int time = resultSetS.getInt("time");
                    String artistNickname = resultSetS.getString("artist_nickname");
                    int years = resultSetS.getInt("year");
                    Song mSong = new Song();
                    mSong.setSongTime(time);
                    mSong.setSongYear(years);
                    mSong.setSongTitle(songTitle);
                    mSong.setSongArtist(artistService.getArtistByName(artistNickname));
                    songList.add(mSong);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //convert ArrayList to array
        Song[] songs = new Song[songList.size()];
        return songList.toArray(songs);
    }
    public Album getAlbumById(int albumId) {
        Album album = null;
        String selectQuery = "SELECT * FROM album WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, albumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    //retrieve album details from the result set
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    int numSongs = resultSet.getInt("numSongs");
                    int artist_id = resultSet.getInt("artist_id");
                    int year = resultSet.getInt("year");
                    int price = resultSet.getInt("price");
                    int duration = resultSet.getInt("duration");

                    //fetch songs associated with the album
                    Song[] songs = getSongsForAlbum(connection, id);

                    //create an Album object with the retrieved details
                    album = new Album(title, songs, numSongs, artistService.getArtistById(artist_id), year, id);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return album;
    }
    public boolean deleteAlbum() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the name of the album you want to delete: ");
        String albumTitle = sc.nextLine();
        System.out.println("Enter the artist of the album: ");
        String artistName = sc.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            //1st delete rows from album_songs
            String deleteSongsQuery = "DELETE FROM album_songs WHERE album_id = (SELECT id FROM album WHERE title = ? AND artist_id = (SELECT id FROM artist WHERE nickname = ?))";
            try (PreparedStatement songsStatement = connection.prepareStatement(deleteSongsQuery)) {
                songsStatement.setString(1, albumTitle);
                songsStatement.setString(2, artistName);
                songsStatement.executeUpdate();
            }

            //then delete genre album from album
            String deleteQuery = "DELETE FROM album WHERE title = ? AND artist_id = (SELECT id FROM artist WHERE nickname = ?)";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, albumTitle);
                statement.setString(2, artistName);
                int rowsDeleted = statement.executeUpdate();
                AuditService.getInstance().logAction("deleteAlbum");
                return rowsDeleted > 0; //returns true if at least one row was deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //return false if an exception occurred
        }
    }
    public void displayAllAlbums() {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, title, artist_id, year, price, duration FROM album");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int albumId = resultSet.getInt("id");
                String albumTitle = resultSet.getString("title");
                int artistId = resultSet.getInt("artist_id");
                int year = resultSet.getInt("year");
                int price = resultSet.getInt("price");
                int duration = resultSet.getInt("duration");
                Artist artist = artistService.getArtistById(artistId);

                System.out.println("Album ID: " + albumId);
                System.out.println("Album: " + albumTitle + " by " + artist.getArtistNickname()
                        + " from " + year + " with duration " + duration + " minutes");
                System.out.println("Price: " + price);

                //printing the songs in the album
                String selectQuerySong = "SELECT s.title, s.time, a.nickname AS artist_nickname, s.year " +
                        "FROM album_songs AS ASongs " +
                        "JOIN Song AS s ON ASongs.song_id = s.id " +
                        "JOIN Artist AS a ON s.artist_id = a.id " +
                        "WHERE ASongs.album_id = ?";
                try (PreparedStatement songStatement = connection.prepareStatement(selectQuerySong)) {
                    songStatement.setInt(1, albumId);
                    try (ResultSet resultSetS = songStatement.executeQuery()) {
                        while (resultSetS.next()) {
                            String songTitle = resultSetS.getString("title");
                            int time = resultSetS.getInt("time");
                            String artistNickname = resultSetS.getString("artist_nickname");
                            int years = resultSetS.getInt("year");
                            System.out.println("Song: '" + songTitle + "' - by " + artistNickname + " from " + years + " with duration " + time + " seconds \n");
                        }
                    }
                    System.out.println("--------------------------------------------------------------------");
                }
            }
            AuditService.getInstance().logAction("displayAlbums");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
