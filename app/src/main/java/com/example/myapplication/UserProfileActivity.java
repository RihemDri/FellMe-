package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {
    private TextView textViewWelcome, textViewFirstName,textViewLastName, textViewEmail,textViewDoB,textViewGender , textViewActivity;
    private ProgressBar progressBar;
    private String name,fullname, email,dob ,gender , activity ;
    private ImageView imageView;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeContainer;
    //TextView dateTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        //getSupportActionBar().setTitle("Home");
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(UserProfileActivity.this, Home.class);
                startActivity(intent);
                finish();
            }
        });

        textViewWelcome =findViewById(R.id.textView_show_welcome);
        textViewFirstName = findViewById(R.id.textView_show_full_name);
        textViewEmail =findViewById(R.id.textView_show_email);
        textViewDoB =findViewById(R.id.textView_show_dob);
        textViewGender= findViewById(R.id.textView_show_gender);
        textViewActivity=findViewById(R.id.textView_show_activity);
        progressBar = findViewById(R.id.progressBar);
        //textViewLastName = findViewById(R.id.textView_show_name1);

        //set Onclick on Image view to open uplodprofile
        imageView = findViewById(R.id.imageView_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this,UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });






        auth =FirebaseAuth.getInstance();
        FirebaseUser firebaseUser =auth.getCurrentUser();

        if (firebaseUser == null ){
            Toast.makeText(UserProfileActivity.this, "Something went wrong! User details are not available at the moment !  ", Toast.LENGTH_LONG).show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }


    }

    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);

        //set up the refresh listner that trigger new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // code tp refresh
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);//any animation
                swipeContainer.setRefreshing(false);
            }
        });
        //swip colors when refresh
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        //since the fire base user not global we pass the firebase user in parammm ( )
        String userID = firebaseUser.getUid();
        //Extracting user ref from db from signup
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //called each time when the data changed canceld stop listner
                ReadwriteUserDetails readUserDetails =snapshot.getValue(ReadwriteUserDetails.class);
                if (readUserDetails!=null){
                    name = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    fullname = readUserDetails.fullName;
                    dob =readUserDetails.dob;
                    gender = readUserDetails.gender;
                    activity =readUserDetails.activity;

                    textViewWelcome.setText("welcome "+ fullname + "!");
                    textViewFirstName.setText(name);
                    // textViewLastName.setText(fullname);
                    textViewEmail.setText(email);
                    textViewDoB.setText(dob);
                    textViewGender.setText(gender);
                    textViewActivity.setText(activity);



                    //set User Dp (After User Has Uploaded )
                    Uri uri = firebaseUser.getPhotoUrl();

                    //ImageView setimage Uri should not be used with regular URIs so we are using picasso
                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);
                } else {
                    Toast.makeText(UserProfileActivity.this, "Somthing went Wrong!!", Toast.LENGTH_SHORT).show();

                }
                progressBar.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something went wrong!  ", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

            }

        });
    }
    /////////////////////////////////////////////////////////////////////////////   ////////////////////////////////////////////////////////////////
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
        int id = item.getItemId();
        if (id == android.R.id.home){
            FirebaseUser firebaseUser =auth.getCurrentUser();
            //Extract user reference from Databease for users
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");

            String userID = firebaseUser.getUid();


            progressBar.setVisibility(View.VISIBLE);
            referenceProfile.child(userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    User value = task.getResult().getValue(User.class); // Value of the child
                    Log.i("test","Clicker " + value);
                    Intent intent;
                    if(value.getRole().equals("Admin")) {
                        intent = new Intent(UserProfileActivity.this, AdminActivity.class);
                    } else {
                        intent = new Intent(UserProfileActivity.this, Home.class);
                    }
                    startActivity(intent);
                    finish();
                }else{
                    NavUtils.navigateUpFromSameTask(UserProfileActivity.this);
                }
            });
            return true;
        }else if (id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);

        }else if (id == R.id.my_profile) {
            Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();


        }else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UserProfileActivity.this,UpdateProfileActivity.class);
            startActivity(intent);

        } else if (id ==R.id.menu_change_password) {
            Intent intent = new Intent(UserProfileActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UserProfileActivity.this,DeleteActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(UserProfileActivity.this,"Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserProfileActivity.this,MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        }else {
            Toast.makeText(UserProfileActivity.this,"Something went wrong ",Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }


}
//////////////////////////////////////////////////////////////////////////////





