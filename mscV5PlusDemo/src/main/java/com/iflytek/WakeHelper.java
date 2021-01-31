package com.iflytek;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.mscv5plusdemo.R;
import com.iflytek.mscv5plusdemo.WakeDemo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author lizhifeng
 * @date 2021/1/4 22:32
 */
public class WakeHelper {
    private static volatile WakeHelper instance;

    private WakeHelper() {
    }

    public static WakeHelper getInstance() {
        if (instance == null) {
            synchronized (WakeHelper.class) {
                if (instance == null) {
                    instance = new WakeHelper();
                }
            }
        }
        return instance;
    }

    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    // 唤醒结果内容
    private String resultString;

    private String keep_alive = "1";
    private String ivwNetMode = "0";
    private OnWakeUpListener wakeUpListener;

    public boolean init(Context context) {
        if (null == VoiceWakeuper.createWakeuper(context, null)) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Toast.makeText(context
                    , "创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化"
                    , Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void startWake(Context context, OnWakeUpListener wakeUpListener) {
        this.wakeUpListener = wakeUpListener;
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + 1500);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource(context));
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH,
                    Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            /*	mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");*/

            mIvw.startListening(mWakeuperListener);
				/*File file = new File(Environment.getExternalStorageDirectory().getPath() + "/msc/ivw1.wav");
				byte[] byetsFromFile = getByetsFromFile(file);
				mIvw.writeAudio(byetsFromFile,0,byetsFromFile.length);*/
            //	mIvw.stopListening();
        } else {
            Toast.makeText(context, "唤醒未初始化", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopWake() {
        mIvw.stopListening();
    }

    public void destroyWake() {
        // 销毁合成对象
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        }
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d("wake_up", "onResult");
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
                wakeUpListener.onWakeUpSuccess();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
                wakeUpListener.onWakeUpError();
            }
        }

        @Override
        public void onError(SpeechError error) {
            wakeUpListener.onWakeUpError();
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    private String getResource(Context context) {
        final String resPath = ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + context.getString(R.string.app_id) + ".jet");
        Log.d("wake_up", "resPath: " + resPath);
        return resPath;
    }

}
