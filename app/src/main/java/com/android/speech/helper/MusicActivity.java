package com.android.speech.helper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.speech.helper.adapter.MusicAdapter;
import com.android.speech.helper.utils.AudioPlayUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private TextView musicName;

    private TextView currentTime;
    private TextView totalTime;
    private SeekBar musicPlayProgress;



    private ImageView comeBack;

    private ImageView before;
    private ImageView play;
    private ImageView next;

    private ImageView menu;




    private List<String> musicList;
    private String currentMusicName;
    private int currentMusicPosition = 0;

    public static void start(Context context, String name) {
        Intent intent = new Intent(context, MusicActivity.class);
        intent.putExtra("name", name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_music);

        LottieAnimationView lottie = findViewById(R.id.play_area);
        lottie.setAnimation(R.raw.animator_0);
        lottie.playAnimation();

        comeBack = findViewById(R.id.comeBack);
        musicName = findViewById(R.id.musicName);

        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        musicPlayProgress = findViewById(R.id.seekBar);

        before = findViewById(R.id.before);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        menu = findViewById(R.id.menu);

        //初始化 数据
        initData();
        //开始播放
        startPlay();
        //设置点击事件
        setOnClickListener();
    }

    private void startPlay() {
        //key-value
        if (getIntent().getStringExtra("name") == null) {
            currentMusicName = musicList.get((int) (System.currentTimeMillis() % 14));
        } else {
            currentMusicName = getIntent().getStringExtra("name");
        }
        currentMusicPosition = musicList.indexOf(currentMusicName);
        musicName.setText(currentMusicName);

        //开始 播放
        //先生命周期
        AudioPlayUtils.getInstance().bindLifeCycle(this,
                new AudioPlayUtils.SimpleMediaPlayImpl() {

                    @Override
                    public void onPrepared(long durationInMilliseconds) {
                        super.onPrepared(durationInMilliseconds);
                        // 准备完成
                        totalTime.setText(translateTime(AudioPlayUtils.getInstance().getCurrentMusicTotalTime()));
                    }

                    @Override
                    public void onPlay(long timeInMilliseconds) {
                        super.onPlay(timeInMilliseconds);
                        //正在播放
                        play.setImageResource(R.drawable.icon_pause);
                        musicPlayProgress.setProgress((int) (timeInMilliseconds * 1.0f * 100 / AudioPlayUtils.getInstance().getCurrentMusicTotalTime()));
                        currentTime.setText(translateTime(timeInMilliseconds));
                    }

                    @Override
                    public void onPause() {
                        super.onPause();
                        play.setImageResource(R.drawable.icon_play);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        play.setImageResource(R.drawable.icon_play);
                    }
                });

        AudioPlayUtils.getInstance().playLocalAudio(this, currentMusicName);
    }

    private void initData() {
        musicList = new ArrayList<>();
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
    }

    private void setOnClickListener() {
        comeBack.setOnClickListener(v -> finish());

        before.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMusicPosition == 0) {
                    Toast.makeText(MusicActivity.this, "前面没有音乐了", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentMusicPosition=currentMusicPosition-1;
                musicName.setText(musicList.get(currentMusicPosition));

                AudioPlayUtils.getInstance().playLocalAudio(MusicActivity.this,
                        musicList.get(currentMusicPosition));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMusicPosition == musicList.size() - 1) {
                    Toast.makeText(MusicActivity.this, "后面没有音乐了", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentMusicPosition=currentMusicPosition+1;
                musicName.setText(musicList.get(currentMusicPosition));

                AudioPlayUtils.getInstance().playLocalAudio(MusicActivity.this,
                        musicList.get(currentMusicPosition));
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AudioPlayUtils.getInstance().isPlay()) {
                    AudioPlayUtils.getInstance().pause();
                } else {
                    AudioPlayUtils.getInstance().restart();
                }
            }
        });

        musicPlayProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("seek", "onProgressChanged");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e("seek", "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e("seek", "onStopTrackingTouch");
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMusicListDialog();
            }
        });
    }

    public String translateTime(long time) {
        int second = (int) (time / 1000);
        if (second < 60) {
            if (second < 10) {
                return "00:0" + second;
            } else {
                return "00:" + second;
            }

        } else {
            return "0" + second / 60 + ":" + second % 60;
        }
    }


    private BottomSheetDialog bottomSheetDialog;
    private RecyclerView recycler_music;

    public void showMusicListDialog() {
        if (bottomSheetDialog == null) {
            bottomSheetDialog = new BottomSheetDialog(this);

            View view = LayoutInflater.from(this).inflate(R.layout.dialog_music_list, null, false);
            recycler_music = view.findViewById(R.id.recycler_music);
            initMusicRecycler();

            //设置 弹窗 展示 内容
            bottomSheetDialog.setContentView(view);
        }
        //展示 弹窗
        bottomSheetDialog.show();
    }

    private void initMusicRecycler() {
        //设置 recyclerView的布局管理
        recycler_music.setLayoutManager(new LinearLayoutManager(this));

        MusicAdapter adapter = new MusicAdapter(musicList, new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                currentMusicPosition = position;
                musicName.setText(musicList.get(currentMusicPosition));

                AudioPlayUtils.getInstance().playLocalAudio(MusicActivity.this,
                        musicList.get(currentMusicPosition));

                bottomSheetDialog.dismiss();
            }
        });

        recycler_music.setAdapter(adapter);
    }
}