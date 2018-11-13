package ch.epfl.sweng.SDP.game.drawing;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import ch.epfl.sweng.SDP.R;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static org.junit.Assert.*;

public class DrawingGameTest {

    @Rule
    public final ActivityTestRule<DrawingOffline> activityRule =
            new ActivityTestRule<DrawingOffline>(DrawingOffline.class){

                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent();
                    return intent;
                }
            };


    @Test
    public void getLayoutid() {
        int layoutId = activityRule.getActivity().getLayoutid();
        assertEquals(layoutId, R.layout.activity_drawing_offline);
    }

    @Test
    public void exitClick() {
        onView(ViewMatchers.withId(R.id.exit)).perform(click());
        assertTrue(activityRule.getActivity().isFinishing());
    }

}