package khiem.it.tinyenglish.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import khiem.it.tinyenglish.MainActivity;
import khiem.it.tinyenglish.R;
import khiem.it.tinyenglish.model.UserProfile;

public class ProfileFragment extends Fragment {

    private TextView usernameText;
    private TextView emailText;
    private DatabaseReference profileRef;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        usernameText = view.findViewById(R.id.profileUsernameText);
        emailText = view.findViewById(R.id.profileEmailText);
        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if (requireActivity() instanceof MainActivity mainActivity) {
                mainActivity.refreshUi();
            }
        });

        bindProfile();
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
                    usernameText.setText(R.string.profile_not_available);
                    emailText.setText("");
                    return;
                }
                usernameText.setText(getString(R.string.profile_username_format, profile.getUsername()));
                emailText.setText(getString(R.string.profile_email_format, profile.getEmail()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                usernameText.setText(R.string.profile_not_available);
                emailText.setText("");
            }
        };
        profileRef.addValueEventListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileRef != null && listener != null) {
            profileRef.removeEventListener(listener);
        }
    }
}
