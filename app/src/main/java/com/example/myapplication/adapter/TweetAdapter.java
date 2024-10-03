package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.EditTweetActivity;
import com.example.myapplication.R;
import com.example.myapplication.ReplyActivity;
import com.example.myapplication.model.Comment;
import com.example.myapplication.model.Tweet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {
    private List<Tweet> tweetList;
    private Context context;

    public TweetAdapter(Context context, List<Tweet> tweetList) {
        this.context = context;
        this.tweetList = tweetList;
    }

    public void updateTweets(List<Tweet> newTweets) {
        this.tweetList = newTweets;
        // Reverse the order of the tweet lists
        Collections.reverse(this.tweetList);
        notifyDataSetChanged(); // Notify the adapter that data has changed
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Tweet tweet = tweetList.get(position);
        holder.tweetText.setText(tweet.getTweet());
        holder.dateTime.setText(tweet.getDate() + " " + tweet.getTime());
        holder.userNameText.setText(tweet.getId());






        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        String userId = firebaseUser.getUid();

        int likeCount = tweet.getLikes(); //get the number of likes
        String likeText = likeCount + (likeCount == 1? "like" : " likes");
        holder.likeCountText.setText(likeText);
        try {
            if (tweet.getLikedBy().contains(tweet.getId())) {
                holder.likeButton.setImageResource(R.drawable.ic_heart_filled);
            } else {
                holder.likeButton.setImageResource(R.drawable.ic_heart_outline);
            }
        } catch (Exception e) {

        }





        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(tweet.getUserId());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    // Check if fullName field exists
                    if (snapshot.hasChild("fullName")) {
                        // Fetch the user's name
                        String userName = snapshot.child("fullName").getValue(String.class);
                        holder.initials.setText(getFirstLetterUpperCased(userName));
                        if (userName != null && !userName.isEmpty()) {
                            holder.userNameText.setText(userName); // Set the user's name in the TextView
                        } else {
                            holder.userNameText.setText("No Name Found"); // Handle case where userName is null
                        }
                    } else {
                        holder.userNameText.setText("Name Not Available"); // Handle missing fullName field
                    }
                } else {
                    Log.e("TweetAdapter", "User data does not exist for ID:"+ tweet.getUserId());
                    holder.userNameText.setText("Unknown User"); // Default text if user data is missing
                }



                // Reference to Firebase Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();

                // Reference to the specific image
                StorageReference imageReference = storage.getReference().child("DisplayPics/" + tweet.getUserId() + ".jpg");

                // Get the download URL
                imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    //ImageView setimage Uri should not be used with regular URIs so we are using picasso
                    Picasso
                            .with(context)
                            .load(uri)
                            .into(holder.profilePic, new Callback() {
                                @Override
                                public void onSuccess() {
                                    // Image loaded successfully
                                    holder.profilePicHolder.setVisibility(View.VISIBLE);
                                    holder.initialsHolder.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    // Handle the error
                                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                                    holder.profilePicHolder.setVisibility(View.GONE);
                                    holder.initialsHolder.setVisibility(View.VISIBLE);
                                }
                            });
                }).addOnFailureListener(exception -> {
                    holder.profilePicHolder.setVisibility(View.GONE);
                    holder.initialsHolder.setVisibility(View.VISIBLE);
                });

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.profilePicHolder.setVisibility(View.GONE);
                holder.initialsHolder.setVisibility(View.GONE);
                Log.e("TweetAdapter", "Database error:" + error.getMessage());
                holder.userNameText.setText("Unknown User");
            }
        });



        // holder.predictionResultTextView.setText(tweet.getPredictionResult());
        // holder.modelConfidenceTextView.setText(tweet.getModelConfidence());

/*        // Handle image visibility if needed
        if (tweet.getImage().isEmpty()) {
            holder.tweetImage.setVisibility(View.GONE);
        } else {
            holder.tweetImage.setVisibility(View.VISIBLE);
            // Load image using an image loading library (e.g., Glide, Picasso)
        }*/

        // Show comments
        holder.commentsContainer.removeAllViews(); // Clear previous comments
        if (tweet.getComments() != null) {
            for (Comment comment : tweet.getComments()) {
                View commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, holder.commentsContainer, false);
                TextView commentUserId = commentView.findViewById(R.id.commentUserId);
                TextView commentText = commentView.findViewById(R.id.commentText);

                commentUserId.setText(comment.getName());
                commentText.setText(comment.getComment());

                holder.commentsContainer.addView(commentView);
            }
        }

        // Expand/Collapse Comments
        holder.show_comment.setOnClickListener(v -> {
            if (holder.commentsContainer.getVisibility() == View.VISIBLE) {
                holder.commentsContainer.setVisibility(View.GONE);
                //c holder.expandCollapseButton.setText("Show Comments");
            } else {
                holder.commentsContainer.setVisibility(View.VISIBLE);
                // holder.expandCollapseButton.setText("Hide Comments");
            }
        });

        // Handle Reply Button
        holder.comment_image.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReplyActivity.class);
            intent.putExtra("tweet_id", tweet.getId());
            intent.putExtra("tweet", tweet.getTweet());
            context.startActivity(intent);
        });

        // Handle ImageView click for popup menu
        holder.actionImage.setOnClickListener(v -> {
            if (userId.equals(tweet.getUserId())) {

                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.tweet_edit, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.edit_text_id) {
                        // Handle Edit action

                        Intent intent = new Intent(context, EditTweetActivity.class);
                        intent.putExtra("tweet_id", tweet.getId());
                        intent.putExtra("tweet", tweet.getTweet());
                        context.startActivity(intent);

                        Toast.makeText(context, "Edit Post clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.delete_tweet) {
                        // Handle Delete action
                        Toast.makeText(context, "Delete Post clicked", Toast.LENGTH_SHORT).show();
                        deleteTweet(tweet.getId(), position);
                        return true;
                    } else {
                        return false;
                    }
                });

                popupMenu.show();
            } else {
                Toast.makeText(context, "You can't edit or delete others' posts", Toast.LENGTH_SHORT).show();
            }
        });
        ////////////////like
        try {

            if (tweet.getLikedBy().contains(userId)) {
                holder.likeButton.setImageResource(R.drawable.ic_heart_filled);
            } else {
                holder.likeButton.setImageResource(R.drawable.ic_heart_outline);
            }
        } catch (Exception e) {

        }
        holder.likeButton.setOnClickListener(v -> {
            holder.likeProgressBar.setVisibility(View.VISIBLE);
            List<String> valuesList = new ArrayList<>();
            DatabaseReference CommentsHolder = FirebaseDatabase
                    .getInstance()
                    .getReference("Tweets")
                    .child(tweet.getId())
                    .child("likedBy");
            CommentsHolder.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        // Access child data
                        String key = snapshot.getKey(); // Key of the child
                        String value = snapshot.getValue(String.class); // Value of the child

                        valuesList.add(value);
                    }


                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (valuesList.contains(userId)) {
                                valuesList.remove(userId);
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("Tweets")
                                        .child(tweet.getId())
                                        .child("likedBy")
                                        .setValue(valuesList)
                                        .addOnCompleteListener(task2 -> {
                                            holder.likeButton.setImageResource(R.drawable.ic_heart_outline);
                                            tweet.setLikedBy(valuesList);
                                            tweetList.set(position, tweet);
                                            updateTweets(tweetList);
                                            // update like count after unliking
                                            holder.likeCountText.setText(valuesList.size()+ " likes");
                                            notifyItemChanged(position);
                                            holder.likeProgressBar.setVisibility(View.GONE);

                                        });
                            } else {
                                valuesList.add(userId);

                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("Tweets")
                                        .child(tweet.getId())
                                        .child("likedBy")
                                        .setValue(valuesList)
                                        .addOnCompleteListener(task2 -> {
                                            holder.likeButton.setImageResource(R.drawable.ic_heart_filled);
                                            tweet.setLikedBy(valuesList);
                                            tweetList.set(position, tweet);
                                            updateTweets(tweetList);

                                            holder.likeCountText.setText(valuesList.size()+" likes");
                                            notifyItemChanged(position);
                                            holder.likeProgressBar.setVisibility(View.GONE);
                                        });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }
////////////////////////delete

    private void deleteTweet(String tweetId, int position) {
        DatabaseReference tweetRef = FirebaseDatabase.getInstance().getReference("Tweets");
        tweetRef.child(tweetId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tweetList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Tweet deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete tweet", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //////////////////////




    private static String getFirstLetterUpperCased(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase();
    }
    public static class TweetViewHolder extends RecyclerView.ViewHolder {
        TextView tweetText;
        TextView userNameText;
        ImageView tweetImage;
        TextView dateTime;
        LinearLayout commentsContainer;
        Button replyButton;
        Button expandCollapseButton;
        ImageView profilePic;
        CardView profilePicHolder;
        CardView initialsHolder;
        TextView initials;
        ImageView actionImage;
        ImageView likeButton;
        TextView likeCountText;
        ProgressBar likeProgressBar;
        ImageView comment_image;
        ImageView show_comment;

        public TweetViewHolder(@NonNull View itemView) {
            super(itemView);
            tweetText = itemView.findViewById(R.id.tweetText);
            //tweetImage = itemView.findViewById(R.id.tweetImage);
            dateTime = itemView.findViewById(R.id.dateTime);
            commentsContainer = itemView.findViewById(R.id.commentsContainer);
            // replyButton = itemView.findViewById(R.id.replyButton);
            //expandCollapseButton = itemView.findViewById(R.id.expandCollapseButton);
            initials = itemView.findViewById(R.id.initials);
            profilePic = itemView.findViewById(R.id.profilePic);
            profilePicHolder = itemView.findViewById(R.id.profilePicHolder);
            initialsHolder = itemView.findViewById(R.id.initialsHolder);
            actionImage = itemView.findViewById(R.id.actionImageView);
            likeButton = itemView.findViewById(R.id.likeButton);  // like
            likeCountText = itemView.findViewById(R.id.likeCountText); //like count
            likeProgressBar = itemView.findViewById(R.id.likeProgressBar);//progressbar like
            comment_image = itemView.findViewById(R.id.comment_image);//comment
            show_comment = itemView.findViewById(R.id.show_comment);
            userNameText = itemView.findViewById(R.id.userNameText);

        }
    }
}