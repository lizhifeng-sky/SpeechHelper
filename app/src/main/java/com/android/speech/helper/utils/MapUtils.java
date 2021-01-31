package com.android.speech.helper.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.speech.helper.MainActivity;
import com.android.speech.helper.WebViewActivity;
import com.android.speech.helper.bean.AddressBean;
import com.android.speech.helper.bean.LocationBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.core.content.ContextCompat;

/**
 * @author lizhifeng
 * @date 2021/1/3 15:45
 */
public class MapUtils {
//    百度地图包名：com.baidu.BaiduMap
//    高德地图包名：com.autonavi.minimap
//    腾讯地图包名：com.tencent.map

    public static final String BAIDU = "com.baidu.BaiduMap";
    public static final String GAODE = "com.autonavi.minimap";
    public static final String TENCENT = "com.tencent.map";

    public static boolean isPackageInstalled(Context mContext, String packagename) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    /**
     * @param addr 查询的地址
     *             https://lbsyun.baidu.com/index.php?title=uri/api/web
     * @throws IOException
     */
    public static LocationBean getCoordinate(String addr) throws IOException {
        String address;
        try {
            address = java.net.URLEncoder.encode(addr, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }
        String key = "0qej8ylSsHg0e3iyDn5uFb1q0Qm1aMLu";
        String url = String.format("https://api.map.baidu.com/geocoding/v3/?address=%s&city=上海&output=json&ak=%s&mcode=E7:13:7D:89:17:F2:71:5C:E5:2F:34:3B:27:5C:CB:1F:23:10:F4:45;com.android.speech.helper", address, key);
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
                AddressBean addressBean = GsonUtils.getGson().fromJson(br.readLine(), AddressBean.class);
                Log.e("map", addressBean.toString());
                return addressBean.getResult().getLocation();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (insr != null) {
                insr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return null;
    }


    public static void openMap(Context mContext, LocationBean locationBean) {
        String type;
        if (isPackageInstalled(mContext, GAODE)) {
            type = GAODE;
        } else if (isPackageInstalled(mContext, BAIDU)) {
            type = BAIDU;
        } else if (isPackageInstalled(mContext, TENCENT)) {
            type = TENCENT;
        } else {
            Toast.makeText(mContext, "尚未安装地图", Toast.LENGTH_SHORT).show();
            return;
        }
        // 百度地图
        if (Objects.equals(type, BAIDU)) {
            Intent intent = new Intent("android.intent.action.VIEW",
                    android.net.Uri.parse("baidumap://map/geocoder?location="
                            + locationBean.getLat() + "," + locationBean.getLng()));
            mContext.startActivity(intent);
        } else if (Objects.equals(type, GAODE)) {
            // 高德地图
            Intent intent = new Intent("android.intent.action.VIEW",
                    android.net.Uri.parse(
                            "androidamap://route?sourceApplication=appName&slat=&slon=&sname=我的位置&dlat="
                                    + locationBean.getLat()
                                    + "&dlon="
                                    + locationBean.getLng() + "&dname=目的地&dev=0&t=2"));
            mContext.startActivity(intent);
        } else {
            // 腾讯地图
            Intent intent = new Intent("android.intent.action.VIEW",
                    android.net.Uri.parse("qqmap://map/routeplan?type=drive&from=&fromcoord=&to=目的地&tocoord="
                            + locationBean.getLat()
                            + ","
                            + locationBean.getLng() + "&policy=0&referer=appName"));
            mContext.startActivity(intent);
        }
    }

    public static void onGetAddress(Context context, String address) {
        new Thread(() -> {
            try {
                String addressStartIndex = null;
                for (int i = 0; i < address.length(); i++) {
                    if (address.substring(0, i).contains("带我去")) {
                        addressStartIndex = address.substring(i);
                        break;
                    }
                }
                if (addressStartIndex != null) {
                    Log.e("匹配结果", "匹配到地址   " + addressStartIndex + "   即将进行跳转地图");
                    LocationBean lng = MapUtils.getCoordinate(addressStartIndex);
                    MapUtils.openMap(context, lng);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    //美食
    public static void poi(Context context, String target) {
        String address = "http://api.map.baidu.com/place/search?query=" + target +
                "&location=31.204055632862,121.41117785465&radius=1000" +
                "&region=上海&output=html&src=webapp.baidu.openAPIdemo";
        WebViewActivity.start(context, address);
    }

    //关键景点匹配
    public static String checkTarget(String text) {
        List<String> stringList = new ArrayList<>();
        stringList.add("海底捞");
        stringList.add("外滩");
        stringList.add("城隍庙");
        stringList.add("东方明珠广播电视塔");
        stringList.add("五角场");
        stringList.add("世纪公园");
        stringList.add("上海迪士尼乐园");
        stringList.add("南京步行街");
        stringList.add("田子坊");
        stringList.add("豫园");
        stringList.add("上海科技馆");
        stringList.add("上海欢乐谷");
        String matchTargetAddress = null;
        for (int i = 0; i < stringList.size(); i++) {
            if (text.contains(stringList.get(i))) {
                matchTargetAddress = stringList.get(i);
                break;
            }
        }
        return matchTargetAddress;
    }
}
