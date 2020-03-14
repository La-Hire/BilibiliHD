package com.duzhaokun123.bilibilihd.mybilibiliapi.dynamic;

import android.icu.text.SimpleDateFormat;

import com.duzhaokun123.bilibilihd.mybilibiliapi.MyBilibiliClient;
import com.duzhaokun123.bilibilihd.mybilibiliapi.dynamic.model.DynamicPage;
import com.duzhaokun123.bilibilihd.mybilibiliapi.dynamic.model.NestedCard;
import com.duzhaokun123.bilibilihd.pbilibiliapi.api.PBilibiliClient;
import com.google.gson.Gson;
import com.hiczp.bilibili.api.passport.model.LoginResponse;
import com.hiczp.bilibili.api.retrofit.CommonResponse;
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException;

import java.util.Map;

public class DynamicAPI {

    private static DynamicAPI dynamicAPI;

    public static DynamicAPI getDynamicAPI() {
        if (dynamicAPI == null) {
            dynamicAPI = new DynamicAPI();
        }
        return dynamicAPI;
    }

    public static NestedCard getNestedCard(String card) {
        Gson gson = new Gson();
        return gson.fromJson(card, NestedCard.class);
    }

    private DynamicAPI() {
    }

    private PBilibiliClient pBilibiliClient;
    private Gson gson;
    private SimpleDateFormat simpleDateFormat;

    public void getDynamic(int page, MyBilibiliClient.Callback<DynamicPage> callback) {
        getDynamic(page,"" , callback);
    }

    public void getDynamic(int page,String offsetDynamicId, MyBilibiliClient.Callback<DynamicPage> callback) {
        if (pBilibiliClient == null) {
            pBilibiliClient = PBilibiliClient.Companion.getPBilibiliClient();
        }
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("YYYYMMDDhhmm000ss");
        }
        LoginResponse loginResponse = pBilibiliClient.getBilibiliClient().getLoginResponse();
        try {
            String response = MyBilibiliClient.getMyBilibiliClient().getResponse(new MyBilibiliClient.GetRequest() {
                @Override
                public String getUrl() {
                    return "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";
                }

                @Override
                public void addUserParams(Map<String, String> paramsMap) {
                    paramsMap.put("from", "feed");
                    paramsMap.put("mobi_app", "android");
                    paramsMap.put("offset_dynamic_id", offsetDynamicId);
                    paramsMap.put("page", String.valueOf(page));
                    paramsMap.put("qn", "32");
                    paramsMap.put("rsp_type", "2");
                    paramsMap.put("src", "bilih5");
                    paramsMap.put("statistics", "%7B%22appId%22%3A1%2C%22platform%22%3A3%2C%22version%22%3A%225.54.0%22%2C%22abtest%22%3A%22%22%7D");
                    paramsMap.put("type_list", "268435455");
                    paramsMap.put("video_meta", "fourk%3A1%2Cfnval%3A16%2Cfnver%3A0%2Cqn%3A32");
                    if (loginResponse != null) {
                        paramsMap.put("uid", String.valueOf(loginResponse.getUserId()));
                    }
                    paramsMap.put("trace_id", simpleDateFormat.format(paramsMap.get("ts")));
                }
            });
            if (gson == null) {
                gson = new Gson();
            }
            DynamicPage dynamicPage = gson.fromJson(response, DynamicPage.class);
            if (dynamicPage.getCode() != 0) {
                throw new BilibiliApiException(new CommonResponse(
                        dynamicPage.getCode(),
                        dynamicPage.getMsg(),
                        dynamicPage.getMsg(),
                        System.currentTimeMillis(),
                        null,
                        dynamicPage.getTtl()
                ));
            }
            callback.onSuccess(dynamicPage);
        } catch (Exception e) {
            callback.onException(e);
        }

    }


}
