package models;


import java.util.Objects;
import java.util.Set;

public class GenreAlbum extends Album{
    private int id;
    private Set<String> genres;

    //constructor
    public GenreAlbum(String title, Song[] songs, int numSongs, Artist artist, int year, Set<String> genres, int id) {
        super(title, songs, numSongs, artist, year, id);
        this.genres = genres;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenreAlbum that = (GenreAlbum) o;
        return Objects.equals(genres, that.genres);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), genres);
    }

    //setter + getter
    public Set<String> getGenres() {
        return genres;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    @Override
    public void displayDetails(){
        System.out.println("Genre album: " + getAlbumTitle() + " by " + getAlbumArtist().getArtistNickname()
                + " from " + getAlbumYear() + " with duration " + calculateDuration() + " minutes");
//        for(Song song : songs)
//            song.displayDetails();
        //for genre albums we will print the genres instead of the songs
        System.out.print("Genres: ");
        for(String genre : this.getGenres())
            System.out.print(genre + " ");
        System.out.println();
    }

    @Override
    public void play(){
        System.out.println("Playing genre album: " + getAlbumTitle() + " by " + getAlbumArtist().getArtistNickname());
    }
}

