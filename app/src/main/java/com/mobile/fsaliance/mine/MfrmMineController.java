package com.mobile.fsaliance.mine;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.fsaliance.R;
import com.mobile.fsaliance.common.base.BaseFragmentController;
import com.mobile.fsaliance.common.common.AppMacro;
import com.mobile.fsaliance.common.util.L;
import com.mobile.fsaliance.common.util.LoginUtils;
import com.mobile.fsaliance.common.util.StatusBarUtil;
import com.mobile.fsaliance.common.util.T;
import com.mobile.fsaliance.common.vo.User;
import com.mobile.fsaliance.share.ShareActivity;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;


/**
 * Created by tanyadong on 17/5/15.
 */

public class MfrmMineController extends BaseFragmentController implements MfrmMineView.MfrmMineViewDelegate, OnResponseListener<String> {
    private MfrmMineView mfrmMineView;
    private Object cancelObject = new Object();
    private RequestQueue queue;
    private User user;
    @Override
    protected View onCreateViewFunc(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = null;
        view = inflater.inflate(R.layout.activity_mine_controller,
                null);
        mfrmMineView = (MfrmMineView) view
                .findViewById(R.id.mfrm_mine_view);
        mfrmMineView.setDelegate(this);
        queue = NoHttp.newRequestQueue();
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getActivity().getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(option);
            StatusBarUtil.setStatusBarColor(getActivity(), getResources().getColor(R.color.login_btn_color));
            StatusBarUtil.StatusBarLightMode(getActivity());
        }
        user = LoginUtils.getUserInfo(getContext());
        mfrmMineView.initData(user);
        lazyLoad();
        return view;
    }

    @Override
    protected void getBundleData() {

    }

    @Override
    protected void lazyLoad() {
//        if (user == null) {
//            return;
//        }

    }

    @Override
    public void onResume() {
        super.onResume();
        // 获取网络用户数据
        if (user != null) {
            String userId = user.getId();
            getUserInfoData(userId);
        }

    }

    /**
     * @author tanyadong
     * @Title: getUserInfoData
     * @Description: 获取用户相关信息
     * @date 2018/2/9 0009 20:28
     */

    private void getUserInfoData(String userid) {
        if (TextUtils.isEmpty(userid)) {
            L.e("TextUtils.isEmpty(userid)");
            return;
        }
        String uri = AppMacro.REQUEST_IP_PORT + AppMacro.REQUEST_GOODS_PATH + AppMacro.REQUEST_GET_USER_INFO;
        Request<String> request = NoHttp.createStringRequest(uri);
        request.cancelBySign(cancelObject);
        request.add("userId", userid);
        queue.add(0, request, this);
        L.e("tyd----"+request.url());
    }

    @Override
    public void onClickToUserInfo() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        intent.putExtras(bundle);
        intent.setClass(context, MfrmUserInfoController.class);
        startActivity(intent);
//        getActivity().finish();
    }

    @Override
    public void onClickMyBalance() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmWalletController.class);
        startActivity(intent);
    }

    @Override
    public void onClickInPresent() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmWalletController.class);
        startActivity(intent);
    }

    @Override
    public void onClickHasBalance() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmWalletController.class);
        startActivity(intent);
    }

    @Override
    public void onClickMyOrder() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmMyOrderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickFindMyOrder() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmFindMyOrderController.class);
        startActivity(intent);
    }

    @Override
    public void onClickBoundAlipay() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmBoundAlipayController.class);
        startActivity(intent);
    }

    @Override
    public void onClickShare() {
        Intent intent = new Intent();
        intent.setClass(context, ShareActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickPresentRecord() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmPresentRecordListController.class);
        startActivity(intent);
    }

    @Override
    public void onClickImmediateCash() {
        if (user == null) {
            L.e("user == null");
            return;
        }
        Intent intent = new Intent();
        String myAlipayNum = user.getAliPayAccount();
        if (myAlipayNum == null || "".equals(myAlipayNum)) {
            //跳转填写支付宝界面
            intent.setClass(context, MfrmBoundAlipayController.class);
        } else {
            //跳转提现界面
            intent.setClass(context, MfrmWithdrawalsController.class);
        }
        startActivity(intent);
    }

    @Override
    public void onClickRecordOfIncome() {
        Intent intent = new Intent();
        intent.setClass(context, MfrmIncomeListController.class);
        startActivity(intent);
    }

    /**
     * @author yuanxueyuan
     * @Title: onClickContactUs
     * @Description: 点击联系我们
     * @date 2018/2/27 20:44
     */
    @Override
    public void onClickContactUs() {
        Intent intent = new Intent();
        intent.setClass(context, ContactUsActivity .class);
        startActivity(intent);
    }

    @Override
    public void onStart(int i) {

    }

    @Override
    public void onSucceed(int i, Response<String> response) {
        if (response.responseCode() == AppMacro.RESPONCESUCCESS) {
            String result = (String) response.get();
            if (result == null) {
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.optInt("ret") == 0) {
                    JSONObject jsonObject1 = jsonObject.optJSONObject("content");
                    if (user == null) {
                        user = new User();
                    }
                    user.setShareCode(jsonObject1.optString("SInviteNum"));
                    user.setUserHead(jsonObject1.optString("SUserPic"));
                    user.setPassword(jsonObject1.optString("SPassword"));
                    user.setNickName(jsonObject1.optString("SName"));
                    double anBalance = jsonObject1.optDouble("DCanBalance") * 100;
                    user.setCanPresentMoney((long)anBalance);
                    double cashed = jsonObject1.optDouble("DCashed") * 100;
                    user.setCashed((long)cashed);
                    double cashing = jsonObject1.optDouble("DCashing") * 100;
                    user.setCashing((long)cashing);
                    double balanceNum = jsonObject1.optDouble("DBalanceNum") * 100;
                    user.setBalanceNum((long)balanceNum);
                    user.setScoreNum(jsonObject1.optString("IScoreNum"));
                    user.setAliPayAccount(jsonObject1.optString("SAlipayNum"));
                    user.setPhoneNum(jsonObject1.optString("SPhoneNum"));
                    mfrmMineView.initData(user);
                    //保存用户信息
                    LoginUtils.saveUserInfo(getContext(), user);
                } else {
                    // 不用提示
//                    T.showShort(context, getResources().getString(R.string.get_userinfo_failed));
                }
            } catch (JSONException e) {
                e.printStackTrace();
//                T.showShort(context, getResources().getString(R.string.get_userinfo_failed));
            }

        }
    }

    @Override
    public void onFailed(int i, Response<String> response) {
        Exception exception = response.getException();
//        if (exception instanceof NetworkError) {
//            T.showShort(context, R.string.network_error);
//            return;
//        }
//        if (exception instanceof UnKnownHostError) {
//            T.showShort(context, R.string.network_unknown_host_error);
//            return;
//        }
//        if (exception instanceof SocketTimeoutException) {
//            T.showShort(context, R.string.network_socket_timeout_error);
//            return;
//        }
//        if (exception instanceof ConnectException) {
//            T.showShort(context, R.string.network_error);
//            return;
//        }
//        T.showShort(context, R.string.get_userinfo_failed);
    }

    @Override
    public void onFinish(int i) {

    }
}
