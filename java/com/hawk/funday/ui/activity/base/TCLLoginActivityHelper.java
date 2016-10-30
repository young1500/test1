package com.hawk.funday.ui.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.analytics.StatEvent;
import com.hawk.funday.support.analytics.Stats;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.sdk.bean.TokenBean;
import com.hawk.funday.support.sdk.bean.TokenUserBean;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.support.utils.TokenUtils;
import com.hawk.funday.ui.fragment.profile.RegisterProfileEditFragment;
import com.tcl.example.tcllogin.LoginTclManager;
import com.tcl.example.tcllogin.custom.CallBackInterface;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.ui.activity.basic.BaseActivityHelper;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/8/22 21:15
 * @copyright HAWK
 */

public class TCLLoginActivityHelper extends BaseActivityHelper {

    private static final String TAG = "TCLLoginHelper";

    private TokenUtils mTokenUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTokenUtils = new TokenUtils(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void login(final OnUserLoginCallback callback) {
        Logger.d(TAG + "_Account", "Request Account SDK");

        doTCLLogin(callback);
    }

    private void doTCLLogin(final OnUserLoginCallback callback) {
        if (getActivity() != null) {
            LoginTclManager.getInstance().startLoginActivity(getActivity(), new CallBackInterface() {
                @Override
                public void onSuccess(String string) {
                    Log.d(TAG + "_Account", "SDK Success string = " + string);
                    final TokenBean tokenBean = JSON.parseObject(string, new TypeReference<TokenBean>() {});
                    if (tokenBean != null) {
                        final TokenUserBean tokenUserBean = tokenBean.getUser();
                        final String token = tokenBean.getToken();
                        if (!TextUtils.isEmpty(token)
                                && tokenUserBean != null && !TextUtils.isEmpty(tokenUserBean.getAccountName())) {
                            new WorkTask<String, Void, AccountBean>() {

                                @Override
                                protected void onPrepare() {
                                    super.onPrepare();

                                    ViewUtils.createProgressDialog(getActivity(), getActivity().getString(R.string.dialog_logining), FundayUtils.getThemeColor(getActivity())).show();
                                }

                                @Override
                                public AccountBean workInBackground(String[] params) throws TaskException {
                                    try {
                                        FundayUserBean userBean = FundaySDK.newInstance().getUserByToken(params[0], params[1]); // 从服务器获取UserInfo
                                        AccountBean accountBean = new AccountBean();
                                        accountBean.setUser(userBean);
                                        accountBean.setToken(token);
                                        accountBean.setOpenId(tokenUserBean.getAccountName());
                                        mTokenUtils.saveAccountBean(accountBean);
                                        AppContext.setLoginedAccountBean(accountBean);

                                        Logger.d(TAG + "_Account", "User Logined");

                                        return accountBean;
                                    } catch (TaskException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onSuccess(AccountBean accountBean) {
                                    super.onSuccess(accountBean);

                                    if (accountBean == null) {
                                        callback.onFaild();
                                    } else {
                                        callback.onSuccess(accountBean);
                                        if (getActivity() != null) {
                                            if (TextUtils.isEmpty(accountBean.getUser().getName()) ||
                                                    accountBean.getUser().getName().equals(accountBean.getUser().getEmail())) {
                                                RegisterProfileEditFragment.launch(getActivity(), accountBean);
                                            }
                                        }
                                    }
                                }

                                @Override
                                protected void onFinished() {
                                    super.onFinished();

                                    ViewUtils.dismissProgressDialog();
                                }

                            }.execute(tokenUserBean.getAccountName(), token);
                        } else {
                            callback.onFaild();
                        }
                    } else {
                        callback.onFaild();
                    }
                }

                @Override
                public void onFailed(String string) {
                    Log.d(TAG + "_Account", "SDK onFailed string = " + string);
                    callback.onFaild();
                }
            });
        }
    }

}
