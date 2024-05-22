package models;

public class UserAccount {
    //User is inherited from this
    protected String username;
    protected String password;

    protected boolean changePassword(String thisPassword, String newPassword) {
        if (thisPassword.equals(password)) { //check if the entered current password matches the stored password
            password = newPassword;
            return true;
        } else {
            return false;
        }
    }

    //constructor
    public UserAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }
    //getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
