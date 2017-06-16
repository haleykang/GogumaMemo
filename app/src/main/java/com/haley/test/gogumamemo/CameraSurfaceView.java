package com.haley.test.gogumamemo;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.hardware.camera2.*;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder.Callback;

/**
 * Created by 202-18 on 2017-06-14.
 */

// 카메라 미리보기를 위한 서피스 뷰

public class CameraSurfaceView extends SurfaceView implements Callback {

    // 1. 전역 변수
    public static final String TAG = "CameraSurfaceView";
    private SurfaceHolder mHolder;
    private Camera mCamera = null;

    // 2. 생성자 함수
    public CameraSurfaceView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 3. 재정의 함수
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.startPreview();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
    }

    // 4. 사용자 정의 함수
    public Surface getSurface() {
        return mHolder.getSurface();
    }

    public boolean capture(Camera.PictureCallback jpegHandler) {
        if(mCamera != null) {
            mCamera.takePicture(null, null, jpegHandler);
            return true;
        } else {
            return false;
        }
    }

    public void stopPreview() {
        mCamera.startPreview();
        mCamera.release();
        mCamera = null;
    }

    public void startPreview() {
        openCamera();
        mCamera.startPreview();
    }

    public void openCamera() {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch(Exception e) {
            Log.e(TAG, "Failed to set camera preview display", e);
        }
    }


} // end of class
