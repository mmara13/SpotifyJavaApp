package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Playlist extends Media implements Searchable, Playable{
    private int id;
    private String title;
    private Song[] songs;
    private int numSongs;

    //constructor

    public Playlist(Song[] songs, int numSongs) {
        this.songs = songs;
        this.numSongs = numSongs;
        this.title = "Not assigned yet";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return numSongs == playlist.numSongs && Objects.equals(title, playlist.title) && Arrays.equals(songs, playlist.songs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(title, numSongs);
        result = 31 * result + Arrays.hashCode(songs);
        return result;
    }

    //sett+gett

    public void setPlaylistId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getPlaylistName() {
        return title;
    }

    public void setPlaylistName(String title) {
        if(title.length() >50)
            throw new IllegalArgumentException("The name of the playlist must be under 50 characters!");
        this.title= title;
    }

    public void setSongs(Song[] songs) {
        this.songs = songs;
    }


    public Song[] getPlaylistSongs() {
        return songs;
    }

    public int getPlaylistNumSongs() {
        return numSongs;
    }

    //method to rpint its details
    private void printPlaylist(){
        System.out.println("Playlist title: " + this.title);
        System.out.println("Songs in this playlist: ");
        int i = 1;
        for(Song song : this.songs) {
            System.out.println(i + ") \"" + song.getSongTitle() + "\" by " + song.getSongArtist().getArtistNickname() + ".");
            i++;
        }
    }

    //methods from Searchable interface
    @Override
    public List<Song> searchByTitle(String title){
        List<Song> result = new ArrayList<Song>();
        for(Song song : songs){
            if(song.getSongTitle().equals(title))
                result.add(song);
        }
        return result;
    }
    @Override
    public List<Song> searchByArtist(String artist){
        List<Song> result = new ArrayList<Song>();
        for(Song song : songs){
            if(song.getSongArtist().getArtistNickname().equals(artist))
                result.add(song);
        }
        return result;
    }

    @Override
    public List<Song> searchByYear(int year){
        List<Song> result = new ArrayList<Song>();
        for(Song song : songs){
            if(song.getSongYear()==year)
                result.add(song);
        }
        return result;
    }
    @Override
    public void displayDetails(){
        System.out.println("Playlist: " + title);
        for(Song song : songs)
            song.displayDetails();
        System.out.println();
    }

    //method for playing
    @Override
    public void play(){
        System.out.println("Playing playlist: " + title);
    }
}
