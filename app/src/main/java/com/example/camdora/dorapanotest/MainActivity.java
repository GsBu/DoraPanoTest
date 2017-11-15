package com.example.camdora.dorapanotest;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import me.camdora.dorapanoimageviewer.PanoSurfaceView;

public class MainActivity extends AppCompatActivity {

    private PanoSurfaceView mPanoSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPanoSurfaceView = (PanoSurfaceView)findViewById(R.id.dora_pano_surface_view);

        String imagePath = Environment.getExternalStorageDirectory().getPath() +
                File.separator + "DCIM" + File.separator + "Camera" +
                File.separator + "test.jpg";
        mPanoSurfaceView.setImagePath(imagePath);
    }
}
