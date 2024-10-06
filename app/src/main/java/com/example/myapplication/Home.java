package com.example.myapplication;

import static com.example.myapplication.adapter.TweetAdapter.getFirstLetterUpperCased;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.TweetAdapter;
import com.example.myapplication.model.Tweet;
import com.example.myapplication.model.TweetHolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

// TODO: handle tweet image.
public class Home extends AppCompatActivity {

    // UI Elements
    FloatingActionButton addTweet;
    RecyclerView tweetList;
    ProgressBar progress;

    // Firebase
    private FirebaseAuth auth;

    //Data
    private TweetAdapter adapter;
    private List<TweetHolder> tweets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Home");
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addTweet = findViewById(R.id.addTweet);
        progress = findViewById(R.id.progress);
        tweetList = findViewById(R.id.tweetList);

        addTweet.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, TweetActivity.class);
            startActivity(intent);
        });

        getTweetsList();

        tweetList.setLayoutManager(new LinearLayoutManager(this));

        // Set up the adapter
        adapter = new TweetAdapter(this, tweets);
        tweetList.setAdapter(adapter);
    }

    private void getTweetsList() {
        tweetList.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(Home.this, "Something went wrong! User details are not available at the moment!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Home.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        DatabaseReference tweetsHolder = FirebaseDatabase.getInstance().getReference("Tweets");
        tweetsHolder.get().addOnSuccessListener(tweetsSnapshot -> {
            List<Tweet> valuesList = new ArrayList<>();
            for (DataSnapshot snapshot : tweetsSnapshot.getChildren()) {
                Tweet tweet = snapshot.getValue(Tweet.class);
                valuesList.add(tweet);
            }

            // Now handle getting user info and images for each tweet asynchronously
            List<Task<TweetHolder>> tasks = new ArrayList<>();
            for (Tweet tweet : valuesList) {
                Task<TweetHolder> task = getUserAndImageInfo(tweet, firebaseUser);
                tasks.add(task);
            }

            // Wait for all tasks to complete
            Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
                List<TweetHolder> tweetHolderList = new ArrayList<>();
                for (Object object : objects) {
                    tweetHolderList.add((TweetHolder) object);
                }

                Log.i("Home", "Tweets: " + tweetHolderList);

                tweets = tweetHolderList;
                adapter.updateTweetsAndRefresh(tweets);
                tweetList.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
            }).addOnFailureListener(e -> {
                // Handle error
                Log.e("Home", "Error fetching tweets or user info", e);
                progress.setVisibility(View.INVISIBLE);
            });
        }).addOnFailureListener(e -> {
            // Handle error retrieving tweets
            Log.e("Home", "Error fetching tweets", e);
            progress.setVisibility(View.INVISIBLE);
        });
    }

    private Task<TweetHolder> getUserAndImageInfo(Tweet tweet, FirebaseUser firebaseUser) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(tweet.getUserId());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageReference = storage.getReference().child("DisplayPics/" + tweet.getUserId() + ".jpg");

        // Get user info
        return userRef.get().continueWithTask(userSnapshotTask -> {
            DataSnapshot tweetUserSnapshot = userSnapshotTask.getResult();
            String userName;
            String initials;

            if (tweetUserSnapshot.exists() && tweetUserSnapshot.hasChild("fullName")) {
                userName = tweetUserSnapshot.child("fullName").getValue(String.class);
                initials = (userName != null && !userName.isEmpty()) ? getFirstLetterUpperCased(userName) : "No Name Found";
            } else {
                initials = "Unknown User";
                userName = "Unknown User";
            }

            // Get image URL
            return imageReference.getDownloadUrl().continueWith(imageUrlTask -> {
                String imageUrl = imageUrlTask.isSuccessful() ? imageUrlTask.getResult().toString() : "";
                // Create and return TweetHolder object
                return new TweetHolder(
                        tweet.getTweet(),
                        tweet.getDate(),
                        tweet.getTime(),
                        tweet.getId(),
                        tweet.getLikes(),
                        tweet.getUserId(),
                        imageUrl,
                        userName,
                        initials,
                        tweet.getLikedBy(),
                        tweet.getComments()
                );
            });
        });
    }




    //////////////   ///////////////////////////////////////////////////////////////////////////////////
    //creating ActionBar Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflater menu Items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //when any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(Home.this);
        }else if  (id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);

        } else if (id == R.id.my_profile) {
            Intent intent = new Intent(Home.this, UserProfileActivity.class);
            startActivity(intent);
            finish();


        }else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(Home.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();

        }else if (id ==R.id.menu_change_password) {
            Intent intent = new Intent(Home.this,ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(Home.this,DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(Home.this,"Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Home.this,MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        }else {
            Toast.makeText(Home.this,"Something went wrong ",Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }

////////////////////////////////////////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();
        getTweetsList();
    }
}