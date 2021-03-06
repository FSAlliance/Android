package com.mobile.fsaliance.common.vo;

import java.io.Serializable;

/**
 * Created by 78326 on 2017.9.9.
 */
public class User implements Serializable{
    private String id;//序号
    private String phoneNum;//用户名
    private String nickName; //昵称
    private String password;//密码
    private String userHead; //头像
    private String aliPayAccount; //支付宝账号
    private String  shareCode; //推荐码
    private String  scoreNum; //积分
    private long  cashing; //提现中
    private long  cashed; //已提现
    private long balanceNum;//余额
    private long canPresentMoney;//可提现金额
    private String myOrder;//订单编号
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserHead() {
        return userHead;
    }

    public void setUserHead(String userHead) {
        this.userHead = userHead;
    }

    public String getAliPayAccount() {
        return aliPayAccount;
    }

    public void setAliPayAccount(String aliPayAccount) {
        this.aliPayAccount = aliPayAccount;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getScoreNum() {
        return scoreNum;
    }

    public void setScoreNum(String scoreNum) {
        this.scoreNum = scoreNum;
    }

    public long getCashing() {
        return cashing;
    }

    public void setCashing(long cashing) {
        this.cashing = cashing;
    }

    public long getCashed() {
        return cashed;
    }

    public void setCashed(long cashed) {
        this.cashed = cashed;
    }

    public long getBalanceNum() {
        return balanceNum;
    }

    public void setBalanceNum(long balanceNum) {
        this.balanceNum = balanceNum;
    }

    public String getMyOrder() {
        return myOrder;
    }

    public void setMyOrder(String myOrder) {
        this.myOrder = myOrder;
    }

    public long getCanPresentMoney() {
        return canPresentMoney;
    }

    public void setCanPresentMoney(long canPresentMoney) {
        this.canPresentMoney = canPresentMoney;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", nickName='" + nickName + '\'' +
                ", password='" + password + '\'' +
                ", userHead='" + userHead + '\'' +
                ", aliPayAccount='" + aliPayAccount + '\'' +
                ", shareCode='" + shareCode + '\'' +
                ", scoreNum='" + scoreNum + '\'' +
                ", cashing=" + cashing +
                ", cashed=" + cashed +
                ", balanceNum=" + balanceNum +
                ", canPresentMoney=" + canPresentMoney +
                ", myOrder='" + myOrder + '\'' +
                '}';
    }
}
