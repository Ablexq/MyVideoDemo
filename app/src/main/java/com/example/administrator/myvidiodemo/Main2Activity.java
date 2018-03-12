package com.example.administrator.myvidiodemo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by Administrator on 2018/3/12.
 */

public class Main2Activity extends Activity {

    private VideoView videoView;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initView();
    }

    private void initView() {
        et = (EditText) findViewById(R.id.et1);
        videoView = (VideoView) findViewById(R.id.video);
        Button btn = (Button) findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/" +
                        et.getText().toString();//获取视频路径
                Log.e("Main2Activity", "path=======" + path);
                Uri uri = Uri.parse(path);//将路径转换成uri
                videoView.setVideoURI(uri);//为视频播放器设置视频路径
                videoView.setMediaController(new MediaController(Main2Activity.this));//显示控制栏
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        videoView.start();//开始播放视频
                    }
                });
            }
        });
    }
}