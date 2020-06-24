package com.duzhaokun123.bilibilihd.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.duzhaokun123.bilibilihd.R;
import com.duzhaokun123.bilibilihd.databinding.ActivityLoginBinding;
import com.duzhaokun123.bilibilihd.pbilibiliapi.api.PBilibiliClient;
import com.duzhaokun123.bilibilihd.pbilibiliapi.utils.BilibiliApiExceptionUtil;
import com.duzhaokun123.bilibilihd.bases.BaseActivity;
import com.duzhaokun123.bilibilihd.utils.BrowserUtil;
import com.duzhaokun123.bilibilihd.utils.GeetestUtil;
import com.duzhaokun123.bilibilihd.utils.GsonUtil;
import com.duzhaokun123.bilibilihd.utils.LoginUserInfoMap;
import com.duzhaokun123.bilibilihd.utils.Settings;
import com.duzhaokun123.bilibilihd.utils.ToastUtil;
import com.hiczp.bilibili.api.passport.model.LoginResponse;
import com.hiczp.bilibili.api.retrofit.exception.BilibiliApiException;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private PBilibiliClient pBilibiliClient = PBilibiliClient.Companion.getInstance();
    private long uid;

    @Override
    protected int initConfig() {
        return NEED_HANDLER | FIX_LAYOUT;
    }

    @Override
    public int initLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        baseBind.btnLogin.setOnClickListener(v -> new Thread() {
            @Override
            public void run() {
                LoginResponse loginResponse = null;
                try {
                    loginResponse = pBilibiliClient.login(baseBind.etUsername.getText().toString(), baseBind.etPassword.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof BilibiliApiException && ((BilibiliApiException) e).getCommonResponse().getCode() == -105) {
                        Log.d("LoginActivity", "here");
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("url", BilibiliApiExceptionUtil.Companion.getGeetestUrl((BilibiliApiException) e));
                        message.what = 0;
                        message.setData(bundle);
                        if (handler != null) {
                            handler.sendMessage(message);
                        }
                    }
                    runOnUiThread(() -> ToastUtil.sendMsg(LoginActivity.this, e.getMessage()));
                }

                if (loginResponse != null) {
                    if (loginResponse.getData().getUrl() == null) {
                        uid = loginResponse.getUserId();
                        LoginUserInfoMap loginUserInfoMap = Settings.getLoginUserInfoMap();
                        loginUserInfoMap.put(loginResponse.getUserId(), loginResponse);
                        loginUserInfoMap.setLoggedUid(loginResponse.getUserId());
                        Settings.saveLoginUserInfoMap();
                        if (handler != null) {
                            handler.sendEmptyMessage(1);
                        }
                    } else {
                        pBilibiliClient.getBilibiliClient().setLoginResponse(null);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("url", loginResponse.getData().getUrl());
                        message.what = 0;
                        message.setData(bundle);
                        if (handler != null) {
                            handler.sendMessage(message);
                        }
                    }
                }
            }
        }.start());
    }

    @Override
    public void initData() {

    }

    @Override
    public void handlerCallback(@NonNull Message msg) {
        switch (msg.what) {
            case 0:
                GeetestUtil.doTest(LoginActivity.this, msg.getData().getString("url"));
                break;
            case 1:
                finish();
                break;
        }
    }
}
