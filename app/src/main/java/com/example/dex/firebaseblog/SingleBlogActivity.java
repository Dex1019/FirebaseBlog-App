package com.example.dex.firebaseblog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class SingleBlogActivity extends AppCompatActivity {
    private static final String TAG = "SingleBlogActivity";
    private String mPost_key = null;

    private DatabaseReference mDatabase;

    private ImageView mSingleBlogImage;
    private EditText mSingleTitle, mSingleDesc;
    private Button mRemoveBtn;
    private Button mUpdateBtn;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_blog);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        final String mPost_key = getIntent().getStringExtra("blog_id");

//        Log.d(TAG, "onCreate: mPostKey :" + mPost_key);


        mSingleBlogImage = findViewById(R.id.single_postImage);
        mSingleTitle = findViewById(R.id.single_postTitle);
        mSingleDesc = findViewById(R.id.single_postDesc);
        mRemoveBtn = findViewById(R.id.single_removeBtn);
        mUpdateBtn = findViewById(R.id.single_updateBtn);

        mRemoveBtn.setVisibility(View.INVISIBLE);

        mDatabase.child(mPost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                final String post_image = (String) dataSnapshot.child("image").getValue();
                final String post_uid = (String) dataSnapshot.child("uid").getValue();
                String post_userName = (String) dataSnapshot.child("username").getValue();

                getSupportActionBar().setTitle(post_userName);

                mSingleTitle.setText(post_title);
                mSingleDesc.setText(post_desc);


                Picasso.with(SingleBlogActivity.this)
                        .load(post_image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_error)
                        .into(mSingleBlogImage, new Callback() {
                            @Override
                            public void onSuccess() {


                            }

                            @Override
                            public void onError() {
                                Picasso.with(SingleBlogActivity.this)
                                        .load(post_image)
                                        .placeholder(R.drawable.ic_error)
                                        .into(mSingleBlogImage);


                            }
                        });

                if (mAuth.getCurrentUser().getUid().equals(post_uid)) {

                    mRemoveBtn.setVisibility(View.VISIBLE);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SingleBlogActivity.this);
                builder.setTitle("Remove Post");
                builder.setMessage("Do you sure want to remove the Post?");
                builder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {


                                mDatabase.child(mPost_key).removeValue();

                                startActivity(new Intent(SingleBlogActivity.this, MainActivity.class));


                            }
                        });
                builder.setNeutralButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {

                            }
                        });

                builder.show();


            }
        });

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(SingleBlogActivity.this, "Update Post", Toast.LENGTH_SHORT).show();


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.action_logout) {
            logout();
        }


        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(SingleBlogActivity.this, SetupActivity.class));
        }


        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }
}
