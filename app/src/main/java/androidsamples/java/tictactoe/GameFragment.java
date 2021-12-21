package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;


public class GameFragment extends Fragment {
    private static final int GRID_SIZE = 9;

    private boolean userTurn, gameEnded = false;
    private boolean isOnePlayer, isFirst;
    private boolean opponentConnected = false;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DatabaseReference gameRef ,userRef;
    private String gameAddress;
    private String userID;
    private final Button[] mButtons = new Button[GRID_SIZE];
    private NavController mNavController;
    private User curUser;
    private TextView mStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //TODO add firebase real time database link
        database = FirebaseDatabase.getInstance("");
        ref = database.getReference("user-data");

        GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
        isOnePlayer = args.getGameType().equals(getString(R.string.one_player));
        gameAddress = args.getGameAddress();


        if(isOnePlayer){
            isFirst = true;
            opponentConnected = true;
        }
        else {
            if(gameAddress.equals("")){
                isFirst = true;
                gameAddress = FirebaseAuth.getInstance().getCurrentUser().getUid();
                ref.child("ActiveGames").child(gameAddress).child("player1move").setValue(new gameData(10,false,true));
            }
            else{
                isFirst = false;
                ref.child("ActiveGames").child(gameAddress).child("player2move").setValue(new gameData(10,false,true));

            }

        }


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = ref.child("user").child(userID);
        gameRef = ref.child("ActiveGames").child(gameAddress);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                curUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        userTurn = isFirst ? true : false;

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(!gameEnded) {
                    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.confirm)
                            .setMessage(R.string.forfeit_game_dialog_message)
                            .setPositiveButton(R.string.yes, (d, which) -> {
                                userRef.setValue(new User(curUser.emailId, curUser.wins, curUser.losses + 1));
                                if(isFirst) {
                                    gameRef.child("player1move").setValue(new gameData(10, true,true));
                                    if(!opponentConnected) {
                                        ref.child("ActiveUsers").child(gameAddress).getRef().removeValue();
                                    }
                                }
                                else{
                                    gameRef.child("player2move").setValue(new gameData(10, true,true));
                                }
                                gameEnded = true;
                                userLoss();
                            })
                            .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                            .create();
                    dialog.show();
                }
                else{
                    for(int i=0;i<9;i++ ){
                        mButtons[i].setText("");
                    }
                    mNavController.popBackStack();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = Navigation.findNavController(view);

        mButtons[0] = view.findViewById(R.id.button0);
        mButtons[1] = view.findViewById(R.id.button1);
        mButtons[2] = view.findViewById(R.id.button2);

        mButtons[3] = view.findViewById(R.id.button3);
        mButtons[4] = view.findViewById(R.id.button4);
        mButtons[5] = view.findViewById(R.id.button5);

        mButtons[6] = view.findViewById(R.id.button6);
        mButtons[7] = view.findViewById(R.id.button7);
        mButtons[8] = view.findViewById(R.id.button8);
        mStatus = view.findViewById(R.id.status);

        if(isOnePlayer){
            mStatus.setText("One Player Game");
        }

        if(!isOnePlayer){
            twoPlayer();
        }

        for(int i=0; i<9; i++) {
            final int finalI = i;
            mButtons[i].setOnClickListener(v -> {
                if (gameEnded || !userTurn || !opponentConnected || !mButtons[finalI].getText().toString().equals("")) return;
                userTurn = false;
                mButtons[finalI].setText(isFirst ? "X" : "O");
                if(isFirst && !isOnePlayer) {
                    gameRef.child("player1move").setValue(new gameData(finalI, false,true));
                }
                else if(!isOnePlayer){
                    gameRef.child("player2move").setValue(new gameData(finalI, false,true));
                }
                userTurn = false;
                checkGameStatus(isFirst);
                if (isOnePlayer) onePlayer();
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isOnePlayer || gameEnded){
            return;
        }
        if(isFirst) {
            gameRef.child("player1move").setValue(new gameData(10, false,false));
        }
        else{
            gameRef.child("player2move").setValue(new gameData(10, false,false));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isOnePlayer && opponentConnected){
            return;
        }
        if(isFirst) {
            gameRef.child("player1move").setValue(new gameData(10, false,true));
        }
        else{
            gameRef.child("player2move").setValue(new gameData(10, false,true));
        }
    }

    private void checkGameStatus(boolean player) {
        int i, j, num = player ? 1 : 2;
        int btnVals[] = new int[9];
        for(i=0; i<9; i++) {
            String str = mButtons[i].getText().toString();
            if(str.equals("")) btnVals[i] = 0;
            else if(str.equals("X")) btnVals[i] = 1;
            else btnVals[i] = 2;
        }
        for(i=0; i<3; i++) {
            boolean hasWon = true;
            for(j=0; j<9; j+=3)
                if(btnVals[i+j] != num)
                    hasWon = false;
            if(hasWon) {
                gameEnded = true;
                if ((isFirst == player)) userWin();
                else userLoss();
            }
        }
        for(i=0; i<9; i+=3) {
            boolean hasWon = true;
            for(j=0; j<3; j++)
                if(btnVals[i+j] != num)
                    hasWon = false;
            if(hasWon) {
                gameEnded = true;
                if ((isFirst == player)) userWin();
                else userLoss();
            }
        }
        if((num == btnVals[0]) && (btnVals[0] == btnVals[4]) && (btnVals[4] == btnVals[8])) {
            gameEnded = true;
            if ((isFirst == player)) userWin();
            else userLoss();
        }
        if ((num == btnVals[2]) && (btnVals[2] == btnVals[4]) && (btnVals[4] == btnVals[6])) {
            gameEnded = true;
            if ((isFirst == player)) userWin();
            else userLoss();
        }
        checkDraw();
    }

    private void userWin() {
        userRef.setValue(new User(curUser.emailId, curUser.wins+1, curUser.losses));
        gameRef.removeValue();
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("WIN")
                .setMessage("You have won")
                .setNeutralButton("Ok", (d, which) -> mNavController.popBackStack())
                .create();
        dialog.show();
    }

    private void userLoss() {
        userRef.setValue(new User(curUser.emailId, curUser.wins, curUser.losses + 1));
        gameRef.removeValue();
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("LOSS")
                .setMessage("You have lost")
                .setNeutralButton("Ok", (d, which) -> mNavController.popBackStack())
                .create();
        dialog.show();
    }

    private void checkDraw() {
        if(gameEnded) return;
        for (Button mButton : mButtons)
            if (mButton.getText().toString().equals(""))
                return;
        gameDraw();
    }

    private void gameDraw() {
        gameEnded = true;
        if(isFirst) gameRef.removeValue();
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("DRAW")
                .setMessage("Its a draw")
                .setNeutralButton("OK", (d, which) -> mNavController.popBackStack())
                .create();
        dialog.show();
    }

    private void onePlayer() {
        if(gameEnded) return;
        Random random = new Random();
        int num = random.nextInt(9);
        while(!mButtons[num].getText().toString().equals(""))
            num = random.nextInt(9);
        mButtons[num].setText("O");
        checkGameStatus(false);
        userTurn = true;
    }

    private void twoPlayer(){
        if(isFirst) {
            gameRef.child("player2move").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    gameData inData = dataSnapshot.getValue(gameData.class);
                    if (inData != null && isAdded()) {
                        String st2 = "";
                        for(int i=0; i<9; i++) st2 += mButtons[i].getText().toString() + " ";
                        if(inData.forfeit)
                            userWin();
                        opponentConnected = inData.pConnected;
                        if(opponentConnected){
                            mStatus.setText("Opponent is connected");
                        }
                        else{
                            mStatus.setText("Opponent is not Connected ");
                        }
                        int move = inData.move;
                        if (inData.move == 10) return;
                        mButtons[move].setText(isFirst ? "O" : "X");
                        checkGameStatus(!isFirst);
                        userTurn = true;

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println(databaseError.getCode());
                }
            });
        }
        else{
            gameRef.child("player1move").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    gameData inData = dataSnapshot.getValue(gameData.class);
                    if (inData != null && isAdded()) {
                        String st2 = "";
                        for(int i=0; i<9; i++) st2 += mButtons[i].getText().toString() + " ";
                        if(inData.forfeit)
                            userWin();
                        opponentConnected = inData.pConnected;
                        if(opponentConnected){
                            mStatus.setText("Opponent is connected");
                        }
                        else{
                            mStatus.setText("Opponent is not Connected ");
                        }
                        int move = inData.move;
                        if (inData.move == 10) return;
                        mButtons[move].setText(isFirst ? "O" : "X");
                        checkGameStatus(!isFirst);
                        userTurn = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println(databaseError.getCode());
                }
            });
        }
    }

}
