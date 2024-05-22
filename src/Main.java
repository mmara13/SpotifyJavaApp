import models.*;
import services.*;

import java.sql.SQLException;
import java.util.Scanner;


public class Main{
    public static void main(String[] args) throws SQLException {
        //adding the services

        SpotifyService spotifyService = new SpotifyService();
        AlbumService albumService = new AlbumService();
        ArtistService artistService = new ArtistService();
        GenreAlbumService genreAlbumService = new GenreAlbumService();
        MixtapeService mixtapeService = new MixtapeService();
        PlaylistService playlistService = new PlaylistService();
        SongService songService = new SongService();
        UserService userService = new UserService();
        //InitialDataLoader.loadInitialData(spotifyService); this was before the db
        Scanner scanner = new Scanner(System.in);

        boolean exit = false;
        while(!exit){
            System.out.println("Main menu:");
            System.out.println("1. Options for Album");
            System.out.println("2. Options for Artist");
            System.out.println("3. Options for GenreAlbum");
            System.out.println("4. Options for Mixtape");
            System.out.println("5. Options for Playlist");
            System.out.println("6. Options for Song");
            System.out.println("7. Options for User");
            System.out.println("8. Play Music");
            System.out.print("Enter your choice (or type 'exit' to quit): ");
            AuditService.getInstance().logAction("openingProject");

            String choice = scanner.nextLine();

            switch (choice){
                case "1":
                    albumOptionsMenu(albumService, scanner);
                    break;
                case "2":
                    artistOptionsMenu(artistService, scanner);
                    break;
                case "3":
                    genreAlbumOptionsMenu(genreAlbumService, scanner);
                    break;
                case "4":
                    mixtapeOptionsMenu(mixtapeService, scanner);
                    break;
                case "5":
                    playlistOptionsMenu(playlistService, scanner);
                    break;
                case "6":
                    songOptionsMenu(songService, scanner);
                    break;
                case "7":
                    userOptionsMenu(userService, scanner);
                    break;
                case "8":
                    spotifyService.playMusic();
                    break;
                case "exit":
                    AuditService.getInstance().logAction("exitingProject");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");

            }
        }
        scanner.close();
    }
    private static void albumOptionsMenu(AlbumService albumService, Scanner scanner) {
        boolean exit = false;
        while(!exit){
            System.out.println("Album Options: ");
            System.out.println("1. Add new Album");
            System.out.println("2. See existing Albums");
            System.out.println("3. Delete an existing album");
            System.out.println("4. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    albumService.addNewAlbum(scanner);
                    break;
                case "2":
                    albumService.displayAllAlbums();
                    break;
                case "3":
                    boolean isDeleted = albumService.deleteAlbum();
                    if (isDeleted) {
                        System.out.println("Album deleted successfully.");
                    } else {
                        System.out.println("Album not found. No album deleted.");
                    }
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    private static void artistOptionsMenu(ArtistService artistService, Scanner scanner) {
        boolean exit = false;
        while(!exit) {
            System.out.println("Artist Options: ");
            System.out.println("1. Add new Artist");
            System.out.println("2. See existing Artists");
            System.out.println("3. Delete an existing artist");
            System.out.println("4. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    artistService.createArtist();
                    break;
                case "2":
                    artistService.displayAllArtists();
                    break;
                case "3":
                    System.out.print("Enter the name of the artist you want to delete: ");
                    String artistNameToDelete = scanner.nextLine();
                    boolean isDeleted = artistService.deleteArtist(artistNameToDelete);
                    if (isDeleted) {
                        System.out.println("Artist deleted successfully.");
                    } else {
                        System.out.println("Artist not found. No artist deleted.");
                    }
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static void genreAlbumOptionsMenu(GenreAlbumService genreAlbumService, Scanner scanner) {
        boolean exit = false;
        while (!exit) {
            System.out.println("Genre Album Options: ");
            System.out.println("1. Add new Genre Album");
            System.out.println("2. See existing Genre Albums");
            System.out.println("3. Delete an existing Genre Album");
            System.out.println("4. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    genreAlbumService.createGenreAlbum();
                    break;
                case "2":
                    genreAlbumService.displayAllGenreAlbums();
                    break;
                case "3":
                    boolean isDeleted = genreAlbumService.deleteGenreAlbum();
                    if (isDeleted) {
                        System.out.println("Genre Album deleted successfully.");
                    } else {
                        System.out.println("Genre Album not found. No album deleted.");
                    }
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    private static void mixtapeOptionsMenu(MixtapeService mixtapeService, Scanner scanner) {
        boolean exit = false;
        while (!exit) {
            System.out.println("Mixtape Options: ");
            System.out.println("1. Add new Mixtape");
            System.out.println("2. See existing Mixtapes");
            System.out.println("3. Delete an existing Mixtape");
            System.out.println("4. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    mixtapeService.createMixtape();
                    break;
                case "2":
                    mixtapeService.displayAllMixtapes();
                    break;
                case "3":
                    boolean isDeleted = mixtapeService.deleteMixtape();
                    if (isDeleted) {
                        System.out.println("Mixtape deleted successfully.");
                    } else {
                        System.out.println("Mixtape not found. No mixtape deleted.");
                    }
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static void playlistOptionsMenu(PlaylistService playlistService, Scanner scanner) {
        boolean exit = false;
        while (!exit) {
            System.out.println("Playlist Options: ");
            System.out.println("1. Add new Playlist");
            System.out.println("2. See existing Playlists");
            System.out.println("3. Delete an existing playlist");
            System.out.println("4. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    playlistService.createPlaylist();
                    break;
                case "2":
                    playlistService.displayAllPlaylists();
                    break;
                case "3":
                    boolean isDeleted = playlistService.deletePlaylist();
                    if (isDeleted) {
                        System.out.println("Playlist deleted successfully.");
                    } else {
                        System.out.println("Playlist not found. No playlist deleted.");
                    }
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    private static void songOptionsMenu(SongService songService, Scanner scanner) {
        boolean exit = false;
        while (!exit) {
            System.out.println("Song Options: ");
            System.out.println("1. Add new Song");
            System.out.println("2. See existing songs");
            System.out.println("3. See all songs alphabetically");
            System.out.println("4. Delete an existing song");
            System.out.println("5. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    songService.createSong();
                    break;
                case "2":
                    songService.displayAllSongs();
                    break;
                case "3":
                    songService.displayAllSongsAlph();
                    break;
                case "4":
                    boolean isDeleted = songService.deleteSong();
                    if (isDeleted) {
                        System.out.println("Song deleted successfully.");
                    } else {
                        System.out.println("Song not found. No song deleted.");
                    }
                    break;
                case "5":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    private static void userOptionsMenu(UserService userService, Scanner scanner) throws SQLException {
        boolean exit = false;
        while(!exit){
            System.out.println("User Options: ");
            System.out.println("1. Add new User");
            System.out.println("2. See existing Users");
            System.out.println("3. Change password");
            System.out.println("4. Delete an account");
            System.out.println("5. Media Menu"); //to access the library of a user
            System.out.println("6. Back to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    userService.createUser();
                    break;
                case "2":
                    userService.displayAllUsers();
                    break;
                case "3":
                    userService.displayAllUsers(); //display all users
                    System.out.println("Enter the username of the user whose password you want to change:");
                    String username = scanner.nextLine();
                    User user = userService.findUserByUsername(username);
                    if (user != null) {
                        System.out.println("Enter the current password:");
                        String currentPassword = scanner.nextLine();
                        if (user.changeUserPassword(currentPassword, currentPassword)) {
                            System.out.println("Enter the new password:");
                            String newPassword = scanner.nextLine();
                            if (user.changeUserPassword(currentPassword, newPassword)) {
                                if (userService.updateUserPasswordInDatabase(username, newPassword)) {
                                    System.out.println("Password changed successfully!");
                                } else {
                                    System.out.println("Error updating password in the database.");
                                }
                            } else {
                                System.out.println("Incorrect current password.");
                            }
                        } else {
                            System.out.println("Incorrect current password.");
                        }
                    } else {
                        System.out.println("User not found!");
                    }
                    break;
                case "4":
                    userService.deleteUser();
                case "5":
                    mediaMenu(userService, scanner);
                    break;
                case "6" :
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    public static void mediaMenu(UserService userService, Scanner scanner) throws SQLException {
        boolean exit = false;
        while (!exit) {
            System.out.println("Media Menu:");
            System.out.println("1. Show Library");
            System.out.println("2. Add to Library");
            System.out.println("3. Back to User Menu");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            switch(choice){
                case "1":
                    //show library
                    System.out.print("Enter your username: ");
                    String username = scanner.nextLine();
                    System.out.println("Enter your password: ");
                    String password = scanner.nextLine();
                    User authenticatedUser = userService.findUserByUsername(username);
                    if(authenticatedUser != null){
                        if(authenticatedUser.authenticateUser(username, password)){
                            userService.displayUserLibrary(authenticatedUser);
                        } else {
                            System.out.println("Authentication failed. Invalid username or password.");
                        }
                    }
                    else {
                        System.out.println("User not found.");
                    }
                    break;
                case "2":
                    //add to library
                    System.out.print("Enter your username: ");
                    String username1 = scanner.nextLine();
                    System.out.print("Enter your password: ");
                    String password1 = scanner.nextLine();
                    User authenticatedUser1 = userService.findUserByUsername(username1);
                    if(authenticatedUser1 != null){
                        if(authenticatedUser1.authenticateUser(username1, password1)){
                            userService.addToUserLibrary(authenticatedUser1);
                        } else {
                            System.out.println("Authentication failed. Invalid username or password.");
                        }
                    }
                    else {
                        System.out.println("User not found.");
                    }
                    break;
                case "3":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
}