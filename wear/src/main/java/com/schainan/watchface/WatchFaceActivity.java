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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.Calendar;

public class WatchFaceActivity extends Activity implements SurfaceHolder.Callback {

    private static final IntentFilter INTENT_FILTER_TIME;
    private static final int CENTER = 160;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;

    private Paint mTenMinutePaint;
    private Paint mHourPaint;

    private Paint mHourHandPaint;

    static {
        INTENT_FILTER_TIME = new IntentFilter();
        INTENT_FILTER_TIME.addAction(Intent.ACTION_TIME_TICK);
        INTENT_FILTER_TIME.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        INTENT_FILTER_TIME.addAction(Intent.ACTION_TIME_CHANGED);
    }

    public BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            onDraw();
        }
    };

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

        mTimeInfoReceiver.onReceive(this, registerReceiver(null, INTENT_FILTER_TIME));
        registerReceiver(mTimeInfoReceiver, INTENT_FILTER_TIME);

        mTenMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTenMinutePaint.setAntiAlias(true);
        mTenMinutePaint.setColor(0xAAFFFFFF);
        mTenMinutePaint.setStyle(Style.STROKE);
        mTenMinutePaint.setStrokeWidth(1f);


        mHourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourPaint.setAntiAlias(true);
        mHourPaint.setColor(Color.WHITE);
        mHourPaint.setStyle(Style.STROKE);
        mHourPaint.setStrokeWidth(2f);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setAntiAlias(true);
        mHourHandPaint.setColor(Color.WHITE);
        mHourPaint.setStyle(Style.STROKE);
        mHourHandPaint.setStrokeCap(Cap.ROUND);
        mHourPaint.setStrokeWidth(4f);
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

        int hourPathStart = 120;
        int hourPathLength = 30;

        for (int i = 0; i < 12; i++) {
            float rotate = i * 30; // degrees

            // Draw hour indicators
            path.reset();
            path.moveTo(CENTER, CENTER + hourPathStart);
            path.lineTo(CENTER, CENTER + hourPathStart + hourPathLength);
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
            path.moveTo(CENTER, CENTER + hourPathStart);
            path.lineTo(CENTER, CENTER + hourPathStart + hourPathLength);
            mCanvas.save();
            mCanvas.rotate(rotate, CENTER, CENTER);
            mCanvas.drawPath(path, mTenMinutePaint);
            mCanvas.restore();
        }


        // Now for the hands!

        float hourRotate = 0.5f * (60 * hour + minute);
        path.reset();
        path.moveTo(CENTER, CENTER);
        path.lineTo(CENTER, CENTER + 40);
        mCanvas.save();
        mCanvas.rotate(hourRotate, CENTER, CENTER);
        mCanvas.drawPath(path, mHourHandPaint);
        mCanvas.restore();




        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }
}
