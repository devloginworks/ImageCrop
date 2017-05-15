package com.loginworks.imagecropdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.loginworks.imagecropdemo.ImageProcessor.Direction.HORIZONTAL;
import static com.loginworks.imagecropdemo.ImageProcessor.Direction.NINTY;
import static com.loginworks.imagecropdemo.ImageProcessor.Direction.VERTICAL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int REQUEST_PERMISSIONS = 121;
    private ImageView resultView;
    private ImageProcessor imageProcessor;
    private FloatingActionButton iv_create;
    private ImageView iv_rotateLeft;
    private ImageView iv_rotateRight;
    private ImageView iv_Mirror;
    private ImageView iv_Crop;
    private RelativeLayout rel_tool;
    private TextView tv_select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (ImageView) findViewById(R.id.result_image);
        imageProcessor = new ImageProcessor();

        rel_tool = (RelativeLayout) findViewById(R.id.rel_tool);
        tv_select = (TextView) findViewById(R.id.tv_select);

//        iv_create = (ImageView) findViewById(R.id.image_create);
//        iv_create.setOnClickListener(this);

        iv_create = (FloatingActionButton) findViewById(R.id.edit_float);
        iv_create.setOnClickListener(this);

        iv_rotateLeft = (ImageView) findViewById(R.id.image_rotateLeft);
        iv_rotateLeft.setOnClickListener(this);

        iv_rotateRight = (ImageView) findViewById(R.id.image_rotateRight);
        iv_rotateRight.setOnClickListener(this);

        iv_Mirror = (ImageView) findViewById(R.id.image_mirror);
        iv_Mirror.setOnClickListener(this);

        iv_Crop = (ImageView) findViewById(R.id.image_crop);
        iv_Crop.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= 23) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            resultView.setImageDrawable(null);
            resultView.setImageURI(Crop.getOutput(result));

            rel_tool.setVisibility(View.VISIBLE);
            tv_select.setVisibility(View.GONE);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_float:
                resultView.setImageDrawable(null);
                Crop.pickImage(MainActivity.this);
                break;
            case R.id.image_rotateLeft:
                Bitmap bitmap = ((BitmapDrawable) resultView.getDrawable()).getBitmap();
                Bitmap rotateImage = imageProcessor.resizeAndRotate(1f, bitmap, VERTICAL);
                resultView.setImageBitmap(rotateImage);
                break;
            case R.id.image_rotateRight:
                Bitmap bitmap2 = ((BitmapDrawable) resultView.getDrawable()).getBitmap();
                Bitmap rightBitmap = imageProcessor.resizeAndRotate(1f, bitmap2, NINTY);
                resultView.setImageBitmap(rightBitmap);
                break;
            case R.id.image_mirror:
                Bitmap bitmap1 = ((BitmapDrawable) resultView.getDrawable()).getBitmap();
                Bitmap rotateImage1 = imageProcessor.resizeAndRotate(1f, bitmap1, HORIZONTAL);
                resultView.setImageBitmap(rotateImage1);
                break;
            case R.id.image_crop:

                if (LocalUtilty.hasPermissions(this, LocalUtilty.PERMISSIONS)) {
                    Bitmap bitmapCrop = ((BitmapDrawable) resultView.getDrawable()).getBitmap();
                    Uri source = getImageUri(getApplicationContext(), bitmapCrop);
                    beginCrop(source);
                } else {

                    int hasReadExternalPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                    if (hasReadExternalPermission != PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("You need to allow read storeage")
                                    .setPositiveButton("OK", null)
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                        }
                    }
                    ActivityCompat.requestPermissions(this, LocalUtilty.PERMISSIONS, REQUEST_PERMISSIONS);
                }
                break;

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
            return Uri.parse(path);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Uri.parse("");
        }
    }

}
