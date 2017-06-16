package com.haley.test.gogumamemo;

/**
 * Created by 202-18 on 2017-06-14.
 */

public class MemoListItem {
    private String[] mData;
    private String mId;
    private boolean mSelectable = true;

    // 생성자
    public MemoListItem(String itemId, String[] ob) {
        mId = itemId;
        mData = ob;
    }

    public MemoListItem(String memoId, String memoDate, String memoText,
                        String id_photo, String uri_photo) {
        mId = memoId;
        mData = new String[4];
        mData[0] = memoDate;
        mData[1] = memoText;
        mData[2] = id_photo;
        mData[3] = uri_photo;
    }

    // getter setter


    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean mSelectable) {
        this.mSelectable = mSelectable;
    }

    public String[] getData() {
        return mData;
    }

    public String getData(int index) {

        if(mData == null || index >= mData.length) {
            return null;
        }
        return mData[index];
    }

    public void setData(String[] mData) {
        this.mData = mData;
    }

    public int compareTo(MemoListItem other) {
        if(mData != null) {
            Object[] otherData = other.getData();
            if(mData.length == otherData.length) {
                for(int i = 0; i < mData.length; ++i) {
                    if(!mData[i].equals(otherData[i])) {
                        return -1;
                    }
                }
            } else {
                return -1;
            }
        } else {
            throw new IllegalArgumentException();
        }
        return 0;
    }
}
