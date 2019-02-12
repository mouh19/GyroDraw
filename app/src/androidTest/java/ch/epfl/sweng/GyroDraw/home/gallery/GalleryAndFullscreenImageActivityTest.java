package ch.epfl.sweng.GyroDraw.home.gallery;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.graphics.Bitmap;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import ch.epfl.sweng.GyroDraw.R;
import ch.epfl.sweng.GyroDraw.home.HomeActivity;
import ch.epfl.sweng.GyroDraw.localDatabase.LocalDbHandlerForImages;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GalleryAndFullscreenImageActivityTest {

    @Rule
    public final ActivityTestRule<GalleryActivity> activityRule = new ActivityTestRule<>(
            GalleryActivity.class);

    @Before
    public void init() {
        Intents.init();
        final GalleryActivity activity = activityRule.getActivity();
        LocalDbHandlerForImages dbHandler = new LocalDbHandlerForImages(activity, null, 1);
        dbHandler.addBitmap(Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888), 2);
    }

    @After
    public void release() {
        Intents.release();
    }

    @Test
    public void clickOnItemOpensFullscreenImageActivity() {
        openFullscreenImageActivity();
        intended(hasComponent(FullscreenImageActivity.class.getName()));
    }

    @Test
    public void clickOnExitCrossOpensHomeActivity() {
        onView(withId(R.id.crossText)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    ////////// FullscreenImageActivity /////////////

    @Test
    public void clickOnExitCrossOpensGalleryActivity() {
        openFullscreenImageActivity();
        onView(withId(R.id.crossText)).perform(click());
        intended(hasComponent(GalleryActivity.class.getName()));
    }

    @Test
    public void testDeleteButton() {
        onView(withId(R.id.deleteButton)).perform(click());
        assertThat(((RecyclerView) activityRule.getActivity().findViewById(R.id.galleryList))
                .getAdapter().getItemCount(), is(0));
    }

    /**
     * Opens an instance of {@link FullscreenImageActivity} by clicking on the first image in the
     * gallery.
     */
    private void openFullscreenImageActivity() {
        onView(withId(R.id.galleryList)).perform(RecyclerViewActions.scrollToPosition(0));
        onView(withId(R.id.galleryList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }
}
