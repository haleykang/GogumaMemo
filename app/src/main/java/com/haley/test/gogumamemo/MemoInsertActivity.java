package com.haley.test.gogumamemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.haley.test.gogumamemo.db.MemoDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 202-18 on 2017-06-14.
 */

// 새 메모, 메모 보기 액티비티
public class MemoInsertActivity extends Activity {

    public static final String TAG = "MemoInsertActivity";
    EditText mMemoEdit;
    ImageView mPhoto;

    String mMemoMode;
    String mMemoId;
    String mMediaPhotoId;
    String mMediaPhotoUri;
    String tempPhotoUri;
    String mDateStr;
    String mMemoStr;

    Bitmap resultPhotoBitmap;
    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    Calendar mCalendar = Calendar.getInstance();

    Button insertDateButton;
    int mSelectedContentArray;
    int mChoicedArrayItem;

    Button titleBackgroundBtn;
    Button insertSaveBtn;
    Button delete_btn;
    Button insertCancelBtn;
    EditText insert_memoEdit;
    Animation translateLeftAnim;
    Animation translatRightAnim;

    // 재정의 함수
    // onCreate()
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_insert_activity);

        titleBackgroundBtn = (Button)findViewById(R.id.titleBackgroundBtn);
        mPhoto = (ImageView)findViewById(R.id.insert_photo);
        mMemoEdit = (EditText)findViewById(R.id.insert_memoEdit);
        translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translatRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);
        SlidingPageAnimationListener animListener =
                new SlidingPageAnimationListener();
        translateLeftAnim.setAnimationListener(animListener);
        translatRightAnim.setAnimationListener(animListener);

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(BasicInfo.CONTENT_PHOTO_EX);
                } else {
                    showDialog(BasicInfo.CONTENT_PHOTO);
                }
            }
        }); // end of mPhoto.setOnClickListener

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(BasicInfo.CONFIRM_DELETE);
            }
        }); // end of delete_btn.setOnClickListener

        setBottomButtons();
        setMediaLayout();
        setCalendar();
        Intent intent = getIntent();
        mMemoMode = intent.getStringExtra(BasicInfo.KEY_MEMO_MODE);
        if(mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {

            processIntent(intent);
            titleBackgroundBtn.setText("메모 보기");
            insertSaveBtn.setText("수정");
            delete_btn.setVisibility(View.VISIBLE);

        } else {
            titleBackgroundBtn.setText("새 메모");
            insertSaveBtn.setText("저장");
            delete_btn.setVisibility(View.GONE);
        }

    } // end of onCreate()

    private class SlidingPageAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    } // end of SlidingPageAnimationListener 클래스

    public void processIntent(Intent intent) {
        mMemoId = intent.getStringExtra(BasicInfo.KEY_MEMO_ID);
        mMemoEdit.setText(intent.getStringExtra(BasicInfo.KEY_MEMO_TEXT));
        mMediaPhotoId = intent.getStringExtra(BasicInfo.KEY_ID_PHOTO);
        mMediaPhotoUri = intent.getStringExtra(BasicInfo.KEY_URI_PHOTO);
        setMediaImage(mMediaPhotoId, mMediaPhotoUri);
    } // end of processIntent()

    public void setMediaImage(String photoId, String photoUri) {
        Log.d(TAG, "photoId : " + photoId + ", photoUri : " + photoUri);
        if(photoId.equals("") || photoId.equals("-1")) {
            mPhoto.setImageResource(R.drawable.person);
        } else {
            isPhotoFileSaved = true;
            mPhoto.setImageURI(Uri.parse(BasicInfo.FOLDER_PHOTO + photoUri));
        }
    } // end of setMediaImage()

    public void setBottomButtons() {
        insertSaveBtn = (Button)findViewById(R.id.insert_saveBtn);
        insertCancelBtn = (Button)findViewById(R.id.insert_cancelBtn);

        // 저장 버튼
        insertSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isParsed = parseValues();
                if(isParsed) {
                    if(mMemoMode.equals(BasicInfo.MODE_INSERT)) {
                        saveInput();
                    } else if(mMemoMode.equals(BasicInfo.MODE_MODIFY) ||
                            mMemoMode.equals(BasicInfo.MODE_VIEW)) {
                        modifyInput();
                    }
                }
            }
        });

        // 닫기 버튼
        insertCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    } // end of setBottomButtons()

    // 데이터 베이스에 레코드 추가
    private void saveInput() {

        String photoFilename = insertPhoto();
        int photoId = -1;
        String SQL = null;
        if(photoFilename != null) {
            SQL = "select _ID from " +
                    MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";
            Log.d(TAG, "SQL : " + SQL);
            if(MemoActivity.mDatabase != null) {
                Cursor cursor = MemoActivity.mDatabase.rawQuery(SQL);
                if(cursor.moveToNext()) {
                    photoId = cursor.getInt(0);
                }
                cursor.close();
            }
        }

        SQL = "insert into " + MemoDatabase.TABLE_MEMO +
                "(INPUT_DATE, CONTENT_TEXT, ID_PHOTO) values(" +
                "DATETIME('" + mDateStr + "'), " +
                "'" + mMemoStr + "', " +
                "'" + photoId + "')";
        Log.d(TAG, "SQL : " + SQL);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    } // end of saveInput()

    /**
     * 데이터베이스 레코드 수정 -> 여기서 부터 다시 입력하기
     */
    private void modifyInput() {
        Intent intent = getIntent();
        String photoFilename = insertPhoto();
        int photoId = -1;
        String SQL = null;
        if(photoFilename != null) {
            SQL = "select _ID from " + MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";
            Log.d(TAG, "SQL : " + SQL);
            if(MemoActivity.mDatabase != null) {
                Cursor cursor = MemoActivity.mDatabase.rawQuery(SQL);
                if(cursor.moveToNext()) {
                    photoId = cursor.getInt(0);
                }
                cursor.close();
                mMediaPhotoUri = photoFilename;
                SQL = "update " + MemoDatabase.TABLE_MEMO + " set "
                        + " ID_PHOTO = '" + photoId + "'"
                        + " where _id = '" + mMemoId + "'";
                if(MemoActivity.mDatabase != null) {
                    MemoActivity.mDatabase.rawQuery(SQL);
                }
                mMediaPhotoId = String.valueOf(photoId);
            } // end of if(MemoActivity.mDatabase != null)
        } else if(isPhotoCanceled && isPhotoFileSaved) {
            SQL = "delete from " + MemoDatabase.TABLE_PHOTO
                    + " where _ID = '" + mMediaPhotoId + "'";
            Log.d(TAG, "SQL : " + SQL);
            if(MemoActivity.mDatabase != null) {
                MemoActivity.mDatabase.execSQL(SQL);
            }
            File photoFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
            if(photoFile.exists()) {
                photoFile.delete();
            }
            SQL = "update " + MemoDatabase.TABLE_MEMO
                    + " set "
                    + " ID_PHOTO = '" + photoId + "'"
                    + " where _id = '" + mMemoId + "'";
            if(MemoActivity.mDatabase != null) {
                MemoActivity.mDatabase.rawQuery(SQL);
            }
            mMediaPhotoId = String.valueOf(photoId);
        }

        // update memo info
        SQL = "update " + MemoDatabase.TABLE_MEMO +
                " set " +
                " INPUT_DATE = DATETIME('" + mDateStr + "'), " +
                " CONTENT_TEXT = '" + mMemoStr + "'" +
                " where _id = '" + mMemoId + "'";
        Log.d(TAG, "SQL : " + SQL);
        if(MemoActivity.mDatabase != null) {
            MemoActivity.mDatabase.execSQL(SQL);
        }
        intent.putExtra(BasicInfo.KEY_MEMO_TEXT, mMemoStr);
        intent.putExtra(BasicInfo.KEY_ID_PHOTO, mMediaPhotoId);
        intent.putExtra(BasicInfo.KEY_URI_PHOTO, mMediaPhotoUri);
        setResult(RESULT_OK, intent);
        finish();

    } // end of modifyInput()

    /*
        앨범의 사진을 사진 폴더에 복사한 후, PICTURE 테이블에 사진 정보 추가
        이미지의 이름은 현재 시간을 기준으로 한 getTime() 값 문자열 사용
     */
    private String insertPhoto() {

        String photoName = null;
        if(isPhotoCaptured) {
            try {
                if(mMemoMode != null && mMemoMode.equals(BasicInfo.MODE_MODIFY)) {
                    Log.d(TAG, "previous photo is newly created for modify mode.");
                    String SQL = "delete from " + MemoDatabase.TABLE_PHOTO +
                            " where _ID = '" + mMediaPhotoId + "'";
                    Log.d(TAG, "SQL : " + SQL);
                    if(MemoActivity.mDatabase != null) {
                        MemoActivity.mDatabase.execSQL(SQL);
                    }
                    File previousFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
                    if(previousFile.exists()) {
                        previousFile.delete();
                    }
                }
                File photoFolder = new File(BasicInfo.FOLDER_PHOTO);
                // 폴더가 없으면 폴더 생성
                if(!photoFolder.isDirectory()) {
                    Log.d(TAG, "creating photo folder : " + photoFolder);
                    photoFolder.mkdirs();
                }
                // 파일 이름 설정
                photoName = createFilename();
                FileOutputStream outstream = new FileOutputStream(BasicInfo.FOLDER_PHOTO + photoName);
                resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                outstream.close();
                if(photoName != null) {
                    Log.d(TAG, "isCaptured: " + isPhotoCaptured);
                    // INSERT PICTURE INFO
                    String SQL = "insert into " + MemoDatabase.TABLE_PHOTO +
                            "(URI) values(" + "'" + photoName + "')";
                    if(MemoActivity.mDatabase != null) {
                        MemoActivity.mDatabase.execSQL(SQL);
                    }
                }

            } catch(IOException e) {
                Log.d(TAG, "Exception in copying photo : " + e.toString());
            }
        }
        return photoName;
    } // end of insertPhoto()

    private void deleteMemo() {
        // 포토 데이터 베이스 삭제
        Log.d(TAG, "사진기록과 파일 삭제 : " + mMediaPhotoId);
        String SQL = "delete from " + MemoDatabase.TABLE_PHOTO +
                " where _ID = '" + mMediaPhotoId + "'";
        Log.d(TAG, "SQL : " + SQL);
        if(MemoActivity.mDatabase != null) {
            MemoActivity.mDatabase.execSQL(SQL);
        }
        File photoFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
        if(photoFile.exists()) {
            photoFile.delete();
        }
        Log.d(TAG, "메모기록 삭제 : " + mMemoId);
        SQL = "delete from " + MemoDatabase.TABLE_MEMO +
                "where _id = '" + mMemoId + "'";
        Log.d(TAG, "SQL : " + SQL);
        if(MemoActivity.mDatabase != null) {
            MemoActivity.mDatabase.execSQL(SQL);
        }
        setResult(RESULT_OK);
        finish();
    } // end of deleteMemo()

    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());
        return curDateStr;
    } // createFilename()

    public void setMediaLayout() {
        isPhotoCaptured = false;
    }

    private void setCalendar() {
        insertDateButton = (Button)findViewById(R.id.insert_dateBtn);
        insertDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mDateStr = insertDateButton.getText().toString();
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                try {
                    date = BasicInfo.dateDayNameFormat.parse(mDateStr);
                } catch(Exception e) {
                    Log.d(TAG, "Exception in parsing date : " + date);
                }
                calendar.setTime(date);
                new DatePickerDialog(
                        MemoInsertActivity.this,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
        Date curDate = new Date();
        mCalendar.setTime(curDate);
        int year = mCalendar.get(Calendar.YEAR);
        int monthOfYear = mCalendar.get(Calendar.MONTH);
        int dayOfmonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        insertDateButton.setText(year + "년 " + (monthOfYear + 1) + "월 " + dayOfmonth + "일");
    } // end of setCalendar()

    /*
        날짜 설정 리스너
     */
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(year, month, dayOfMonth);
            insertDateButton.setText(year + "년 " + (month + 1) + "월 " + dayOfMonth + "일");
        }
    };

    // 일자와 메모 확인
    private boolean parseValues() {
        String insertDateStr = insertDateButton.getText().toString();
        try {
            Date insertDate = BasicInfo.dateDayNameFormat.parse(insertDateStr);
            mDateStr = BasicInfo.dateDayFormat.format(insertDate);
        } catch(java.text.ParseException e) {
            Log.e(TAG, "Exception in parsing date : " + e);
        }
        String memotxt = mMemoEdit.getText().toString();
        mMemoStr = memotxt;
        if(mMemoStr.trim().length() < 1) {
            showDialog(BasicInfo.CONFIRM_TEXT_INPUT);
            return false;
        }
        return true;
    } // end of parseValues()

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = null;
        switch(id) {
            case BasicInfo.CONFIRM_TEXT_INPUT:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("메모");
                builder.setMessage("텍스트를 입력하세요.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
            case BasicInfo.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(this);
                mSelectedContentArray = R.array.array_photo;
                builder.setTitle("선택하세요");
                builder.setSingleChoiceItems(mSelectedContentArray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoicedArrayItem = which;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mChoicedArrayItem == 0) {
                            showPhotoCaptureActivity();
                        } else if(mChoicedArrayItem == 1) {
                            showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "whichButton3 ===== " + which);
                    }
                });
                break;
            case BasicInfo.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(this);
                mSelectedContentArray = R.array.array_photo_ex;
                builder.setTitle("선택하세요");
                builder.setSingleChoiceItems(mSelectedContentArray, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoicedArrayItem = which;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mChoicedArrayItem == 0) {
                            showPhotoCaptureActivity();
                        } else if(mChoicedArrayItem == 1) {
                            showPhotoSelectionActivity();
                        } else if(mChoicedArrayItem == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;
                            mPhoto.setImageResource(R.drawable.person_add);

                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
            case BasicInfo.CONFIRM_DELETE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("메모");
                builder.setMessage("메모를 삭제하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMemo();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismissDialog(BasicInfo.CONFIRM_DELETE);
                    }
                });
                break;
            default:
                break;
        }
        return builder.create();
    } // end of onCreateDialog()

    public void showPhotoCaptureActivity() {

        Intent intent = new Intent(getApplicationContext(), PhotoCaptureActivity.class);
        startActivityForResult(intent, BasicInfo.REQ_PHOTO_CAPTURE_ACTIVITY);

    } // end of showPhotoCaptureActivity

    public void showPhotoSelectionActivity() {
        Intent intent = new Intent(getApplicationContext(), PhotoSelectionActivity.class);
        startActivityForResult(intent, BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY);
    }

    // 다른 액티비티로부터의 응답 처리
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode) {
            case BasicInfo.REQ_PHOTO_CAPTURE_ACTIVITY:
                Log.d(TAG, "onActivityResult() for REQ_PHOTO_CAPTURE_ACTIVITY.");
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "resultCode : " + resultCode);
                    boolean isPhotoExists = checkCapturedPhotoFile();
                    if(isPhotoExists) {
                        Log.d(TAG, "image file exists : " + BasicInfo.FOLDER_PHOTO + "captured");
                        resultPhotoBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_PHOTO + "captured");
                        tempPhotoUri = "captured";
                        mPhoto.setImageBitmap(resultPhotoBitmap);
                        isPhotoCaptured = true;
                        mPhoto.invalidate();
                    } else {
                        Log.d(TAG, "image file doesn't exists : " + BasicInfo.FOLDER_PHOTO + "captured");
                    }
                }
                break;
            case BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY: // 사진을 앨범에서 선택하는 경우
                Log.d(TAG, "onActivityResult() for REQ_PHOTO_SELECTION_ACTIVITY.");
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "resultCode : " + resultCode);
                    Uri getPhotoUri = intent.getParcelableExtra(BasicInfo.KEY_URI_PHOTO);
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        resultPhotoBitmap =
                                BitmapFactory.decodeStream(
                                        getContentResolver().openInputStream(getPhotoUri), null, options);

                    } catch(FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    mPhoto.setImageBitmap(resultPhotoBitmap);
                    isPhotoCaptured = true;
                    mPhoto.invalidate();
                }
                break;
        }

    } // end of onActivityResult()

    // 저장된 사진 파일 확인
    private boolean checkCapturedPhotoFile() {
        File file = new File(BasicInfo.FOLDER_PHOTO + "captured");
        if(file.exists()) {
            return true;
        }
        return false;
    }

} // end of MemoInsertActivity 클래스
