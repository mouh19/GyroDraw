package ch.epfl.sweng.SDP.game;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.RatingBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;

import ch.epfl.sweng.SDP.R;
import ch.epfl.sweng.SDP.auth.Account;
import ch.epfl.sweng.SDP.auth.ConstantsWrapper;
import ch.epfl.sweng.SDP.firebase.Database;
import ch.epfl.sweng.SDP.home.HomeActivity;
import ch.epfl.sweng.SDP.localDatabase.LocalDbHandlerForImages;
import ch.epfl.sweng.SDP.utils.BitmapManipulator;
import ch.epfl.sweng.SDP.utils.ImageSharer;
import ch.epfl.sweng.SDP.utils.ImageStorageManager;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static ch.epfl.sweng.SDP.game.drawing.DrawingOnlineTest.initializedBitmap;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class VotingPageActivityTest {

    private DataSnapshot dataSnapshotMock;
    private DatabaseError databaseErrorMock;
    private StarAnimationView starsAnimation;

    @Rule
    public final ActivityTestRule<VotingPageActivity> activityRule =
            new ActivityTestRule<VotingPageActivity>(VotingPageActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    VotingPageActivity.disableAnimations();
                    Account.deleteAccount();
                    Account.createAccount(InstrumentationRegistry.getTargetContext(),
                            new ConstantsWrapper(), "userA", "test");
                    Account.getInstance(InstrumentationRegistry.getTargetContext())
                            .setUserId("userA");
                }

                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent();
                    intent.putExtra("RoomID", "0123457890");
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
        // Open fragment
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(5);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));

        // Share image
        Bitmap bitmap = BitmapFactory.decodeResource(activityRule.getActivity().getResources(), R.drawable.league_1);
        LocalDbHandlerForImages localDbHandler = new LocalDbHandlerForImages(
                activityRule.getActivity().getApplicationContext(), null, 1);
        localDbHandler.addBitmapToDb(bitmap,2);
        onView(withId(R.id.share)).perform(click());
        assertThat(myFragment.isVisible(), is(true));
    }

    @Test
    public void testSaveImage() {
        // Open fragment
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(5);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));

        // Save image
        Bitmap bitmap = initializedBitmap();
        LocalDbHandlerForImages localDbHandler = new LocalDbHandlerForImages(
                activityRule.getActivity().getApplicationContext(), null, 1);
        localDbHandler.addBitmapToDb(bitmap,2);
        onView(withId(R.id.save)).perform(click());
        assertThat(myFragment.isVisible(), is(true));
    }

    @Test
    public void ratingUsingRatingBarShouldBeSaved() {

        // To ensure that the rating value does not get above 20
        Database.getReference("realRooms.0123457890.ranking.userA").setValue(0);

        short counter = activityRule.getActivity().getChangeDrawingCounter();
        SystemClock.sleep(5000);
        ((RatingBar) activityRule.getActivity().findViewById(R.id.ratingBar)).setRating(3);
        SystemClock.sleep(5000);
        assertThat(activityRule.getActivity().getRatings()[counter], is(3));
    }

    @Test
    public void addStarsHandlesBigNumber() {
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
        Intents.init();
        activityRule.getActivity().startHomeActivity(null);
        SystemClock.sleep(2000);
        intended(hasComponent(HomeActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void testState5Change() {
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(5);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);

        RankingFragment myFragment = (RankingFragment) activityRule.getActivity()
                .getSupportFragmentManager().findFragmentById(R.id.votingPageLayout);
        assertThat(myFragment.isVisible(), is(true));
    }

    @Test
    public void testState4Change() {
        SystemClock.sleep(1000);
        when(dataSnapshotMock.getValue(Integer.class)).thenReturn(4);
        activityRule.getActivity().callOnStateChange(dataSnapshotMock);
        SystemClock.sleep(2000);
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
        assertThat(imageSharer.shareImageToFacebookApp(initializedBitmap()), is(false));
    }

    @Test
    public void testToastAfterSuccessfulDownload() {
        ImageStorageManager.successfullyDownloadedImageToast(activityRule.getActivity());
        onView(withText(activityRule.getActivity().getString(R.string.successfulImageDownload)))
                .inRoot(withDecorView(not(is(activityRule.getActivity()
                        .getWindow().getDecorView())))).check(matches(isDisplayed()));
    }
}
