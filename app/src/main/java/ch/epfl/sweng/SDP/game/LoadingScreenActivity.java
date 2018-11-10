package ch.epfl.sweng.SDP.game;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.ImageView;
import ch.epfl.sweng.SDP.Activity;
import ch.epfl.sweng.SDP.BooleanVariableListener;
import ch.epfl.sweng.SDP.R;
import ch.epfl.sweng.SDP.auth.ConstantsWrapper;
import ch.epfl.sweng.SDP.firebase.Database;
import ch.epfl.sweng.SDP.home.HomeActivity;
import ch.epfl.sweng.SDP.matchmaking.Matchmaker;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctionsException;
import java.util.ArrayList;

public class LoadingScreenActivity extends Activity {

    private String roomID = null;

    private BooleanVariableListener isRoomReady = new BooleanVariableListener();
    private BooleanVariableListener areWordsReady = new BooleanVariableListener();
    private static boolean enableWaitingAnimation = true;
    private static boolean isTesting = false;
    private boolean hasLeft = false;

    private static final String WORD_CHILDREN_DB_ID = "words";
    private static final String TOP_ROOM_NODE_ID = "realRooms";

    private boolean isWord1Ready = false;
    private boolean isWord2Ready = false;

    private DatabaseReference wordsVotesRef;

    private String word1 = null;
    private String word2 = null;

    private Database database;

    private BooleanVariableListener.ChangeListener listenerRoomReady =
            new BooleanVariableListener.ChangeListener() {
                @Override
                public void onChange() {
                    if(areWordsReady.getBoo() && isRoomReady.getBoo()) {
                        // Start new activity
                        wordsVotesRef.removeEventListener(listenerWords);
                        Intent intent = new Intent(getApplicationContext(),
                                WaitingPageActivity.class);
                        intent.putExtra("word1", word1);
                        intent.putExtra("word2", word2);
                        intent.putExtra("roomID", roomID);
                        startActivity(intent);
                    }
                }
            };

    private final ValueEventListener listenerWords = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<String> words = new ArrayList<>();

            for(DataSnapshot snap : dataSnapshot.getChildren()) {
                words.add(snap.getKey());
            }

            if(areWordsReady(words)) {
                areWordsReady.setBoo(true);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            throw databaseError.toException();
        }
    };

    protected boolean areWordsReady(ArrayList<String> words) {
        try {
            word1 = words.get(0);
            word2 = words.get(1);
        } catch (Exception e) {
            // Values not ready
        }

        if (word1 != null) {
            isWord1Ready = true;
        }

        if (word2 != null) {
            isWord2Ready = true;
        }

        return (isWord1Ready && isWord2Ready);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        database = Database.INSTANCE;

        if(!isTesting) {
            lookingForRoom();
        }

        overridePendingTransition(0, 0);

        isRoomReady.setListener(listenerRoomReady);
        areWordsReady.setListener(listenerRoomReady);

        if(enableWaitingAnimation) {
            Glide.with(this).load(R.drawable.waiting_animation_dots)
                    .into((ImageView) findViewById(R.id.waitingAnimationDots));
            Glide.with(this).load(R.drawable.background_animation)
                    .into((ImageView) findViewById(R.id.waitingBackgroundAnimation));
        }

    }

    protected void lookingForRoom() {
        Matchmaker.getInstance(new ConstantsWrapper())
                .joinRoom().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Exception exception = task.getException();
                    if (exception instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) exception;
                    }
                } else {
                    roomID = task.getResult();
                    if(hasLeft) {
                        Matchmaker.getInstance(new ConstantsWrapper()).leaveRoom(roomID);
                        finish();
                    } else {
                        wordsVotesRef = database.getReference(
                                TOP_ROOM_NODE_ID + "." + roomID + "." + WORD_CHILDREN_DB_ID);
                        wordsVotesRef.addValueEventListener(listenerWords);
                        isRoomReady.setBoo(true);
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            hasLeft = true;
            launchActivity(HomeActivity.class);
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void disableLoadingAnimations() {
        enableWaitingAnimation = false;
    }

    public static void setOnTest() {
        isTesting = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        hasLeft = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}