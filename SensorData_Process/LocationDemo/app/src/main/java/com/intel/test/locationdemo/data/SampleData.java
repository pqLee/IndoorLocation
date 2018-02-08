package com.intel.test.locationdemo.data;

/**
 * Created by qixing on 17-8-3.
 */

public class SampleData {

//    public int mId;
    public String mTag;
    public float mX;
    public float mY;
    public float mZ;


    public SampleData(){

    }
    public SampleData(float x, float y, float z,  String tag){

        mX = x;
        mY = y;
        mZ = z;
        mTag = tag;
    }


    public String getmTag(){
        return mTag;
    }
    public void setmTag(String tag){
        this.mTag = tag;
    }


    public float getmX(){
        return mX;
    }
    public void setmX(float x){
        this.mX = x;
    }
    public float getmY(){
        return mY;
    }
    public void setmY(float y){
        this.mY = y;
    }

    public float getmZ(){
        return mZ;
    }
    public void setmZ(float z){
        this.mZ = z;
    }
}
