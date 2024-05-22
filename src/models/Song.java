package models;

import java.util.Objects;

public class Song extends Media implements Playable{
    private int id;
    private String title;
    private Artist artist;
    private int year;
    private int time; //duration of the song

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return year == song.year && time == song.time && Objects.equals(title, song.title) && Objects.equals(artist, song.artist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist, year, time);
    }

    //setters
    public void setSongId(int id) { this.id = id; }
    public void setSongTitle(String title) {
        this.title = title;
    }

    public void setSongArtist(Artist artist) {
        this.artist = artist;
    }

    public void setSongYear(int year) {
        this.year = year;
    }

    public void setSongTime(int time) {
        if(time <= 0){
            throw new IllegalArgumentException("Time must be greater than 0");
        }
        this.time = time;
    }

    //getters

    public int getId() { return id; }
    public String getTitle() {
        return title;
    }

    @Override // the one from Media
    public String getSongTitle() {
        return title;
    }

    public Artist getSongArtist() {
        return artist;
    }

    public int getSongYear() {
        return year;
    }

    public int getSongTime() {
        return time;
    }

    @Override
    public void displayDetails(){
        System.out.println("Song: '" + title + "'  - by " + artist.getArtistNickname()
        + " from " + year + " with duration " + time + " seconds " + '\n');
    }

    @Override
    public void play(){
        System.out.println("Playing: " + title + " by " + artist.getArtistNickname());
    }


}
