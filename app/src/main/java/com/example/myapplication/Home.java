package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private List<Tweet> tweets = new ArrayList<>();

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

        if (firebaseUser == null ){
            Toast.makeText(Home.this, "Something went wrong! User details are not available at the moment !  ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Home.this, LoginActivity.class);
            startActivity(intent);
        }

        assert firebaseUser != null;
        DatabaseReference tweetsHolder = FirebaseDatabase.getInstance().getReference("Tweets");
        tweetsHolder.get().addOnCompleteListener(task -> {
            List<Tweet> valuesList = new ArrayList<>();
            if (task.isSuccessful()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    // Access child data
                    String key = snapshot.getKey(); // Key of the child
                    Tweet value = snapshot.getValue(Tweet.class); // Value of the child

                    valuesList.add(value);
                }
                tweets = valuesList;
                adapter.updateTweets(tweets);
                tweetList.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(Home.this, "Failed to get tweets" + task.getException().getMessage() ,Toast.LENGTH_LONG).show();
            }
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