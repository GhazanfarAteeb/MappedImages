package com.app.mappedimages;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.app.mappedimages.databinding.ActivityPreviewBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class PreviewActivity extends AppCompatActivity {
    String imageData;
    ActivityPreviewBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        imageData = getIntent().getStringExtra(Constants.KEY_IMAGE);
        CropImage.activity(Uri.parse(imageData))
                .setGuidelines(CropImageView.Guidelines.ON).start(PreviewActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent intent = new Intent(PreviewActivity.this, MapsActivityMain.class);
                intent.setData(resultUri);
                startActivity(intent);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                errorOrCancel();
            }
            else if (resultCode == RESULT_CANCELED) {
                errorOrCancel();
            }
        }
    }
    private void errorOrCancel() {
        File file = new File(URI.create(imageData));
        if(file.delete()) {
            System.out.println("File deleted "+ file.toURI());
        }
        else {
            System.out.println("File deletion failed");
        }
        onBackPressed();
    }
}