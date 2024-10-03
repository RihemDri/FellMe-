package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.myapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateProfileActivity extends AppCompatActivity {
    private EditText editTextUpdateFirstName, editTextUpdateLastName,editTextUpdateDoB, editTextUpdateActivity ;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGender;
    private String textFullName , textDoB , textGender , textActiviy;
    private FirebaseAuth auth ;
    private ProgressBar progressBar;
    TextView loginRedirectupload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
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
                            intent = new Intent(UpdateProfileActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(UpdateProfileActivity.this, Home.class);
                        }
                        startActivity(intent);
                        finish();
                    }else{
                        NavUtils.navigateUpFromSameTask(UpdateProfileActivity.this);
                    }
                });
            }
        });

        progressBar = findViewById(R.id.progressBar);
        editTextUpdateFirstName =findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB =findViewById(R.id.editText_update_profile_dob);
        editTextUpdateActivity =findViewById(R.id.editText_update_profile_activity);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);
        loginRedirectupload = findViewById(R.id.textView_profile_upload_pic);

        auth =FirebaseAuth.getInstance();
        FirebaseUser firebaseUser =auth.getCurrentUser();


        //show Profile Data
        showProfile(firebaseUser);
        //Upload profile data
        Button buttonUploadProfilePic = findViewById(R.id.button_update_profile);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });
        loginRedirectupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });



        //Update Email
        TextView buttonUpdateEmail  = findViewById(R.id.textView_profile_update_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //change DOB
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Extracting date

                String textSADoB[] =textDoB.split("/");//SPLIT THE DATE

                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1])-1;//moth index start from 0
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker =new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year );

                    }
                } ,year, month, day);
                picker.show();
            }
        });
        //Update  profile
        Button buttonUpdateProfile =findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });


    }

    private void updateProfile(FirebaseUser firebaseUser) {
        //save the id of the radio button
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGender =findViewById(selectedGenderID);
        //
        if (TextUtils.isEmpty(textFullName)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your first name", Toast.LENGTH_LONG).show();
            editTextUpdateFirstName.setError("Full name is required");
            editTextUpdateFirstName.requestFocus();
        }    else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            editTextUpdateDoB.setError("Date of birth is required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGender.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGender.setError("Gender is required");
            radioButtonUpdateGender.requestFocus();
        } else if (TextUtils.isEmpty(textActiviy)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your activity", Toast.LENGTH_LONG).show();
            editTextUpdateActivity.setError("Activity is required");
            editTextUpdateActivity.requestFocus();
        }  else {
            //obtain the data entered by user
            textGender = radioButtonUpdateGender.getText().toString();
            textFullName =editTextUpdateFirstName.getText().toString();
            textDoB =editTextUpdateDoB.getText().toString();
            textActiviy = editTextUpdateActivity.getText().toString();




            //Enter User Data into the firebase Realtime Database  set up dependencies
            ReadwriteUserDetails writeUserDetails = new ReadwriteUserDetails(textFullName, textActiviy ,textGender, textDoB ,"User");

            //Extract user reference from Databease for users
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");

            String userID = firebaseUser.getUid();


            progressBar.setVisibility(View.VISIBLE);
            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    //setting new display name
                    UserProfileChangeRequest profileUpdates =new UserProfileChangeRequest.Builder().
                            setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileUpdates);

                    Toast.makeText(UpdateProfileActivity.this, "Update Successful", Toast.LENGTH_SHORT).show();

                    //Stop from returning to updatefile activity on pressing back button and clove activity
                    Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();





                }else{
                    try{
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);

            });

        }
    }
    //fetch data from Firebase and display


    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        //Extraction reference from Database Register
        DatabaseReference refProfile = FirebaseDatabase.getInstance().getReference("Users");
        progressBar.setVisibility(View.VISIBLE);
        refProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //obtain the data and save it in our helper class
                ReadwriteUserDetails readUserDetails = snapshot.getValue(ReadwriteUserDetails.class);
                if (readUserDetails != null){
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.dob;
                    textGender =readUserDetails.gender;
                    textActiviy = readUserDetails.activity;



                    editTextUpdateFirstName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateActivity.setText(textActiviy);

                    //show gender through the radio button
                    if (textGender.equals("Male")){
                        radioButtonUpdateGender = findViewById(R.id.radio_male);

                    }else {
                        radioButtonUpdateGender = findViewById(R.id.radio_female);

                    }
                    radioButtonUpdateGender.setChecked(true);
                }else {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Somthing went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);



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
                        intent = new Intent(UpdateProfileActivity.this, AdminActivity.class);
                    } else {
                        intent = new Intent(UpdateProfileActivity.this, Home.class);
                    }
                    startActivity(intent);
                    finish();
                }else{
                    NavUtils.navigateUpFromSameTask(UpdateProfileActivity.this);
                }
            });
            return true;

        }else if  (id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);

        }else if (id == R.id.my_profile) {
            Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();

        }else if (id ==R.id.menu_change_password) {
            Intent intent = new Intent(UpdateProfileActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateProfileActivity.this,DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(UpdateProfileActivity.this,"Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this,MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        }else  {
            Toast.makeText(UpdateProfileActivity.this,"Something went wrong ",Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }
}
////////////////////////////////////////////////////////////////////////
