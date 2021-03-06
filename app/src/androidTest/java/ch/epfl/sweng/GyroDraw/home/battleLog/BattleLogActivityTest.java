package ch.epfl.sweng.GyroDraw.home.battleLog;

import android.app.Activity;
import android.graphics.Bitmap;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.GyroDraw.R;
import ch.epfl.sweng.GyroDraw.localDatabase.LocalDbForGameResults;
import ch.epfl.sweng.GyroDraw.localDatabase.LocalDbHandlerForGameResults;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sweng.GyroDraw.game.drawing.DrawingOnlineActivityTest.bitmapEqualsNewBitmap;
import static ch.epfl.sweng.GyroDraw.game.drawing.DrawingOnlineActivityTest.compressBitmap;
import static ch.epfl.sweng.GyroDraw.game.drawing.DrawingOnlineActivityTest.initializedBitmap;
import static ch.epfl.sweng.GyroDraw.home.leaderboard.LeaderboardActivityTest.testExitButtonBody;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@RunWith(AndroidJUnit4.class)
public class BattleLogActivityTest {

    private static List<String> rankedUsernames = getUsernameList();

    private static final int RANK = 2;
    private static final int STARS = 15;
    private static final int TROPHIES = -5;
    private final Bitmap drawing = initializedBitmap();

    private GameResult gameResult;
    private LocalDbForGameResults localDbHandler;

    @Rule
    public final ActivityTestRule<BattleLogActivity> activityRule =
            new ActivityTestRule<>(BattleLogActivity.class);

    /**
     * Initialize the game result and the local database handler.
     */
    @Before
    public void init() {
        gameResult = new GameResult(rankedUsernames, RANK, STARS, TROPHIES, drawing);
        localDbHandler = new LocalDbHandlerForGameResults(
                activityRule.getActivity(), null, 1);
    }

    @Test
    public void testClickOnExitButtonOpensHomeActivity() {
        testExitButtonBody();
    }

    @Test
    public void testLocalDb() {
        localDbHandler.addGameResultToDb(gameResult);
        GameResult newGameResult =
                localDbHandler.getGameResultsFromDb(activityRule.getActivity()).get(0);

        assertThat(newGameResult.getRankedUsername(), is(rankedUsernames));
        assertThat(newGameResult.getRank(), is(RANK));
        assertThat(newGameResult.getStars(), is(STARS));
        assertThat(newGameResult.getTrophies(), is(TROPHIES));
        Bitmap compressedDrawing = compressBitmap(drawing, 20);
        bitmapEqualsNewBitmap(compressedDrawing, newGameResult.getDrawing());
    }

    @Test
    public void testGameResult() {
        assertThat(gameResult.getRankedUsername(), is(rankedUsernames));
        assertThat(gameResult.getRank(), is(RANK));
        assertThat(gameResult.getStars(), is(STARS));
        assertThat(gameResult.getTrophies(), is(TROPHIES));
        bitmapEqualsNewBitmap(drawing, gameResult.getDrawing());
    }

    @Test
    public void testGameResultToLayout() {
        localDbHandler.addGameResultToDb(gameResult);
        onView(withId(R.id.exitButton)).perform(click());
        onView(withId(R.id.battleLogButton)).perform(click());
        assertThat(activityRule.getActivity().getGameResultsCount(), greaterThanOrEqualTo(1));
    }

    @Test
    public void testNullBitmapToDatabase() {
        localDbHandler.addGameResultToDb(
                new GameResult(rankedUsernames, RANK, STARS, TROPHIES, null));
        Activity activity = activityRule.getActivity();
        Bitmap defaultDrawing = localDbHandler.getGameResultsFromDb(activity).get(0).getDrawing();
        for (int i = 5; i < 10; i++) {
            for (int j = 5; j < 10; j++) {
                assertThat(defaultDrawing.getPixel(i, j), is(0xFFFFFFFF));
            }
        }
    }

    private static List<String> getUsernameList() {
        List<String> rankedUsername = new ArrayList<>();
        rankedUsername.add("User1");
        rankedUsername.add("User2");
        rankedUsername.add("User3");
        rankedUsername.add("User4");
        rankedUsername.add("User5");

        return rankedUsername;
    }
}
