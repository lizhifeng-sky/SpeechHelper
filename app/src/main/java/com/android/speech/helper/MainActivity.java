package com.android.speech.helper;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.speech.helper.utils.IntentUtils;
import com.android.speech.helper.utils.JokeUtils;
import com.android.speech.helper.utils.MusicUtils;
import com.iflytek.OnSpeakListener;
import com.iflytek.OnSpeechInitListener;
import com.iflytek.OnWakeUpListener;
import com.iflytek.SimpleSpeakListenerImpl;
import com.iflytek.SimpleSpeechListenerImpl;
import com.iflytek.SpeakHelper;
import com.iflytek.SpeechHelper;
import com.iflytek.WakeHelper;
import com.iflytek.cloud.SpeechError;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnSpeakListener {

    //动画播放view
    private LottieAnimationView animatorView;

    //菜单容器
    private LinearLayout menu_linear;
    //地图导航
    private ImageView guide;
    //音乐播放
    private ImageView music;
    //发送信息
    private ImageView message;
    //拨打电话
    private ImageView phone;

    //底部 的 三个操作按钮
    //菜单
    private ImageView menu;
    //语音
    private ImageView speak;
    //笑话
    private ImageView emoji;

    //robot提示性文本
    private TextView robot_message;


    //科大讯飞 是否 初始化成功
    private boolean isSpeechInitSuccess = false;

    //菜单是否展开
    private boolean isMenuOpen = false;

    //是否正在说话
    private boolean isSpeaking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //先 加载 布局
        setContentView(R.layout.activity_main);

        //对view 进行赋值
        animatorView = findViewById(R.id.lottie);

        menu_linear = findViewById(R.id.menu_linear);
        guide = findViewById(R.id.guide);
        music = findViewById(R.id.music);
        message = findViewById(R.id.message);
        phone = findViewById(R.id.phone);

        speak = findViewById(R.id.speak);
        emoji = findViewById(R.id.emoji);
        menu = findViewById(R.id.menu);

        robot_message = findViewById(R.id.robot_message);

        //启动 动画
        //设置 动画
        animatorView.setAnimation(R.raw.animator_0);
        //启动 播放动画
        animatorView.playAnimation();

        //设置 按钮 的 点击事件
        setOnClickListener();

        //初始化 语音合成、语音播放
        initSpeech();
        //初始化 唤醒 功能
        initWakeUp();
    }

    private long lastWakeTime = 0L;

    private void initWakeUp() {
        //延迟消息
        speak.postDelayed(() -> {

            //这些 代码 会 延时3s执行
            boolean initSuccess = WakeHelper.getInstance().init(MainActivity.this);
            if (initSuccess) {
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
        //初始化 语音识别
        getLifecycle().addObserver(SpeechHelper.getInstance());
        //在 主进程中 进行初始
        SpeechHelper.getInstance().initInMainProcess(this, "601663eb");
        //请求 权限
        SpeechHelper.getInstance().requestPermission(this);
        //进行 准备工作
        SpeechHelper.getInstance().prepare(this, new OnSpeechInitListener() {
            @Override
            public void initSuccess() {
                isSpeechInitSuccess = true;
                Log.e("prepare", "initSuccess");
                //初始化 语音播放
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
            changeMenuState();
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
            animatorView.setAnimation(R.raw.joke);
            animatorView.playAnimation();
            JokeUtils.speakJoke(this);
        });

        //地图 导航
        guide.setOnClickListener(v -> {
            if (isSpeaking) {
                return;
            }
            animatorView.setAnimation(R.raw.animator_0);
            animatorView.playAnimation();
            robot_message.setText("你可以说：带我去 人民广场");
            SpeakHelper.getInstance().startSpeak("你可以说：带我去 人民广场", this);
        });

        //音乐播放
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
            animatorView.setAnimation(R.raw.animator_0);
            animatorView.playAnimation();
            robot_message.setText("你可以说：给某个手机号发短信");
            SpeakHelper.getInstance().startSpeak("你可以说：给某个手机号发短信", this);
        });

        phone.setOnClickListener(v -> {
            if (isSpeaking) {
                return;
            }
            animatorView.setAnimation(R.raw.animator_0);
            animatorView.playAnimation();
            robot_message.setText("你可以说：给某个手机号打电话");
            SpeakHelper.getInstance().startSpeak("你可以说：给某个手机号打电话", this);
        });
    }

    //开始 语音听写
    public void startSpeech() {
        SpeechHelper.getInstance().start(new SimpleSpeechListenerImpl() {
            @Override
            public void onResult(String originalText, String text) {

                if (text.contains("笑话")) {
                    animatorView.setAnimation(R.raw.joke);
                    animatorView.playAnimation();
                } else {
                    animatorView.setAnimation(R.raw.animator_0);
                    animatorView.playAnimation();
                }

                //进行 意图识别 处理
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


    //改变 菜单状态
    private void changeMenuState() {
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