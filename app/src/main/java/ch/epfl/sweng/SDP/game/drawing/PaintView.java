package ch.epfl.sweng.SDP.game.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import ch.epfl.sweng.SDP.firebase.FbStorageHandler;
import ch.epfl.sweng.SDP.LocalDbHandler;


public class PaintView extends View {

    private Paint paint;
    private Paint paintC;
    private int circleRadius;
    private float circleX;
    private float circleY;
    private Path path;
    private Boolean draw;
    private Bitmap bitmap;
    private Canvas canvas;
    private LocalDbHandler localDbHandler;
    private FbStorageHandler fbStorageHandler;

    /**
     * Constructor for the view.
     *
     * @param context Context of class
     * @param attrs   Attributes of class
     */
    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        localDbHandler = new LocalDbHandler(context, null, 1);
        fbStorageHandler = new FbStorageHandler();
        setFocusable(true);
        paint = new Paint();
        paintC = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paintC.setColor(Color.RED);
        paintC.setStyle(Paint.Style.STROKE);
        paintC.setStrokeWidth(10);

        circleRadius = 10; //will be modifiable in future, not hardcoded
        circleX = 0;
        circleY = 0;
        draw = false;
        path = new Path();
        path.moveTo(circleX, circleY);
    }

    public float getCircleX() {
        return circleX;
    }

    public float getCircleY() {
        return circleY;
    }

    public boolean getDraw() {
        return draw;
    }

    public void setCircleX(float circleX) {
        this.circleX = circleX;
    }

    public void setCircleY(float circleY) {
        this.circleY = circleY;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    /**
     * Initializes coordinates of pen and size of bitmap.
     * Creates a bitmap and a canvas.
     * @param size size of the screen
     */
    public void setSizeAndInit(Point size) {
        circleX = size.x / 2 - circleRadius;
        circleY = size.y / 2 - circleRadius;
        bitmap = Bitmap.createBitmap(size.x,
                (int)(((float)size.y/size.x)*size.x), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    /**
     * Clears the canvas.
     */
    public void clear() {
        path.reset();
    }

    /**
     * Draws the path and circle, if draw is set.
     *
     * @param canvas to draw on
     */
    public void onDraw(Canvas canvas) {
        canvas.save();
        if (draw) {
            paintC.setStyle(Paint.Style.FILL);
            paintC.setStrokeWidth(10);
            path.lineTo(circleX, circleY);
        } else {
            paintC.setStyle(Paint.Style.STROKE);
            paintC.setStrokeWidth(5);
        }
        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(circleX, circleY, circleRadius, paintC);
        canvas.drawPath(path, paint);
        canvas.restore();
        path.moveTo(circleX, circleY);
    }

    /**
     * Gets called when time for drawing is over.
     * Saves the bitmap in the local DB.
     */
    public void saveCanvasInDb(){
        this.draw(canvas);
        localDbHandler.addBitmapToDb(bitmap, new ByteArrayOutputStream());
        // Create timestamp as name for image. Will include userID in future
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(""+ts+".jpg");
        fbStorageHandler.sendBitmapToFireBaseStorage(bitmap, imageRef);
    }
}
