package ch.epfl.sweng.GyroDraw.game.drawing.items;

import ch.epfl.sweng.GyroDraw.R;
import ch.epfl.sweng.GyroDraw.game.drawing.PaintView;

/**
 * Class representing an item which slows down the player's cursor.
 */
public class SlowdownItem extends DeactivableItem {

    private static final double SLOWDOWN_FACTOR = 0.5;

    public SlowdownItem(int posX, int posY, int radius) {
        super(posX, posY, radius);
    }

    @Override
    public void activate(final PaintView paintView) {
        vibrate(paintView);
        paintView.multSpeed(SLOWDOWN_FACTOR);
        launchCountDownUntilDeactivation(paintView).start();
    }

    @Override
    public void deactivate(PaintView paintView) {
        paintView.multSpeed(1 / SLOWDOWN_FACTOR);
    }

    @Override
    public String getTextFeedback() {
        return "SLOWDOWN ! ";
    }

    @Override
    public int getColorId() {
        return R.color.colorGreen;
    }
}
