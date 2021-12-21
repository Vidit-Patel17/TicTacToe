package androidsamples.java.tictactoe;

public class ActiveUsers {
    public String emailId;
    public String UID;
    public ActiveUsers(){
        this.emailId = "";
        this.UID = "";
    }
    public ActiveUsers(String emailId,String UID){
        this.emailId = emailId;
        this.UID = UID;
    }
    public String getEmailId(){
        return this.emailId;
    }
}
