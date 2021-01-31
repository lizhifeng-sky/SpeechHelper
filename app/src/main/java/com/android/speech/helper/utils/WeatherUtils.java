package com.android.speech.helper.utils;

import android.util.Log;

import com.android.speech.helper.bean.WeatherBean;
import com.android.speech.helper.bean.WeatherDateBean;
import com.iflytek.SimpleSpeakListenerImpl;
import com.iflytek.SpeakHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author lizhifeng
 * @date 2021/1/4 13:42
 * https://www.tianqiapi.com/user/index
 */
public class WeatherUtils {
    /**
     * @return
     */
    public static void getWeather(String city) {
        new Thread(() -> {
            String url = String.format("https://www.tianqiapi.com/api?version=v9&appid=18152514&appsecret=zPbnC3kL&city=%s", city);
            URL myURL = null;
            URLConnection httpsConn = null;
            try {
                myURL = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            InputStreamReader insr = null;
            BufferedReader br = null;
            try {
                if (myURL != null) {
                    httpsConn = myURL.openConnection();// 不使用代理
                }
                if (httpsConn != null) {
                    insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");
                    br = new BufferedReader(insr);
                    WeatherBean weatherBean = GsonUtils.getGson().fromJson(br.readLine(), WeatherBean.class);
                    Log.e("weather", weatherBean.toString());
                    StringBuilder weatherSpeakString = new StringBuilder();
                    WeatherDateBean weatherDateBean;
                    weatherDateBean = weatherBean.getData().get(0);
                    weatherSpeakString
                            .append(weatherBean.getCity())
                            .append("今日天气")
                            .append(weatherDateBean.getWea())
                            .append("空气质量")
                            .append(weatherDateBean.getAir_level())
                            .append("最高温度")
                            .append(weatherDateBean.getTem1())
                            .append("最低温度")
                            .append(weatherDateBean.getTem2())
                            .append(weatherDateBean.getWin().get(0))
                            .append("风力")
                            .append(weatherDateBean.getWin_speed())
                            .append(weatherDateBean.getAir_tips());
                    SpeakHelper.getInstance().startSpeak(weatherSpeakString.toString(), new SimpleSpeakListenerImpl());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (insr != null) {
                    try {
                        insr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
