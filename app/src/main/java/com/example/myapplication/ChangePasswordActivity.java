package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText editTextPwdCurr , editTextPwdNew , getEditTextPwdConfirmNew;
    private TextView textViewAuth;
    private Button buttonChangePwd , buttonReAuth;
    private ProgressBar progressBar;
    private String userPwdCurr;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        editTextPwdNew = findViewById(R.id.editText_change_pwd_new);
        editTextPwdCurr = findViewById(R.id.editText_change_pwd_current);
        textViewAuth = findViewById(R.id.textView_change_pwd_authenticated);
        buttonReAuth = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_change_pwd);


        buttonChangePwd.setEnabled(false);//
        editTextPwdNew.setEnabled(false);


        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser =auth.getCurrentUser();

        //set old email Id on TextView


        if (firebaseUser.equals("")){
            Toast.makeText(this, "Somthing went wrong! User's details not available ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this,UserProfileActivity.class);
            startActivity(intent);
            finish();
         }else {
            reAuthenticate(firebaseUser);

         }


    }

          private void reAuthenticate(FirebaseUser firebaseUser) {
            buttonReAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurr = editTextPwdCurr.getText().toString();
                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this, "Password is needed to continue ", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Please enter your password for authentication");
                    editTextPwdCurr.requestFocus();

                }else{
                    //after enter the password start the progressbar
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);


                                buttonReAuth.setEnabled(false);
                                buttonChangePwd.setEnabled(true);//



                                textViewAuth.setText("you are authentificated/verified" +
                                " You can Change password now ");
                                Toast.makeText(ChangePasswordActivity.this, "Password has been verified " + "  Change password now ", Toast.LENGTH_SHORT).show();
                                //change color of update email button
                                buttonChangePwd.setBackgroundTintList((ContextCompat.getColorStateList(ChangePasswordActivity.this,
                                        R.color.blue6)));
                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd (firebaseUser);
                                        
                                    }
                                });










                            }else{
                                try {
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(ChangePasswordActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);



                        }
                    });

                }


            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew= editTextPwdNew.getText().toString();
        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "new password is needed", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter you password ");
            editTextPwdNew.requestFocus();
        }else if (userPwdCurr.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "new password cannot be the same as old password ", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new password ");
            editTextPwdNew.requestFocus();

        }else {
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password  has been Changed" ,Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        finish();


                    }else {
                        try {
                            throw task.getException();

                            ///////////////////

                        }catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                    progressBar.setVisibility(View.GONE);

                }

            });
        }
    }
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
        int id =item.getItemId();
        if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(ChangePasswordActivity.this);
        }else if  (id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);

        }else if (id == R.id.my_profile) {
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(ChangePasswordActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id ==R.id.menu_update_email) {
            Intent intent = new Intent(ChangePasswordActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }else if (id ==R.id.menu_change_password) {
            Intent intent = new Intent(ChangePasswordActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(ChangePasswordActivity.this,DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(ChangePasswordActivity.this,"Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePasswordActivity.this,MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        }else {
            Toast.makeText(ChangePasswordActivity.this,"Something went wrong ",Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }
}