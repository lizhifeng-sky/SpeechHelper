package com.iflytek;


import com.iflytek.cloud.SpeechError;

/**
 * @author lizhifeng
 * @date 2020/4/23 20:55
 */
public interface OnSpeakListener {
    //开始说话
    void onSpeakBegin();
    //说话结束
    void onCompleted(SpeechError speechError);
}
