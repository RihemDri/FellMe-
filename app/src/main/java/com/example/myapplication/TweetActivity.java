package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.model.Tweet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TweetActivity extends AppCompatActivity {

    // UI Elements
    AppCompatButton cancel;
    AppCompatButton post;
    AppCompatEditText multilineText;
    TextView initials;
    ImageView profilePic;
    ImageView openGallery;
    ImageView openCamera;
    CardView profilePicHolder;
    CardView initialsHolder;


    // Firebase
    private FirebaseAuth auth;

    private Uri photoUri; // URI of the captured photo

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        cancel = findViewById(R.id.cancel);
        post = findViewById(R.id.post);
        multilineText = findViewById(R.id.multilineText);
        initials = findViewById(R.id.initials);
        profilePic = findViewById(R.id.profilePic);
        openGallery = findViewById(R.id.openGallery);
        openCamera = findViewById(R.id.openCamera);
        profilePicHolder = findViewById(R.id.profilePicHolder);
        initialsHolder = findViewById(R.id.initialsHolder);


        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null ){
            Toast.makeText(TweetActivity.this, "Something went wrong! User details are not available at the moment !  ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(TweetActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        assert firebaseUser != null;
        initials.setText(getFirstLetterUpperCased(firebaseUser.getDisplayName()));

        //set User Dp (After User Has Uploaded )
        Uri uri = firebaseUser.getPhotoUrl();

        //ImageView setimage Uri should not be used with regular URIs so we are using picasso
        Picasso
                .with(TweetActivity.this)
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
                        Toast.makeText(TweetActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        profilePicHolder.setVisibility(View.GONE);
                        initialsHolder.setVisibility(View.VISIBLE);
                    }
                });

        cancel.setOnClickListener(v -> showAlertDialog());

        openGallery.setOnClickListener( v -> {
            // Open gallery to select an image
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        openCamera.setOnClickListener(v -> {
            requestCameraPermission();
        });

        post.setOnClickListener(v -> {
            String text = multilineText.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                DatabaseReference

                        tweetHolder = FirebaseDatabase.getInstance().getReference("Tweets");
                String tweetId = tweetHolder.push().getKey();
                Tweet tweet = Tweet.createTweet(
                        text,
                        tweetId,
                        firebaseUser.getUid()
                );
                tweetHolder.child(tweetId).setValue(tweet).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TweetActivity.this, "Post successful", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(TweetActivity.this, "Failed to post" + task.getException().getMessage() ,Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // The EditText is empty
                Toast.makeText(TweetActivity.this, "Post must not be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static final int PICK_IMAGE_REQUEST = 1; // Request code for picking image
    private static final int REQUEST_IMAGE_CAPTURE = 2; // Request code for capturing image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                // Handle the image URI as needed
                handleImageUri(imageUri);
            }

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                handleImageUri(photoUri);
            }
        }
    }

    private void handleImageUri(Uri imageUri) {
        // Process the image URI (e.g., display it in an ImageView, upload it, etc.)
        Toast.makeText(TweetActivity.this, "Image Acquired " + imageUri, Toast.LENGTH_SHORT).show();
    }

    // TODO: finish this once we create tweet class.
    /*private void UploadPic(){
        if (uriImage!= null)
        {
            ///save the image with uid of the currently logged user
            StorageReference fileReference = storageReference.child(auth.getCurrentUser().getUid() + "."
                    + getFileExtension(uriImage));
            //upload image to storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri =uri;
                            firebaseUser =auth.getCurrentUser();

                            //Finally set the display image after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);

                        }
                    });

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this, "Upload successful !",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadProfilePicActivity.this,UserProfileActivity.class);
                    startActivity(intent);



                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this, "No Profile picture was Selected !",Toast.LENGTH_SHORT).show();

        }
    }*/

    private static String getFirstLetterUpperCased(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Post");
        builder.setMessage("Are you sure you want to cancel?");

        builder.setPositiveButton("Cancel", (dialog, which) -> finish());

        builder.setNegativeButton("Keep Editing", (dialog, which) -> {
            // Handle the "Cancel" button click
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(TweetActivity.this, "Camera Permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
        }

        // Continue only if the File was successfully created
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this,
                    "com.example.myapplication.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
