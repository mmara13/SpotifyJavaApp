package models;

import java.util.ArrayList;
import java.util.Objects;

public class User extends UserAccount{
    private int id;
    private String name;
    public ArrayList<Media> library = new ArrayList<>(); //public so that it can be accessed from Spotify Service to add a media to the user library
    private static int nrOfUsers = 0; // crt nr of users
    private static final int MAX_NR_OF_USERS = 10; //MAXIMUM nr of users


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(library, user.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, library);
    }

    //constructor

    public User(String username, String password, String name) {
        super(username, password);
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public static int getNrOfUsers() {
        return nrOfUsers;
    }
    public  String getName() { return name; }
    public String getUsername() { return username;}

    public static boolean isMaxUserReached(){
        return nrOfUsers >= MAX_NR_OF_USERS;
    }

    public boolean changeUserPassword(String thisPassword, String newPassword) {
        return changePassword(thisPassword, newPassword);
    }

    public void addToLibrary(Media media){
        if(library.contains(media)){
          throw new IllegalArgumentException("This element is already in the library");
        }
        library.add(media);
        System.out.println("Successfully added " + media.getSongTitle() + " to the library!");
    }

    public boolean authenticateUser(String u, String pss){
        if(u.isBlank() || pss.isBlank())
            throw new IllegalArgumentException("Username or password cannot be empty!");
        if(!u.equals(username) || !pss.equals(password))
            return false; //modified so that we can display the library in main only if authenticated with success
        System.out.println("Successfully authenticated!");
        return true;
    }
}
