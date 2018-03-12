package com.example.administrator.myvidiodemo;


import android.app.Activity;
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

    private VideoView video;
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
        video = (VideoView) findViewById(R.id.video1);
        btn = (Button) findViewById(R.id.btn1);
        et = (EditText) findViewById(R.id.et2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/" +
                        et.getText().toString();
                Log.e("Main3Activity", "path====" + path);
                Uri uri = Uri.parse(path);
                video.setVideoURI(uri);
                video.setMediaController(new MediaController(Main3Activity.this));

                //设置监听
                video.setOnPreparedListener(Main3Activity.this);
                video.setOnErrorListener(Main3Activity.this);
                video.setOnCompletionListener(Main3Activity.this);
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        video.start();
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
}