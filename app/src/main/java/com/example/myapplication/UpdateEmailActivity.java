package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.example.myapplication.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateEmailActivity extends AppCompatActivity {
    private static final String TAG = "UpdateEmailActivity";

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuth;
    private String userOldEmail , userNewEmail, userPwd ;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail ,editTextPwd;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Update Email");
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
                            intent = new Intent(UpdateEmailActivity.this, AdminActivity.class);
                        } else {
                            intent = new Intent(UpdateEmailActivity.this, Home.class);
                        }
                        startActivity(intent);
                        finish();
                    }else{
                        NavUtils.navigateUpFromSameTask(UpdateEmailActivity.this);
                    }
                });
            }
        });


        progressBar = findViewById(R.id.progressBar);
        editTextPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuth = findViewById(R.id.textView_update_email_authenticated);
        buttonUpdateEmail =findViewById(R.id.button_update_email);

        buttonUpdateEmail.setEnabled(false);//make button disabled in the beginnig until the user is auth
        editTextNewEmail.setEnabled(false);


        auth = FirebaseAuth.getInstance();
        firebaseUser =auth.getCurrentUser();

        //set old email Id on TextView
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textView_update_email_old);
        textViewOldEmail.setText(userOldEmail);
        if (firebaseUser.equals("")){
            Toast.makeText(this, "Somthing went wrong! User's details not available ", Toast.LENGTH_SHORT).show();
        }else {
            reAuthenticate(firebaseUser);
            
        }
    }

    //ReAuth/Verify User before updating Email

    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.button_authenticate_user);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtain password for auth 
                userPwd = editTextPwd.getText().toString();
                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue ", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Please enter your password for authentication");

                }else{
                    //after enter the password start the progressbar
                    progressBar.setVisibility(View.VISIBLE);

                    //////////////
                    Toast.makeText(UpdateEmailActivity.this, "enter  your new email !!", Toast.LENGTH_SHORT).show();


                    //authenticate the user
                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                //the user is auth so the progress bar gone
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UpdateEmailActivity.this, "Password has been verified " + "You can update email now ", Toast.LENGTH_SHORT).show();

                                //Set Textview to show that user is authentificated
                                textViewAuth.setText("you are authentificated. you can update email now ");

                                //Disable EditText for password button to verifu user and enable edit Text for new email and update email
                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                //change color of update email button
                                buttonUpdateEmail.setBackgroundTintList((ContextCompat.getColorStateList(UpdateEmailActivity.this,
                                        R.color.blue6)));
                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        if(TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New Email is required", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter new Email");
                                            editTextNewEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter valid Email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please provide valid Email");
                                            editTextNewEmail.requestFocus();

                                        }else if (userOldEmail.matches(userNewEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "New email cannot be same as olf email ", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter New Email");
                                            editTextNewEmail.requestFocus();
                                        }else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);

                                        }
                                    }
                                });
                            }else{
                                try {
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(UpdateEmailActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }

            }
        });
    }



    private void updateEmail(FirebaseUser firebaseUser) {



        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //if (task.isComplete()){
                if (task.isSuccessful()){

                    //Verify Email
                   // firebaseUser.sendEmailVerification();
                    if (task.isSuccessful()) {
                        firebaseUser.sendEmailVerification();
                        // Additional success handling
                    }



                    Toast.makeText(UpdateEmailActivity.this, "Email has been Updated , Verify your new email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    try {
                        //throw task.getException();
                        //
                        if (task.getException() instanceof FirebaseAuthException) {
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        ///////////////////

                    }catch (Exception e){
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
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
                        intent = new Intent(UpdateEmailActivity.this, AdminActivity.class);
                    } else {
                        intent = new Intent(UpdateEmailActivity.this, Home.class);
                    }
                    startActivity(intent);
                    finish();
                }else{
                    NavUtils.navigateUpFromSameTask(UpdateEmailActivity.this);
                }
            });
            return true;
        }else if  (id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);

        } else if (id == R.id.my_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();


        }
        else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UpdateEmailActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();

        }else if (id ==R.id.menu_change_password) {
            Intent intent = new Intent(UpdateEmailActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this,DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(UpdateEmailActivity.this,"Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateEmailActivity.this,MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        }else {
            Toast.makeText(UpdateEmailActivity.this,"Something went wrong ",Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }
}
////////////////////////////////////////////////////////////////////////
