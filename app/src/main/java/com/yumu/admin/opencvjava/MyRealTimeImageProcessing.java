package com.yumu.admin.opencvjava;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by admin on 2017/3/9.
 */

public class MyRealTimeImageProcessing extends Activity {
    private CameraPreview cameraPreview;
    private ImageView MyCameraPreview=null;
    private FrameLayout frameLayout;
    private int PreviewSizeWidth=640;
    private int PreviewSizeHeight=480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cameralayout);

        MyCameraPreview=new ImageView(this);

        SurfaceView camView=new SurfaceView(this);
        SurfaceHolder camHolder=camView.getHolder();
        cameraPreview=new CameraPreview(PreviewSizeWidth,PreviewSizeHeight,MyCameraPreview);
        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
        frameLayout.addView(camView,new WindowManager.LayoutParams(PreviewSizeWidth,PreviewSizeHeight));
        frameLayout.addView(MyCameraPreview,new WindowManager.LayoutParams(PreviewSizeWidth,PreviewSizeHeight));
    }
    protected void onPause(){
        if(cameraPreview!=null){
            cameraPreview.onPause();
            super.onPause();
        }
    }
}
