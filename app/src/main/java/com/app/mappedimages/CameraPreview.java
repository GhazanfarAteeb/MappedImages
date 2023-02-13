package com.app.mappedimages;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;

//THIS CLASS IS RESPONSIBLE FOR CREATING THE CAMERA PREVIEW WHICH IS SHOWN ON THE SURFACE VIEW
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
//    CAMERA INSTANCE
    private Camera camera;
//    SURFACE VIEW HOLDER TO SHOW THE PREVIEW
    private SurfaceHolder holder;
    public CameraPreview(Context context, Camera camera) {
        super(context);
//        INITIALIZING THE HOLDER AND CAMERA AND ITS CALLBACKS
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
    }

    // WHEN THE SURFACE HOLDER IS CREATED THEN IT WILL START SHOWING THE CAMERA DISPLAY ON THE SURFACE VIEW
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("CameraPreview", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();

        } catch (Exception e) {
            Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
        }
    }

}
