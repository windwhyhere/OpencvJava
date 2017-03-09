package com.yumu.admin.opencvjava;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {
     private  final String TAG="main";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private Mat originalMat;
    private Bitmap currentBitmap;
    private Bitmap originalBitmap;
    private EditText editText;
    private SeekBar seekBar;
    private Button play;
    private  Button pause;
    private  Button replay;
    private  Button stop;
    private Button mCamera;
    private MediaPlayer mediaPlayer;
    private  boolean isplaying;
    private SurfaceView surfaceView;
    private  int currPosition=0;

    private BaseLoaderCallback mLoaderCallerBack=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=(EditText)findViewById(R.id.edit);
        seekBar=(SeekBar)findViewById(R.id.seek_bar);
        play=(Button)findViewById(R.id.play);
        pause=(Button)findViewById(R.id.pause);
        replay=(Button)findViewById(R.id.replay);
        stop=(Button)findViewById(R.id.stop);
        mCamera=(Button)findViewById(R.id.camera);
        surfaceView=(SurfaceView)findViewById(R.id.sv);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        replay.setOnClickListener(this);
        stop.setOnClickListener(this);
        mCamera.setOnClickListener(this);

        surfaceView.getHolder().addCallback(callback);


        seekBar.setOnSeekBarChangeListener(this);
        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
//        main();

    }
    private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
               Log.i(TAG,"surfaceHoldeer is created");
              if(currPosition>0){
                  play(currPosition);
                  currPosition=0;
              }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
               Log.i(TAG,"surfacedHolder is changed");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
               Log.i(TAG,"surfaceHolder is destoryed");
               if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                     currPosition=mediaPlayer.getCurrentPosition();
                   mediaPlayer.stop();
               }
        }
    };


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void framevideo();
    public native void capturevideo();
    public native int main();
//    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                play(0);
                break;
            case R.id.pause:
                pause();
                break;
            case R.id.replay:
                replay();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.camera:
                photo();
            default:
                break;
        }
    }

    private void photo() {
        Intent intent=new Intent(this,MyRealTimeImageProcessing.class);
        startActivity(intent);
    }

    private void replay() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(0);
            Toast.makeText(this,"重新播放",Toast.LENGTH_SHORT).show();
            pause.setText("暂停");
            return;
        }
        isplaying=false;
        play(0);
    }

    private void pause() {
        if(pause.getText().toString().trim().equals("继续")){
            pause.setText("暂停");
            mediaPlayer.start();
            Toast.makeText(this,"继续播放",Toast.LENGTH_SHORT).show();
            return;

        }
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            pause.setText("继续");
            Toast.makeText(this,"暂停播放",Toast.LENGTH_SHORT).show();
        }
    }

    private void play(final int msec) {
        String path=editText.getText().toString().trim();
        File file=new File(path);
        if(!file.exists()){
            Toast.makeText(this,"视频文件路径错误",Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            mediaPlayer=new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setDisplay(surfaceView.getHolder());
            Log.i(TAG,"开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG,"装载完毕");
                    mediaPlayer.start();
                    mediaPlayer.seekTo(msec);
                    seekBar.setMax(mediaPlayer.getDuration());
                    new Thread(){
                        public void run(){
                            isplaying=true;
                            while (isplaying){
                                try {
                                    int current=mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(current);
                                    sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                    play.setEnabled(false);
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play.setEnabled(true);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    play(0);
                    isplaying=false;
                    return  false;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void  stop(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
            play.setEnabled(true);
            isplaying=false;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress=seekBar.getProgress();
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(progress);
        }
    }
}
