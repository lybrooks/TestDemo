package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class MediaPlayerActivity extends AppCompatActivity {
    SurfaceView mSvVideoPlayer;

    private MediaPlayer mMediaPlayer;
    private int mPosition = 0;
    private boolean hasActiveHolder = false;
    private SeekBar mSb_main_bar;

    private Timer timer;//定时器
    private boolean isSeekbarChaning;
    private ImageButton mIbcontrol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        mSvVideoPlayer = findViewById(R.id.sfv);
        mSb_main_bar = findViewById(R.id.sb_main_bar);
        mIbcontrol = findViewById(R.id.ib_control);
        initListener();

        playVideo();
    }

    private void initListener() {
        mIbcontrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        timer.cancel();
                        timer=null;
                        mMediaPlayer.pause();
                    } else {
                        mMediaPlayer.start();
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (!isSeekbarChaning) {
                                    mSb_main_bar.setProgress(mMediaPlayer.getCurrentPosition());
                                }
                            }
                        }, 0, 50);
                    }

                }
            }
        });

        mSb_main_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("main","onStartTrackingTouch");
                mMediaPlayer.pause();
                isSeekbarChaning = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("main","onStopTrackingTouch");
                isSeekbarChaning = false;
                mMediaPlayer.seekTo(seekBar.getProgress());
                mMediaPlayer.start();

            }
        });


    }

    /**
     * 播放视频
     */
    public void playVideo() {
        if (mMediaPlayer == null) {
            //实例化MediaPlayer对象
            mMediaPlayer = new MediaPlayer();
            mSvVideoPlayer.setVisibility(View.VISIBLE);
            boolean mHardwareDecoder = false;
            // 不维持自身缓冲区，直接显示
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && mHardwareDecoder) {
                mSvVideoPlayer.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
            mSvVideoPlayer.getHolder().setFixedSize(getScreenWidth(), getScreenHeight());
            mSvVideoPlayer.getHolder().setKeepScreenOn(true);//保持屏幕常亮
            mSvVideoPlayer.getHolder().addCallback(new SurFaceCallback());
        }
    }

    public void isPlayOrPause(View view) {
    }

    /**
     * 向player中设置dispay，也就是SurfaceHolder。但此时有可能SurfaceView还没有创建成功，所以需要监听SurfaceView的创建事件
     */
    private final class SurFaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mMediaPlayer == null) {
                return;
            }
            if (!hasActiveHolder) {
                play(mPosition);
                hasActiveHolder = true;
            }
            if (mPosition > 0) {
                play(mPosition);
                mPosition = 0;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mMediaPlayer == null) {
                return;
            }
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mPosition = mMediaPlayer.getCurrentPosition();
            }
        }

        private void play(int position) {
            try {
                //添加播放视频的路径与配置MediaPlayer
                AssetFileDescriptor fileDescriptor = getResources().openRawResourceFd(R.raw.test);
                mMediaPlayer.reset();
                //给mMediaPlayer添加预览的SurfaceHolder，将播放器和SurfaceView关联起来
                mMediaPlayer.setDisplay(mSvVideoPlayer.getHolder());


                mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());
                // 缓冲
                mMediaPlayer.prepare();
                mMediaPlayer.setOnBufferingUpdateListener(new BufferingUpdateListener());
                mMediaPlayer.setOnPreparedListener(new PreparedListener(position));
                mMediaPlayer.setOnCompletionListener(new CompletionListener());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 缓冲变化时回调
     */
    private final class BufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.d("main", "percent=" + percent);
        }
    }

    /**
     * 准备完成回调
     * 只有当播放器准备好了之后才能够播放，所以播放的出发只能在触发了prepare之后
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int position;

        public PreparedListener(int position) {
            this.position = position;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            int duration = mMediaPlayer.getDuration();
            mSb_main_bar.setMax(duration);
//            mMediaPlayer.start();
            if (position > 0) {

                mMediaPlayer.seekTo(position);
            }
        }
    }

    /**
     * 播放结束时回调
     */
    private final class CompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(timer!=null){
                timer.cancel();
                timer = null;
            }

//            mMediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        //释放内存，MediaPlayer底层是运行C++的函数方法，不使用后必需释放内存
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mPosition = mMediaPlayer.getCurrentPosition();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    private int getScreenWidth() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    private int getScreenHeight() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }
}
