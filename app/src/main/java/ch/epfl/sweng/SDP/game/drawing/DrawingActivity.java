package ch.epfl.sweng.SDP.game.drawing;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.util.ArrayUtils;

import ch.epfl.sweng.SDP.Activity;
import ch.epfl.sweng.SDP.LocalDbHandler;
import ch.epfl.sweng.SDP.R;


public class DrawingActivity extends Activity implements SensorEventListener {
    private static final String TAG = "DrawingActivity";
    private PaintView paintView;
    private int speed;
    private int time;
    private int timeInterval;
    private Point size;
    private Handler handler;
    private SensorManager sensorManager;

    private ImageView[] colorButtons;

    private ImageView pencilButton;
    private ImageView eraserButton;
    private ImageView bucketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.fui_slide_in_right,
                R.anim.fui_slide_out_left);
        setContentView(R.layout.activity_drawing);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        colorButtons = new ImageView[]{findViewById(R.id.blackButton),
                findViewById(R.id.blueButton), findViewById(R.id.greenButton),
                findViewById(R.id.yellowButton), findViewById(R.id.redButton)};

        pencilButton = findViewById(R.id.pencilButton);
        eraserButton = findViewById(R.id.eraserButton);
        bucketButton = findViewById(R.id.bucketButton);

        Resources res = getResources();
        colorButtons[1].setColorFilter(res.getColor(R.color.colorBlue), PorterDuff.Mode.SRC_ATOP);
        colorButtons[2].setColorFilter(res.getColor(R.color.colorGreen), PorterDuff.Mode.SRC_ATOP);
        colorButtons[3].setColorFilter(res.getColor(R.color.colorYellow), PorterDuff.Mode.SRC_ATOP);
        colorButtons[4].setColorFilter(res.getColor(R.color.colorRed), PorterDuff.Mode.SRC_ATOP);

        Typeface typeMuro = Typeface.createFromAsset(getAssets(), "fonts/Muro.otf");
        ((TextView) findViewById(R.id.timeRemaining)).setTypeface(typeMuro);

        speed = 5; //will be passed as variable in future, not hardcoded
        time = 60000; //will be passed as variable in future, not hardcoded
        timeInterval = 1000; //will be passed as variable in future, not hardcoded

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        paintView = findViewById(R.id.paintView);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE);
        // Set the content to appear under the system bars so that the
        // content doesn't resize when the system bars hide and show.

        final Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        setCountdownTimer();

        // informes the paintView that it has to be updated
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                paintView.invalidate();
            }
        };
    }

    public Point getSize() {
        return size;
    }

    /**
     * Initializes the countdown to a given time.
     *
     * @return the countdown
     */
    private CountDownTimer setCountdownTimer() {
        return new CountDownTimer(time, timeInterval) {
            public void onTick(long millisUntilFinished) {
                TextView textView = findViewById(R.id.timeRemaining);
                textView.setText(Long.toString(millisUntilFinished / timeInterval));
            }

            public void onFinish() {
                TextView textView = findViewById(R.id.timeRemaining);
                textView.setTextSize(20);
                textView.setText("Time over!");
                stop();
            }
        }.start();
    }

    /**
     * Getter of the paint view.
     *
     * @return the paint view
     */
    public PaintView getPaintView() {
        return paintView;
    }

    /**
     * Clears the entire Path in paintView.
     *
     * @param view paintView
     */
    public void clear(View view) {
        paintView.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        // Register accelerometer sensor listener
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d(TAG, "We couldn't find the accelerometer on device.");
        }
    }

    /**
     * Fires when a sensor detected a change.
     *
     * @param sensorEvent the sensor that has changed
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        // we only use accelerometer
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updateValues(sensorEvent.values[0], sensorEvent.values[1]);
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //does nothing
    }

    /**
     * Called when accelerometer changed, circle coordinates are updated.
     *
     * @param coordinateX coordiate
     * @param coordinateY coordinate
     */
    public void updateValues(float coordinateX, float coordinateY) {
        float tempX = paintView.getCircleX();
        float tempY = paintView.getCircleY();

        tempX -= coordinateX * speed;
        tempY += coordinateY * speed;

        paintView.setCircle((int) tempX, (int) tempY);
    }

    /**
     * Gets called when time is over.
     * Saves drawing in database and storage and calls new activity.
     */
    private void stop() {
        LocalDbHandler localDbHandler = new LocalDbHandler(this, null, 1);
        paintView.saveCanvasInDb(localDbHandler);
        paintView.saveCanvasInStorage();
        // add redirection here
    }

    /**
     * Sets the clicked button to selected and sets the corresponding color.
     *
     * @param view the clicked view
     */
    public void colorClickHandler(View view) {
        int index = ArrayUtils.indexOf(colorButtons, view);
        paintView.setColor(index);
        colorButtons[index].setImageResource(R.drawable.color_circle_selected);

        for (int i = 0; i < colorButtons.length; i++) {
            if (i != index) {
                colorButtons[i].setImageResource(R.drawable.color_circle);
            }
        }
    }

    /**
     * Sets the clicked button to selected and sets the corresponding color.
     *
     * @param view the clicked view
     */
    public void toolClickHandler(View view) {
        switch (view.getId()) {
            case R.id.pencilButton:
                paintView.setPencil();
                pencilButton.setImageResource(R.drawable.pencil_selected);
                eraserButton.setImageResource(R.drawable.eraser);
                bucketButton.setImageResource(R.drawable.bucket);
                break;
            case R.id.eraserButton:
                paintView.setEraser();
                pencilButton.setImageResource(R.drawable.pencil);
                eraserButton.setImageResource(R.drawable.eraser_selected);
                bucketButton.setImageResource(R.drawable.bucket);
                break;
            case R.id.bucketButton:
                paintView.setBucket();
                pencilButton.setImageResource(R.drawable.pencil);
                eraserButton.setImageResource(R.drawable.eraser);
                bucketButton.setImageResource(R.drawable.bucket_selected);
                break;
            default:
        }
    }
}
