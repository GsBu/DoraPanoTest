package me.camdora.dorapanoimageviewer.test;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by camdora on 17-11-13.
 */

public class MySurfaceView extends GLSurfaceView {
    public MySurfaceView(Context context) {
        super(context);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(new MyRenderer(context));
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
