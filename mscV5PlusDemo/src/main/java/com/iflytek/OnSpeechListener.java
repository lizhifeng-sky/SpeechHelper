package com.iflytek;

import com.iflytek.cloud.SpeechError;

public interface OnSpeechListener {
    /** 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入*/
    void onSpeechStart();
    /**此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入*/
    void onSpeechEnd();

    void onResult(String originalText, String text);

    void onVolumeChanged(int volume);
    void onError(SpeechError error);

}
