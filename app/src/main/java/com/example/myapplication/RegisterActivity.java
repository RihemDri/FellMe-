package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextRegisterFirstName, editTextRegisterLastName, editTextRegisterEmail, editTextRegisterDoB, editTextRegisterActivity, editTextRegisterPwd, editTextValidPwd
            ,editTextLoginPwd;
    private ProgressBar progressBar;
    TextView loginRedirectText;
    DatePickerDialog picker;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;

    private static final String TAG = "RegisterActivity";
    private Spinner spinnerDepartement;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        editTextRegisterFirstName = findViewById(R.id.editText_register_first_name);
        editTextRegisterLastName = findViewById(R.id.editText_register_last_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterPwd = findViewById(R.id.editText_register_password);
        //  editTextRegisterActivity = findViewById(R.id.editText_register_activity);



        editTextValidPwd = findViewById(R.id.signup_confirm);

        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        progressBar = findViewById(R.id.progressBar);

///////////Spinner depatrement////////
        spinnerDepartement = findViewById(R.id.spinner_departement);
        String[] departements = new String[]{"HR", "IT", "Finance", "Marketing", "Sales"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, departements);
        spinnerDepartement.setAdapter(adapter);




        Button buttonRegister = findViewById(R.id.button_register);

        loginRedirectText = findViewById(R.id.loginRedirectText);
        //setting up date picker on edittext
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            //Calendar abstract class
            public void onClick(View v) {
                final Calendar calendar =Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker =new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year );

                    }
                } ,year, month, day);
                picker.show();
            }
        });

        //Button buttonRegister = findViewById(R.id.button_register);

        //Show Hide password  using Eye Icon
        ImageView imageViewShowHidePwd =  findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextRegisterPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then Hide it
                    editTextRegisterPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextRegisterPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                String textFirstName = editTextRegisterFirstName.getText().toString();
                String textLastName = editTextRegisterLastName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textActivity = spinnerDepartement.getSelectedItem().toString(); //get selected departement from spinner
                //String textActivity = editTextRegisterActivity.getText().toString();
                String textPwd = editTextRegisterPwd.getText().toString();
                String textValidPwd = editTextValidPwd.getText().toString();
                String textGender;

                if (TextUtils.isEmpty(textFirstName)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your first name", Toast.LENGTH_LONG).show();
                    editTextRegisterFirstName.setError("Full name is required");
                    editTextRegisterFirstName.requestFocus();
                } else if (TextUtils.isEmpty(textFirstName)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your lastname", Toast.LENGTH_LONG).show();
                    editTextRegisterLastName.setError("Full name is required");
                    editTextRegisterLastName.requestFocus();
                }else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("Valid email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
                    editTextRegisterDoB.setError("Date of birth is required");
                    editTextRegisterDoB.requestFocus();
                } else if (selectedGenderId == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required");
                    radioButtonRegisterGenderSelected.requestFocus();

                } else if (TextUtils.isEmpty(textActivity)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your activity", Toast.LENGTH_LONG).show();
                    editTextRegisterActivity.setError("Activity is required");
                    editTextRegisterActivity.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password is required");
                    editTextRegisterPwd.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password is too short");
                    editTextRegisterPwd.requestFocus();


                } else if (TextUtils.isEmpty(textValidPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please confirm your Password ", Toast.LENGTH_SHORT).show();
                    editTextValidPwd.setError("password confirmation is required ");
                    editTextValidPwd.requestFocus();

                } else if (!textPwd.equals(textValidPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please enter the  same password ", Toast.LENGTH_SHORT).show();
                    editTextValidPwd.setError("password confirmation is required ");
                    editTextValidPwd.requestFocus();
                    //clear the entered password taa kbal
                    editTextValidPwd.clearComposingText();
                    editTextRegisterPwd.clearComposingText();
                } else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textFirstName, textLastName, textEmail, textDoB, textGender, textActivity, textPwd);
                }
            }
        });
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void registerUser(String textFirstName, String textLastName, String textEmail, String textDoB, String textGender, String textActivity, String textPwd) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            //update display name of user
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textLastName).build();
                            firebaseUser.updateProfile(profileChangeRequest);



                            //Enter user data into firebase realtime database
                            ReadwriteUserDetails writeUserDetails = new ReadwriteUserDetails( textFirstName,textActivity ,textGender, textDoB , "User");
                            //
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");
                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        //send Verification Email
                                        firebaseUser.sendEmailVerification();

                                        Toast.makeText(RegisterActivity.this, "user registered succesfuly, please verify your email", Toast.LENGTH_LONG).show();


                                        //open the profile after successuful registration
                                        Intent intent = new Intent(RegisterActivity.this,Home.class );

                                        //to prevent user from returning back to register Activity on pressing back button after registration
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                |Intent.FLAG_ACTIVITY_NEW_TASK);

                                        startActivity(intent);
                                        finish();//to close the register Activity

                                    } else {
                                        Toast.makeText(RegisterActivity.this, "signup failed" + task.getException().getMessage() ,Toast.LENGTH_LONG).show();
                                        //+ task.getException().getMessage(),

                                    }
                                    //hide progress bar
                                    progressBar.setVisibility(View.GONE);
                                }


                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                editTextRegisterPwd.setError("Password is too weak");
                                editTextRegisterPwd.requestFocus();

                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextRegisterEmail.setError("Invalid email format");
                                editTextRegisterEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
