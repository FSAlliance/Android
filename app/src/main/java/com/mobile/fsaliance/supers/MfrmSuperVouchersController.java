package com.mobile.fsaliance.supers;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.fsaliance.R;
import com.mobile.fsaliance.common.base.BaseFragmentController;
import com.mobile.fsaliance.common.common.AppMacro;
import com.mobile.fsaliance.common.util.L;
import com.mobile.fsaliance.common.util.T;
import com.mobile.fsaliance.common.vo.Asset;

import com.mobile.fsaliance.goods.MfrmGoodsInfoController;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


public class MfrmSuperVouchersController extends BaseFragmentController implements
		MfrmSuperVouchersView.MfrmMineAssetDelegate, OnResponseListener {
	private MfrmSuperVouchersView mfrmSuperVouchersView;
	private RequestQueue queue;
	private static final int GET_ASSET_LIST = 0;
	private Object cancelObject = new Object();
	private List<Asset> assetList;
	private boolean refreshList = false;
	private boolean loadMoreList = false;
	private boolean mHasLoadedOnce;
	private boolean isPrepared;

	private static final int PAGE_SIZE = 10;//每页数据条数
	private static final int FIRST_PAGE = 0;//第几页
	private int pageNo = 0;
	private int lastCount = 0;//上次请求数据个数

	@Override
	protected View onCreateViewFunc(LayoutInflater inflater,
									ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_super_vouchers_controller,null);
		mfrmSuperVouchersView = (MfrmSuperVouchersView) view.findViewById(R.id.mfrm_super_vouchers_view);
		mfrmSuperVouchersView.setDelegate(this);
		queue = NoHttp.newRequestQueue();
		assetList = new ArrayList<>();
		isPrepared = true;
		refreshList = false;
		loadMoreList = false;
		lazyLoad();
		return view;
	}

	private void getSearchAssetData(String param, int pageNo) {
		String uri = AppMacro.REQUEST_URL + "/asset/query";
		Request<String> request = NoHttp.createStringRequest(uri);
		request.cancelBySign(cancelObject);
		request.add("param", param);
		request.add("page", pageNo);
		request.add("limit", PAGE_SIZE);
		queue.add(GET_ASSET_LIST, request, this);
	}

	@Override
	protected void getBundleData() {

	}

	@Override
	protected void lazyLoad() {
		if (!isPrepared || !isVisible || mHasLoadedOnce) {
			return;
		}
		getSearchAssetData("", FIRST_PAGE);
		mHasLoadedOnce = true;
	}


	/**
	 * @author tanyadong
	 * @Title: onResume
	 * @Description: 生命周期重进入
	 * @date 2016-9-19 下午6:57:28
	 */
	@Override
	public void onResume() {
		super.onResume();
		lazyLoad();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		queue.cancelBySign(cancelObject);
	}

	/**
	 * @param asset 数据
	 * @author yuanxueyuan
	 * @Title: onClickToDetail
	 * @Description: 点击详情
	 * @date 2017/12/26 22:08
	 */
	@Override
	public void onClickToDetail(Asset asset) {
		Intent intent = new Intent();
		intent.setClass(this.getContext(), MfrmGoodsInfoController.class);
		startActivity(intent);
	}


	 /**
	 * @author  yuanxueyuan
	 * @Title:  pullDownRefresh
	 * @Description: 下拉刷新
	 * @date 2017/12/26 22:09
	 */
	@Override
	public void pullDownRefresh() {
		refreshList = true;
		getSearchAssetData("", FIRST_PAGE);
	}


	/**
	 * @author yuanxueyuan
	 * @Title: onClickLoadMore
	 * @Description: 上拉加载更多
	 * @date 2017/12/26 22:09
	 */
	@Override
	public void onClickLoadMore() {
		loadMoreList = true;
		getSearchAssetData("", pageNo);
	}

	@Override
	public void onStart(int i) {
		if (refreshList == true || loadMoreList == true) {
			return;
		}
		if (mfrmSuperVouchersView.circleProgressBarView != null) {
			mfrmSuperVouchersView.circleProgressBarView.showProgressBar();
		}
	}

	@Override
	public void onSucceed(int i, Response response) {
		if (response.responseCode() == AppMacro.RESPONCESUCCESS) {
			String result = (String) response.get();
			assetList = analyzeAssetsData(result);
			mfrmSuperVouchersView.showMyAssetList(assetList);
		}
	}

	/**
	 * @author tanyadong
	 * @Title analyzeAssetsData
	 * @Description 解析查询到的资产
	 * @date 2017/9/9 20:57
	 */
	private List<Asset> analyzeAssetsData(String result) {
		if (!loadMoreList) {
			if (assetList != null) {
				assetList.clear();
			}
		}
		if (null == result || "".equals(result)) {
			T.showShort(this.getContext(), R.string.get_myasset_failed);
			reloadNoDataList();
			L.e("result == null");
			return null;
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.has("code") && jsonObject.optInt("code") == 0) {
				JSONArray jsonArray = jsonObject.optJSONArray("content");
				mfrmSuperVouchersView.isLoadMore = true;
				if (jsonArray.length() <= 0) {
					if (loadMoreList) {
						mfrmSuperVouchersView.isLoadMore = false;
						T.showShort(this.getContext(), R.string.check_asset_no_more);
					} else {
						reloadNoDataList();
					}
					return null;
				} else {
					mfrmSuperVouchersView.setNoDataView(false);
				}
				if (assetList == null){
					assetList = new ArrayList<>();
				}
				int arrCount = 0;
				if (assetList != null) {
					arrCount = assetList.size();
				}
				if (jsonArray.length() >= PAGE_SIZE) {
					pageNo++;
				} else {
					if (lastCount < PAGE_SIZE && arrCount > 0) {
						int index = (pageNo - 1) * PAGE_SIZE;//开始从某一位移除
						for (int i = index; i < arrCount; i++) {
							if (i >= index && i < index + lastCount) {
								if (index < assetList.size()){
									assetList.remove(index);
								}
							}
						}
					}
				}
				for (int i = 0; i < jsonArray.length(); i++) {
					Asset asset = new Asset();
					JSONObject jsonObjectContent = jsonArray.getJSONObject(i);
					asset.setState(jsonObjectContent.getInt("state"));
					asset.setType(jsonObjectContent.getString("type"));
					asset.setCodeId(jsonObjectContent.getString("codeId"));
					asset.setJobId(jsonObjectContent.getString("jobId"));
					asset.setUserName(jsonObjectContent.optString("user"));
					asset.setName(jsonObjectContent.getString("name"));
					asset.setBoard(jsonObjectContent.getString("board"));
					asset.setBox(jsonObjectContent.getString("box"));
					asset.setBuild(jsonObjectContent.getString("build"));
					asset.setCenter(jsonObjectContent.getString("center"));
					asset.setCost(jsonObjectContent.getString("cost"));
					asset.setCostIt(jsonObjectContent.getString("costIt"));
					asset.setCount(jsonObjectContent.getString("count"));
					asset.setCpu(jsonObjectContent.getString("cpu"));
					asset.setDisk(jsonObjectContent.getString("disk"));
					asset.setFloor(jsonObjectContent.getString("floor"));
					asset.setHardDriver(jsonObjectContent.getString("hardDriver"));
					asset.setLeavePlace(jsonObjectContent.getString("leavePlace"));
					asset.setMemory(jsonObjectContent.getString("memory"));
					asset.setModel(jsonObjectContent.getString("model"));
					asset.setMoney(jsonObjectContent.getString("money"));
					asset.setOther(jsonObjectContent.getString("other"));
					asset.setPart(jsonObjectContent.getString("part"));
					asset.setPlace(jsonObjectContent.getString("place"));
					asset.setRealPlace(jsonObjectContent.getString("realPlace"));
					asset.setSaver(jsonObjectContent.getString("saver"));
					asset.setRealSaver(jsonObjectContent.getString("realSaver"));
					asset.setPrice(jsonObjectContent.getString("price"));
					asset.setSoftDriver(jsonObjectContent.getString("softDriver"));
					asset.setTime(jsonObjectContent.getString("time"));
					asset.setVideoCard(jsonObjectContent.getString("videoCard"));
					assetList.add(asset);
				}
				lastCount = jsonArray.length();
			} else {
				T.showShort(this.getContext(), R.string.get_myasset_failed);
				reloadNoDataList();
				return null;
			}
		} catch (JSONException e) {
			T.showShort(this.getContext(), R.string.get_myasset_failed);
			reloadNoDataList();
			e.printStackTrace();
		}
		return  assetList;
	}


	/**
	 * @author tanyadong
	 * @Title reloadNoDataList
	 * @Description 无数据刷新列表
	 * @date 2017/9/9 20:59
	 */
	private void reloadNoDataList() {
		if (assetList == null || assetList.size() <= 0) {
			mfrmSuperVouchersView.setNoDataView(true);
			mfrmSuperVouchersView.showMyAssetList(assetList);
		}
	}


	@Override
	public void onFailed(int i, Response response) {
		if (refreshList == true) {
			if (assetList != null) {
				assetList.clear();
			}
		}
		Exception exception = response.getException();
		reloadNoDataList();
		if (exception instanceof NetworkError) {
			T.showShort(this.getContext(), R.string.network_error);
			return;
		}
		if (exception instanceof UnKnownHostError) {
			T.showShort(this.getContext(), R.string.network_unknown_host_error);
			return;
		}
		if (exception instanceof SocketTimeoutException) {
			T.showShort(this.getContext(), R.string.network_socket_timeout_error);
			return;
		}
		if (exception instanceof ConnectException) {
			T.showShort(this.getContext(), R.string.network_error);
			return;
		}
		T.showShort(this.getContext(), R.string.login_failed);
	}

	@Override
	public void onFinish(int i) {
		mfrmSuperVouchersView.circleProgressBarView.hideProgressBar();
		mfrmSuperVouchersView.endRefreshLayout();
		refreshList = false;
		loadMoreList = false;
	}


}