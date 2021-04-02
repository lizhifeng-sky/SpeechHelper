package com.android.speech.helper.aiui.handler;

import android.content.Context;
import android.text.TextUtils;

import com.android.speech.helper.aiui.model.Answer;
import com.android.speech.helper.aiui.model.RawMessage;
import com.android.speech.helper.aiui.model.SemanticResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 聊天消息处理类
 * <p>
 * 通过service将解析分配到不同的IntentHandler，通过getFormatMessage返回处理后的格式化内容
 */

public class ChatMessageHandler{
    private static final String KEY_SEMANTIC = "semantic";
    private static final String KEY_OPERATION = "operation";
    private static final String SLOTS = "slots";
    private static Map<String, Class<?>> handlerMap = new HashMap<>();
    private static Answer latestTTSAnswer = null;

    static {
        handlerMap.put("weather", WeatherHandler.class);
        handlerMap.put("message", SMSHandler.class);
        handlerMap.put("telephone", TelephoneHandler.class);
        handlerMap.put("mapU", MapHandler.class);
    }

    public static Answer getLatestTTSAnswer() {
        return latestTTSAnswer;
    }


    private RawMessage mMessage;
    private SemanticResult mSemanticResult;
    private IntentHandler mHandler;
    private Context context;


    public ChatMessageHandler(Context context,RawMessage message) {
        this.mMessage = message;
        this.context=context;
    }

    public String getFormatMessage() {
        if (mMessage.fromType == RawMessage.FromType.USER) {
            //用户消息
            if (mMessage.msgType == RawMessage.MsgType.TEXT) {
                return new String(mMessage.msgData);
            } else {
                return "";
            }
        } else {
            initHandler();
//            if(!mSemanticResult.service.startsWith("fake.") && !TextUtils.isEmpty(mSemanticResult.answer)){
//                //这种情况下会下发语义后合成音频，先暂停播放，后面根据情况再进一步处理
//                mPlayer.pauseTTS();
//            }
            if (mHandler != null) {
                Answer answer =  mHandler.getFormatContent(mSemanticResult);
                //不是本地构造的结果并且处理后结果的合成内容和Answer相同，直接继续之前暂停语义后合成音频，不使用主动语音合成
                mSemanticResult.answer = answer.getAnswer();
                //保存最后一次可播放的Answer，用于后面 再说一次 问法的处理
                if(!TextUtils.isEmpty(answer.getTTSContent())) {
                    latestTTSAnswer = answer;
                }
                return answer.getAnswer();
            } else {
                return "错误";
            }
        }
    }

    private void initHandler() {
        if (mMessage.fromType == RawMessage.FromType.USER) {
            return;
        }

        initSemanticResult();

        if (mHandler == null) {
            //根据语义结果的service查找对应的IntentHandler，并实例化
            Class<?> handlerClass = handlerMap.get(mSemanticResult.service);
            if (handlerClass == null) {
                return;
            }
            try {
                Constructor<?> constructor = handlerClass.getConstructor(Context.class);
                mHandler = (IntentHandler) constructor.newInstance(context);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void initSemanticResult() {
        if (mSemanticResult != null) return;
        // 解析语义结果
        JSONObject semanticResult;
        mSemanticResult = new SemanticResult();
        try {
            semanticResult = new JSONObject(new String(mMessage.msgData));
            mSemanticResult.rc = semanticResult.optInt("rc");
            if (mSemanticResult.rc == 4) {
                mSemanticResult.service = "fake.unknown";
            } else if (mSemanticResult.rc == 1) {
                mSemanticResult.service = semanticResult.optString("service");
                mSemanticResult.answer = "语义错误";
            } else {
                mSemanticResult.service = semanticResult.optString("service");
                mSemanticResult.answer = semanticResult.optJSONObject("answer") == null ?
                        "已为您完成操作" : semanticResult.optJSONObject("answer").optString("text");
                // 兼容3.1和4.0的语义结果，通过判断结果最外层的operation字段
                boolean isAIUI3_0 = semanticResult.has(KEY_OPERATION);
                if (isAIUI3_0) {
                    //将3.1语义格式的语义转换成4.1
                    JSONObject semantic = semanticResult.optJSONObject(KEY_SEMANTIC);
                    if (semantic != null) {
                        JSONObject slots = semantic.optJSONObject(SLOTS);
                        JSONArray fakeSlots = new JSONArray();
                        Iterator<String> keys = slots.keys();
                        while (keys.hasNext()) {
                            JSONObject item = new JSONObject();
                            String name = keys.next();
                            item.put("name", name);
                            item.put("value", slots.get(name));

                            fakeSlots.put(item);
                        }

                        semantic.put(SLOTS, fakeSlots);
                        semantic.put("intent", semanticResult.optString(KEY_OPERATION));
                        mSemanticResult.semantic = semantic;
                    }
                } else {
                    mSemanticResult.semantic = semanticResult.optJSONArray(KEY_SEMANTIC) == null ?
                            semanticResult.optJSONObject(KEY_SEMANTIC) :
                            semanticResult.optJSONArray(KEY_SEMANTIC).optJSONObject(0);
                }
                mSemanticResult.answer = mSemanticResult.answer.replaceAll("\\[[a-zA-Z0-9]{2}\\]", "");
                mSemanticResult.data = semanticResult.optJSONObject("data");
                if(mSemanticResult.data == null) {
                    mSemanticResult.data = new JSONObject();
                }
            }
        } catch (JSONException e) {
            mSemanticResult.rc = 4;
            mSemanticResult.service = "fake.unknown";
        }
    }
}
