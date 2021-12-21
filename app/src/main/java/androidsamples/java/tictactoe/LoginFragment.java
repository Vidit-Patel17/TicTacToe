package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {
    EditText edit_email, edit_password;
    String email = "", password = "";
    FirebaseAuth firebaseAuth;
    NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        backBtnCallback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        edit_email = view.findViewById(R.id.edit_email);
        edit_password = view.findViewById(R.id.edit_password);
        view.findViewById(R.id.btn_log_in).setOnClickListener(v -> btnLogIN());
        view.findViewById(R.id.btn_forgot_pwd).setOnClickListener(v -> btnForgot());
    }

    private void btnLogIN() {
        email = edit_email.getText().toString();
        if (email.equals("")) {
            Toast.makeText(requireContext(), "Please enter email address", Toast.LENGTH_SHORT).show();
            return;
        }
        password = edit_password.getText().toString();
        if (password.equals("")) {
            Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        logIN();
    }

    private void btnForgot() {
        email = edit_email.getText().toString();
        if (email.equals("")) {
            Toast.makeText(requireContext(), "Please enter email address", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(requireContext(), "Password reset link sent to your email", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show();
                });
    }

    private void logIN() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (!task.isSuccessful()) {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            if (e.getClass() == FirebaseAuthInvalidCredentialsException.class)
                                Toast.makeText(requireContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                            else
                                registerDialog();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.dashboardFragment);
                    }
                });
    }

    private void registerDialog() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE)
                register();
        };
        AlertDialog alertDialog = new AlertDialog
                .Builder(requireActivity())
                .setTitle("Register")
                .setMessage("Email not found. Would you like to register now?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", (d, which) -> d.dismiss())
                .create();
        alertDialog.show();
    }

    private void register() {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (!task.isSuccessful()) {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            if (e instanceof FirebaseAuthWeakPasswordException)
                                Toast.makeText(requireContext(), ((FirebaseAuthWeakPasswordException) e).getReason(), Toast.LENGTH_SHORT).show();
                            else if (e instanceof FirebaseAuthInvalidCredentialsException)
                                Toast.makeText(requireContext(), "Registration failure, invalid email address", Toast.LENGTH_SHORT).show();
                            else if (e instanceof FirebaseAuthUserCollisionException)
                                Toast.makeText(requireContext(), "Registration failure, email already exists", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(requireContext(), "Registration failure, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //TODO add firebase real time database link
                        FirebaseDatabase database = FirebaseDatabase.getInstance("");
                        DatabaseReference userRef = database.getReference("user-data").child("user");
                        String UUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        userRef.child(UUID).setValue(new User(email,0,0));
                        Toast.makeText(requireContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.dashboardFragment);
                    }
                });
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
