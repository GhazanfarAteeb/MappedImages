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

// THIS ACTIVITY IS RESPONSIBLE FOR THE PREVIEWING OF THE CAPTURED IMAGE
public class PreviewActivity extends AppCompatActivity {
    String imageData;
    ActivityPreviewBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        THE IMAGE URI WILL BE PASSED WHEN THE IMAGE IS CLICKED BY THE USER THEN THE CROPPER LIBRARY
//        WILL SHOW THE IMAGE WITH THE GUIDELINES
        imageData = getIntent().getStringExtra(Constants.KEY_IMAGE);
        CropImage.activity(Uri.parse(imageData))
                .setGuidelines(CropImageView.Guidelines.ON).start(PreviewActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // THIS FUNCTION WILL GET THE RESULT CODE OF THE CROPPER WHEN THE CROP WILL BE CLICKED
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            // IN CASE OF THE RESULT CODE OK THEN THE URI WILL BE PASSED AFTER SETTING DATA IN THE INTENT TO THE MAPS MAIN ACTIVITY
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent intent = new Intent(PreviewActivity.this, MapsActivityMain.class);
                intent.setData(resultUri);
                startActivity(intent);
                // IF THERE IS ANY ERROR OR CANCELLATION BY THE USER THEN THE FOLLOWING FUNCTION WILL RUN
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                errorOrCancel();
            }
            else if (resultCode == RESULT_CANCELED) {
                errorOrCancel();
            }
        }
    }
    // THIS WILL DELETE THE CAPTURED IMAGE BT THE USER
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