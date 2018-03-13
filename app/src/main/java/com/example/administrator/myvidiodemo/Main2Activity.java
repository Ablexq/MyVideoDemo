package com.example.administrator.myvidiodemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * VideoView的使用
 */

public class Main2Activity extends Activity {

    private VideoView videoView;
    private ImageView mIv;
    private TextView mProgress;
    private TextView totalTime;
    private SeekBar seekBar;

    //设置一个变量 判断当前是否在播放：防止退出应用后 handler还在发送消息
    //需要在onDestory()方法中配置
    private boolean isplay = false;

    //显示视频播放时间 显示方式 00：00
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    //接受消息并处理
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isplay) {
                //设置当前进度
                mProgress.setText(simpleDateFormat.format(new Date(videoView.getCurrentPosition())));
                //更新seekbar
                seekBar.setProgress(videoView.getCurrentPosition());
                //每隔0.01秒再发送一条消息 这样seekbar就能实时更新
                mhandler.sendEmptyMessageDelayed(0, 10);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isplay = true;
    }

    private void initView() {
        videoView = (VideoView) findViewById(R.id.video);
        mIv = ((ImageView) this.findViewById(R.id.image));
        mProgress = ((TextView) this.findViewById(R.id.progress));
        totalTime = ((TextView) this.findViewById(R.id.total));
        seekBar = ((SeekBar) this.findViewById(R.id.seekbar));

        String path = Environment.getExternalStorageDirectory().getPath() + "/" + "IMG_2948.mp4";//获取视频路径
        Log.e("Main2Activity", "path=======" + path);
        Uri uri = Uri.parse(path);//将路径转换成uri
        videoView.setVideoURI(uri);//为视频播放器设置视频路径

//        //系统自带的控制面板
//        MediaController mediaController = new MediaController(Main2Activity.this);
//        //使用系统自带的控制栏时需要将VideoView和控制栏进行双向绑定
//        videoView.setMediaController(mediaController);//显示控制栏
//        mediaController.setMediaPlayer(videoView);

        //获得视频的缩略图initImage(file)该方法返回一个bitmap
        File file = new File(path);
        Bitmap bitmap = initImage(file);
        //给ImageView设置bitmap
        mIv.setImageBitmap(bitmap);
        //设置ImageView的监听事件 当点击Imageview的时候把Imageview隐藏，然后播放视频
        mIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIv.setVisibility(View.GONE);
                videoView.start();
            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        } else {
                            videoView.start();
                        }
                        break;
                }
                //返回True代表事件已经处理了
                return true;
            }
        });

        //拖动seekbar改变播放进度
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //fromUser代表是否用户在点击 true  progress代表拖动的位置  进度
                if (fromUser) {
                    videoView.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //接收到ImageView传递过来的消息后设置Textview的值
                //设置视频总长 videoView.getDuration()为毫秒数需要转换
                //格式化时间方式1：
//                totalTime.setText(getShowTime(videoView.getDuration()));
                //格式化时间方式2：
                totalTime.setText(simpleDateFormat.format(new Date(videoView.getDuration())));

                //设置seekbar 的最大值
                seekBar.setMax(videoView.getDuration());
                //创建Handler 发送一条 空消息 通知seekbar 和 2个TextView视频播放了
                mhandler.sendEmptyMessage(0);
            }
        });
    }

    /**
     * 获取缩略图
     */
    private Bitmap initImage(File file) {
        //获得媒体文件信息的一个类
        MediaMetadataRetriever mediaretriever = new MediaMetadataRetriever();
        Bitmap bitmap = null;
        try {
            mediaretriever.setDataSource(this, Uri.parse(file.getAbsolutePath()));
            //任意时间 的一帧
            bitmap = mediaretriever.getFrameAtTime();
            return bitmap;
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        } finally {
            //销毁
            mediaretriever.release();
        }

        return null;
    }

    /**
     * 格式化总时长
     */
    public String getShowTime(long milliseconds) {
        // 获取日历函数
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat dateFormat = null;
        // 判断是否大于60分钟，如果大于就显示小时。设置日期格式
        if (milliseconds / 60000 > 60) {
            dateFormat = new SimpleDateFormat("hh:mm:ss");
        } else {
            dateFormat = new SimpleDateFormat("mm:ss");
        }
        return dateFormat.format(calendar.getTime());
    }
}