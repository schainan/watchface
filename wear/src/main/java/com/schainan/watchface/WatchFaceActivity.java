package com.schainan.watchface;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WatchFaceActivity extends Activity implements SurfaceHolder.Callback {

    private static final int CENTER = 160;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;

    private Paint mTenMinutePaint;
    private Paint mHourPaint;

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSecondHandPaint;

    private Timer mTimer = new Timer("seconds");

    private static final int TICK_START = 120;
    private static final int TICK_LENGTH = 30;
    private static final float MINOR_TICK_WIDTH = 1f;
    private static final float MAJOR_TICK_WIDTH = 2f;
    private static final int HOUR_HAND_LENGTH = 70;
    private static final float HOUR_HAND_WIDTH = 8f;
    private static final int MINUTE_HAND_LENGTH = 95;
    private static final float MINUTE_HAND_WIDTH = 5f;
    private static final int SECOND_HAND_LENGTH = 92;
    private static final float SECOND_HAND_WIDTH = 3f;


    public BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            onDraw();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

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

//        mTimeInfoReceiver.onReceive(this, registerReceiver(null, INTENT_FILTER_TIME));
//        registerReceiver(mTimeInfoReceiver, INTENT_FILTER_TIME);

        mTenMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTenMinutePaint.setAntiAlias(true);
        mTenMinutePaint.setColor(0xAAFFFFFF);
        mTenMinutePaint.setStyle(Style.STROKE);
        mTenMinutePaint.setStrokeWidth(MINOR_TICK_WIDTH);

        mHourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourPaint.setAntiAlias(true);
        mHourPaint.setColor(Color.WHITE);
        mHourPaint.setStyle(Style.STROKE);
        mHourPaint.setStrokeWidth(MAJOR_TICK_WIDTH);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setAntiAlias(true);
        mHourHandPaint.setColor(Color.WHITE);
        mHourHandPaint.setStyle(Style.STROKE);
        mHourHandPaint.setStrokeCap(Cap.ROUND);
        mHourHandPaint.setStrokeWidth(HOUR_HAND_WIDTH);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setAntiAlias(true);
        mMinuteHandPaint.setColor(Color.WHITE);
        mMinuteHandPaint.setStyle(Style.STROKE);
        mMinuteHandPaint.setStrokeCap(Cap.ROUND);
        mMinuteHandPaint.setStrokeWidth(MINUTE_HAND_WIDTH);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setAntiAlias(true);
        mSecondHandPaint.setColor(0xFFF9E31B);
        mSecondHandPaint.setStyle(Style.STROKE);
        mSecondHandPaint.setStrokeCap(Cap.ROUND);
        mSecondHandPaint.setStrokeWidth(SECOND_HAND_WIDTH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeInfoReceiver);
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

        for (int i = 0; i < 12; i++) {
            float rotate = i * 30; // degrees

            // Draw hour indicators
            path.reset();
            path.moveTo(CENTER, CENTER + TICK_START);
            path.lineTo(CENTER, CENTER + TICK_START + TICK_LENGTH);
            mCanvas.save();
            mCanvas.rotate(rotate, CENTER, CENTER);
            mCanvas.drawPath(path, mHourPaint);
            mCanvas.restore();
        }

        for (int i = 0; i < 60; i++) {
            float rotate = i * 6;

            if (((int) rotate) % 30 == 0) continue;

            // Draw hour indicators
            path.reset();
            path.moveTo(CENTER, CENTER + TICK_START);
            path.lineTo(CENTER, CENTER + TICK_START + TICK_LENGTH);
            mCanvas.save();
            mCanvas.rotate(rotate, CENTER, CENTER);
            mCanvas.drawPath(path, mTenMinutePaint);
            mCanvas.restore();
        }

        // Now for the hands!

        float hourRotate = 180 + 0.5f * (60 * hour + minute);
        path.reset();
        path.moveTo(CENTER, CENTER);
        path.lineTo(CENTER, CENTER + HOUR_HAND_LENGTH);
        mCanvas.save();
        mCanvas.rotate(hourRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mHourHandPaint);
        mCanvas.restore();


        float minuteRotate = 180 + minute * 6;
        path.reset();
        path.moveTo(CENTER, CENTER);
        path.lineTo(CENTER, CENTER + MINUTE_HAND_LENGTH);
        mCanvas.save();
        mCanvas.rotate(minuteRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mMinuteHandPaint);
        mCanvas.restore();


        float secondRotate = 180 + second * 6;
        path.reset();
        path.moveTo(CENTER, CENTER);
        path.lineTo(CENTER, CENTER + SECOND_HAND_LENGTH);
        mCanvas.save();
        mCanvas.rotate(secondRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mSecondHandPaint);
        mCanvas.restore();

//        Log.d("asdf", "hour: " + hour + " " + hourRotate);
//        Log.d("asdf", "minute: " + minute + " " + minuteRotate);
//        Log.d("asdf", "second: " + second + " " + secondRotate);


        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }
}
