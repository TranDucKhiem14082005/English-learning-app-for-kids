package khiem.it.tinyenglish;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import khiem.it.tinyenglish.ui.HomeFragment;
import khiem.it.tinyenglish.ui.LessonsFragment;
import khiem.it.tinyenglish.ui.ProfileFragment;
import khiem.it.tinyenglish.ui.VocabularyFragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout authContainer;
    private LinearLayout appContainer;
    private TextView statusText;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authContainer = findViewById(R.id.authContainer);
        appContainer = findViewById(R.id.appContainer);
        statusText = findViewById(R.id.statusText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        MaterialButton registerButton = findViewById(R.id.registerButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        loginButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                openFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_lessons) {
                openFragment(new LessonsFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_vocabulary) {
                openFragment(new VocabularyFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                openFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null && auth.getCurrentUser() != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        refreshUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    public void refreshUi() {
        FirebaseUser currentUser = auth.getCurrentUser();
        boolean loggedIn = currentUser != null;

        authContainer.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        appContainer.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        statusText.setText(loggedIn ? getString(R.string.logged_in_message)
                : getString(R.string.logged_out_message));

        if (loggedIn && getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            openFragment(new HomeFragment());
        }
    }

    private void openFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
