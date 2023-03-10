package com.app.mappedimages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;

import com.app.mappedimages.databinding.ActivityCameraBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    Camera camera;
    CameraPreview cameraPreview;
    ActivityCameraBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // THIS WILL CHECK THE READ AND WRITE PERMISSIONS AND WILL REQUEST FOR PERMISSIONS GRANTING
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        else {
//            THIS FUNCTION WILL OPEN THE CAMERA
            openCamera();
        }


    }

    private void openCamera() {
        // INITIALIZING THE CAMERA INSTANCE
        camera = getCameraInstance();
        if(camera != null) {
            // SETTING UP THE CAMERA SURFACE PROPERLY
            camera.setDisplayOrientation(90);
        }
        else {
            Log.d("CAMERA", "camera is null");
        }
        // INITIALIZING THE DEFINED CAMERA PREVIEW CLASS
        cameraPreview = new CameraPreview(this, camera);
        binding.cameraPreview.addView(cameraPreview);
        // WHEN THE CAMERA SCREEN IS CLICKED THIS WILL AUTO FOCUS THE CAMERA
        binding.cameraPreview.setOnClickListener(view -> {
            Camera.AutoFocusCallback autoFocus = (success, camera) -> {
                if (success) {
                    Log.d("AutoFocus", "Focus success");
                } else {
                    Log.d("AutoFocus", "Focus failed");
                }
            };
            camera.autoFocus(autoFocus);
        });
        // WHEN THE CAMERA BUTTON IS CLICKED IT WILL CAPTURE THE IMAGE
        binding.fabCapturePicture.setOnClickListener(view -> {
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    // WHEN THE PICTURE IS TAKEN IT WILL BE SAVED IN THE GALLARY
                    Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                    File file = writeImage(data);
                    System.out.println(file.toURI());
                    intent.putExtra(Constants.KEY_IMAGE, file.toURI().toString());
                    startActivity(intent);
                }
            });
        });
    }

    // FUNCTION TO OPEN AND INITIALIZING THE CAMERA
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                    openCamera();
                } else {
                    // Permission has been denied
                }
                return;
            }
        }
    }

    // WRITING OF FILE TO THE LOCAL STORAGE WITH THE CURRENT SYSTEM TIMESTAMP AS THE NAME WITH THE PREFIX IMG_
    private File writeImage(byte[] data) {
        Bitmap image = BitmapFactory.decodeByteArray(data, 0 , data.length);
        String fileName = "IMG_"+ System.currentTimeMillis()+".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            runOnUiThread(() -> image.compress(Bitmap.CompressFormat.JPEG, 100, fos));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}