package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail, editTextLoginPwd;

    Button loginButton;
    Button buttonLogin;
    TextView signupRedirectText;
    TextView forgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private static final String TAG = "LoginActivity";
    private ConstraintLayout progress;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Toast.makeText(LoginActivity.this,"Already Logged In ! " + currentUser.getDisplayName(),Toast.LENGTH_SHORT).show();
                    String role = snapshot.child("role").getValue(String.class);

                    Log.i("Role", "onDataChange: " + role);
                    if ("Admin".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                        finish();//close login Activity
                    } else  {
                        startActivity(new Intent(LoginActivity.this, Home.class));
                        finish();//close login Activity
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Error getting role", Toast.LENGTH_SHORT).show();
                }
            });


           /* Intent i = new Intent(getApplicationContext(), welcomePage.class);
            startActivity(i);
            finish();*/
          /*  if (currentUser.getEmail().equals("admin@gmail.com")) {
                Toast.makeText(Login.this, "Login successful as admin", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), HomeAdmin.class));
                finish();
            } else {
                Intent i = new Intent(getApplicationContext(), welcomePage.class);
                startActivity(i);
                finish();
            }*/
            FirebaseUser firebaseUser = auth.getCurrentUser();
            String userId = firebaseUser.getUid();
            FirebaseDatabase firebaseDatabase =FirebaseDatabase.getInstance();


        }else {
            Toast.makeText(LoginActivity.this,"you can login now ! !",Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, currentUser.toString(), Toast.LENGTH_SHORT).show();

            progress.setVisibility(View.GONE);
        }

    }
//////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
       /* ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/


        progress = findViewById(R.id.progressHolder);
        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPwd = findViewById(R.id.editText_login_pwd);
        progressBar = findViewById(R.id.progressBar);

        forgotPassword = findViewById(R.id.forgot_password);
        //Button forgotpwd =findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();
        signupRedirectText = findViewById(R.id.textView_register_link);
        progressBar = findViewById(R.id.progressBar);


        //Show Hide password  using Eye Icon
        ImageView imageViewShowHidePwd =  findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextLoginPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //If password is visible then Hide it
                    editTextLoginPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Change Icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextLoginPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        //Login user
        buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextLoginPwd.getText().toString();
                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText( LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Valid email is required");
                    editTextLoginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText( LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    editTextLoginPwd.setError("Password id required");
                    editTextLoginPwd.requestFocus();

                } else {
                    // progressBar.setVisibility((View.VISIBLE));
                    LoginUser(textEmail, textPwd);
                }

            }
        });


        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // System.out.printf("testttttttttttttttttttttttttttttttttttttt\n");

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
                EditText emailBox = dialogView.findViewById(R.id.emailBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userEmail = emailBox.getText().toString();
                        if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                            Toast.makeText(LoginActivity.this, "Enter your registered email id", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Unable to send, failed", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                            }
                        });
                        if (dialog.getWindow() != null){
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }




                    }
                });
                dialog.show();
            }
        });

        //Inside onCreate
       /* gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(this,gOptions);
        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (gAccount != null){
            finish();
            Intent intent = new Intent(Login.this, welcomePage.class);
            startActivity(intent);
         }
         ActivityResultLauncher<Intent> activityResultLauncher =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode()== Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task =GoogleSignIn.getSignedInAccountFromIntent(data);
                            try{
                                task.getResult(ApiException.class);
                                finish();
                                Intent intent =new Intent(Login.this, welcomePage.class);
                                startActivity(intent);
                            }catch (ApiException e){
                                Toast.makeText(Login.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }dialo
                });
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = gClient.getSignInIntent();



                activityResultLauncher.launch(signInIntent);
            }
        });*/
    }

    private void LoginUser(String email, String pwd) {
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //get instance of the current User
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    //check if email is verified before user can access their profile
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Toast.makeText(LoginActivity.this, "You are logged now", Toast.LENGTH_SHORT).show();
                            String role = snapshot.child("role").getValue(String.class);

                            Log.i("Role", "onDataChange: " + role);
                            if ("Admin".equals(role)) {
                                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                finish();//close login Activity
                            } else  {
                                startActivity(new Intent(LoginActivity.this, Home.class));
                                finish();//close login Activity
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(LoginActivity.this, "Error getting role", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("User does not exist or is no longer valid .Please register again  ");
                        editTextLoginEmail.requestFocus();
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("Invalid credentials kindly , check and re-enter  ");
                        editTextLoginEmail.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
                progressBar.setVisibility(View.GONE);

            }
        });




    }
}