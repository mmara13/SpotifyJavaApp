package models;

public abstract class Media {
    private String title;

    //public abstract void Media(){}; //abstract constructor
    public String getSongTitle() {
        return title;
    }
    public abstract void displayDetails();
}
