package com.iflytek;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class SpeechHelper implements LifecycleObserver {
    public static String TAG = "SpeechHelper";
    /**
     * 语音听写对象
     */
    private SpeechRecognizer mIat;
    /**
     * 用HashMap存储听写结果
     */
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private SharedPreferences mSharedPreferences;
    private OnSpeechListener onSpeechListener;
    private OnSpeechInitListener onSpeechInitListener;
    private StringBuilder stringBuilder;

    private static volatile SpeechHelper speechHelper;

    private SpeechHelper() {
    }

    public static SpeechHelper getInstance() {
        if (speechHelper == null) {
            synchronized (SpeechHelper.class) {
                if (speechHelper == null) {
                    speechHelper = new SpeechHelper();
                }
            }
        }
        return speechHelper;
    }

    /**
     *调用流程
     * 1、initInMainProcess 可在任意地点
     * 2、requestPermission 在activity
     * 3、prepare           提前准备
     * 4、start             开始使用
     *
     */
    public void initInMainProcess(Context context,
                                  String appId) {
        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        SpeechUtility.createUtility(context, "appid=" + appId);
    }

    /**
     * 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
     */
    public static void showSpeechLog(boolean showLog) {
        Setting.setShowLog(showLog);
    }

    public void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.RECORD_AUDIO);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.LOCATION_HARDWARE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_SETTINGS,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_CONTACTS}, 0x0010);
            }
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 0x0010);
            }
        }
    }

    public void prepare(Context context, OnSpeechInitListener onSpeechInitListener) {
        this.onSpeechInitListener = onSpeechInitListener;
        mSharedPreferences = context.getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
    }

    public void start(final OnSpeechListener onSpeechListener) {
        stringBuilder=new StringBuilder();
        if (this.onSpeechListener == null) {
            this.onSpeechListener = onSpeechListener;
        }
        setParam();
        /*
         * 函数调用返回值
         */
        mIat.startListening(new RecognizerListener() {

            @Override
            public void onBeginOfSpeech() {
                if (onSpeechListener != null) {
                    onSpeechListener.onSpeechStart();
                }
            }

            @Override
            public void onError(SpeechError error) {
                // Tips：
                // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
                if (onSpeechListener != null) {
                    onSpeechListener.onError(error);
                }
            }

            @Override
            public void onEndOfSpeech() {
                // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
                if (onSpeechListener != null) {
                    onSpeechListener.onSpeechEnd();
                }
            }

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d(TAG, results.getResultString());
                String s = printResult(results);
                stringBuilder.append(s);
                if (isLast){
                    Log.d(TAG, "听写结果   "+s);
                    if (onSpeechListener != null) {
                        onSpeechListener.onResult(results.getResultString(), s);
                    }
                }
            }

            @Override
            public void onVolumeChanged(int volume, byte[] data) {
                if (onSpeechListener != null) {
                    onSpeechListener.onVolumeChanged(volume);
                }
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
                // 若使用本地能力，会话id为null
                //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                //		Log.d(TAG, "session id =" + sid);
                //	}
            }
        });
//        if (ret != ErrorCode.SUCCESS) {
////            showTip("听写失败,错误码：" + ret+",
////            请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
//        } else {
//            showTip(getString(R.string.text_begin));
//        }
    }

    /**
     * 参数设置
     */
    private void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        /*
         * 引擎类型
         */
        String mEngineType = SpeechConstant.TYPE_CLOUD;
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        String resultType = "json";
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);
        // 设置语言
        String defaultLanguage = "zh_cn";
        String language = "zh_cn";
        if (defaultLanguage.equals(language)) {
            String lag = mSharedPreferences.getString(
                    "iat_language_preference",
                    "mandarin");
//            Log.e(TAG, "language:" + language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
//        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));
        //此处用于设置dialog中不显示错误码信息
        //设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS,
                mSharedPreferences.getString("iat_vadbos_preference", "3000"));

        //设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,
                mSharedPreferences.getString("iat_vadeos_preference", "3000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT,
                mSharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (onSpeechInitListener != null) {
                if (code != ErrorCode.SUCCESS) {
                    onSpeechInitListener.initFail(code);
                } else {
                    onSpeechInitListener.initSuccess();
                }
            }
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            if (onSpeechListener != null) {
                onSpeechListener.onSpeechStart();
            }
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            if (onSpeechListener != null) {
                onSpeechListener.onError(error);
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            if (onSpeechListener != null) {
                onSpeechListener.onSpeechEnd();
            }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());

            String s = printResult(results);
            Log.d(TAG, "听写结果   "+s);
            if (onSpeechListener != null) {
                onSpeechListener.onResult(results.getResultString(), s);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            if (onSpeechListener != null) {
                onSpeechListener.onVolumeChanged(volume);
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    public void stopSpeech(){
        mIat.stopListening();
    }

    public void cancelSpeech(){
        mIat.cancel();
    }

    private String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        return resultBuffer.toString();
    }

    public void reset() {
        mIatResults.clear();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (null != mIat) {
            //退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }
}
