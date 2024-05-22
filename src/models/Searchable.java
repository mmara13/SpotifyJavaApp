package models;

import java.util.List;

public interface Searchable {
    List<Song> searchByTitle(String title);
    List<Song> searchByArtist(String artist);
    List<Song> searchByYear(int year);
}


