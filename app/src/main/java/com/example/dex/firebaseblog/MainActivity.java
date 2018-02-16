package com.example.dex.firebaseblog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private boolean mProcessLike = false;


    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseCurrentUser;
    private Query mQueryCurrentUser;

    private DatabaseReference mDatabaseLike;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        // recyclerView
        mBlogList = findViewById(R.id.rv_blogList);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));

        //Firebase instances
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        // Offline functionality
        mDatabaseLike.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);


//        String current_userId = mAuth.getCurrentUser().getUid();
//        Log.d(TAG, "currentUser: "+current_userId);

//        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Blog");
//        mQueryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(current_userId);


        checkUserExist();


    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: started");

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerOptions<Blog> options =
                new FirebaseRecyclerOptions.Builder<Blog>()
                        .setQuery(mDatabase, Blog.class)
                        .build();

        final FirebaseRecyclerAdapter<Blog, BlogViewHolder> mAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder viewHolder, int position, @NonNull Blog model) {

                try {
                    final String post_key = getRef(position).getKey();

                    String current_UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    String user_uid = model.getUid().toString();


//                viewHolder.setDesc(model.getDesc());
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setImage(getApplicationContext(), model.getImage());
                    viewHolder.setUserName(model.getUsername());

                    viewHolder.setLikeBtn(post_key);

                    viewHolder.setUserProfile(getApplicationContext(), current_UserId, user_uid);


                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent singleBlogIntent = new Intent(MainActivity.this, SingleBlogActivity.class);
                            singleBlogIntent.putExtra("blog_id", post_key);
                            startActivity(singleBlogIntent);

                        }
                    });


                    viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mProcessLike = true;

                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (mProcessLike) {

                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid())
                                                    .removeValue();

                                            mProcessLike = false;

                                        } else {
                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid())
                                                    .setValue("Random value");

                                            mProcessLike = false;
                                        }


                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }


                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }


            @Override
            public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.blog_row, parent, false);

                return new BlogViewHolder(view);
            }
        };

        // recyclerView Adapter
        mBlogList.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {

            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, SetupActivity.class));
        }


        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        ImageView mLikeBtn;

        CircularImageView mUserProfileImage;

        DatabaseReference mDatabaseLike;

        DatabaseReference mDatabaseUser;

        FirebaseAuth mAuth;

        ProgressBar mProgressbar;


        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mLikeBtn = mView.findViewById(R.id.post_likeBtn);
            mUserProfileImage = mView.findViewById(R.id.post_profilePic);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

            mProgressbar = mView.findViewById(R.id.post_progressbar);


        }

        public void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

//        public void setDesc(String desc) {
//            TextView post_desc = mView.findViewById(R.id.post_desc);
//            post_desc.setText(desc);
//        }

        public void setUserName(String userName) {
            TextView post_userName = mView.findViewById(R.id.post_userName);
            post_userName.setText(userName);
        }


        public void setLikeBtn(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                        mLikeBtn.setImageResource(R.drawable.like);


                    } else {

                        mLikeBtn.setImageResource(R.drawable.dislike);


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


        public void setImage(final Context mCtx, final String image) {
            final ImageView post_image = mView.findViewById(R.id.post_image);

            mProgressbar.setVisibility(View.VISIBLE);


            Picasso.with(mCtx)
                    .load(image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(post_image, new Callback() {
                        @Override
                        public void onSuccess() {

                            mProgressbar.setVisibility(View.GONE);


                        }

                        @Override
                        public void onError() {
                            mProgressbar.setVisibility(View.GONE);
                            Picasso.with(mCtx)
                                    .load(image)
                                    .error(R.drawable.ic_error)
                                    .into(post_image);

                        }
                    });
        }


        public void setUserProfile(final Context mCtx, final String current_userId, final String user_uid) {

//            Log.d(TAG, "userKey: " + current_userId);
//            Log.d(TAG, "userUID: " + user_uid);

            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String userProfileImage = (String) dataSnapshot.child(user_uid).child("image").getValue();

                    if (dataSnapshot.hasChild(user_uid)) {
                        Picasso.with(mCtx)
                                .load(userProfileImage)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(mUserProfileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                        mProgressbar.setVisibility(View.GONE);


                                    }

                                    @Override
                                    public void onError() {
                                        mProgressbar.setVisibility(View.GONE);
                                        Picasso.with(mCtx)
                                                .load(userProfileImage)
                                                .error(R.drawable.ic_error)
                                                .into(mUserProfileImage);

                                    }
                                });
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


}

