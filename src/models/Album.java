package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Album extends Media implements Searchable, Playable {
    private int id;
    private String title;
    private Song[] songs;
    private int numSongs;
    private Artist artist;
    private int year;
    private int price;
    private int discount = 0;
    //we use discount initialized with 0 so that when the discount is applied
    //we set it to 1 so we cant use the discount again

    //constructor
    public Album(String title, Song[] songs, int numSongs, Artist artist, int year, int id) {
        this.title = title;
        this.songs = songs;
        this.numSongs = numSongs;
        this.artist = artist;
        this.year = year;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return numSongs == album.numSongs && year == album.year && price == album.price && discount == album.discount && Objects.equals(title, album.title) && Arrays.equals(songs, album.songs) && Objects.equals(artist, album.artist);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(title, numSongs, artist, year, price, discount);
        result = 31 * result + Arrays.hashCode(songs);
        return result;
    }

    //getteri

    public Artist getArtist(){
        return artist;
    }
    public int getId() {
        return id;
    }

    public String getAlbumTitle() {
        return title;
    }

    public Song[] getAlbumSongs() {
        return songs;
    }

    public int getNumSongs() {
        return numSongs;
    }

    public Artist getAlbumArtist() {
        return artist;
    }

    public int getAlbumYear() {
        return year;
    }

    public void setAlbumSongs(Song[] albumSongs) {
        this.songs = albumSongs;
        numSongs = albumSongs.length;
    }

    public int Discount(){
        //if the album has more than 10 songs substract 3 units/song from the total price
        int price = 0;
        if (numSongs<10){
            price = 10 * numSongs;
        }
        else {
            price = 7 * numSongs;
        }
        if(year <= 2015){//if the album is released before 2015 -> 50% off
            price /= 2;
        }
        return price;
    }

    public int calculateDuration(){
        int d = 0;
        for (Song song: songs){
            d += song.getSongTime();
        }
        return d/60;
    }


    //searchable functions
    @Override
    public List<Song> searchByTitle(String title){
        List<Song> result = new ArrayList<Song>();
        for (Song song : songs){
            if (song.getSongTitle().equals(title))
                result.add(song);
        }
        return result;
    }

    @Override
    public List<Song> searchByArtist(String artist){
        List<Song> result = new ArrayList<Song>();
        for (Song song : songs){
            if(song.getSongArtist().getArtistNickname().equals(artist))
                result.add(song);
        }
        return result;
    }

    @Override
    public List<Song> searchByYear(int year){
        List<Song> result = new ArrayList<Song>();
        for(Song song : songs){
            if (song.getSongYear() == year)
                result.add(song);
        }
        return result;
    }

    @Override
    public void displayDetails(){
        System.out.println("Album: " + title + " by " + artist.getArtistNickname()
        + " from " + year + " with duration " + calculateDuration() + " minutes");
        System.out.println("Price: " + Discount());
        for(Song song : songs)
            song.displayDetails();
        System.out.println();
    }

    //method to play a track
    @Override
    public void play(){
        System.out.println("Playing album: " + title + " by " + artist.getArtistNickname());
    }
}
