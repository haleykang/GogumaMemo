package com.haley.test.gogumamemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;


/**
 * Created by 202-18 on 2017-06-14.
 */

public class PhotoCaptureActivity extends Activity {

    public static final String TAG = "PhotoCaptureActivity";
    CameraSurfaceView mCameraView;
    FrameLayout mFrameLayout;

    // 버튼 두 번 이상 누를 때 문제 해결
    boolean processing = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상태바, 타이틀 설정
        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.photo_capture_activity);

        mCameraView = new CameraSurfaceView(getApplicationContext());
        mFrameLayout = (FrameLayout)findViewById(R.id.frame);
        mFrameLayout.addView(mCameraView);
        setCaptureBtn();
    }

    // 사용자 정의 함수
    public void setCaptureBtn() {
        Button takeBtn = (Button)findViewById(R.id.capture_takeBtn);
        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!processing) {
                    processing = true;
                    mCameraView.capture(new CameraPictureCallback());
                }
            }
        });
    }

    // 카메라 찍기 버튼
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_CAMERA) {
            mCameraView.capture(new CameraPictureCallback());
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false;
    }

    // 내부 클래스
    class CameraPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.v(TAG, "onPictureTaked() called");
            int bitmapWidth = 480;
            int bitmapHeight = 360;
            Bitmap capturedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap scaledBitmap =
                    Bitmap.createScaledBitmap(capturedBitmap, bitmapWidth, bitmapHeight, false);
            Bitmap resultBitmap = null;
            Matrix matrix = new Matrix();
            matrix.postRotate(0);
            resultBitmap =
                    Bitmap.createBitmap(scaledBitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);

            try {
                File photoFolder = new File(BasicInfo.FOLDER_PHOTO);
                // 폴더가 없으면 폴더 생성
                if(!photoFolder.isDirectory()) {
                    Log.d(TAG, "creating photo folder : " + photoFolder);
                    photoFolder.mkdirs();
                }
                String photoName = "captured";
                // 기존 이미지가 있으면 삭제
                File file = new File(BasicInfo.FOLDER_PHOTO + photoName);
                if(file.exists()) {
                    file.delete();
                }
                FileOutputStream outputStream =
                        new FileOutputStream(BasicInfo.FOLDER_PHOTO + photoName);
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch(Exception e) {
                Log.e(TAG, "Error in writing captured image.", e);
                showDialog(BasicInfo.IMAGE_CANNOT_BE_STORED);
            }

            showParentActivity();


        } // end of public void onPictureTaken
    } // end of CameraPictureCallback

    // 부모 액티비티로 돌아가기
    private void showParentActivity() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    protected Dialog onCreateDialog(int id) {
        Log.d(TAG, "onCreateDialog() called");
        switch(id) {
            case BasicInfo.IMAGE_CANNOT_BE_STORED:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("사진을 저장할 수 없습니다. SD카드 상태를 확인하세요.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                return builder.create();

        }
        return null;
    }

} // end of PhotoCaptureActivity
