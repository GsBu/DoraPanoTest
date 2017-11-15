package me.camdora.dorapanoimageviewer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.opengl.GLSurfaceView;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

/**
 * Created by edwin on 20/05/2017.
 */

public class PanoSurfaceView extends GLSurfaceView implements GestureDetector.OnGestureListener {
    private static final String TAG = "PanoSurfaceView";
    private final PanoRenderer mRenderer;

    public PanoSurfaceView(Context context) {
        this(context, null);
    }

    public PanoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRenderer = new PanoRenderer(context);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mDetector = new GestureDetectorCompat(context, this);
    }

    private float mPreviousX;
    private float mPreviousY;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mDetector;
    private float mScaleFactor = 1.0f;


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e2.getPointerCount() <= 1) {
            mRenderer.setAngles((float)Math.toDegrees(distanceX / Math.PI / 100), (float)Math.toDegrees(-distanceY / Math.PI / 100));
            requestRender();
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.f));
            mRenderer.setFov(mScaleFactor);
            requestRender();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);
        mDetector.onTouchEvent(e);
        return true;
    }

    public void setImagePath(String imagePath) {
        mRenderer.setImagePath(imagePath);
    }

    public void switchMode(int mode) {
        mRenderer.switchViewMode(mode);
        requestRender();
    }
}



