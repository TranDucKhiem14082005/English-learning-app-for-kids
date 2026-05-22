package khiem.it.tinyenglish;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginActionButton);
        Button backButton = findViewById(R.id.backButton);

        loginButton.setOnClickListener(v -> login());
        backButton.setOnClickListener(v -> finish());
    }

    private void login() {
        String username = valueOf(usernameInput);
        String password = valueOf(passwordInput);

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError(getString(R.string.error_username_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.error_password_required));
            return;
        }

        String key = username.toLowerCase(Locale.US);
        usernamesRef.child(key).get().addOnSuccessListener(snapshot -> {
            String email = snapshot.child("email").getValue(String.class);
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.error_username_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(error -> {
                        if (error instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, R.string.error_wrong_password, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, R.string.error_username_not_found, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }).addOnFailureListener(error ->
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show());
    }

    private String valueOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
