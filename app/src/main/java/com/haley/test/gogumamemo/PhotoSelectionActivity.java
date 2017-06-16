package com.haley.test.gogumamemo;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by 202-18 on 2017-06-14.
 */

// 앨범에서 사진 선택 액티비티
public class PhotoSelectionActivity extends Activity {

    public static final String TAG = "PhotoSelectionActivity";
    TextView mSelectPhotoText;
    Uri mAlbumPhotoUri; // 앨범 사진에서 선택한 URI
    Bitmap resultPhotoBitmap = null;
    CoverFlow mPhotoCallery;
    public static int spacing = -45; // 갤러리 간격
    ImageView mSelectedPhotoImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection_activity);
        setBottomBtns();
        setSelectPhotoLayout();
        Log.d(TAG, "Loading gallery data...");
        mPhotoCallery = (CoverFlow)findViewById(R.id.loading_gallery);
        Cursor c =
                getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null,
                        MediaStore.Images.Media.DATE_TAKEN + " DESC"); // 이미지 데이터 쿼리
        PhotoCursorAdapter adapter = new PhotoCursorAdapter(this, c);
        mPhotoCallery.setAdapter(adapter);
        mPhotoCallery.setSpacing(spacing);
        mPhotoCallery.setSelection(2, true);
        mPhotoCallery.setAnimationDuration(3000);
        Log.d(TAG, "Count of gallery images : " + mPhotoCallery.getCount());
        mPhotoCallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // 개별 이미지에 대한 URI 생성
                    Uri uri =
                            ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    mAlbumPhotoUri = uri; // 앨범에서 이미지를 선택한 URI
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    resultPhotoBitmap =
                            BitmapFactory.decodeStream(
                                    getContentResolver().openInputStream(uri), null, options);

                    Log.d(TAG, "Selected image URI from Album : " + mAlbumPhotoUri);
                    mSelectPhotoText.setVisibility(View.GONE);
                    mSelectedPhotoImage.setImageBitmap(resultPhotoBitmap);
                    ;
                    mSelectedPhotoImage.setVisibility(View.VISIBLE);

                } catch(Exception e) {
                    Log.d(TAG, e.toString());
                }
            } // end of public void onItemClick
        }); // end of mPhotoCallery.setOnItemClickListener
    } // end of onCreate()


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            MediaScannerConnection.scanFile(
                    this, new String[]
                            { Environment.getExternalStorageDirectory().getAbsolutePath() },
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d(TAG, "Media scan completed");
                        }
                    });
        }
    } // end of onWindowFocusChanged()

    private void setBottomBtns() {

        Button loading_okBtn = (Button)findViewById(R.id.loading_okBtn);
        loading_okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showParentActivity();
            }
        });

        Button loading_cancelBtn = (Button)findViewById(R.id.loading_cancelBtn);
        loading_cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    } // end of setBottomBtns()

    public void setSelectPhotoLayout() {
        mSelectPhotoText = (TextView)findViewById(R.id.loading_selectPhotoText);
        mSelectedPhotoImage = (ImageView)findViewById(R.id.loading_selectedPhoto);
        mSelectedPhotoImage.setVisibility(View.VISIBLE);
    } // end of setSelectPhotoLayout()

    public static int getBitmapOfWidth(String fileName) {

        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            return options.outWidth;

        } catch(Exception e) {
            Log.d(TAG, e.toString());
            return 0;
        }
    } // end of getBitmapOfWidth()

    class PhotoCursorAdapter extends CursorAdapter {

        int mGalleryItemBackground;

        // 생성자
        public PhotoCursorAdapter(Context context, Cursor c) {
            super(context, c);
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
            mGalleryItemBackground =
                    a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();
        } // end of PhotoCursorAdapter 생성자

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView img = (ImageView)view;
            long id = cursor.getLong
                    (cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            Log.d(TAG, "id ->" + id + ", uri ->" + uri);

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Bitmap bm = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(uri), null, options);
                img.setImageBitmap(bm);

            } catch(Exception e) {
                e.printStackTrace();
            }
        } // end of bindView()

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ImageView v = new ImageView(context);
            v.setLayoutParams(new Gallery.LayoutParams(220, 160));
            v.setBackgroundResource(mGalleryItemBackground);
            return v;
        } // end of newView()
    } // end of PhotoCursorAdapter class

    // 부모 액티비티로 돌아가기
    private void showParentActivity() {
        Intent intent = getIntent();
        if(mAlbumPhotoUri != null && resultPhotoBitmap != null) {
            intent.putExtra(BasicInfo.KEY_URI_PHOTO, mAlbumPhotoUri);
            setResult(RESULT_OK, intent);
        }
        finish();
    } // end of showParentActivity()

} // end ofPhotoSelectionActivity
