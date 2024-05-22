package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Mixtape extends Media implements Playable{
    private int id;
    private String title;
    private Album album;
    private Playlist playlist;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mixtape mixtape = (Mixtape) o;
        return id == mixtape.id && Objects.equals(title, mixtape.title) && Objects.equals(album, mixtape.album) && Objects.equals(playlist, mixtape.playlist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, album, playlist);
    }

    public Mixtape(String title, Artist artist, int nrAlbum, Song[] albumSongs, int y,
                   int nrPlaylist, Song[] playlistSongs, int id) {
        this.title = title;
        this.album = new Album(title, albumSongs, nrAlbum, artist, y, id);
        this.playlist = new Playlist(playlistSongs, nrPlaylist);
    }


    public String getMixtapeName() {
        return title;
    }

    public void displayDetails(){
        System.out.println("Mixtape: " + title);
        List<Song> songs = new ArrayList<Song>();
        songs.addAll(Arrays.asList(album.getAlbumSongs()));
        songs.addAll(Arrays.asList(playlist.getPlaylistSongs()));

        for(Song song : songs)
            song.displayDetails();
        System.out.println();
    }
    //we dont need anything else so we can print the mixtape bcs we have this fct

    @Override
    public void play(){
        System.out.println("Playing mixtape: " + title);
    }
}

