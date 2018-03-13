package com.example.administrator.myvidiodemo;


import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by Administrator on 2018/3/12.
 */

public class Main3Activity extends Activity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private VideoView videoView;
    private Button btn;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //一定要初始化
        Vitamio.initialize(this);

        initView();
    }

    private void initView() {
        videoView = (VideoView) findViewById(R.id.video1);
        btn = (Button) findViewById(R.id.btn1);
        et = (EditText) findViewById(R.id.et2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/" +
                        et.getText().toString();
                Log.e("Main3Activity", "path====" + path);
                Uri uri = Uri.parse(path);
                videoView.setVideoURI(uri);

                MediaController mediaController = new MediaController(Main3Activity.this);
                videoView.setMediaController(mediaController);
                mediaController.setMediaPlayer(videoView);

                //设置监听
                videoView.setOnPreparedListener(Main3Activity.this);
                videoView.setOnErrorListener(Main3Activity.this);
                videoView.setOnCompletionListener(Main3Activity.this);
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.start();
        Log.e("Main3Activity", "====onPrepared====");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("Main3Activity", "====onCompletion====");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("Main3Activity", "====onError====");
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //屏幕切换时，设置全屏
        if (videoView != null) {
            videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        }
        super.onConfigurationChanged(newConfig);
    }

}