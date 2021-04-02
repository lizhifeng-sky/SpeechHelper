package com.android.speech.helper.aiui.handler;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.speech.helper.aiui.model.Answer;
import com.android.speech.helper.aiui.model.SemanticResult;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


/**
 * 打电话技能处理类，用户级动态实体示例
 */

public class SMSHandler extends IntentHandler {
    private static List<String> numberRecords = new ArrayList<>();
    private static String pendingContent = null;
    private Context context;
    public SMSHandler(Context context) {
        super();
        this.context=context;
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        //没有找到联系人，提示上传本地联系人
        if(result.answer.contains("没有找到")) {
            StringBuilder builder = new StringBuilder();
            builder.append(result.answer);
            builder.append(NEWLINE);
            builder.append(NEWLINE);
            builder.append("<a href=\"upload_contact\">上传本地联系人数据</a>");
            return new Answer(builder.toString(), result.answer);
        }

        //处理电话技能
        String intent = result.semantic.optString("intent");
        switch (intent) {
            case "":
            case "SEND": {
                JSONArray data = result.data.optJSONArray("result");
                numberRecords.clear();
                if(data != null && data.length() > 0) {
                    for(int index = 0; index < data.length(); index++) {
                        String phoneNumber = data.optJSONObject(index).optString("phoneNumber");
                        if(!TextUtils.isEmpty(phoneNumber)) {
                            numberRecords.add(phoneNumber);
                        }
                    }
                }

                String content = optSlotValue(result, "content");
                if(!TextUtils.isEmpty(content)) {
                    pendingContent = content;
                }

                //多个号码显示
                if(numberRecords.size() > 1) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(result.answer);
                    builder.append(NEWLINE);
                    builder.append(NEWLINE);

                    for(int index = 0; index < numberRecords.size(); index++) {
                        builder.append((index + 1) + ". " + numberRecords.get(index));
                        builder.append(NEWLINE);
                    }

                    return new Answer(builder.toString(), result.answer);
                }
            }
            break;

            case "INSTRUCTION": {
                String insType = optSlotValue(result, "insType");
                switch (insType) {
                    case "CONFIRM": {
                        if(numberRecords.size() > 0) {
                            String phone = numberRecords.get(0);
                            messageTo(phone);
                        }
                    }
                    break;

                    case "SEQUENCE": {
                        String content = optSlotValue(result, "content");
                        if(!TextUtils.isEmpty(content)) {
                            pendingContent = content;
                        }

                        String direct = optSlotValue(result, "posRank.direct");

                        //选择范围解析
                        int offset = 1;
                        try {
                            offset = Integer.parseInt(optSlotValue(result, "posRank.offset"));
                        } catch (NumberFormatException e) {
                            return new Answer("请在有效的范围内选择");
                        }

                        if(offset > numberRecords.size()) {
                            return new Answer("请在有效的范围内选择");
                        }

                        String phone = null;
                        switch (direct) {
                            case "+": {
                                phone = numberRecords.get(offset - 1);
                            }
                            break;

                            case "-": {
                                phone = numberRecords.get(numberRecords.size() - offset);
                            }
                            break;
                        }

                        if(phone != null && !TextUtils.isEmpty(pendingContent)) {
                            messageTo(phone);
                        }
                    }
                    break;
                }
            }
            break;
        }

        return new Answer(result.answer);
    }

    private void messageTo(String phone) {
        Uri smsToUri = Uri.parse("smsto:" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", "");
        //避免有的手机无法打开短信奔溃
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(context, "no activity", Toast.LENGTH_SHORT).show();
        }
    }
}
