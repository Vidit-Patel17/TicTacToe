package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";
    private NavController mNavController;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DatabaseReference userRef;
    private String userID;
    private TextView userDetails;
    private ArrayList<String> userList = new ArrayList<String>();
    private User currentUser;
    public DashboardFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO add firebase real time database link
        database = FirebaseDatabase.getInstance("");
        ref = database.getReference("user-data");
        userRef = ref.child("user");
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = userRef.child(userID);
        }
        setHasOptionsMenu(true); // Needed to display the action menu for this fragment
        backBtnCallback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);



        return view;
    }


    public void setPlaceHolder(){
        if(currentUser == null){
            return;
        }
        String txt = "Welcome "  + currentUser.emailId + " " + "\n" + "Your Wins are: " + currentUser.wins + "\n" + "Your losses are: " + currentUser.losses;
        userDetails.setText(txt);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = Navigation.findNavController(view);

        userDetails = view.findViewById(R.id.txt_score);

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            mNavController.navigate(R.id.action_need_auth);

        RecyclerView entriesList = view.findViewById(R.id.list);
        entriesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        Query query = ref.child("ActiveUsers").limitToLast(50);
        FirebaseRecyclerOptions<ActiveUsers> options
                = new FirebaseRecyclerOptions.Builder<ActiveUsers>()
                .setQuery(query, ActiveUsers.class)
                .setLifecycleOwner(this)
                .build();
        OpenGamesAdapter adapter = new OpenGamesAdapter(options,mNavController);
        entriesList.setAdapter(adapter);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                setPlaceHolder();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getCode());
            }
        });


        // Show a dialog when the user clicks the "new game" button
        view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {
            // A listener for the positive and negative buttons of the dialog
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                String gameType = "No type";
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    gameType = getString(R.string.two_player);
                    ref.child("ActiveUsers").child(userID).setValue(new ActiveUsers(currentUser.getEmailId(),userID));
                }
                else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    gameType = getString(R.string.one_player);
                }
                Log.d(TAG, "New Game: " + gameType);

                // Passing the game type as a parameter to the action
                // extract it in GameFragment in a type safe way
                NavDirections action = DashboardFragmentDirections.actionGame(gameType,"");
                mNavController.navigate(action);
            };

            // create the dialog
            AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.new_game)
                    .setMessage(R.string.new_game_dialog_message)
                    .setPositiveButton(R.string.two_player, listener)
                    .setNegativeButton(R.string.one_player, listener)
                    .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
                    .create();
            dialog.show();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
    }


    private void backBtnCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                        .setTitle("Confirm")
                        .setMessage("Do you want to Quit?")
                        .setPositiveButton("Yes", (d, which) -> {
                            requireActivity().finishAffinity();
                            System.exit(0);
                        })
                        .setNegativeButton("No", (d, which) -> d.dismiss())
                        .create();
                dialog.show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
