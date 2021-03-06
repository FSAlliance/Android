package com.mobile.fsaliance.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.mobile.fsaliance.R;
import com.mobile.fsaliance.common.base.BaseController;
import com.mobile.fsaliance.common.common.AppMacro;
import com.mobile.fsaliance.common.common.InitApplication;
import com.mobile.fsaliance.common.util.L;
import com.mobile.fsaliance.common.util.LoginUtils;
import com.mobile.fsaliance.common.util.StatusBarUtil;
import com.mobile.fsaliance.common.util.T;
import com.mobile.fsaliance.common.vo.User;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;


public class MfrmLoginController extends BaseController implements MfrmLoginView.MfrmLoginViewDelegate, OnResponseListener<String> {

    private MfrmLoginView mfrmLoginView;
    private Object cancelObject = new Object();
    private RequestQueue queue;
    private static final int LOGON_IN = 0;
    @Override
    protected void getBundleData() {

    }

    @Override
    protected void onCreateFunc(Bundle savedInstanceState) {
        int result = StatusBarUtil.StatusBarLightMode(this);
        if (result != 0) {
            StatusBarUtil.initWindows(this, getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_login_controller);
        mfrmLoginView = (MfrmLoginView) findViewById(R.id.activity_login_view);
        mfrmLoginView.setDelegate(this);
        queue = NoHttp.newRequestQueue();
    }

    /**
      * @author tanyadong
      * @Title onClickLogin
      * @Description 点击登录
      * @date 2017/9/6 22:16
    */
    @Override
    public void onClickLogin(String phoneNum, String password) {
        if (phoneNum == null || "".equals(phoneNum) || password == null || "".equals(password)) {
            L.e("username == null || password == null");
            return;
        }
        String uri = AppMacro.REQUEST_IP_PORT + AppMacro.REQUEST_GOODS_PATH +AppMacro.REQUEST_LOGIN;
        Request<String> request = NoHttp.createStringRequest(uri);
        request.setCancelSign(cancelObject);
        request.add("phoneNum", phoneNum);
        request.add("password", password);
        queue.add(LOGON_IN, request, this);
        L.e("tyd--"+request.url());
    }
    /**
      * @author tanyadong
      * @Title onClickRegister
      * @Description 点击注册
      * @date 2017/9/6 22:15
    */
    @Override
    public void onClickRegister() {
        Intent intent = new Intent(this, MfrmRegisterController.class);
        startActivity(intent);
    }


    @Override
    public void onFailed(int i, Response response) {
        mfrmLoginView.circleProgressBarView.hideProgressBar();
        Exception exception = response.getException();
        if (exception instanceof NetworkError) {
            T.showShort(this, R.string.network_error);
            return;
        }
        if (exception instanceof UnKnownHostError) {
            T.showShort(this, R.string.network_unknown_host_error);
            return;
        }
        if (exception instanceof SocketTimeoutException) {
            T.showShort(this, R.string.network_socket_timeout_error);
            return;
        }
        T.showShort(this, R.string.login_failed);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        queue.cancelBySign(cancelObject);
    }

    @Override
    public void onStart(int i) {
        if (mfrmLoginView.circleProgressBarView != null) {
            mfrmLoginView.circleProgressBarView.showProgressBar();
        }
    }

    @Override
    public void onSucceed(int i, Response<String> response) {
        if (response.responseCode() == AppMacro.RESPONCESUCCESS) {
            String result = (String) response.get();
            if (result == null || "".equals(result)) {
                T.showShort(this, R.string.login_failed);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                int ret = -1;
                if (jsonObject.has("ret")) {
                    ret = jsonObject.getInt("ret");
                }
                if (ret == AppMacro.GET_DATA_RET_SUCCESS) {
                    JSONObject jsonUser = jsonObject.optJSONObject("content");
                    User user = new User();
                    user.setId(jsonUser.optString("SUserId"));
                    user.setPhoneNum(jsonUser.optString("SPhoneNum"));
                    user.setPassword(jsonUser.optString("SPassword"));
                    user.setNickName(jsonUser.optString("SName"));
                    user.setAliPayAccount(jsonUser.optString("SAlipayNum"));
                    user.setShareCode(jsonUser.optString("SInviteNum"));
                    user.setUserHead(jsonUser.optString("SUserPic"));
                    user.setScoreNum(jsonUser.optString("IScoreNum"));
                    double anBalance = jsonUser.optDouble("DCanBalance") * 100;
                    user.setCanPresentMoney((long)anBalance);
                    double cashing = jsonUser.optDouble("DCashing") * 100;
                    user.setCashing((long)cashing);
                    double cashed = jsonUser.optDouble("DCashed") * 100;
                    user.setCashed((long)cashed);
                    LoginUtils.saveUserInfo(this, user);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("id",1);
                    startActivity(intent);
                    finish();
                } else if (ret == AppMacro.GET_DATA_RET_NO_USERNAME) {
                    //没有此用户
                    T.showShort(this, R.string.login_failed_no_username);
                } else if (ret == AppMacro.GET_DATA_RET_NO_PASSWORD) {
                    //密码不正确
                    T.showShort(this, R.string.login_failed_no_password);
                } else {
                    T.showShort(this, R.string.login_failed);
                }
            } catch (JSONException e) {
                T.showShort(this, R.string.login_failed);
                e.printStackTrace();
            }
        } else {
            T.showShort(this, R.string.login_failed);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFinish(int i) {
        if (mfrmLoginView.circleProgressBarView != null) {
            mfrmLoginView.circleProgressBarView.hideProgressBar();
        }
    }
}
