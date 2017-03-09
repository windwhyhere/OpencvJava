package com.yumu.admin.opencvjava;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.IOException;
import java.security.Policy;

/**
 * Created by admin on 2017/3/9.
 */

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    private ImageView MyCameraPreview=null;
    private Bitmap bitmap=null;
    private int[]pixes=null;
    private byte[]FrameData=null;
    private int imageFormat;
    private int PreviewSizeWidth;
    private int PreviewSizeHeight;
    private  boolean bProcessing=false;


    Handler mHandler=new Handler(Looper.getMainLooper());
    public CameraPreview(int PreviewlayoutWidth,int PreviewlayoutHeight,ImageView CameraPreview){
        PreviewSizeWidth=PreviewlayoutWidth;
        PreviewSizeHeight=PreviewlayoutHeight;
        MyCameraPreview=CameraPreview;
        bitmap=Bitmap.createBitmap(PreviewSizeWidth,PreviewSizeHeight, Bitmap.Config.ARGB_8888);
        pixes=new int[PreviewSizeWidth*PreviewSizeHeight];
    }
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
         if(imageFormat== ImageFormat.NV21){
             if(!bProcessing){
                 FrameData=data;
                 mHandler.post(DoImageProcessing);
             }
         }
    }
    public void onPause(){
        mCamera.stopPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
       mCamera=Camera.open();  //6.0以上需要在app里面开启权限（相机）
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            mCamera.release();
            mCamera=null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters;
        parameters=mCamera.getParameters();
        parameters.setPreviewSize(PreviewSizeWidth,PreviewSizeHeight);
        imageFormat=parameters.getPreviewFormat();
        mCamera.setParameters(parameters);
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera=null;
    }
    public native boolean ImageProcessing(int width,int height,byte[]Nv21FrameData,int []pixels);
    static {
      System.loadLibrary("native-lib");
    }
    private Runnable DoImageProcessing=new Runnable() {
        @Override
        public void run() {
            Log.i("MyRealTimeImage","DoImageProcesing:");
            bProcessing=true;
            ImageProcessing(PreviewSizeWidth,PreviewSizeHeight,FrameData,pixes);

            bitmap.setPixels(pixes,0,PreviewSizeWidth,0,0,PreviewSizeWidth,PreviewSizeHeight);
            MyCameraPreview.setImageBitmap(bitmap);
            bProcessing=false;
        }
    };
}
