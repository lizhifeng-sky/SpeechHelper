package com.android.speech.helper.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * @author lizhifeng
 * @date 2021/1/6 18:48
 */
public class MusicUtils {
    private static volatile MusicUtils instance;

    private MusicUtils() {
    }

    public static MusicUtils getInstance() {
        if (instance == null) {
            synchronized (MusicUtils.class) {
                if (instance == null) {
                    instance = new MusicUtils();
                }
            }
        }
        return instance;
    }

    private int currentTime = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            currentTime++;
            handler.sendEmptyMessageDelayed(1, 1000);
        }
    };

    private MediaPlayer mediaPlayer;

    public void play(Context context, String musicName) {
        //实例化播放内核
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }
        //获得播放源访问入口
        AssetManager am = context.getAssets();
        try {
            AssetFileDescriptor afd = am.openFd(musicName + ".mp3");// 注意这里的区别
            //给MediaPlayer设置播放源
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //设置准备就绪状态监听
        mediaPlayer.setOnPreparedListener(mp -> {
            // 开始播放
            mediaPlayer.start();
            Message message = handler.obtainMessage();
            handler.sendMessage(message);
        });
        mediaPlayer.setOnCompletionListener(mp -> handler.removeCallbacksAndMessages(null));
        //准备播放
        mediaPlayer.prepareAsync();
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public static String matchMusic(String text) {
        List<String> musicList = new ArrayList<>();
        musicList.add("倔强");
        musicList.add("反方向的钟");
        musicList.add("可爱女人");
        musicList.add("后来的我们");
        musicList.add("娘子");
        musicList.add("完美主义");
        musicList.add("宠上天");
        musicList.add("干杯");
        musicList.add("成名在望");
        musicList.add("斗牛");
        musicList.add("第一天");
        musicList.add("纯真");
        musicList.add("黑色幽默");
        musicList.add("龙卷风");
        String matchMusic = null;
        for (int i = 0; i < musicList.size(); i++) {
            if (text.contains(musicList.get(i))) {
                matchMusic = musicList.get(i);
                break;
            }
        }
        return matchMusic;
    }
}
