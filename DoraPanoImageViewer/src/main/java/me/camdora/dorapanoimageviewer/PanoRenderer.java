package me.camdora.dorapanoimageviewer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by edwin on 20/05/2017.
 */

public class PanoRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "PanoRenderer";
    public static final int VIEW_MODE_NORMAL = 0;
    public static final int VIEW_MODE_FISHEYE = 1;
    public static final int VIEW_MODE_LITTLE_PLANET = 2;

    private static final float DEFAULT_FOV_LITTLE_PLANET = 120.0f;

    private static final float DEFAULT_FOV_NORMAL = 70.0f;

    private static final float DEFAULT_FOV_FISHEYE = 70.0f;

    private static final float Z_ANGLE_MIN_FISHEYE = -90;
    private static final float Z_ANGLE_MAX_FISHEYE = 90;

    private static final float Z_ANGLE_MIN_LITTLE_PLANET = 0;
    private static final float Z_ANGLE_MAX_LITTLE_PLANET = 180;

    private static final float Z_ANGLE_MIN_NORMAL = -90;
    private static final float Z_ANGLE_MAX_NORMAL = 90;

    private final Context mContext;
    private int mCurrentViewMode = VIEW_MODE_NORMAL;
    private Sphere mSphere;
    private String mImagePath;
    private float mXAngle = 0;
    private float mZAngle = 0;
    private float mZAngleMin = Z_ANGLE_MIN_NORMAL;
    private float mZAngleMax = Z_ANGLE_MAX_NORMAL;
    private float mFov = DEFAULT_FOV_NORMAL;
    private float mRatio = 1.0f;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mLookAtMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    public PanoRenderer() {
        this(null, 0);
    }

    public PanoRenderer(Context context) {
        this(context, 0);
    }

    public PanoRenderer(Context context, int viewMode) {
        super();
        mContext = context;
        mCurrentViewMode = viewMode;
    }



    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSphere = new Sphere(mImagePath, mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mRatio = (float) width / height;
        this.initMatrix();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        draw();
    }

    private void draw() {
        mSphere.draw(mProjectionMatrix, mLookAtMatrix, mRotationMatrix);
    }

    public void initMatrix() {
        Matrix.setIdentityM(mRotationMatrix, 0);
        switch (mCurrentViewMode) {
            case VIEW_MODE_FISHEYE:
                Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
                break;
            case VIEW_MODE_LITTLE_PLANET:
                Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
                break;
            case VIEW_MODE_NORMAL:
            default:
                Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
                break;
        }
        Matrix.perspectiveM(mProjectionMatrix, 0, mFov, mRatio, 0.01f, 10.0f);
    }

    public void setAngles(float x, float y) {
        mXAngle += x;
        mZAngle += y;
        if (mZAngle <= mZAngleMin) {
            mZAngle -= y;
            y = 0;
        }
        if (mZAngle >= mZAngleMax) {
            mZAngle -= y;
            y = 0;
        }
        Matrix.rotateM(mRotationMatrix, 0, x, 0.0f,1.0f,0);
        Matrix.rotateM(mLookAtMatrix, 0, y, 1.0f,0.0f,0.0f);
    }

    public void rotate(float[] matrix) {
        Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrix, 0, matrix, 0);
    }

    public void setFov(float scaleFactor) {
        float fov = mFov - 40.0f * scaleFactor + 40.0f;
        Matrix.perspectiveM(mProjectionMatrix, 0, fov, mRatio, 0.01f, 10.0f);
    }

    public void switchViewMode(int viewMode) {
        mCurrentViewMode = viewMode;
        switch (mCurrentViewMode) {
            case VIEW_MODE_FISHEYE:
                mZAngleMin = Z_ANGLE_MIN_FISHEYE;
                mZAngleMax = Z_ANGLE_MAX_FISHEYE;
                Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
                mFov = DEFAULT_FOV_FISHEYE;
                break;
            case VIEW_MODE_LITTLE_PLANET:
                mZAngleMin = Z_ANGLE_MIN_LITTLE_PLANET;
                mZAngleMax = Z_ANGLE_MAX_LITTLE_PLANET;
                Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f);
                mFov = DEFAULT_FOV_LITTLE_PLANET;
                break;
            case VIEW_MODE_NORMAL:
            default:
                mZAngleMin = Z_ANGLE_MIN_NORMAL;
                mZAngleMax = Z_ANGLE_MAX_NORMAL;
                Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f);
                mFov = DEFAULT_FOV_NORMAL;
                break;
        }
        setFov(1.0f);
        mZAngle = 0;
    }
}

