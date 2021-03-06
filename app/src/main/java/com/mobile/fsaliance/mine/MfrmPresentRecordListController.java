package com.mobile.fsaliance.mine;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobile.fsaliance.R;
import com.mobile.fsaliance.common.base.BaseController;
import com.mobile.fsaliance.common.common.AppMacro;
import com.mobile.fsaliance.common.common.CircleProgressBarView;
import com.mobile.fsaliance.common.common.InitApplication;
import com.mobile.fsaliance.common.util.L;
import com.mobile.fsaliance.common.util.LoginUtils;
import com.mobile.fsaliance.common.util.StatusBarUtil;
import com.mobile.fsaliance.common.util.T;
import com.mobile.fsaliance.common.vo.IncomeRecord;
import com.mobile.fsaliance.common.vo.PresentRecord;
import com.mobile.fsaliance.common.vo.User;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static android.view.View.INVISIBLE;


public class MfrmPresentRecordListController extends BaseController implements View.OnClickListener, BGARefreshLayout.BGARefreshLayoutDelegate, OnResponseListener<String> {

    private Object cancelObject = new Object();
    private RequestQueue queue;
    private ListView presentReccordListview;
    private ImageView incomeListBackImg;
    private TextView titleTxt, noData;
    private LinearLayout titleLiftLl, titleRightLl;
    private BGARefreshLayout refreshLayout;
    private int pageSize = 20;
    private int pageNo = 0;
    private PresentRecordListViewAdapter presentRecordListViewAdapter;
    public CircleProgressBarView circleProgressBarView;
    private boolean refreshList = false;
    private boolean loadMoreList = false;
    private List<PresentRecord> presentRecordList;
    private User user;
    @Override
    protected void getBundleData() {

    }

    @Override
    protected void onCreateFunc(Bundle savedInstanceState) {
        int result = StatusBarUtil.StatusBarLightMode(this);
        if (result != 0) {
            StatusBarUtil.initWindows(this, getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_presentlist_controller);
        user = LoginUtils.getUserInfo(this);
        loadMoreList = false;
        refreshList = false;
        queue = NoHttp.newRequestQueue();
        presentRecordList = new ArrayList<>();
        initView();
        addListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresentListData(0, pageSize);
    }

    private void initView() {
        initRefresh();
        presentReccordListview = (ListView) findViewById(R.id.presentrecord_list_view);
        circleProgressBarView = (CircleProgressBarView) findViewById(R.id.circleProgressBarView);
        titleLiftLl = (LinearLayout) findViewById(R.id.ll_title_left);
        titleRightLl = (LinearLayout) findViewById(R.id.ll_title_right);
        titleRightLl.setVisibility(INVISIBLE);
        incomeListBackImg = (ImageView) findViewById(R.id.img_back);
        incomeListBackImg.setImageResource(R.drawable.goback);
        noData = (TextView) findViewById(R.id.txt_present_list_no_data);
        titleTxt = (TextView) findViewById(R.id.txt_title);
        titleTxt.setText(getResources().getString(R.string.ming_present_record));
    }

    private void initRefresh() {
        refreshLayout = (BGARefreshLayout)findViewById(R.id.presentrecord_freshlayout);
        refreshLayout.setDelegate(this);
        //true代表开启上拉加载更多
        BGANormalRefreshViewHolder bgaNormalRefreshViewHolder = new BGANormalRefreshViewHolder(this, true);
        refreshLayout.setRefreshViewHolder(bgaNormalRefreshViewHolder);
    }

    private void addListener() {
        titleLiftLl.setOnClickListener(this);
    }

    /**
     * @author tanyadong
     * @Title: getIncomeListData
     * @Description: 获取提现记录
     * @date 2018/1/14 0014 13:00
     */

    public void getPresentListData( int pageNo, int pageSize) {
        if (user == null) {
            return;
        }
        String uri = AppMacro.REQUEST_IP_PORT + AppMacro.REQUEST_GOODS_PATH + AppMacro.REQUEST_GET_PRESENT_RECORD;
        Request<String> request = NoHttp.createStringRequest(uri);
        request.cancelBySign(cancelObject);
        request.add("userId", user.getId());
        request.add("pageNo", pageNo);
        request.add("pageSize", pageSize);
        queue.add(0, request, this);
        L.e("tyd---"+request.url());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        queue.cancelBySign(cancelObject);
    }


    /**
     * @author tanyadong
     * @Title showPresentRecordList
     * @Description 刷新并显示数据
     * @date 2017/9/8 14:44
     */
    public void showPresentRecordList(List<PresentRecord> presentRecordList) {
        if (presentRecordList == null) {
            L.e("presentRecordList == null");
            return;
        }
        if (presentRecordListViewAdapter == null) {
            presentRecordListViewAdapter = new PresentRecordListViewAdapter(this,
                    presentRecordList);
            presentReccordListview.setAdapter(presentRecordListViewAdapter);
        } else {
            presentRecordListViewAdapter.update(presentRecordList);
            presentRecordListViewAdapter.notifyDataSetChanged();
        }
    }

    private void reloadNoDataList() {
        if (presentRecordList == null || presentRecordList.size() <= 0) {
            noData.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_title_left:
                finish();
                break;
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        refreshList = true;
        getPresentListData(0,pageSize);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        loadMoreList = true;
        getPresentListData(pageNo,pageSize);
        return true;
    }

    @Override
    public void onStart(int i) {
        if (refreshList == true || loadMoreList == true) {
            return;
        }
        circleProgressBarView.showProgressBar();
    }

    @Override
    public void onSucceed(int i, Response<String> response) {
        if (response.responseCode() == AppMacro.RESPONCESUCCESS) {
            String result = (String) response.get();
            presentRecordList = analyzeListData(result);
            if (presentRecordList != null) {
                showPresentRecordList(presentRecordList);
            }
        } else {
            T.showShort(this, R.string.get_record_failed);
            reloadNoDataList();
        }
    }

    /**
     * @author tanyadong
     * @Title: analyzeOrderListData
     * @Description: 解析提现记录
     * @date 2018/1/11 0011 22:33
     */

    private ArrayList<PresentRecord> analyzeListData(String result) {
        if (!loadMoreList) {
            if (presentRecordList != null) {
                presentRecordList.clear();
            }
        }
        if (null == result || "".equals(result)) {
            L.e("result == null");
            T.showShort(this, R.string.get_record_failed);
            reloadNoDataList();
            return null;
        }

        ArrayList<PresentRecord> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("ret")) {
                int ret = jsonObject.optInt("ret");
                if (ret == 0) {
                    JSONArray jsonArray = jsonObject.optJSONArray("content");

                    if (jsonArray == null || "".equals(jsonArray)) {
                        T.showShort(this, R.string.get_record_failed);
                        return null;
                    }
                    if (jsonArray.length() <= 0) {
                        if (loadMoreList) {
                            T.showShort(this, R.string.check_asset_no_more);
                        }
                        else {
                            reloadNoDataList();
                        }
                        return null;
                    } else {
                        pageNo++;
                        noData.setVisibility(View.GONE);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        PresentRecord presentRecord = new PresentRecord();
                        JSONObject presentRecotdJson = (JSONObject) jsonArray.get(i);
                        presentRecord.setId(presentRecotdJson.optString("id"));
                        presentRecord.setState(presentRecotdJson.optInt("state"));
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                         presentRecord.setPresentTime(df.format(df.parse(presentRecotdJson.getString("presentTime"))));
                        presentRecord.setPresentMoneny(presentRecotdJson.optString("presentMoneny"));
                        list.add(presentRecord);
                    }
                } else {
                    reloadNoDataList();
                    T.showShort(this, R.string.get_record_failed);
                }
            } else {
                reloadNoDataList();
                T.showShort(this, R.string.get_record_failed);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reloadNoDataList();
            T.showShort(this, R.string.get_record_failed);
        }
        return list;
    }

    @Override
    public void onFailed(int i, Response<String> response) {
        if (refreshList == true) {
            if (presentRecordList != null) {
                presentRecordList.clear();
            }
        }
        Exception exception = response.getException();
        reloadNoDataList();
        if (exception instanceof NetworkError) {
            T.showShort(InitApplication.getInstance().getApplicationContext(), R.string.network_error);
            return;
        }
        if (exception instanceof SocketTimeoutException) {
            T.showShort(InitApplication.getInstance().getApplicationContext(), R.string.network_socket_timeout_error);
            return;
        }
        if (exception instanceof UnKnownHostError) {
            T.showShort(InitApplication.getInstance().getApplicationContext(), R.string.network_unknown_host_error);
            return;
        }
        if (exception instanceof ConnectException) {
            T.showShort(InitApplication.getInstance().getApplicationContext(), R.string.network_error);
            return;
        }
        T.showShort(this, R.string.get_record_failed);
    }

    @Override
    public void onFinish(int i) {
        refreshLayout.endRefreshing();
        refreshLayout.endLoadingMore();
        circleProgressBarView.hideProgressBar();
        refreshList = false;
        loadMoreList = false;
    }
}
