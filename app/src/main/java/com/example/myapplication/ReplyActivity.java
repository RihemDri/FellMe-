package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;

import com.example.myapplication.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ReplyActivity extends AppCompatActivity {

    // UI Elements
    AppCompatButton cancel;
    AppCompatButton post;
    AppCompatEditText multilineText;
    TextView initials;
    TextView tweet;
    ImageView profilePic;
    CardView profilePicHolder;
    CardView initialsHolder;

    // Firebase
    private FirebaseAuth auth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        cancel = findViewById(R.id.cancel);
        post = findViewById(R.id.post);
        multilineText = findViewById(R.id.multilineText);
        initials = findViewById(R.id.initials);
        tweet = findViewById(R.id.tweet);
        profilePic = findViewById(R.id.profilePic);
        profilePicHolder = findViewById(R.id.profilePicHolder);
        initialsHolder = findViewById(R.id.initialsHolder);

        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null ){
            Toast.makeText(ReplyActivity.this, "Something went wrong! User details are not available at the moment !  ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ReplyActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        assert firebaseUser != null;
        initials.setText(getFirstLetterUpperCased(firebaseUser.getDisplayName()));

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            String tweetText = extras.getString("tweet"); // Retrieve the tweet ID
            tweet.setText(tweetText);
        } else {
            tweet.setText("unavailable");
        }

        //set User Dp (After User Has Uploaded )
        Uri uri = firebaseUser.getPhotoUrl();

        //ImageView setimage Uri should not be used with regular URIs so we are using picasso
        Picasso
                .with(ReplyActivity.this)
                .load(uri)
                .into(profilePic, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Image loaded successfully
                        profilePicHolder.setVisibility(View.VISIBLE);
                        initialsHolder.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        // Handle the error
                        Toast.makeText(ReplyActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        profilePicHolder.setVisibility(View.GONE);
                        initialsHolder.setVisibility(View.VISIBLE);
                    }
                });

        cancel.setOnClickListener(v -> showAlertDialog());

        post.setOnClickListener(v -> {
            String text = multilineText.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                String tweetId;
                if (getIntent() != null && getIntent().getExtras() != null) {
                    Bundle extras = getIntent().getExtras();
                    tweetId = extras.getString("tweet_id"); // Retrieve the tweet ID
                } else {
                    tweetId = "";
                }

                Comment newComment = new Comment(firebaseUser.getUid(), firebaseUser.getDisplayName(), text);
                List<Comment> valuesList = new ArrayList<>();
                DatabaseReference CommentsHolder = FirebaseDatabase
                        .getInstance()
                        .getReference("Tweets")
                        .child(tweetId)
                        .child("comments");
                CommentsHolder.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            // Access child data
                            String key = snapshot.getKey(); // Key of the child
                            Comment value = snapshot.getValue(Comment.class); // Value of the child

                            valuesList.add(value);
                        }




                        valuesList.add(newComment);

                        FirebaseDatabase
                                .getInstance()
                                .getReference("Tweets")
                                .child(tweetId)
                                .child("comments")
                                .setValue(valuesList)
                                .addOnCompleteListener(task2 -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ReplyActivity.this, "Comment Added Successfully", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ReplyActivity.this, "Failed to comment" + task2.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ReplyActivity.this, "Failed to get tweets" + task.getException().getMessage() ,Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private static String getFirstLetterUpperCased(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Reply");
        builder.setMessage("Are you sure you want to cancel?");

        builder.setPositiveButton("Cancel", (dialog, which) -> finish());

        builder.setNegativeButton("Keep Editing", (dialog, which) -> {
            // Handle the "Cancel" button click
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
