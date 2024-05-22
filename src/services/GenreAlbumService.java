package services;

import database.DatabaseManager;
import models.Artist;
import models.GenreAlbum;
import models.Song;

import java.sql.*;
import java.util.*;

public class GenreAlbumService {

    ArtistService artistService = new ArtistService();

    SongService songService = new SongService();

    public int getGenreAlbumIdFromTitle(String title) {
        int genreAlbumId = -1;
        try (Connection connection = DatabaseManager.getConnection()) {
            String selectGenreAlbumIdQuery = "SELECT id FROM genrealbum WHERE title = ?";
            try (PreparedStatement selectGenreAlbumIdStatement = connection.prepareStatement(selectGenreAlbumIdQuery)) {
                selectGenreAlbumIdStatement.setString(1, title);
                try (ResultSet resultSet = selectGenreAlbumIdStatement.executeQuery()) {
                    if (resultSet.next()) {
                        genreAlbumId = resultSet.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return genreAlbumId;
    }
    public GenreAlbum createGenreAlbum() {
        //ARTIST AND SONGS
        Scanner sc = new Scanner(System.in);

        //ask the user for the artist name
        System.out.println("Enter the name of the artist: ");
        String artistName = sc.nextLine();

        //find the object artist with this name
        Artist artist = artistService.getArtistByName(artistName);
        if (artist == null) {
            System.out.println("Artist not found.");
            return null;
        }

        //nr of songs
        System.out.println("Enter the number of songs in the Genre Album: ");
        int numSongs = -1;
        //validate and prompt for number of songs
        boolean validNumSongs = false;
        while (!validNumSongs) {
            try {
                numSongs = Integer.parseInt(sc.nextLine());
                if (numSongs > 0) {
                    validNumSongs = true;
                } else {
                    System.out.println("Invalid number of songs. Please enter a positive value.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value for the number of songs.");
            }
        }

        Song[] albumSongs = new Song[numSongs];

        for (int i = 0; i < numSongs; i++) {
            System.out.println("Details for song number " + (i + 1) + ":");
            System.out.println("Title: ");
            String songTitle = sc.nextLine();

            Song song = songService.getSongByTitleAndArtist(songTitle, artistName);
            if (song == null) {
                System.out.println("Song '" + songTitle + "' by artist '" + artistName + "' not found.");
                i--;
            } else {
                albumSongs[i] = song;
            }
        }

        //prompt for title
        System.out.println("Introduce the title of the genre album: ");
        String title = sc.nextLine();

        //validate and prompt for release year
        int year = -1;
        boolean validYear = false;
        while (!validYear) {
            try {
                System.out.print("Introduce the release year of the genre album: ");
                year = Integer.parseInt(sc.nextLine());
                if (year >= 1900 && year <= 2024) {
                    validYear = true;
                } else {
                    System.out.println("Invalid release year. Please enter a valid year (1900-2024).");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric value for the release year.");
            }
        }

        //prompt for genres
        System.out.print("Introduce the genres (separated by commas): ");
        String genresInput = sc.nextLine();
        Set<String> genres = new HashSet<>(Arrays.asList(genresInput.split(",")));

        //insert genre album into the genrealbum table
        try (Connection connection = DatabaseManager.getConnection()) {
            String insertGenreAlbumQuery = "INSERT INTO genrealbum (title, numSongs, artist_id, year, genres, price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement genreAlbumStatement = connection.prepareStatement(insertGenreAlbumQuery, Statement.RETURN_GENERATED_KEYS)) {
                genreAlbumStatement.setString(1, title);
                genreAlbumStatement.setInt(2, numSongs);
                genreAlbumStatement.setInt(3, artist.getId());
                genreAlbumStatement.setInt(4, year);
                genreAlbumStatement.setString(5, genresInput);
                GenreAlbum newGenreAlbum = new GenreAlbum(title, albumSongs, numSongs, artist, year, genres, 0);
                int price = newGenreAlbum.Discount();
                genreAlbumStatement.setInt(6, price);

                int affectedRows = genreAlbumStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating genre album failed, no rows affected.");
                }

                int genreAlbumId = getGenreAlbumIdFromTitle(title);
                //insert songs into the genrealbum_songs table using the retrieved genre album ID
                String insertSongQuery = "INSERT INTO genrealbum_songs (genrealbum_id, song_id) VALUES (?, ?)";
                try (PreparedStatement songStatement = connection.prepareStatement(insertSongQuery)) {
                    for (Song song : albumSongs) {
                        songStatement.setInt(1, genreAlbumId);
                        int id = songService.getSongIdByTitle(song.getTitle());
                        songStatement.setInt(2, id);
                        songStatement.executeUpdate();
                    }
                }
                AuditService.getInstance().logAction("createGenreAlbum");

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public boolean deleteGenreAlbum() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the name of the genre album you want to delete: ");
        String albumTitle = sc.nextLine();
        System.out.print("Enter the artist of the album: ");
        String artistName = sc.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            //delete rows from genrealbum_songs
            String deleteSongsQuery = "DELETE FROM genrealbum_songs WHERE genrealbum_id = (SELECT id FROM genrealbum WHERE title = ? AND artist_id = (SELECT id FROM artist WHERE nickname = ?))";
            try (PreparedStatement songsStatement = connection.prepareStatement(deleteSongsQuery)) {
                songsStatement.setString(1, albumTitle);
                songsStatement.setString(2, artistName);
                songsStatement.executeUpdate();
            }

            // delete genre album from genrealbum
            String deleteQuery = "DELETE FROM genrealbum WHERE title = ? AND artist_id = (SELECT id FROM artist WHERE nickname = ?)";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, albumTitle);
                statement.setString(2, artistName);
                int rowsDeleted = statement.executeUpdate();
                AuditService.getInstance().logAction("deleteGenreAlbum");
                return rowsDeleted > 0; //returns true if at least one row was deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //return false if an exception occurred
        }
    }
    public void displayAllGenreAlbums() {
        System.out.println("List of Genre Albums:");
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, title, numSongs, artist_id, year, price, genres FROM genrealbum")) {

            while (resultSet.next()) {
                int albumId = resultSet.getInt("id");
                String albumTitle = resultSet.getString("title");
                int numSongs = resultSet.getInt("numSongs");
                int artistId = resultSet.getInt("artist_id");
                int year = resultSet.getInt("year");
                int price = resultSet.getInt("price");
                String genresString = resultSet.getString("genres");
                String[] genres = genresString.split(","); // Split genres by comma

                //fetch artist information
                Artist artist = artistService.getArtistById(artistId);

                System.out.println("Genre album ID: " + albumId);
                System.out.println("Genre album: " + albumTitle + " by " + artist.getArtistNickname()
                        + " from " + year + " with " + numSongs + " songs and price $" + price);

                //printing genres
                System.out.print("Genres: ");
                for (String genre : genres) {
                    System.out.print(genre.trim() + " "); //trim to remove any spaces left
                }
                System.out.println("\n------------------------------------");
            }
            AuditService.getInstance().logAction("displayGenreAlbums");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public GenreAlbum getGenreAlbumById(int genreAlbumId) {
        GenreAlbum genreAlbum = null;
        String selectQuery = "SELECT * FROM genrealbum WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, genreAlbumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    //retrieve genre album details from the result set
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    int numSongs = resultSet.getInt("numSongs");
                    int artistId = resultSet.getInt("artist_id");
                    int year = resultSet.getInt("year");
                    int price = resultSet.getInt("price");
                    String genres = resultSet.getString("genres");
                    //fetch songs associated with the genre album
                    Song[] songs = getSongsForGenreAlbum(connection, id);
                    //split genres after the commas
                    Set<String> genre = new HashSet<>(Arrays.asList(genres.split(",\\s*")));

                    //create a GenreAlbum obj
                    genreAlbum = new GenreAlbum(title, songs, numSongs, artistService.getArtistById(artistId), year, genre, id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genreAlbum;
    }
    private Song[] getSongsForGenreAlbum(Connection connection, int genreAlbumId) {
        List<Song> songList = new ArrayList<>();
        String selectQuerySong = "SELECT s.title, s.time, a.nickname AS artist_nickname, s.year " +
                "FROM genrealbum_songs AS GASongs " +
                "JOIN Song AS s ON GASongs.song_id = s.id " +
                "JOIN Artist AS a ON s.artist_id = a.id " +
                "WHERE GASongs.genrealbum_id = ?";
        try (PreparedStatement songStatement = connection.prepareStatement(selectQuerySong)) {
            songStatement.setInt(1, genreAlbumId);
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
        //convert to array
        Song[] songs = new Song[songList.size()];
        return songList.toArray(songs);
    }

}
