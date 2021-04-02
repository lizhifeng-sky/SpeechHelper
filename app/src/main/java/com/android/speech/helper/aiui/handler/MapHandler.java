package com.android.speech.helper.aiui.handler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.android.speech.helper.aiui.model.Answer;
import com.android.speech.helper.aiui.model.SemanticResult;
import com.android.speech.helper.bean.aiui.JsonRootBean;
import com.android.speech.helper.bean.aiui.Slots;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


public class MapHandler extends IntentHandler {
    private Context context;

    public MapHandler(Context context) {
        super();
        this.context = context;
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        if (!isNativeMapSupport()) {
            return new Answer("亲，你好像还没有安装启用高德地图或百度地图，建议先安装启用该应用，再使用导航技能");
        }

        String intent = result.semantic.optString("intent");

//        Answer location = dealMapIntent(result, intent);
//        if (location != null) return location;

        return new Answer(result.answer);
    }

    public Answer dealMapIntent(JsonRootBean result) {
        switch (result.semantic.get(0).getIntent()) {
            case "LOCATE": {
                String location = optSlotValue(result, "endLoc.ori_loc");
                String landmark = optSlotValue(result, "landmark.ori_loc");
                if (location.equals("CURRENT_ORI_LOC")) {
                    //当前位置查询
                    return currentLocation();
                } else if (!TextUtils.isEmpty(landmark)) {
                    //xxx附近的xxx
                    if (landmark.equals("CURRENT_ORI_LOC")) {
                        //当前位置附近的xxx
                        return nearby(location);
                    } else {
                        //xxx附近的xxx
                        return search(landmark + "附近的" + location);
                    }
                } else {
                    //xxx在哪
                    return search(location);
                }
            }

            case "QUERY": {
                String startLocation = optSlotValue(result, "startLoc.ori_loc");
                if ("CURRENT_ORI_LOC".equals(startLocation)) {
                    //当前位置出发
                    startLocation = null;
                }

                String endLocation = optSlotValue(result, "endLoc.ori_loc");
                String landmark = optSlotValue(result, "landmark.ori_loc");

                if (!TextUtils.isEmpty(landmark) && !landmark.equals("CURRENT_ORI_LOC")) {
                    endLocation = landmark + "附近的" + endLocation;
                }

                return navigation(startLocation, endLocation);
            }
        }
        return null;
    }

    protected String optSlotValue(JsonRootBean result, String slotKey) {
        List<Slots> slots = result.semantic.get(0).getSlots();
        if (slots != null) {
            for (int index = 0; index < slots.size(); index++) {
                String key = slots.get(index).getName();
                if (key.equalsIgnoreCase(slotKey)) {
                    return slots.get(index).getValue();
                }
            }
        }
        return null;
    }

    private boolean isNativeMapSupport() {
        return isBaiduMapSupport() || isAMapSupport();
    }

    private boolean isBaiduMapSupport() {
        return isAvailable("com.baidu.BaiduMap");
    }

    private boolean isAMapSupport() {
        return isAvailable("com.autonavi.minimap");
    }

    private Answer currentLocation() {
        String uri = null;
        String answer = null;
        if (isAMapSupport()) {
            uri = "androidamap://myLocation?sourceApplication=aiui";
            answer = "即将为你打开高德地图";
        } else if (isBaiduMapSupport()) {
            uri = "baidumap://map/show";
            answer = "即将为你打开百度地图";
        }
        context.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    private Answer nearby(String type) {
        String uri = null;
        String answer = null;
        if (isAMapSupport()) {
            uri = "androidamap://arroundpoi?sourceApplication=softname&keywords=" + type;
            answer = "即将为你打开高德地图";
        } else if (isBaiduMapSupport()) {
            uri = "baidumap://map/place/nearby?query=" + type;
            answer = "即将为你打开百度地图";
        }
        context.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    private Answer search(String location) {
        String uri = null;
        String answer = null;
        if (isAMapSupport()) {
            uri = "androidamap://poi?sourceApplication=aiui&keywords=" + location;
            answer = "即将为你打开高德地图";
        } else if (isBaiduMapSupport()) {
            uri = "baidumap://map/place/search?query=" + location;
            answer = "即将为你打开百度地图";
        }
        context.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    private Answer navigation(String startLocation, String endLocation) {
        String uri = null;
        String answer = null;
        if (isAMapSupport()) {
            //不支持设置起点名称
            uri = "androidamap://keywordNavi?sourceApplication=aiui&keyword=" + endLocation + "&style=2";
            answer = "即将为你打开高德地图进行导航";
        } else if (isBaiduMapSupport()) {
            uri = String.format("baidumap://map/direction?%sdestination=%s", startLocation == null ? "" : "origin=" + startLocation + "&", endLocation);
            answer = "即将为你打开百度地图进行导航";
        }
        context.startActivity(new Intent(null, Uri.parse(uri)));
        return new Answer(answer, "");
    }

    /**
     * 判断手机是否安装启用该应用
     *
     * @param packageName 应用包名
     * @return
     */
    public boolean isAvailable(String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();
        if (packages != null) {
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                if (packageInfo.applicationInfo.enabled) {
                    String packName = packageInfo.packageName;
                    packageNames.add(packName);
                }
            }
        }
        return packageNames.contains(packageName);
    }

}
