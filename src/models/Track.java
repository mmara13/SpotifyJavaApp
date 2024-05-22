package models;

public class Track<T extends Playable>{
    private T data;

    public Track(T trackData) {
        this.data = trackData;
    }

    public void play(){
        data.play();
    }
}
