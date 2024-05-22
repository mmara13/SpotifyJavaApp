package models;

import java.util.Objects;

public class Artist {
    private int id;
    private String nickname; //artist name
    private int age; //artist age

    //constructor without param
    public Artist() {
        nickname = "Unknown";
        age = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return age == artist.age && Objects.equals(nickname, artist.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, age);
    }

    //getteri

    public int getId() { return id;}

    public String getArtistNickname() {
        return nickname;
    }

    public int getArtistAge() {
        return age;
    }

    //setteri

    public void setId(int id) { this.id = id;}

    public void setArtistNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setArtistAge(int age) {
        this.age = age;
    }
}
