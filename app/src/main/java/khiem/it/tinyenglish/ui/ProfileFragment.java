package khiem.it.tinyenglish.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import khiem.it.tinyenglish.MainActivity;
import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.model.UserProfile;

public class ProfileFragment extends Fragment {

    private static final String BIRTH_DATE_PATTERN = "dd/MM/yyyy";
    private static final String PREFS_NAME = "profile_prefs";
    private static final String PREF_AVATAR_PREFIX = "avatar_uri_";

    private TextView usernameText;
    private TextView emailText;
    private MaterialAutoCompleteTextView genderInput;
    private TextInputEditText ageInput;
    private TextInputEditText birthInput;
    private ImageView avatarImage;
    private MaterialButton saveButton;
    private DatabaseReference profileRef;
    private DatabaseReference rootRef;
    private ValueEventListener listener;
    private UserProfile currentProfile;
    private ActivityResultLauncher<String[]> avatarPicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarPicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) {
                return;
            }
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
            saveAvatarUri(uri.toString());
            loadAvatar(uri.toString());
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        usernameText = view.findViewById(R.id.profileUsernameText);
        emailText = view.findViewById(R.id.profileEmailText);
        genderInput = view.findViewById(R.id.profileGenderInput);
        ageInput = view.findViewById(R.id.profileAgeInput);
        birthInput = view.findViewById(R.id.profileBirthInput);
        avatarImage = view.findViewById(R.id.profileAvatarImage);
        saveButton = view.findViewById(R.id.saveProfileButton);
        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);
        rootRef = FirebaseDatabase.getInstance().getReference();

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.profile_gender_options,
                android.R.layout.simple_list_item_1
        );
        genderInput.setAdapter(genderAdapter);

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if (requireActivity() instanceof MainActivity mainActivity) {
                mainActivity.refreshUi();
            }
        });

        avatarImage.setOnClickListener(v -> avatarPicker.launch(new String[]{"image/*"}));
        birthInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String birthDate = s == null ? "" : s.toString().trim();
                if (TextUtils.isEmpty(birthDate)) {
                    birthInput.setError(null);
                    return;
                }
                if (birthDate.length() < BIRTH_DATE_PATTERN.length()) {
                    birthInput.setError(null);
                    return;
                }
                Long computedAge = computeAgeFromBirthDate(birthDate);
                if (computedAge == null) {
                    birthInput.setError(getString(R.string.profile_birth_error));
                    return;
                }
                birthInput.setError(null);
                ageInput.setText(String.valueOf(computedAge));
            }
        });
        saveButton.setOnClickListener(v -> saveProfile());

        bindProfile();
        loadAvatarFromPrefs();
        return view;
    }

    private void bindProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (TextUtils.isEmpty(uid)) {
            usernameText.setText(R.string.profile_not_available);
            emailText.setText("");
            return;
        }

        profileRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile profile = snapshot.getValue(UserProfile.class);
                if (profile == null) {
                    currentProfile = null;
                    usernameText.setText(R.string.profile_not_available);
                    emailText.setText("");
                    genderInput.setText("", false);
                    ageInput.setText("");
                    birthInput.setText("");
                    return;
                }
                currentProfile = profile;
                usernameText.setText(getString(R.string.profile_username_format, profile.getUsername()));
                emailText.setText(getString(R.string.profile_email_format, profile.getEmail()));
                genderInput.setText(profile.getGender() == null ? "" : profile.getGender(), false);
                String birthDate = profile.getBirthDate() == null ? "" : profile.getBirthDate();
                birthInput.setText(birthDate);
                Long computedAge = computeAgeFromBirthDate(birthDate);
                ageInput.setText(computedAge == null
                        ? (profile.getAge() == null ? "" : String.valueOf(profile.getAge()))
                        : String.valueOf(computedAge));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                usernameText.setText(R.string.profile_not_available);
                emailText.setText("");
                genderInput.setText("", false);
                ageInput.setText("");
                birthInput.setText("");
            }
        };
        profileRef.addValueEventListener(listener);
    }

    private void saveProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (TextUtils.isEmpty(uid)) {
            Toast.makeText(requireContext(), R.string.profile_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = valueOf(genderInput);
        String ageText = valueOf(ageInput);
        String birthDate = valueOf(birthInput);
        Long ageValue = null;

        if (!TextUtils.isEmpty(birthDate)) {
            Long computedAge = computeAgeFromBirthDate(birthDate);
            if (computedAge == null) {
                birthInput.setError(getString(R.string.profile_birth_error));
                return;
            }
            ageValue = computedAge;
            ageInput.setText(String.valueOf(computedAge));
        } else if (!TextUtils.isEmpty(ageText)) {
            try {
                ageValue = Long.parseLong(ageText);
                if (ageValue <= 0) {
                    ageInput.setError(getString(R.string.profile_age_error));
                    return;
                }
            } catch (NumberFormatException exception) {
                ageInput.setError(getString(R.string.profile_age_error));
                return;
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("gender", TextUtils.isEmpty(gender) ? null : gender);
        updates.put("age", ageValue);
        updates.put("birthDate", TextUtils.isEmpty(birthDate) ? null : birthDate);

        rootRef.child("users").child(uid).updateChildren(updates)
                .addOnSuccessListener(unused -> updateUsernameIndex(updates))
                .addOnFailureListener(error ->
                        Toast.makeText(requireContext(), R.string.profile_save_failed, Toast.LENGTH_SHORT).show());
    }

    private void updateUsernameIndex(Map<String, Object> updates) {
        if (currentProfile == null || TextUtils.isEmpty(currentProfile.getUsername())) {
            Toast.makeText(requireContext(), R.string.profile_save_success, Toast.LENGTH_SHORT).show();
            return;
        }
        String usernameKey = currentProfile.getUsername().toLowerCase(Locale.US);
        rootRef.child("usernames").child(usernameKey).updateChildren(updates)
                .addOnSuccessListener(unused ->
                        Toast.makeText(requireContext(), R.string.profile_save_success, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error ->
                        Toast.makeText(requireContext(), R.string.profile_save_failed, Toast.LENGTH_SHORT).show());
    }

    private void loadAvatarFromPrefs() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (TextUtils.isEmpty(uid)) {
            avatarImage.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }
        String uri = getPrefs().getString(PREF_AVATAR_PREFIX + uid, null);
        if (TextUtils.isEmpty(uri)) {
            avatarImage.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }
        loadAvatar(uri);
    }

    private void saveAvatarUri(String uri) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        getPrefs().edit().putString(PREF_AVATAR_PREFIX + uid, uri).apply();
    }

    private void loadAvatar(String uriString) {
        Glide.with(this)
                .load(Uri.parse(uriString))
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(avatarImage);
    }

    private String valueOf(TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    @Nullable
    private Long computeAgeFromBirthDate(String birthDate) {
        if (TextUtils.isEmpty(birthDate)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(BIRTH_DATE_PATTERN, Locale.getDefault());
        format.setLenient(false);
        Date parsedDate;
        try {
            parsedDate = format.parse(birthDate);
        } catch (ParseException exception) {
            return null;
        }
        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTime(parsedDate);
        Calendar today = Calendar.getInstance();
        if (birthCalendar.after(today)) {
            return null;
        }
        int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age <= 0 ? null : (long) age;
    }

    private android.content.SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileRef != null && listener != null) {
            profileRef.removeEventListener(listener);
        }
    }
}
