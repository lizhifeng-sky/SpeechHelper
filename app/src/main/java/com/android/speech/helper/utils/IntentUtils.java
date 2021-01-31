package com.android.speech.helper.utils;

import android.content.Context;
import android.util.Log;

import com.iflytek.OnSpeakListener;
import com.iflytek.SimpleSpeakListenerImpl;
import com.iflytek.SpeakHelper;
import com.iflytek.cloud.SpeechError;

/**
 * @author lizhifeng
 * @date 2021/1/30 13:01
 */
public class IntentUtils {
    public static void intent(Context context, String text, OnSpeakListener onSpeakListener) {
        Log.e("识别结果", text);
        if (MapUtils.checkTarget(text) != null) {
            //主要景点匹配
            SpeakHelper.getInstance().startSpeak("好的，请稍候", onSpeakListener);
            MapUtils.poi(context, MapUtils.checkTarget(text));
        } else if (text.contains("带我去")) {
            //地址匹配
            SpeakHelper.getInstance().startSpeak("好的，请稍候", onSpeakListener);
            MapUtils.onGetAddress(context, text);
        } else if (text.contains("天气")) {
            //关键字匹配
            SpeakHelper.getInstance().startSpeak("好的，请稍候", onSpeakListener);
            WeatherUtils.getWeather("上海");
        } else if (text.contains("播放")) {
            //音乐播放
            SpeakHelper.getInstance().startSpeak("好的，请稍候", new SimpleSpeakListenerImpl() {
                @Override
                public void onCompleted(SpeechError speechError) {
                    super.onCompleted(speechError);
                    String music = MusicUtils.matchMusic(text);
                    if (music != null) {
                        MusicUtils.getInstance().play(context, music);
                    } else {
                        SpeakHelper.getInstance().startSpeak("小麦没有找到这个音乐呢，你能不能再说一下呢", onSpeakListener);
                    }
                }
            });

        } else if (text.contains("提醒")) {
            //日程
            boolean b = AlarmUtils.matchAlarm(context, text);
            if (b) {
                SpeakHelper.getInstance().startSpeak("好的", onSpeakListener);
            } else {
                SpeakHelper.getInstance().startSpeak("小麦没听懂你的意思呢，你可以说 明天中午提醒我做美发", onSpeakListener);
            }

        } else if (text.contains("给") && (text.contains("打电话") | text.contains("发短信"))) {
            String mobile = text.substring(1, 12);
            try {
                if (text.contains("打电话")) {
                    ContactUtils.callPhone(context, mobile);
                } else {
                    ContactUtils.sendSMS(context, mobile);
                }
            } catch (Exception e) {
                SpeakHelper.getInstance().startSpeak("小麦需要完整的手机号呢", onSpeakListener);
            }
        } else if (text.contains("笑话")) {
            JokeUtils.speakJoke(onSpeakListener);
        } else {
            switch ((int) (System.currentTimeMillis()%6)){
                case 1:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说 今天天气怎么样", onSpeakListener);
                    break;
                case 2:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说 明天中午提醒我做美发", onSpeakListener);
                    break;
                case 3:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说：带我去 人民广场", onSpeakListener);
                    break;
                case 4:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说：播放 一首音乐", onSpeakListener);
                    break;
                case 5:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说：给某个手机号发短信", onSpeakListener);
                    break;
                case 6:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说：给某个手机号打电话", onSpeakListener);
                    break;
                default:
                    SpeakHelper.getInstance().startSpeak("小麦没听懂您的意思呢，你可以说 城隍庙附近有什么好吃哒", onSpeakListener);
                    break;
            }

        }
    }
}
