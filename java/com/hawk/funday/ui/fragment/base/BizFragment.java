package com.hawk.funday.ui.fragment.base;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;

import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.db.FundayDB;
import com.hawk.funday.support.eventbus.CommentEvent;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.BaseBean;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.support.utils.FundayExceptionDelegate;
import com.hawk.funday.ui.activity.base.FundayActivityHelper;
import com.hawk.funday.ui.activity.base.OnUserLoginCallback;
import com.hawk.funday.ui.widget.FabAnimator;
import com.tma.analytics.TmaAgent;
import com.wcc.framework.notification.NotificationCenter;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.action.IAction;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;

/**
 * 维护功能逻辑类
 *
 * Created by wangdan on 16/8/17.
 */
public class BizFragment extends ABaseFragment {

    private static final String FRAGMENT_TAG = "com.hawk.funday.ui.fragment.base.BizFragment";

    private Activity mActivity;

    private Activity getRealActivity() {
        if (getActivity() != null)
            return getActivity();

        return mActivity;
    }

    private String getRealString(int resId) {
        if (getActivity() != null && getResources() != null) {
            return getString(resId);
        }

        return mActivity.getString(resId);
    }

    @Override
    public int inflateContentView() {
        return -1;
    }

    public static BizFragment createBizFragment(ABaseFragment fragment) {
        try {
            if (fragment != null && fragment.getActivity() != null) {
                BizFragment bizFragment = (BizFragment) fragment.getActivity().getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

                if (bizFragment == null) {
                    bizFragment = new BizFragment();
                    bizFragment.mActivity = fragment.getActivity();
                    fragment.getActivity().getFragmentManager().beginTransaction().add(bizFragment, FRAGMENT_TAG).commit();
                }

                return bizFragment;
            }
        } catch (IllegalStateException e) {

        }

        return null;
    }

    public static BizFragment createBizFragment(Activity activity) {
        BizFragment bizFragment = (BizFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (bizFragment == null) {
            bizFragment = new BizFragment();
            bizFragment.mActivity = activity;

            if (activity instanceof BaseActivity) {
                if (((BaseActivity) activity).isDestory()) {
                    return bizFragment;
                }
            }

            activity.getFragmentManager().beginTransaction().add(bizFragment, FRAGMENT_TAG).commit();
        }
        return bizFragment;
    }

    private FabAnimator fabAnimator;

    public void createFabAnimator(View fabBtn) {
        fabAnimator = FabAnimator.create(fabBtn, GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.fab_scrollthreshold));
    }

    public FabAnimator getFabAnimator() {
        return fabAnimator;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void login(final OnUserLoginCallback callback, boolean ignore) {
        if (getActivity() instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) getActivity();
            if (activity.getActivityHelper() instanceof FundayActivityHelper) {
                FundayActivityHelper activityHelper = (FundayActivityHelper) activity.getActivityHelper();
                activityHelper.login(callback, ignore);
            }
        }
    }

    /**
     * 某操作必须有用户权限，请使用该API
     *
     * @param callback
     */
    public void checkUserPermission(final OnUserPermissionCallback callback) {
        checkUserPermission(callback, false);
    }
    public void checkUserPermission(final OnUserPermissionCallback callback, boolean ignore) {
        OnUserLoginCallback onUserLoginCallback = new OnUserLoginCallback() {

            @Override
            public void onSuccess(AccountBean accountBean) {
                callback.onSuccess(accountBean);

                TmaAgent.onEvent(getActivity(), Consts.Event.Event_profile_login_success);
            }

            @Override
            public void onFaild() {
                callback.onFaild();

                TmaAgent.onEvent(getActivity(), Consts.Event.Event_profile_login_faild);
            }

        };

        new IAction(getRealActivity(), new CheckUserLoginedAction(getRealActivity(), onUserLoginCallback, ignore)) {

            @Override
            public void doAction() {
                if (AppContext.getLoginedAccount() == null) {
                    callback.onFaild();
                }
                else {
                    callback.onSuccess(AppContext.getLoginedAccount());
                }
            }

        }.run();
    }

    class CheckUserLoginedAction extends IAction {

        final OnUserLoginCallback callback;
        final boolean ignore;

        public CheckUserLoginedAction(Activity context, OnUserLoginCallback callback, boolean ignore) {
            super(context, null);
            this.callback = callback;
            this.ignore = ignore;
        }

        @Override
        protected boolean interrupt() {
            if (AppContext.getLoginedAccount() == null) {
                doInterrupt();
                return true;
            }

            return super.interrupt();
        }

        @Override
        public void doInterrupt() {
            login(callback, ignore);
        }

    }


    public interface OnUserPermissionCallback {

        void onSuccess(AccountBean account);

        void onFaild();

    }


    public void destoryComment(final PostBean postBean, final CommentBean comment) {
        if (getActivity() == null)
            return;

        TmaAgent.onEvent(getActivity(), Consts.Event.Event_comment_destory);

        new WorkTask<Void, Void, BaseBean>() {

            @Override
            public BaseBean workInBackground(Void... voids) throws TaskException {
                BaseBean result = FundaySDK.newInstance().doCancelComment(comment.getId());

                postBean.setCommentCount(postBean.getCommentCount() - 1);

                ContentValues values = new ContentValues();
                values.put("commentCount", postBean.getCommentCount());
                String whereClause = " resourceId = ? ";
                String[] whereArgs = new String[]{ postBean.getResourceId() + "" };
                try {
                    FundayDB.getCacheDB().update(PostBean.class, values, whereClause, whereArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onSuccess(BaseBean s) {
                super.onSuccess(s);

                if (getActivity() != null) {
                    ViewUtils.showMessage(getActivity(), R.string.delete_comment_success);
                }

                NotificationCenter.defaultCenter().publish(new CommentEvent(CommentEvent.Type.destory, postBean, comment));
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                if (getActivity() != null) {
                    ViewUtils.showMessage(getActivity(), FundayExceptionDelegate.getMessage(getActivity(), exception, R.string.delete_comment_failure));
                }
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            }

        }.execute();
    }

}
