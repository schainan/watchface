package com.schainan.watchface;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.schainan.watchface.font.TypefaceManager;
import com.schainan.watchface.font.TypefaceSpan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class WatchFaceActivity extends Activity implements SurfaceHolder.Callback,
        ConnectionCallbacks, OnConnectionFailedListener, DataApi.DataListener,
        MessageApi.MessageListener {

    private static final String TAG = "WatchFaceActivity";
    private static final String SECOND_HAND_COLOR_KEY = "secondhandcolor";
    private GoogleApiClient mGoogleApiClient;

    private static int CENTER;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;

    private Paint mTenMinutePaint;
    private Paint mHourPaint;

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSecondHandPaint;

    private Paint mCirclePaint;
    private Paint mBatteryPaint;
    private Paint mFaintBatteryPaint;

    private int mColor = -1;

    private Timer mTimer = new Timer("seconds");


    SimpleDateFormat df = new SimpleDateFormat("LLL d");

    private final float MINOR_TICK_WIDTH = 2f;
    private final float MAJOR_TICK_WIDTH = 2f;

    private final float HOUR_HAND_WIDTH = 7f;

    private final float MINUTE_HAND_WIDTH = 5f;

    private final float SECOND_HAND_WIDTH = 3f;

    private int mBatteryLevel = -1;


//    public BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context arg0, Intent intent) {
//            onDraw();
//        }
//    };

    @Override
    protected void onResume() {
        super.onResume();

        if (mTimer == null) {
            mTimer = new Timer("seconds");
        }

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onDraw();
            }
        }, 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimer.cancel();
        mTimer = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mSurfaceView = (SurfaceView) stub.findViewById(R.id.surfaceView);
                mSurfaceView.getHolder().addCallback(WatchFaceActivity.this);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        TypefaceManager.init(this);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mBatInfoReceiver.onReceive(this, registerReceiver(null, batteryFilter));
        registerReceiver(this.mBatInfoReceiver, batteryFilter);

        updatePaints();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBatInfoReceiver);

        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        onDraw();
    }

    private void updatePaints() {
        mTenMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTenMinutePaint.setAntiAlias(true);
        mTenMinutePaint.setColor(0x44FFFFFF);
        mTenMinutePaint.setStyle(Style.STROKE);
        mTenMinutePaint.setStrokeWidth(MINOR_TICK_WIDTH);

        mHourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourPaint.setAntiAlias(true);
        mHourPaint.setColor(0xDDFFFFFF);
        mHourPaint.setStyle(Style.STROKE);
        mHourPaint.setStrokeWidth(MAJOR_TICK_WIDTH);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setAntiAlias(true);
        mHourHandPaint.setColor(0xFFEEEEEE);
        mHourHandPaint.setStyle(Style.STROKE);
        mHourHandPaint.setStrokeCap(Cap.ROUND);
        mHourHandPaint.setStrokeWidth(HOUR_HAND_WIDTH);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setAntiAlias(true);
        mMinuteHandPaint.setColor(Color.WHITE);
        mMinuteHandPaint.setStyle(Style.STROKE);
        mMinuteHandPaint.setStrokeCap(Cap.ROUND);
        mMinuteHandPaint.setStrokeWidth(MINUTE_HAND_WIDTH);

        if (mColor == -1) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            mColor = prefs.getInt(SECOND_HAND_COLOR_KEY, getResources().getColor(R.color.secondhand_yellow));
        }

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setAntiAlias(true);
        mSecondHandPaint.setColor(mColor);
        mSecondHandPaint.setStyle(Style.STROKE);
        mSecondHandPaint.setStrokeCap(Cap.ROUND);
        mSecondHandPaint.setStrokeWidth(SECOND_HAND_WIDTH);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.WHITE);
        mCirclePaint.setStyle(Style.FILL);

        mBatteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setColor(Color.WHITE);
        mBatteryPaint.setStyle(Style.STROKE);
        mBatteryPaint.setStrokeWidth(1.5f);

        mFaintBatteryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFaintBatteryPaint.setAntiAlias(true);
        mFaintBatteryPaint.setColor(0x40FFFFFF);
        mFaintBatteryPaint.setStyle(Style.STROKE);
        mFaintBatteryPaint.setStrokeWidth(MINOR_TICK_WIDTH);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCanvas = null;
    }

    private void onDraw() {
        if (mSurfaceHolder == null) return;
        mCanvas = mSurfaceHolder.lockCanvas();
        if (mCanvas == null) return;

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        mCanvas.drawColor(Color.BLACK);

        Path path = new Path();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        CENTER = size.x / 2;

        int tickStart = getResources().getDimensionPixelSize(R.dimen.tick_start);
        int tickLength = getResources().getDimensionPixelSize(R.dimen.tick_length);

        int tickStartMinor = getResources().getDimensionPixelSize(R.dimen.tick_start_minor);
        int tickLengthMinor = getResources().getDimensionPixelSize(R.dimen.tick_length_minor);
        for (int i = 0; i < 12; i++) {
            float rotate = i * 30; // degrees

            // Draw hour indicators
            path.reset();
            path.moveTo(CENTER, CENTER + tickStart);
            path.lineTo(CENTER, CENTER + tickStart + tickLength);
            mCanvas.save();
            mCanvas.rotate(rotate, CENTER, CENTER);
            mCanvas.drawPath(path, mHourPaint);
            mCanvas.restore();
        }

        int tickmarksPerTenMinutes = 5;

        for (int i = 0; i < tickmarksPerTenMinutes * 12; i++) {
            float rotate = i * 30f / tickmarksPerTenMinutes;

            if (((int) rotate) % 30 == 0) continue;

            // Draw hour indicators
            path.reset();
            path.moveTo(CENTER, CENTER + tickStartMinor);
            path.lineTo(CENTER, CENTER + tickStartMinor + tickLengthMinor);
            mCanvas.save();
            mCanvas.rotate(rotate, CENTER, CENTER);
            mCanvas.drawPath(path, mTenMinutePaint);
            mCanvas.restore();
        }

        // Battery
        if (mBatteryLevel >= 0) {
            int radius = getResources().getDimensionPixelSize(R.dimen.battery_radius);
            int offset = getResources().getDimensionPixelSize(R.dimen.battery_offset);
            RectF batteryOval = new RectF(CENTER + offset, CENTER + offset, CENTER + offset + radius, CENTER + offset + radius);
            path.reset();
            path.moveTo(CENTER + offset, CENTER + offset);
            path.addArc(batteryOval, 270, mBatteryLevel * 3.6f);
            if (mBatteryLevel > 90) {
                mBatteryPaint.setColor(0xFF5CC12F);
            } else if (mBatteryLevel <= 10) {
                mBatteryPaint.setColor(0xFFFF3C2D);
            } else {
                mBatteryPaint.setColor(Color.WHITE);
            }
            mCanvas.drawPath(path, mBatteryPaint);

            path.reset();
            path.moveTo(CENTER + offset, CENTER + offset);
            path.addArc(batteryOval, 270, 360);
            mCanvas.drawPath(path, mFaintBatteryPaint);
        }

        // Now for the hands!

        float whiteCircleRadius = 8.5f;
        float yellowCircleRadius = 6f;
        float blackCircleRadius = 1.5f;

        mCirclePaint.setColor(Color.WHITE);
        RectF whiteCircle = new RectF(CENTER - whiteCircleRadius, CENTER - whiteCircleRadius, CENTER + whiteCircleRadius, CENTER + whiteCircleRadius);
        mCanvas.drawOval(whiteCircle, mCirclePaint);

        int hourHandLength = getResources().getDimensionPixelSize(R.dimen.hour_hand_length);
        int minuteHandLength = getResources().getDimensionPixelSize(R.dimen.minute_hand_length);
        int secondHandLength = getResources().getDimensionPixelSize(R.dimen.second_hand_length);
        int secondHandMinorLength = getResources().getDimensionPixelSize(R.dimen.second_hand_minor_length);
        float hourRotate = 180 + 0.5f * (60 * hour + minute);
        path.reset();
        path.moveTo(CENTER, CENTER);
        path.lineTo(CENTER, CENTER + hourHandLength);
        mCanvas.save();
        mCanvas.rotate(hourRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mHourHandPaint);
        mCanvas.restore();


        float minuteRotate = 180 + ((6f * minute + (second / 10f)));
        path.reset();
        path.moveTo(CENTER, CENTER);
        path.lineTo(CENTER, CENTER + minuteHandLength);
        mCanvas.save();
        mCanvas.rotate(minuteRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mMinuteHandPaint);
        mCanvas.restore();


        float secondRotate = 180 + second * 6;
        path.reset();
        path.moveTo(CENTER, CENTER - secondHandMinorLength);
        path.lineTo(CENTER, CENTER + secondHandLength);
        mCanvas.save();
        mCanvas.rotate(secondRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mSecondHandPaint);
        mCanvas.restore();

        mCirclePaint.setColor(mColor);
        RectF yellowCircle = new RectF(CENTER - yellowCircleRadius, CENTER - yellowCircleRadius, CENTER + yellowCircleRadius, CENTER + yellowCircleRadius);
        mCanvas.drawOval(yellowCircle, mCirclePaint);


        mCirclePaint.setColor(Color.BLACK);
        RectF blackCircle = new RectF(CENTER - blackCircleRadius, CENTER - blackCircleRadius, CENTER + blackCircleRadius, CENTER + blackCircleRadius);
        mCanvas.drawOval(blackCircle, mCirclePaint);

//        Log.d("asdf", "hour: " + hour + " " + hourRotate);
//        Log.d("asdf", "minute: " + minute + " " + minuteRotate);
//        Log.d("asdf", "second: " + second + " " + secondRotate);

        mSurfaceHolder.unlockCanvasAndPost(mCanvas);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvDate = (TextView) findViewById(R.id.tvDate);
                String date = df.format(new Date());
                String[] parts = date.split(" ");
                // bold month
                SpannableStringBuilder sb = new SpannableStringBuilder(date);
                if (parts[0] != null) {
                    sb.setSpan(new TypefaceSpan(TypefaceManager.get().getTypefaceRegular()), 0, parts[0].length(), 0);
                }

                TypefaceManager.get().setTypefaceLight(tvDate);
                tvDate.setText(sb);
            }
        });
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//            Log.d("asdf", "battery: " + mBatteryLevel);
            onDraw();
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
        if ("/color".equals(messageEvent.getPath())) {
            byte[] data = messageEvent.getData();
            mColor = byteArrayToColor(data);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putInt(SECOND_HAND_COLOR_KEY, mColor).commit();
            updatePaints();
            onDraw();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    private int byteArrayToColor(byte[] data) {
        String colorValue = new String(data);
        return Integer.parseInt(colorValue);
    }
}
