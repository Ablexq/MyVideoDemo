package com.example.administrator.myvidiodemo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //SurfaceView是一个在其他线程中显示、更新画面的组件，专门用来完成在单位时间内大量画面变化的需求
    private SurfaceView surfaceView;//能够播放图像的控件
    private SeekBar seekBar;//进度条
    private String path;//本地文件路径
    private SurfaceHolder surfaceHolder;//SurfaceHolder接口为一个显示界面内容的容器
    private MediaPlayer mediaPlayer;//媒体播放器
    private Button playBtn;//播放按钮
    private Timer timer;//定时器
    private TimerTask timerTask;//定时器任务
    private int curPosition = 0;
    private EditText editText;

    private String url1 = "http://flashmedia.eastday.com/newdate/news/2016-11/shznews1125-19.mp4";
    private String url2 = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";
    private String url3 = "http://42.96.249.166/live/388.m3u8";
    private String url4 = "http://61.129.89.191/ThroughTrain/download.html?id=4035&flag=-org-"; //音频url
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mIsVideoSizeKnown;
    private boolean mIsVideoReadyToBePlayed;
    private TextView mTime;
    private String videoTimeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    //初始化控件，并且为进度条和图像控件添加监听
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.sfv);
        seekBar = (SeekBar) findViewById(R.id.sb);
        playBtn = (Button) findViewById(R.id.play);
        editText = (EditText) findViewById(R.id.et);
        mTime = ((TextView) findViewById(R.id.time));
        playBtn.setEnabled(false);

        surfaceHolder = surfaceView.getHolder();//得到显示界面内容的容器
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //设置 surfaceView点击监听
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            } else {
                                mediaPlayer.start();
                            }
                        }
                        break;
                }
                //返回True代表事件已经处理了
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 设置当前播放时间
                mTime.setText(getShowTime(progress) + "/" + videoTimeString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //当进度条停止拖动的时候，把媒体播放器的进度跳转到进度条对应的进度
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //为了避免图像控件还没有创建成功，用户就开始播放视频，造成程序异常，所以在创建成功后才使播放按钮可点击
                Log.e("MainActivity", "surfaceCreated");
                playBtn.setEnabled(true);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e("MainActivity", "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //当程序没有退出，但不在前台运行时，因为surfaceview很耗费空间，所以会自动销毁，
                // 这样就会出现当你再次点击进程序的时候点击播放按钮，声音继续播放，却没有图像
                //为了避免这种不友好的问题，简单的解决方式就是只要surfaceview销毁，我就把媒体播放器等
                //都销毁掉，这样每次进来都会重新播放，当然更好的做法是在这里再记录一下当前的播放位置，
                //每次点击进来的时候把位置赋给媒体播放器，很简单加个全局变量就行了。
                Log.e("MainActivity", "surfaceDestroyed");
                if (mediaPlayer != null) {
                    curPosition = mediaPlayer.getCurrentPosition();
                    stop();
                }
            }
        });
    }

    private void play() {

        playBtn.setEnabled(false);//在播放时不允许再点击播放按钮

        if (isPause) {//如果是暂停状态下播放，直接start
            isPause = false;
            mediaPlayer.start();
            return;
        }

        path = Environment.getExternalStorageDirectory().getPath() + "/";
        path = path + editText.getText().toString();//sdcard的路径加上文件名称是文件全路径
        Log.e("MainActivity", "path====" + path);

        File file = new File(path);
        if (!file.exists()) {//判断需要播放的文件路径是否存在，不存在退出播放流程
            Toast.makeText(this, "文件路径不存在", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(path);
//            mediaPlayer.setDataSource(url1);//file-path or http/rtsp URL
//            mediaPlayer.setDataSource(this, Uri.parse(url1));//Sets the data source as a content Uri

            //MediaPlayer主要用于播放音频，没有提供图像输出界面，
            //所以我们需要借助其他的组件来显示MediaPlayer播放的图像输出，我们可以使用SurfaceView来显示。
            //把视频画面输出到SurfaceView
            mediaPlayer.setDisplay(surfaceHolder);//将影像播放控件与媒体播放控件关联起来

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {//视频播放完成后，释放资源
                    Log.e("MainActivity", "onCompletion");
                    playBtn.setEnabled(true);
                    stop();
                }
            });

            //视频宽高总是等于定义的SurfaceView布局宽高，所以视频可能会被拉伸变形。解决方法：
            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    Log.e("MainActivity", "onVideoSizeChanged");

                    if (width == 0 || height == 0) {
                        Log.e("MainActivity", "invalid video width(" + width + ") or height(" + height + ")");
                        return;
                    }
                    mIsVideoSizeKnown = true;
                    mVideoWidth = width;
                    mVideoHeight = height;
                    if (mIsVideoReadyToBePlayed) {
                        startVideoPlayback();
                    }
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.e("MainActivity", "onPrepared");

                    mIsVideoReadyToBePlayed = true;
                    if (mIsVideoSizeKnown) {
                        startVideoPlayback();
                    }


                }
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startVideoPlayback() {
        surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        //媒体播放器就绪后，设置进度条总长度，开启计时器不断更新进度条，播放视频
        seekBar.setMax(mediaPlayer.getDuration());
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int time = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(time);
                }
            }
        };
        timer.schedule(timerTask, 0, 500);
        seekBar.setProgress(curPosition);
        mediaPlayer.seekTo(curPosition);

        videoTimeString = getShowTime(mediaPlayer.getDuration());
        mTime.setText("00:00:00/" + videoTimeString);

        mediaPlayer.start();
    }

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

    public void play(View v) {
        play();
        Log.e("MainActiviy", path);
    }

    private boolean isPause;

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
            playBtn.setEnabled(true);
        }
    }

    public void pause(View v) {
        pause();
    }

    private void replay() {
        isPause = false;
        if (mediaPlayer != null) {
            stop();
            play();
        }
    }

    public void replay(View v) {
        replay();
    }

    private void stop() {
        isPause = false;
        if (mediaPlayer != null) {
            seekBar.setProgress(0);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            if (timer != null) {
                timer.cancel();
            }
            playBtn.setEnabled(true);
        }
    }

    public void stop(View v) {
        stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }
}