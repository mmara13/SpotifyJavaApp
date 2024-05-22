package services;

import database.DatabaseManager;
import models.Artist;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;


public class ArtistService {

    public Artist createArtist() {
        Scanner sc = new Scanner(System.in);
        //prompt for nickname
        String nickname = " ";
        boolean validName = false;
        while (!validName) {
            try {
                System.out.println("Introduce the nickname of the artist: ");
                nickname = sc.nextLine();
                if (!nickname.isBlank()) {
                    validName = true;
                } else System.out.println("Invalid nickname. Please enter a valid nickname (not empty).");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a nickname.");
                sc.nextLine();
            }
        }
        int age = -1;
        boolean validAge = false;
        while (!validAge) {
            try {
                System.out.print("Introduce the age of the artist: ");
                age = Integer.parseInt(sc.nextLine());
                if (age >= 0 && age <= 99)
                    validAge = true;
                else
                    System.out.println("Invalid age. Please enter a valid age (0-99).");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric age.");
                sc.nextLine(); //clear the input buffer
            }
        }

        //insertion in db

        try (Connection connection = DatabaseManager.getConnection()) {
            String insertQuery = "INSERT INTO Artist (nickname, age) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, nickname);
                statement.setInt(2, age);
                statement.executeUpdate();

                //get the auto-generated ID
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    Artist newArtist = new Artist();
                    newArtist.setId(id);
                    newArtist.setArtistNickname(nickname);
                    newArtist.setArtistAge(age);
                    AuditService.getInstance().logAction("addArtist");
                    return newArtist;
                } else {
                    throw new SQLException("Failed to get the auto-generated ID for the artist.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating artist: " + e.getMessage());
        }
    }
    public boolean deleteArtist(String artistName) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String deleteQuery = "DELETE FROM Artist WHERE nickname = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, artistName);
                int rowsDeleted = statement.executeUpdate();
                AuditService.getInstance().logAction("deleteArtist");
                return rowsDeleted > 0; //returns true if at least one row was deleted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; //return false if an exception occurred
        }
    }
    public void displayAllArtists() {
        try (Connection connection = DatabaseManager.getConnection()) {
            String selectQuery = "SELECT nickname, age FROM artist";
            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = statement.executeQuery();
                System.out.println("List of Artists:");
                while (resultSet.next()) {
                    String artistName = resultSet.getString("nickname");
                    int artistAge = resultSet.getInt("age");
                    System.out.println("Artist Name: " + artistName);
                    System.out.println("Artist Age: " + artistAge);
                    System.out.println("------------------------------------");
                }
            }
            AuditService.getInstance().logAction("displayArtists");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Artist getArtistById(int artistId) {
        Artist artist = null;
        String query = "SELECT * FROM Artist WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, artistId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String nickname = resultSet.getString("nickname");
                    int age = resultSet.getInt("age");

                    //reate an Artist object
                    artist = new Artist();
                    artist.setId(artistId);
                    artist.setArtistNickname(nickname);
                    artist.setArtistAge(age);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return artist;
    }
    public Artist getArtistByName(String artistName) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String selectQuery = "SELECT id, nickname, age FROM artist WHERE nickname = ?";
            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
                statement.setString(1, artistName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String nickname = resultSet.getString("nickname");
                    int age = resultSet.getInt("age");
                    Artist artist = new Artist();
                    artist.setId(id);
                    artist.setArtistNickname(nickname);
                    artist.setArtistAge(age);
                    return artist;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while fetching artist: " + e.getMessage());
        }
        return null; //if artist with given name not found in the db
    }
}
