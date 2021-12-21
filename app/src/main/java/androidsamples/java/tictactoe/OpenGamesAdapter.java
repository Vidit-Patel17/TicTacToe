

package androidsamples.java.tictactoe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class OpenGamesAdapter extends FirebaseRecyclerAdapter<ActiveUsers , OpenGamesAdapter.Viewholder> {
    private NavController mNavController;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    public OpenGamesAdapter(@NonNull FirebaseRecyclerOptions<ActiveUsers> options,NavController navController){
        super(options);
        this.mNavController = navController;
    }

    @Override
    protected void onBindViewHolder(@NonNull Viewholder holder,int position, @NonNull ActiveUsers model) {
        holder.mContent.setText(model.getEmailId());
        holder.gameAddress.setText(model.UID);
    }

    @NonNull
    @Override
    public Viewholder
    onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new Viewholder(view);
    }

    class Viewholder extends RecyclerView.ViewHolder {
        TextView mContent,mIdView,gameAddress;
        public Viewholder(@NonNull View itemView){
            super(itemView);
            mIdView = itemView.findViewById(R.id.item_number);
            mContent = itemView.findViewById(R.id.content);
            gameAddress = itemView.findViewById(R.id.gameAddress);
            //TODO add firebase real time database link
            database = FirebaseDatabase.getInstance("");
            ref = database.getReference("user-data").child("ActiveUsers");

            itemView.setOnClickListener(v-> {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            if(ds.child("emailId").getValue(String.class).equals(mContent.getText().toString())){
                                ds.getRef().removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                NavDirections action = DashboardFragmentDirections.actionGame("Two-Player",gameAddress.getText().toString());
                mNavController.navigate(action);
            });
        }
    }
}
