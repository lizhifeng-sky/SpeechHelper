package com.android.speech.helper.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * @author lizhifeng
 * @date 2020/11/28 10:29
 */
public class AudioPlayUtils implements LifecycleObserver {
    private static volatile AudioPlayUtils instance;

    private AudioPlayUtils() {
    }

    public static AudioPlayUtils getInstance() {
        if (instance == null) {
            synchronized (AudioPlayUtils.class) {
                if (instance == null) {
                    instance = new AudioPlayUtils();
                }
            }
        }
        return instance;
    }

    private MediaPlayer mediaPlayer;
    private OnMediaPlayListener onMediaPlayListener;
    private boolean openLog = true;
    private Timer mTimer;


    public void bindLifeCycle(FragmentActivity fragmentActivity, OnMediaPlayListener onMediaPlayListener) {
        this.onMediaPlayListener = onMediaPlayListener;
        new Handler(Looper.getMainLooper()).post(() -> fragmentActivity.getLifecycle().addObserver(AudioPlayUtils.this));
    }

    public boolean isPlay() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public int getCurrentMusicTotalTime() {
        if (mediaPlayer == null) {
            return 0;
        }
        return mediaPlayer.getDuration();
    }

    public void restart() {
        log("pause");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            startTimer();
        }
    }

    public void seekTo(int duration) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(duration);
        }
    }

    public void playLocalAudio(Context context, String musicName) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        //判断 当前 播放状态
        if (mediaPlayer.isPlaying()) {
            //停止 当前音乐
            mediaPlayer.stop();
            //重置 播放器
            mediaPlayer.reset();
        }
        //获得播放源访问入口
        AssetManager am = context.getAssets();
        try {
            AssetFileDescriptor afd = am.openFd(musicName + ".mp3");// 注意这里的区别
            //给MediaPlayer设置播放源
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            if (onMediaPlayListener != null) {
                onMediaPlayListener.onError(e);
            }
        }
        //开始 播放
        play();
    }

    private void play() {
        //准备
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                log("onPrepared");
                if (onMediaPlayListener != null) {
                    onMediaPlayListener.onPrepared(mp.getDuration());
                }
                start();
            }
        });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            //返回true表示在此处理错误，不会回调onCompletion
            log("onError at play " + what + "  " + extra);
            release();
            return true;
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            log("onCompletion");
            if (onMediaPlayListener != null) {
                onMediaPlayListener.onComplete();
            }
            release();
        });
    }

    private void start() {
        if (mediaPlayer != null) {
            log("start");
            mediaPlayer.start();
            startTimer();
        }
    }

    /**
     * 每隔一秒执行一次，更新当前播放时间
     */
    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        log("timer  start" + System.currentTimeMillis());
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (onMediaPlayListener != null && mediaPlayer != null) {
                        log("timer  schedule" + System.currentTimeMillis() + "    " + mediaPlayer.getCurrentPosition());
                        onMediaPlayListener.onPlay(mediaPlayer.getCurrentPosition());
                    }
                });
            }
        }, 0, 1000);
    }


    public void pause() {
        log("pause");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mTimer.cancel();
            mTimer = null;
            if (onMediaPlayListener != null) {
                onMediaPlayListener.onPause();
            }
        }
    }

    public void stop() {
        log("stop");
        release();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void release() {
        log("release");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        cancelTimer();
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void log(String message) {
        if (onMediaPlayListener != null && openLog) {
            onMediaPlayListener.onLogInfo(message);
        }
    }

    public static void runOnUiThread(Runnable runnable, long delayed) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, delayed);
    }

    public static class SimpleMediaPlayImpl implements OnMediaPlayListener {

        @Override
        public void onPrepared(long durationInMilliseconds) {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onPlay(long timeInMilliseconds) {

        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(@Nullable Exception e) {

        }

        @Override
        public void onLogInfo(String logInfo) {

        }
    }

    public interface OnMediaPlayListener {
        void onPrepared(long durationInMilliseconds);

        void onPause();

        void onPlay(long timeInMilliseconds);

        void onComplete();

        void onError(@Nullable Exception e);

        void onLogInfo(String logInfo);
    }
}
