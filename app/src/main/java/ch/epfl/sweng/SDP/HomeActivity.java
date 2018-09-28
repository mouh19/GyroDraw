package ch.epfl.sweng.SDP;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    /**
     * Signs the current user out and starts the {@link MainActivity}.
     *
     * @param view the view corresponding to the clicked button
     */
    public void signOut(View view) {
        final Toast toast = Toast
                .makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT);
        toast.show();

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            toast.cancel();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Deletion failed
                        }
                    }
                });
    }

    /**
     * Deletes the user from FirebaseAuth and deletes any existing credentials for the user in
     * Google Smart Lock. It then starts the {@link MainActivity}.
     *
     * @param view the view corresponding to the clicked button
     */
    public void delete(View view) {
        final Toast toast = Toast
                .makeText(getApplicationContext(), "Deleting account...", Toast.LENGTH_SHORT);
        toast.show();

        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            toast.cancel();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Deletion failed
                        }
                    }
                });
    }
}
