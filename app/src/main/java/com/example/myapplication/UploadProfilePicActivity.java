package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadProfilePicActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_profile_pic);
        getSupportActionBar().setTitle("Upload Photo");


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button buttonUploadPicChoose =findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        imageViewUploadPic =findViewById(R.id.imageView_profile_dp);
        progressBar =findViewById(R.id.progressBar);


        auth =FirebaseAuth.getInstance();
        firebaseUser =auth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        //use to identifie ressource vidio sound ..
        //uniform resource identifier URI  URL(locator) is subset of uri

        Uri uri  = firebaseUser.getPhotoUrl();

        //set Users current dp //picasso image laibarie fro android process the images from externel location
        //
        Picasso.with(UploadProfilePicActivity.this).load(uri).into(imageViewUploadPic); //.get new vers //
        //Picasso.get().load(uri).into(imageViewUploadPic);

        //choosing image to upload
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        //upload image
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPic();
            }
        });

    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        //PICK_IMAGE_REQUEST global variable


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);


        }

    }

    private void UploadPic(){
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
    }
    ///get file extension of the image
    private String getFileExtension(Uri uri ){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime =MimeTypeMap.getSingleton();// MIME est une façon standard de décrire le format d'un fichier.
        return mime.getExtensionFromMimeType(cR.getType(uri));
        //convert ce type MIME en extension de fichier (comme "jpg" pour une image JPEG..).
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
            NavUtils.navigateUpFromSameTask(UploadProfilePicActivity.this);
        }else if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);

        }
        else if (id == R.id.my_profile) {
            Intent intent = new Intent(UploadProfilePicActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();


        }else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UploadProfilePicActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();


        }else if (id ==R.id.menu_change_password) {
            Intent intent = new Intent(UploadProfilePicActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UploadProfilePicActivity.this,DeleteActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(UploadProfilePicActivity.this,"Logged Out",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadProfilePicActivity.this,MainActivity.class);

            //clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();//close user profile Activity

        }else {
            Toast.makeText(UploadProfilePicActivity.this,"Something went wrong ",Toast.LENGTH_LONG).show();


        }

        return super.onOptionsItemSelected(item);
    }
}
////////////////////////////////////////////////////////////////////////
