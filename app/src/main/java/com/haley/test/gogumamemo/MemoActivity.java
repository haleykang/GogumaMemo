package com.haley.test.gogumamemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.haley.test.gogumamemo.db.MemoDatabase;

import java.io.File;


public class MemoActivity extends Activity {

    public static final String TAG = "MemoActivity";
    ListView mMemoListView;
    MemoListAdapter mMemoListAdapter;
    int mMemoCount = 0;
    public static MemoDatabase mDatabase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        // SD Card checking
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            longToast("SD 카드가 없습니다. SD 카드를 넣은 후 다시 실행하십시오.");
            return;
        } else {
            String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            if(!BasicInfo.ExternalChecked && externalPath != null) {
                BasicInfo.ExternalPath = externalPath + File.separator;
                myLog("ExternalPath : " + BasicInfo.ExternalPath);
                BasicInfo.FOLDER_PHOTO = BasicInfo.ExternalPath + BasicInfo.FOLDER_PHOTO;
                BasicInfo.DATABASE_NAME = BasicInfo.ExternalPath + BasicInfo.DATABASE_NAME;
                BasicInfo.ExternalChecked = true;
            }
        }

        // 메모 리스트
        mMemoListView = (ListView)findViewById(R.id.memoList);
        mMemoListAdapter = new MemoListAdapter(this);
        mMemoListView.setAdapter(mMemoListAdapter);
        mMemoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewMemo(position);
            }
        }); // end of mMemoListView.setOnItemClickListener

        // 새 메모 버튼 설정
        Button newMemoBtn = (Button)findViewById(R.id.newMemoBtn);
        newMemoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLog("newMemoBtn clicked");
                Intent intent = new Intent(getApplicationContext(), MemoInsertActivity.class);
                intent.putExtra(BasicInfo.KEY_MEMO_MODE, BasicInfo.MODE_INSERT);
                startActivityForResult(intent, BasicInfo.REQ_INSERT_ACTIVITY);
            }
        }); // newMemoBtn.setOnClickListener

        // 닫기 버튼 설정
        Button closeBtn = (Button)findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        checkDangerousPermissions();

    } // end of onCreate()

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(int i = 0; i < permissions.length; ++i) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if(permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        } // end of for

        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == 1) {
            for(int i = 0; i < permissions.length; ++i) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    protected void onStart() {
        openDatabase();
        loadMemoListData();
        super.onStart();
    }

    public void openDatabase() {
        if(mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
        mDatabase = MemoDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if(isOpen) {
            myLog("Memo database is open.");
        } else {
            myLog("Memo database is not open.");
        }
    }

    public int loadMemoListData() {
        String SQL = "select _id, INPUT_DATE, CONTENT_TEXT, ID_PHOTO from MEMO order by INPUT_DATE desc";
        int recordCount = -1;
        if(MemoActivity.mDatabase != null) {
            Cursor outCursor = MemoActivity.mDatabase.rawQuery(SQL);
            recordCount = outCursor.getCount();
            myLog("cursor count : " + recordCount + "\n");
            mMemoListAdapter.clear();
            Resources res = getResources();
            for(int i = 0; i < recordCount; ++i) {
                outCursor.moveToNext();
                String memoId = outCursor.getString(0);
                String dataStr = outCursor.getString(1);
                if(dataStr.length() > 10) {
                    dataStr = dataStr.substring(0, 10);
                }
                String memoStr = outCursor.getString(2);
                String photoId = outCursor.getString(3);
                String photoUriStr = getPhotoUriStr(photoId);
                mMemoListAdapter.addItem(
                        new MemoListItem(memoId, dataStr, memoStr, photoId, photoUriStr));

            }
            outCursor.close();
            mMemoListAdapter.notifyDataSetChanged();
        }
        return recordCount;
    } // loadMemoListData()

    // 사진 데이터 URI 가져오기
    public String getPhotoUriStr(String id_photo) {
        String photoUriStr = null;
        if(id_photo != null && !id_photo.equals("-1")) {
            String SQL = "select URI from " + MemoDatabase.TABLE_PHOTO
                    + " where _ID = " + id_photo + "";
            Cursor photoCursor = MemoActivity.mDatabase.rawQuery(SQL);
            if(photoCursor.moveToNext()) {
                photoUriStr = photoCursor.getString(0);
            }
            photoCursor.close();
        } else if(id_photo == null || id_photo.equals("-1")) {
            photoUriStr = "";
        }
        return photoUriStr;
    }

    private void viewMemo(int position) {
        MemoListItem item = (MemoListItem)mMemoListAdapter.getItem(position);
        // 메모 액티비티 띄우고
        Intent intent = new Intent(getApplicationContext(), MemoInsertActivity.class);
        intent.putExtra(BasicInfo.KEY_MEMO_MODE, BasicInfo.MODE_VIEW);
        intent.putExtra(BasicInfo.KEY_MEMO_ID, item.getId());
        intent.putExtra(BasicInfo.KEY_MEMO_DATE, item.getData(0));
        intent.putExtra(BasicInfo.KEY_MEMO_TEXT, item.getData(1));
        intent.putExtra(BasicInfo.KEY_ID_PHOTO, item.getData(2));
        intent.putExtra(BasicInfo.KEY_URI_PHOTO, item.getData(3));
        startActivityForResult(intent, BasicInfo.REQ_VIEW_ACTIVITY);
    }

    // 다른 액티비티 응답 처리
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case BasicInfo.REQ_INSERT_ACTIVITY:
                if(resultCode == RESULT_OK) {
                    loadMemoListData();
                    myLog("받는다");
                }
                break;
            case BasicInfo.REQ_VIEW_ACTIVITY:
                loadMemoListData();
                myLog("받는다233");
                break;
        }
    }


    public void longToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void myLog(String msg) {
        Log.d(TAG, msg);
    }

}
