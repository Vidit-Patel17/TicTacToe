package androidsamples.java.tictactoe;

public class gameData {
    public int move;
    public boolean forfeit;
    public boolean pConnected;
    public gameData(){
        this.move = 10;
        this.forfeit = false;
        this.pConnected = false;
    }
    public gameData(int move,boolean forfeit,boolean pConnected){
        this.move = move;
        this.forfeit = forfeit;
        this.pConnected = pConnected;
    }
}
