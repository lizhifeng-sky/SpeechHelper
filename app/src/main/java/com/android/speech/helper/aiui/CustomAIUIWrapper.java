package com.android.speech.helper.aiui;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.speech.helper.MusicActivity;
import com.android.speech.helper.aiui.handler.ChatMessageHandler;
import com.android.speech.helper.aiui.handler.MapHandler;
import com.android.speech.helper.aiui.model.RawMessage;
import com.android.speech.helper.aiui.model.SemanticResult;
import com.android.speech.helper.bean.aiui.JsonMessageBean;
import com.android.speech.helper.bean.aiui.JsonRootBean;
import com.android.speech.helper.bean.aiui.JsonSimpleBean;
import com.android.speech.helper.utils.AlarmUtils;
import com.android.speech.helper.utils.ContactUtils;
import com.android.speech.helper.utils.GsonUtils;
import com.iflytek.SpeakHelper;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;
import com.iflytek.aiui.jni.AIUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CustomAIUIWrapper {
    private static final String TAG = "CustomAIUIWrapper";
    private AIUIAgent mAgent;
    private Context context;
    private int mAIUIState;
    private boolean mAudioRecording = false;

    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    //唤醒事件
                    Log.i(TAG, "on event: " + event.eventType);
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    //结果解析事件
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                JsonSimpleBean rootBean = GsonUtils.fromJson(resultStr, JsonSimpleBean.class);
                                Log.e(TAG, rootBean.getService());
                                if (rootBean.getService().equals("joke")) {
                                    //joke 内容
                                    Log.e(TAG, rootBean.getAnswer().getText());
                                } else if (rootBean.getService().equals("weather")) {
                                    //weather 内容
                                    Log.e(TAG, rootBean.getAnswer().getText());
                                } else if (rootBean.getService().equals("mapU")) {
                                    //地图 内容
                                    MapHandler mapHandler = new MapHandler(context);
                                    mapHandler.dealMapIntent(resultStr);
                                } else if (rootBean.getService().equals("iFlytekQA")
                                        || rootBean.getService().equals("message")) {
                                    //发短信
                                    //打电话
                                    //说话内容
                                    //rootBean.getText()
                                    JsonMessageBean jsonMessageBean=GsonUtils.fromJson(resultStr,JsonMessageBean.class);
                                    sendMessage(new AIUIMessage(AIUIConstant.CMD_CLEAN_DIALOG_HISTORY, 0, 0, null, null));
                                    try {
                                        if (!rootBean.getAnswer().getText().contains("短信")) {
                                            ContactUtils.callPhone(context, jsonMessageBean.getText().substring(1,12));
                                        } else {
                                            ContactUtils.sendSMS(context, jsonMessageBean.getSemantic().get(0).getSlots().get(0).getValue());
                                        }
                                    } catch (Exception e) {
                                        SpeakHelper.getInstance().startSpeak("小麦需要完整的手机号呢", null);
                                    }
                                } else if (rootBean.getService().equals("scheduleX")) {
                                    //提醒事件
                                    Log.e(TAG, rootBean.getAnswer().getText());
                                    JsonMessageBean jsonMessageBean=GsonUtils.fromJson(resultStr,JsonMessageBean.class);
                                    sendMessage(new AIUIMessage(AIUIConstant.CMD_CLEAN_DIALOG_HISTORY, 0, 0, null, null));
                                    String action=jsonMessageBean.semantic.get(0).getSlots().get(0).getValue();
                                    String time=jsonMessageBean.semantic.get(0).getSlots().get(1).getValue();
                                    AlarmUtils.matchAlarm(context,time,action);
                                } else if (rootBean.getService().equals("musicPlayer_smartHome")) {
                                    MusicActivity.start(context, "");
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                break;

                case AIUIConstant.EVENT_ERROR: {
                    //错误事件
                    Log.i(TAG, "on event: " + event.eventType);
                    Log.e(TAG, "错误: " + event.arg1 + "\n" + event.info);
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        //语音前端点
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        //语音后端点
                    }
                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
                    //开始录音
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
                    // 停止录音
                }
                break;

                case AIUIConstant.EVENT_STATE: {
                    // 状态事件
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                    }
                }
                break;
                case AIUIConstant.CMD_TTS:
                    Log.e("cmd_tts", event.info);
                    break;

                default:
                    break;
            }
        }
    };


    public CustomAIUIWrapper(Context context) {
        //创建AIUIAgent
        this.context = context;
        mAgent = AIUIAgent.createAgent(context, getAIUIParams(), mAIUIListener);
    }

    private String getAIUIParams() {
        String params = "";
        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

    public void sendMessage(String text) {
        //pers_param用于启用动态实体和所见即可说功能
        String voice = "vcn=xunfeixiaojuan" +  //合成发音人
                ",speed=50" +  //合成速度
                ",pitch=50" +  //合成音调
                ",volume=50";//构建合成参数
        String params = "data_type=text,pers_param={\"appid\":\"\",\"uid\":\"\"}" + "," + voice;
        sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0,
                params, text.getBytes()));
    }

    private void sendMessage(AIUIMessage message) {
        if (mAgent != null) {
            //确保AIUI处于唤醒状态
            if (mAIUIState != AIUIConstant.STATE_WORKING) {
                mAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null));
            }
            mAgent.sendMessage(message);
        }
    }

    public void sendCustomMessage(String content) {
        byte[] ttsData;  //转为二进制数据
        try {
            ttsData = content.getBytes("utf-8");

            //开始合成
            String params = "vcn=xunfeixiaojuan" +  //合成发音人
                    ",speed=50" +  //合成速度
                    ",pitch=50" +  //合成音调
                    ",volume=50";//构建合成参数
//合成音量
            AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params, ttsData);
            mAgent.sendMessage(startTts);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 解析处理翻译结果
     *
     * @param sid
     * @param cntJson
     * @param rspTime
     */
    private void processTranslationResult(String sid, JSONObject cntJson, long rspTime) {
        String text = "";
        try {
            cntJson.put("sid", sid);

            JSONObject transResult = cntJson.optJSONObject("trans_result");
            if (transResult != null && transResult.length() != 0) {
                text = transResult.optString("dst");
            }

            if (TextUtils.isEmpty(text)) {
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("trans_data", cntJson.toString());

            String fakeSemanticResult = fakeSemanticResult(0, "fake.trans", text, null, data);
            if (fakeSemanticResult != null) {
                RawMessage rawMessage = new RawMessage(
                        RawMessage.FromType.AIUI,
                        RawMessage.MsgType.TEXT, fakeSemanticResult.getBytes(), null, rspTime);
                ChatMessageHandler chatMessageHandler = new ChatMessageHandler(context, rawMessage);
                Log.e("chat_message", chatMessageHandler.getFormatMessage());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String fakeSemanticResult(int rc, String service, String answer,
                                            Map<String, String> semantic,
                                            Map<String, String> mapData) {
        try {
            JSONObject data = new JSONObject();
            if (mapData != null) {
                for (String key : mapData.keySet()) {
                    data.put(key, mapData.get(key));
                }
            }

            JSONObject semanticData = new JSONObject();
            if (semantic != null) {
                for (String key : semantic.keySet()) {
                    semanticData.put(key, semantic.get(key));
                }
            }


            JSONObject answerData = new JSONObject();
            answerData.put("text", answer);


            JSONObject fakeResult = new JSONObject();
            fakeResult.put("rc", rc);
            fakeResult.put("answer", answerData);
            fakeResult.put("service", service);
            fakeResult.put("semantic", semanticData);
            fakeResult.put("data", data);

            return fakeResult.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 开始录音
     */
    public void startRecordAudio() {
        if (!mAudioRecording) {
            String params = "data_type=audio,sample_rate=16000";
            //流式识别
            params += ",dwa=wpgs";
            sendMessage(new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null));
            mAudioRecording = true;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecordAudio() {
        if (mAudioRecording) {
            sendMessage(new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, "data_type=audio,sample_rate=16000", null));
            mAudioRecording = false;
        }
    }
}
