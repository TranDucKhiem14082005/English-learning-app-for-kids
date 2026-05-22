package khiem.it.tinyenglish;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.regex.Pattern;

import khiem.it.tinyenglish.model.UserProfile;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        Button registerButton = findViewById(R.id.registerActionButton);
        Button backButton = findViewById(R.id.backButton);

        registerButton.setOnClickListener(v -> register());
        backButton.setOnClickListener(v -> finish());
    }

    private void register() {
        String username = valueOf(usernameInput);
        String email = valueOf(emailInput);
        String password = valueOf(passwordInput);
        String confirmPassword = valueOf(confirmPasswordInput);

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError(getString(R.string.error_username_required));
            return;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            usernameInput.setError(getString(R.string.error_username_invalid));
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.error_email_invalid));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.error_password_required));
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError(getString(R.string.error_password_short));
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError(getString(R.string.error_password_mismatch));
            return;
        }

        String usernameKey = username.toLowerCase(Locale.US);
        rootRef.child("usernames").child(usernameKey).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(this, R.string.error_username_taken, Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        if (auth.getCurrentUser() == null) {
                            Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = auth.getCurrentUser().getUid();
                        UserProfile profile = new UserProfile(uid, username, email);
                        rootRef.child("users").child(uid).setValue(profile)
                                .addOnSuccessListener(unused -> rootRef.child("usernames").child(usernameKey).setValue(profile)
                                        .addOnSuccessListener(done -> {
                                            auth.signOut();
                                            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(error ->
                                                Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(error ->
                                        Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(error -> {
                        if (error instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, R.string.error_email_taken, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show();
                        }
                        android.util.Log.e("RegisterActivity", "FirebaseAuth error: " + error.getMessage());
                    });
        }).addOnFailureListener(error ->
                Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show());
    }

    private String valueOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
