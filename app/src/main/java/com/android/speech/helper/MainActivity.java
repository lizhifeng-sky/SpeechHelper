package com.android.speech.helper;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.speech.helper.bean.LocationBean;
import com.android.speech.helper.bean.WeatherBean;
import com.android.speech.helper.bean.WeatherDateBean;
import com.android.speech.helper.utils.AlarmUtils;
import com.android.speech.helper.utils.ContactUtils;
import com.android.speech.helper.utils.IntentUtils;
import com.android.speech.helper.utils.JokeUtils;
import com.android.speech.helper.utils.MapUtils;
import com.android.speech.helper.utils.MusicUtils;
import com.android.speech.helper.utils.WeatherUtils;
import com.iflytek.OnSpeakListener;
import com.iflytek.OnSpeechInitListener;
import com.iflytek.OnSpeechListener;
import com.iflytek.OnWakeUpListener;
import com.iflytek.SimpleSpeakListenerImpl;
import com.iflytek.SimpleSpeechListenerImpl;
import com.iflytek.SpeakHelper;
import com.iflytek.SpeechHelper;
import com.iflytek.WakeHelper;
import com.iflytek.cloud.SpeechError;

import java.io.IOException;
import java.util.logging.Logger;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnSpeakListener {

    private LottieAnimationView lottie;

    private LinearLayout menu_linear;
    private ImageView guide;
    private ImageView music;
    private ImageView message;
    private ImageView phone;

    private ImageView menu;
    private ImageView speak;
    private ImageView emoji;

    private TextView robot_message;

    private boolean isSpeechInitSuccess = false;

    private boolean isMenuOpen = false;

    private boolean isSpeaking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        lottie = findViewById(R.id.lottie);
        lottie.setAnimation(R.raw.animator_0);
        lottie.pauseAnimation();

        menu_linear = findViewById(R.id.menu_linear);

        robot_message = findViewById(R.id.robot_message);

        guide = findViewById(R.id.guide);
        music = findViewById(R.id.music);
        message = findViewById(R.id.message);
        phone = findViewById(R.id.phone);

        speak = findViewById(R.id.speak);
        emoji = findViewById(R.id.emoji);
        menu = findViewById(R.id.menu);

        setOnClickListener();

        initSpeech();
        initWakeUp();
    }

    private long lastWakeTime = 0L;

    private void initWakeUp() {
        speak.postDelayed(() -> {
            boolean init = WakeHelper.getInstance().init(MainActivity.this);
            if (init) {
                WakeHelper.getInstance().startWake(MainActivity.this, new OnWakeUpListener() {
                    @Override
                    public void onWakeUpSuccess() {
                        if (System.currentTimeMillis() - lastWakeTime < 3000) {
                            return;
                        }
                        lastWakeTime = System.currentTimeMillis();
                        isSpeaking = true;
                        robot_message.setText("我在,有什么可以帮助你的嘛");
                        SpeakHelper.getInstance().startSpeak("我在,有什么可以帮助你的嘛？",
                                new SimpleSpeakListenerImpl() {
                                    @Override
                                    public void onCompleted(SpeechError speechError) {
                                        isSpeaking = false;
                                        startSpeech();
                                    }
                                });
                    }

                    @Override
                    public void onWakeUpError() {
                        Toast.makeText(MainActivity.this, "唤醒失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 3000);
    }

    private void initSpeech() {
        //初始化 语音识别、语音播放
        getLifecycle().addObserver(SpeechHelper.getInstance());
        SpeechHelper.getInstance().initInMainProcess(this, "601663eb");
        SpeechHelper.getInstance().requestPermission(this);
        SpeechHelper.getInstance().prepare(this, new OnSpeechInitListener() {
            @Override
            public void initSuccess() {
                isSpeechInitSuccess = true;
                Log.e("prepare", "initSuccess");
                SpeakHelper.getInstance().initSpeak(MainActivity.this);
                SpeakHelper.getInstance().startSpeak("初始化成功", new SimpleSpeakListenerImpl());
            }

            @Override
            public void initFail(int code) {
                Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                Log.e("prepare", "initFail");
                isSpeechInitSuccess = false;
            }
        });

    }


    private void setOnClickListener() {
        //菜单按钮 点击
        menu.setOnClickListener(v -> {
            isMenuOpen = !isMenuOpen;
            changeMenu();
        });
        //语音按钮 点击
        speak.setOnClickListener(v -> {
            if (!isSpeechInitSuccess) {
                Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }
            SpeakHelper.getInstance().startSpeak("小麦在呢，有什么可以帮助你的嘛？",
                    new SimpleSpeakListenerImpl() {
                        @Override
                        public void onCompleted(SpeechError speechError) {
                            isSpeaking = false;
                            startSpeech();
                        }
                    });
        });
        //笑话按钮 点击
        emoji.setOnClickListener(v -> {
            if (isSpeaking) {
                return;
            }
            lottie.setAnimation(R.raw.joke);
            lottie.playAnimation();
            JokeUtils.speakJoke(this);
        });

        guide.setOnClickListener(v -> {
            if (isSpeaking) {
                return;
            }
            lottie.setAnimation(R.raw.animator_0);
            lottie.playAnimation();
            robot_message.setText("你可以说：带我去 人民广场");
            SpeakHelper.getInstance().startSpeak("你可以说：带我去 人民广场", this);
        });

        music.setOnClickListener(v -> {
            MusicActivity.start(this, null);
//            if (isSpeaking) {
//                return;
//            }
//            lottie.setAnimation(R.raw.animator_0);
//            lottie.playAnimation();
//            robot_message.setText("你可以说：播放 一首音乐");
//            SpeakHelper.getInstance().startSpeak("你可以说：播放 一首音乐", this);
        });

        message.setOnClickListener(v -> {
            if (isSpeaking) {
                return;
            }
            lottie.setAnimation(R.raw.animator_0);
            lottie.playAnimation();
            robot_message.setText("你可以说：给某个手机号发短信");
            SpeakHelper.getInstance().startSpeak("你可以说：给某个手机号发短信", this);
        });

        phone.setOnClickListener(v -> {
            if (isSpeaking) {
                return;
            }
            lottie.setAnimation(R.raw.animator_0);
            lottie.playAnimation();
            robot_message.setText("你可以说：给某个手机号打电话");
            SpeakHelper.getInstance().startSpeak("你可以说：给某个手机号打电话", this);
        });
    }

    public void startSpeech() {
        SpeechHelper.getInstance().start(new SimpleSpeechListenerImpl() {
            @Override
            public void onResult(String originalText, String text) {
                if (text.contains("笑话")) {
                    lottie.setAnimation(R.raw.joke);
                    lottie.playAnimation();
                } else {
                    lottie.setAnimation(R.raw.animator_0);
                    lottie.playAnimation();
                }
                IntentUtils.intent(MainActivity.this, text, MainActivity.this);
            }

            @Override
            public void onError(SpeechError error) {
                Log.e("speech", "error" + error.getMessage());
            }
        });
    }

    @Override
    public void onSpeakBegin() {
        MusicUtils.getInstance().pause();
        isSpeaking = true;
    }

    @Override
    public void onCompleted(SpeechError speechError) {
        isSpeaking = false;
    }

    private void changeMenu() {
        if (isMenuOpen) {
            menu.setBackgroundResource(R.drawable.shape_menu_back_bottom);
            //展开菜单
            showMenu();
        } else {
            menu.setBackground(null);
            //关闭菜单
            closeMenu();
        }
    }

    private void closeMenu() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(menu_linear,
                "alpha", 1f, 0f);
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }

    private void showMenu() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(menu_linear,
                "alpha", 0f, 1f);
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }
}