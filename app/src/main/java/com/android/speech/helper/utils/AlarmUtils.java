package com.android.speech.helper.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import static android.provider.CalendarContract.ACCOUNT_TYPE_LOCAL;

/**
 * @author lizhifeng
 * @date 2021/1/4 14:25
 * <p>
 * < uses-permission android:name=“com.android.alarm.permission.SET_ALARM” />
 */
public class AlarmUtils {
    //添加闹钟

    public static boolean matchAlarm(Context context, String time,String action) {
            int day;
            if (time.contains("今天")) {
                day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            } else if (time.contains("明天")) {
                int tims = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                day = (tims + 1) % 7;
            } else {
                return false;
            }
        String substring = time.substring(4);
        String replace = substring.replace("点", "");
        createAlarm(context, day,action, Integer.parseInt(replace), 0);
            return true;
    }

    //提醒我 （周一至周日）（早上/上午/下午/晚上）（0-23）点（事件描述）
    private static void createAlarm(Context context, int day, String message, int hour, int minutes) {
        ArrayList<Integer> testDays = new ArrayList<>();
        testDays.add(Calendar.MONDAY);//周一
        testDays.add(Calendar.TUESDAY);//周二
        testDays.add(Calendar.FRIDAY);//周五

        String packageName = context.getPackageName();
//        Uri ringtoneUri = Uri.parse("android.resource://" + packageName + "/" + resId);
        //action为AlarmClock.ACTION_SET_ALARM
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                //闹钟的小时
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                //闹钟的分钟
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                //响铃时提示的信息
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                //用于指定该闹铃触发时是否振动
                .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                //一个 content: URI，用于指定闹铃使用的铃声，也可指定 VALUE_RINGTONE_SILENT 以不使用铃声。
                //如需使用默认铃声，则无需指定此 extra。
//                .putExtra(AlarmClock.EXTRA_RINGTONE, ringtoneUri)
                //一个 ArrayList，其中包括应重复触发该闹铃的每个周日。
                // 每一天都必须使用 Calendar 类中的某个整型值（如 MONDAY）进行声明。
                //对于一次性闹铃，无需指定此 extra
                .putExtra(AlarmClock.EXTRA_DAYS, testDays)
                //如果为true，则调用startActivity()不会进入手机的闹钟设置界面
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    //添加日程
//    <uses-permission android:name="android.permission.READ_CALENDAR"/>
//    <uses-permission android:name="android.permission.WRITE_CALENDAR"/>

    private static String CALENDARS_NAME = "yjb";
    private static String CALENDARS_ACCOUNT_NAME = "yjb@gmail.com";
    //    private static String CALENDARS_ACCOUNT_TYPE = "com.android.exchange";
    private static String CALENDARS_DISPLAY_NAME = "yjb_user";


    //检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
    public static int checkAndAddCalendarAccount(Context context) {
        int oldId = checkCalendarAccount(context);
        if (oldId >= 0) {
            return oldId;
        } else {
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                return checkCalendarAccount(context);
            } else {
                return -1;
            }
        }
    }

    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);

        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
//        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        //事件不可编辑、删除
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        return result == null ? -1 : ContentUris.parseId(result);
    }

    private static int checkCalendarAccount(Context context) {
        try (Cursor userCursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null,
                null, null, null)) {
            if (userCursor == null)//查询返回空值
                return -1;
            int count = userCursor.getCount();
            if (count > 0) {//存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        }
    }

    public static void insert(Context context,
                              long startTime,
                              long endTime, String desc) {
        //静默插入日历事件提醒
        int calID = checkAndAddCalendarAccount(context);
        if (calID < 0) {
            return;
        }
        Calendar beginC = Calendar.getInstance();
        beginC.setTimeInMillis(startTime);
        Calendar endC = Calendar.getInstance();
        endC.setTimeInMillis(endTime);

        ContentValues eValues = new ContentValues();  //插入事件
        TimeZone tz = TimeZone.getDefault();//获取默认时区

        //插入日程
        eValues.put(CalendarContract.Events.DTSTART, beginC.getTimeInMillis());
        eValues.put(CalendarContract.Events.DTEND, endC.getTimeInMillis());
        eValues.put(CalendarContract.Events.TITLE, "会议提醒");
        eValues.put(CalendarContract.Events.DESCRIPTION, desc);
        eValues.put(CalendarContract.Events.CALENDAR_ID, calID);
        eValues.put(CalendarContract.Events.EVENT_LOCATION, "");
        eValues.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
        eValues.put(CalendarContract.Events.GUESTS_CAN_MODIFY, true);
        Uri uri = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, eValues);

        //插完日程之后必须再插入以下代码段才能实现提醒功能
        ContentValues rValues = new ContentValues();  //插入提醒，与事件配合起来才有效
        String myEventsId = uri.getLastPathSegment(); // 得到当前表的_id
        rValues.put(CalendarContract.Reminders.MINUTES, 15);
        rValues.put(CalendarContract.Reminders.EVENT_ID, Long.parseLong(myEventsId));
        rValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, rValues);
    }
}
