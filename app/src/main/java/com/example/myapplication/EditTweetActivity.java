package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.adapter.TweetAdapter;
import com.example.myapplication.model.Tweet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class EditTweetActivity extends AppCompatActivity {
    private EditText editTweetText;
    private Button saveButton;
    private String tweetId;
    private DatabaseReference tweetRef;
    private TweetAdapter tweetAdapter;
    private List<Tweet> tweetList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_tweet);

        // Find views
        editTweetText = findViewById(R.id.editTweetText);
        saveButton = findViewById(R.id.save_button);

        tweetId = getIntent().getStringExtra("tweet_id");
        String tweetContent = getIntent().getStringExtra("tweet");

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialize firebase reference
        tweetRef = FirebaseDatabase.getInstance().getReference("Tweets").child(tweetId);

        // set current tweet content to EditText
        editTweetText.setText(tweetContent);

        saveButton.setOnClickListener(v -> {
            String updatedTweet = editTweetText.getText().toString().trim();
            if (TextUtils.isEmpty(updatedTweet)) {
                Toast.makeText(EditTweetActivity.this, "Tweet cannot be empty", Toast.LENGTH_SHORT).show();
                return;

            }
            updateTweet(updatedTweet);
        });

    }

    private void updateTweet(String updatedTweet) {
        tweetRef.child("tweet").setValue(updatedTweet).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditTweetActivity.this, "Tweet updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditTweetActivity.this, "Failed to update tweet", Toast.LENGTH_SHORT).show();
            }
        });
        DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
        tweetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Tweet> updatedTweets = new ArrayList<>();
                for (DataSnapshot tweetSnapshot : snapshot.getChildren()) {
                    Tweet tweet = tweetSnapshot.getValue(Tweet.class);
                    updatedTweets.add(tweet);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
}