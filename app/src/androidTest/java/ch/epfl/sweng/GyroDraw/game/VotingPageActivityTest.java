package ch.epfl.sweng.GyroDraw.game;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static ch.epfl.sweng.GyroDraw.firebase.RoomAttributes.RANKING;
import static ch.epfl.sweng.GyroDraw.firebase.RoomAttributes.USERS;
import static ch.epfl.sweng.GyroDraw.game.LoadingScreenActivity.ROOM_ID;
import static ch.epfl.sweng.GyroDraw.game.drawing.DrawingOnlineActivityTest.initializedBitmap;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.View;
import android.widget.RatingBar;
import ch.epfl.sweng.GyroDraw.R;
import ch.epfl.sweng.GyroDraw.auth.Account;
import ch.epfl.sweng.GyroDraw.auth.ConstantsWrapper;
import ch.epfl.sweng.GyroDraw.firebase.FbDatabase;
import ch.epfl.sweng.GyroDraw.firebase.RoomAttributes;
import ch.epfl.sweng.GyroDraw.home.HomeActivity;
import ch.epfl.sweng.GyroDraw.localDatabase.LocalDbHandlerForImages;
import ch.epfl.sweng.GyroDraw.utils.BitmapManipulator;
import ch.epfl.sweng.GyroDraw.utils.ImageSharer;
import ch.epfl.sweng.GyroDraw.utils.ImageStorageManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.storage.FirebaseStorage;
import java.io.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class VotingPageActivityTest {

    private static final String USER_ID = "userA";
    private static final String ROOM_ID_TEST = "0123457890";
    private static final int PERMISSION_WRITE_STORAGE = 1;

    private DataSnapshot dataSnapshotMock;
    private DatabaseError databaseErrorMock;
    private StarAnimationView starsAnimation;

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule
            .grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public final ActivityTestRule<VotingPageActivity> activityRule =
            new ActivityTestRule<VotingPageActivity>(VotingPageActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    VotingPageActivity.disableAnimations();
                    Account.deleteAccount();
                    Account.createAccount(InstrumentationRegistry.getTargetContext(),
                            new ConstantsWrapper(), USER_ID, "test");
                    Account.getInstance(InstrumentationRegistry.getTargetContext())
                            .setUserId(USER_ID);
                }

                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent();
                    intent.putExtra(ROOM_ID, ROOM_ID_TEST);
                    return intent;
                }
            };

    @Before
    public void init() {
        dataSnapshotMock = Mockito.mock(DataSnapshot.class);
        databaseErrorMock = Mockito.mock(DatabaseError.class);
        starsAnimation = activityRule.getActivity()
                .findViewById(R.id.starsAnimation);
    }

    @After
    public void end() {
        Account.deleteAccount();
    }

    @Test
    public void testSharingImage() {
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID, RANKING, 0);
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID, USERS, USER_ID);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(6);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));

        Bitmap bitmap = BitmapFactory.decodeResource(
                activityRule.getActivity().getResources(), R.drawable.league_1);
        LocalDbHandlerForImages localDbHandler = new LocalDbHandlerForImages(
                activityRule.getActivity().getApplicationContext(), null, 1);
        localDbHandler.addBitmap(bitmap, 2);
        onView(withId(R.id.shareButton)).perform(click());
        assertThat(myFragment.isVisible(), is(true));
        onView(withId(R.id.homeButton)).perform(click());
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID, RANKING, 0);
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID, USERS, USER_ID);
    }

    @Test
    public void testSaveImage() {
        // Open fragment
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(6);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));

        // Save image
        Bitmap bitmap = initializedBitmap();
        LocalDbHandlerForImages localDbHandler = new LocalDbHandlerForImages(
                activityRule.getActivity().getApplicationContext(), null, 1);
        localDbHandler.addBitmap(bitmap, 2);
        onView(withId(R.id.saveButton)).perform(click());
        assertThat(myFragment.isVisible(), is(true));
    }

    @Test
    public void ratingUsingRatingBarShouldBeSaved() {
        // To ensure that the rating value does not get above 20
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID,
                RoomAttributes.RANKING, 0);

        short counter = activityRule.getActivity().getChangeDrawingCounter();
        SystemClock.sleep(5000);
        ((RatingBar) activityRule.getActivity().findViewById(R.id.ratingBar)).setRating(3);
        SystemClock.sleep(5000);
        assertThat(activityRule.getActivity().getRatings()[counter], is(3));
    }

    @Test
    public void addStarsHandlesBigNumber() {
        new StarAnimationView(activityRule.getActivity());
        new StarAnimationView(activityRule.getActivity(), null, 0);
        int previousStars = starsAnimation.getNumStars();
        setStarsAnimationToVisible();
        starsAnimation.onSizeChanged(100, 100, 100, 100);
        Canvas canvas = new Canvas();
        SystemClock.sleep(1000);
        starsAnimation.onDraw(canvas);
        starsAnimation.addStars(1000);
        starsAnimation.updateState(1000);
        starsAnimation.onDraw(canvas);
        assertThat(starsAnimation.getNumStars(), greaterThanOrEqualTo(previousStars + 5));
        SystemClock.sleep(10000);
        setStarsAnimationToGone();
    }

    private void setStarsAnimationToVisible() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    starsAnimation.setVisibility(View.VISIBLE);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void setStarsAnimationToGone() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    starsAnimation.setVisibility(View.GONE);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void addStarsHandlesNegativeNumber() {
        int previousStars = starsAnimation.getNumStars();
        starsAnimation.onSizeChanged(100, 100, 100, 100);
        Canvas canvas = new Canvas();
        starsAnimation.onDraw(canvas);
        starsAnimation.addStars(-10);
        starsAnimation.updateState(1000);
        starsAnimation.onDraw(canvas);
        assertThat(starsAnimation.getNumStars(), is(previousStars));
    }

    @Test
    public void startHomeActivityStartsHomeActivity() {
        SystemClock.sleep(1000);
        Intents.init();
        activityRule.getActivity().startHomeActivity();
        SystemClock.sleep(2000);
        intended(hasComponent(HomeActivity.class.getName()));
        Intents.release();
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID, USERS, USER_ID);
        FbDatabase.setValueToUserInRoomAttribute(ROOM_ID_TEST, USER_ID, RANKING, 0);
    }

    @Test
    public void testState6Change() {
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(6);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2500);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));
    }

    @Test
    public void testState5Change() {
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(5);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2500);

        onView(withId(R.id.playerNameView)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testState4Change() {
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(4);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(6000);
        assertThat(activityRule.getActivity().getDrawingsIds(), is(notNullValue()));
    }

    @Test
    public void testShowDrawingImage() {
        Bitmap image = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        image.eraseColor(android.graphics.Color.GREEN);
        activityRule.getActivity().callShowWinnerDrawing(image, "Champion");
    }

    @Test
    public void testChangeImage() {
        short counter = activityRule.getActivity().getChangeDrawingCounter();
        activityRule.getActivity().callChangeImage();

        SystemClock.sleep(6000);

        assertThat((int) activityRule.getActivity().getChangeDrawingCounter(),
                greaterThanOrEqualTo(counter + 1));
    }

    @Test(expected = DatabaseException.class)
    public void testOnCancelledListenerState() {
        when(databaseErrorMock.toException()).thenReturn(new DatabaseException("Cancelled"));
        activityRule.getActivity().listenerState.onCancelled(databaseErrorMock);
    }

    @Test(expected = DatabaseException.class)
    public void testOnCancelledListenerCounter() {
        when(databaseErrorMock.toException()).thenReturn(new DatabaseException("Cancelled"));
        activityRule.getActivity().listenerCounter.onCancelled(databaseErrorMock);
    }

    @Test
    public void testDecodeSampledBitmapFromResource() {
        Bitmap bitmap = BitmapManipulator.decodeSampledBitmapFromResource(
                activityRule.getActivity().getResources(), R.drawable.default_image, 2, 2);
        assertThat(bitmap, is(not(nullValue())));
    }

    @Test
    public void testDecodeSampledBitmapFromByteArray() {
        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();
        int[] source = {Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE};
        Bitmap bitmap = Bitmap.createBitmap(source, 2, 2, Bitmap.Config.ARGB_8888)
                .copy(Bitmap.Config.ARGB_8888, true);
        bitmap.compress(Bitmap.CompressFormat.JPEG,
                1, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        Bitmap newBitmap = BitmapManipulator.decodeSampledBitmapFromByteArray(
                data, 0, data.length, 2, 2);
        assertThat(newBitmap, is(not(nullValue())));
    }

    @Test
    public void testImageSharerShareToAppFails() {
        ImageSharer imageSharer = ImageSharer.getInstance(activityRule.getActivity());
        imageSharer.getUrlAndShare(FirebaseStorage.getInstance().getReference().child("TestImage"));
        imageSharer.shareDrawingToFacebook(Uri.EMPTY);
        assertThat(imageSharer.shareImageToFacebookApp(initializedBitmap()), is(false));
    }

    @Test
    public void testToastAfterSuccessfulDownload() {
        activityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageStorageManager.successfullyDownloadedImageToast(activityRule.getActivity());
            }
        });

        onView(withText(activityRule.getActivity().getString(R.string.successfulImageDownload)))
                .inRoot(withDecorView(not(is(activityRule.getActivity()
                        .getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void testOnRequestWritePermissionsAccepted() {
        // Open fragment
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(6);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));

        // Save image
        Bitmap bitmap = initializedBitmap();
        LocalDbHandlerForImages localDbHandler = new LocalDbHandlerForImages(
                activityRule.getActivity().getApplicationContext(), null, 1);
        localDbHandler.addBitmap(bitmap, 2);
        activityRule.getActivity().onRequestPermissionsResult(PERMISSION_WRITE_STORAGE,
                new String[]{}, new int[]{});

        assertThat(myFragment.isVisible(), is(true));
    }

    @Test
    public void testOnRandomRequestPermissionsAccepted() {
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(6);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));

        activityRule.getActivity().onRequestPermissionsResult(42,
                new String[]{}, new int[]{});

        assertThat(myFragment.isVisible(), is(true));
    }
}
