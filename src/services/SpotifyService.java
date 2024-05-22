package services;
import database.DatabaseManager;
import models.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import services.*;
public class SpotifyService {
    AlbumService albumService = new AlbumService();
    ArtistService artistService = new ArtistService();
    GenreAlbumService genreAlbumService = new GenreAlbumService();
    MixtapeService mixtapeService = new MixtapeService();
    PlaylistService playlistService = new PlaylistService();
    SongService songService = new SongService();
    UserService userService = new UserService();

    public void playMusic() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Choose the type of media you want to play:");
        System.out.println("1. Album");
        System.out.println("2. Genre Album");
        System.out.println("3. Mixtape");
        System.out.println("4. Playlist");
        System.out.println("5. Song");

        System.out.print("Enter your choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.println("Choose an album to play: (By its ID)");
                albumService.displayAllAlbums();
                if (sc.hasNextInt()) {
                    int albumChoice = sc.nextInt();
                    sc.nextLine(); //consume the newline character
                    Album album = albumService.getAlbumById(albumChoice);
                    if (album != null) {
                        album.play();
                    } else {
                        System.out.println("Invalid album choice.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    sc.nextLine(); //clear the invalid input
                }
                AuditService.getInstance().logAction("playingAlbum");
                break;
            case "2":
                System.out.println("Choose a genre album to play: (By its ID)");
                genreAlbumService.displayAllGenreAlbums();
                if (sc.hasNextInt()) {
                    int genreAlbumChoice = sc.nextInt();
                    sc.nextLine(); //consume the newline chr
                    GenreAlbum genreAlbum = genreAlbumService.getGenreAlbumById(genreAlbumChoice);
                    if (genreAlbum != null) {
                        genreAlbum.play();
                    } else {
                        System.out.println("Invalid genre album choice.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    sc.nextLine(); //clear the invalid input
                }
                AuditService.getInstance().logAction("playingGenreAlbum");
                break;
            case "3":
                System.out.println("Choose a mixtape to play: (By its ID)");
                mixtapeService.displayAllMixtapes();
                if (sc.hasNextInt()) {
                    int mixtapeChoice = sc.nextInt();
                    sc.nextLine(); //consume the newline chr
                    String mixtapeTitle = mixtapeService.getMixtapeTitleById(mixtapeChoice);
                    if (mixtapeTitle != null) {
                        System.out.println("Playing mixtape: " + mixtapeTitle);
                    } else {
                        System.out.println("Invalid mixtape choice.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    sc.nextLine(); //clear the invalid input
                }
                AuditService.getInstance().logAction("playingMixtape");
                break;
            case "4":
                System.out.println("Choose a playlist to play: (By its ID)");
                playlistService.displayAllPlaylists();
                if (sc.hasNextInt()) {
                    int playlistChoice = sc.nextInt();
                    sc.nextLine(); //consume the newline chr
                    String playlistTitle = playlistService.getPlaylistTitleById(playlistChoice);
                    if (playlistTitle != null) {
                        System.out.println("Playing playlist: " + playlistTitle);
                    } else {
                        System.out.println("Invalid playlist choice.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    sc.nextLine(); //clear the invalid input
                }
                AuditService.getInstance().logAction("playingPlaylist");
                break;
            case "5":
                System.out.println("Choose a song to play: (By its ID)");
                songService.displayAllSongs();
                if (sc.hasNextInt()) {
                    int songChoice = sc.nextInt();
                    sc.nextLine(); //consume the newline chr
                    String songTitle = songService.getSongTitleById(songChoice);
                    if (songTitle != null) {
                        System.out.println("Playing song: " + songTitle);
                    } else {
                        System.out.println("Invalid song choice.");
                    }
                } else {
                    System.out.println("Invalid input. Please enter a valid number.");
                    sc.nextLine(); //clear the invalid input
                }
                AuditService.getInstance().logAction("playingSong");
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
}