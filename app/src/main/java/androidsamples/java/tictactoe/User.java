package androidsamples.java.tictactoe;

public class User {
    public String emailId;
    public int wins;
    public int losses;

    public User(){
        this.emailId = "";
        this.wins = 0;
        this.losses = 0;
    }

    public User(String emailId,int wins,int losses) {
        this.emailId = emailId;
        this.wins = wins;
        this.losses = losses;
    }
    public String getEmailId() {
        return emailId;
    }
}