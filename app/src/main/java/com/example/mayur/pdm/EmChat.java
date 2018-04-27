package com.example.mayur.pdm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;


public class EmChat extends AppCompatActivity {

    private EditText editMessage;
    private DatabaseReference mDatebase;
    private RecyclerView mMessageList;
    private FirebaseAuth mAuth;
    private  FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_em_chat);

        editMessage = (EditText)findViewById(R.id.editMessageE);
        mDatebase = FirebaseDatabase.getInstance().getReference().child("Messages");
        mMessageList = (RecyclerView)findViewById(R.id.messageRec);
        mMessageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageList.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(EmChat.this,SignupActivity.class));
                }
            }
        };
    }

        public void sendButtonClicked(View view){
            mCurrentUser = mAuth.getCurrentUser();
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        final String messageValue = editMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageValue)){
                final DatabaseReference newPost = mDatebase.push();
                mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newPost.child("content").setValue(messageValue);
                        newPost.child("username").setValue(dataSnapshot.child("fname").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mMessageList.scrollToPosition(mMessageList.getAdapter().getItemCount());
                editMessage.getText().clear();

            }
        }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter <Message,MessageViewHolder> FBRA = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,R.layout.singlemessagelayout,MessageViewHolder.class,mDatebase
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.setContent(model.getContent());
                viewHolder.setUsername(model.getUsername());
            }
        };
        mMessageList.setAdapter(FBRA);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
            View mView;
            public MessageViewHolder(View itemView){
                super(itemView);
                mView = itemView;
            }

            public void setContent(String content){
                TextView message_content = (TextView)mView.findViewById(R.id.messageText);
                TextView message_time = (TextView)mView.findViewById(R.id.message_time);

                message_content.setText(content);
                message_time.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",Calendar.getInstance()));
            }

            public void setUsername(String username){
                TextView username_content = (TextView) mView.findViewById(R.id.usernameText);
                username_content.setText(username);
            }
        }
    }

