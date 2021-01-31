package com.android.speech.helper.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author lizhifeng
 * @date 2021/1/5 22:28
 */
public class ContactUtils {
    /**
     * 拨打电话（直接拨打电话）
     */
    public static void callPhone(Context context, String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) {
            Toast.makeText(context, "电话号码为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        context.startActivity(intent);
    }

    /**
     * 发短信
     * @param phoneNum
     */
    public static void sendSMS(Context context, String phoneNum) {
        Uri smsToUri = Uri.parse("smsto:" + phoneNum);
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
