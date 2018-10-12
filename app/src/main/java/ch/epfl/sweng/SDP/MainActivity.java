package ch.epfl.sweng.SDP;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.login_button).setOnClickListener(this);

        FirebaseApp.initializeApp(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("hasAccount", false)) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    public void onClick(View view) {
        startLogin();
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
