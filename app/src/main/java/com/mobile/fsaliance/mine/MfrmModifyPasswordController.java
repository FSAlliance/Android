package com.mobile.fsaliance.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.fsaliance.R;
import com.mobile.fsaliance.common.base.BaseController;
import com.mobile.fsaliance.common.common.AppMacro;
import com.mobile.fsaliance.common.common.CircleProgressBarView;
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


public class MfrmModifyPasswordController extends BaseController implements OnResponseListener<String>, View.OnClickListener {

    private Object cancelObject = new Object();
    private RequestQueue queue;
    private static final int MODIFY_PWD = 0;
    private User user;
    private EditText originalPwdEdit, newPwdEdit, confirmPwdEdit;
    private TextView confirmModifyTxt;
    private LinearLayout titleLeftLl;
    private ImageView titleLeftImg;
    private CircleProgressBarView circleProgressBarView;
    private String newPassword;//新密码
    @Override
    protected void getBundleData() {
        user = LoginUtils.getUserInfo(this);
    }

    @Override
    protected void onCreateFunc(Bundle savedInstanceState) {
        int result = StatusBarUtil.StatusBarLightMode(this);
        if (result != 0) {
            StatusBarUtil.initWindows(this, getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_modify_password_controller);
        initView();
        addLinster();
        queue = NoHttp.newRequestQueue();
    }

    private void addLinster() {
        confirmModifyTxt.setOnClickListener(this);
        titleLeftLl.setOnClickListener(this);
    }

    private void initView() {
        originalPwdEdit = (EditText) findViewById(R.id.edit_user_original_pwd);
        newPwdEdit = (EditText) findViewById(R.id.edit_user_new_pwd);
        confirmPwdEdit = (EditText) findViewById(R.id.edit_user_confirm_pwd);
        confirmModifyTxt = (TextView) findViewById(R.id.txt_confirm_modify);
        circleProgressBarView = (CircleProgressBarView) findViewById(R.id.circleProgressBarView);
        titleLeftImg = (ImageView) findViewById(R.id.img_back);
        titleLeftImg.setImageResource(R.drawable.goback);
        titleLeftLl = (LinearLayout) findViewById(R.id.ll_title_left);
    }


    @Override
    public void onFailed(int i, Response response) {
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
        T.showShort(this, R.string.modify_pwd_fail);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        queue.cancelBySign(cancelObject);
    }

    @Override
    public void onStart(int i) {
        circleProgressBarView.showProgressBar();
    }

    @Override
    public void onSucceed(int i, Response<String> response) {
        if (response.responseCode() == AppMacro.RESPONCESUCCESS) {
            String result = (String) response.get();
            if (result == null || "".equals(result)) {
                T.showShort(this, R.string.modify_pwd_fail);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("ret") && jsonObject.getInt("ret") == 0) {
                    user.setPassword(newPassword);
                    LoginUtils.saveUserInfo(this, user);
                    T.showShort(this, R.string.modify_pwd_success);
                    finish();
                } else {
                    T.showShort(this, R.string.modify_pwd_fail);
                }
            } catch (JSONException e) {
                T.showShort(this, R.string.modify_pwd_fail);
                e.printStackTrace();
            }
        } else {
            T.showShort(this, R.string.modify_pwd_fail);
        }
    }


    @Override
    public void onFinish(int i) {
        circleProgressBarView.hideProgressBar();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_title_left:
                finish();
                break;
            case R.id.txt_confirm_modify:
                String originalPwd = originalPwdEdit.getText().toString().trim();
                newPassword = newPwdEdit.getText().toString().trim();
                String confirmPwd = confirmPwdEdit.getText().toString().trim();
                if (checkPwd(originalPwd, newPassword, confirmPwd)) {
                    String uri = AppMacro.REQUEST_IP_PORT + AppMacro.REQUEST_GOODS_PATH + AppMacro.REQUEST_UPDATE_PASSWORD;
                    Request<String> request = NoHttp.createStringRequest(uri);
                    request.setCancelSign(cancelObject);
                    request.add("userId",  user.getId());
                    request.add("password",  newPassword);
                    queue.add(MODIFY_PWD, request, this);
                }
                break;
            default:
                break;
        }

    }

    /**
     * @author tanyadong
     * @Title: checkPwd
     * @Description: 校验参数
     * @date 2018/1/2 0002 22:27
     */

    private boolean checkPwd(String originalPwd, String newPwd, String confirmPwd) {
        if (originalPwd.equals("") || newPwd.equals("") || confirmPwd.equals("")) {
            T.showShort(this,R.string.password_is_empty);
            return false;
        }

        if (user == null || user.getPassword() == null) {
            return false;
        }

        if (!user.getPassword().equals(originalPwd)) {
            T.showShort(this,R.string.original_pwd_is_error);
            return false;
        }

        if (!newPwd.equals(confirmPwd)) {
            T.showShort(this, R.string.new_pwd_is_error);
            return false;
        }

        if (newPwd.equals(user.getPassword())) {
            T.showShort(this, R.string.new_pwd_is_equals_original_pwd);
            return false;
        }
        if (newPwd.length() < 6 || newPwd.length() > 16) {
            T.showShort(this, R.string.password_is_short);
            return false;
        }
        return true;
    }
}
