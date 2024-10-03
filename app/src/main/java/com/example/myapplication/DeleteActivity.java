package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private ProgressBar progressBar;
    private TextView textViewAuth;
    private String userPwd;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG = "DeleteProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete);

        getSupportActionBar().setTitle("Delete your Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        editTextUserPwd = findViewById(R.id.editText_delete_user_pwd);
        textViewAuth = findViewById(R.id.textView_delete_user_authenticated);
        buttonDeleteUser = findViewById(R.id.button_delete_user);
        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);

        buttonDeleteUser.setEnabled(false);
        auth = FirebaseAuth.getInstance();

        firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(DeleteActivity.this, "Something went wrong! User details are not available at the moment.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(v -> {
            userPwd = editTextUserPwd.getText().toString();
            if (TextUtils.isEmpty(userPwd)) {
                Toast.makeText(DeleteActivity.this, "Password is needed to continue", Toast.LENGTH_SHORT).show();
                editTextUserPwd.setError("Please enter your password for authentication");
                editTextUserPwd.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);
                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DeleteActivity.this, "Password has been verified. You can now delete your profile.", Toast.LENGTH_SHORT).show();

                        textViewAuth.setText("You are authenticated. You can delete your profile now.");
                        editTextUserPwd.setEnabled(false);
                        buttonReAuthenticate.setEnabled(false);
                        buttonDeleteUser.setEnabled(true);

                        buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteActivity.this, R.color.blue6));
                        buttonDeleteUser.setOnClickListener(v1 -> showAlertDialog());

                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(DeleteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteActivity.this);
        builder.setTitle("Delete User and Related Data!");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible.");

        builder.setPositiveButton("Continue", (dialog, which) -> deleteUser(firebaseUser));
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Intent intent = new Intent(DeleteActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red)));
        alertDialog.show();
    }

    private void deleteUser(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteUserData();
                auth.signOut();
                Toast.makeText(DeleteActivity.this, "User has been deleted!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeleteActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                try {
                    throw task.getException();
                } catch (Exception e) {
                    Toast.makeText(DeleteActivity.this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void deleteUserData() {
        if (firebaseUser.getPhotoUrl() != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(unused -> Log.d(TAG, "OnSuccess: Photo Deleted"))
                    .addOnFailureListener(e -> {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(DeleteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(unused -> Log.d(TAG, "OnSuccess: User Data Deleted"))
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(DeleteActivity.this);
        } else if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else if (id == R.id.my_profile) {
            Intent intent = new Intent(DeleteActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(DeleteActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(DeleteActivity.this, DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(DeleteActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(DeleteActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
