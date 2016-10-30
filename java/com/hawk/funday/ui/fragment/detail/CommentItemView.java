package com.hawk.funday.ui.fragment.detail;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.BaseBean;
import com.hawk.funday.support.sdk.bean.CommentBean;
import com.hawk.funday.support.utils.FundayExceptionDelegate;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.ui.fragment.base.BizFragment;
import com.hawk.funday.ui.fragment.profile.ProfilePagerFragment;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.component.cardmenu.CardMenuBuilder;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.adapter.ARecycleViewItemView;
/**
 * @Description: CommentItemView 评论列表Item类
 * @author  qiangtai.huang
 * @date  2016/8/22
 * @copyright TCL-HAWK
 */
public class CommentItemView extends ARecycleViewItemView<CommentBean> implements View.OnClickListener {

    @ViewInject(id = R.id.txtUserName)
    TextView txtUserName;
    @ViewInject(id = R.id.txtContent)
    TextView txtContent;
    @ViewInject(id = R.id.txtCreate)
    TextView txtCreate;
    @ViewInject(id = R.id.btn_more)
    ImageButton moreBtn;

    private CommentListFragment owner;

    public CommentItemView(Activity context, View itemView, CommentListFragment owner) {
        super(context, itemView);
        this.owner = owner;
    }

    @Override
    public void onBindData(View view, CommentBean commentBean, int i) {
        txtUserName.setTag(commentBean);
        txtUserName.setText(commentBean.getUser().getName());
        txtUserName.setOnClickListener(this);
        txtContent.setText(commentBean.getContent());
        txtCreate.setText(FundayUtils.convDate(commentBean.getPublishTime()));
        if (moreBtn!=null) {
            moreBtn.setOnClickListener(this);
            moreBtn.setTag(commentBean);
        }
    }

    @Override
    public void onClick(View v) {
        final CommentBean bean = (CommentBean) v.getTag();
        switch (v.getId())
        {
            case R.id.txtUserName:
                TmaAgent.onEvent(getContext(), Consts.Event.Event_comment_profile_click);

                ProfilePagerFragment.launch(getContext(), bean.getUser());
                break;
            case R.id.btn_more:
                showOverflowMenu(v, bean);
                break;
        }
    }

    private void showOverflowMenu(final View v, final CommentBean bean) {
        if (bean.getUser() == null) {
            return;
        }

        final int deleteId = 100;
        final int reportId = 101;

        // 是否已经登录，且当前的评论是登录用户发布的评论
        if (AppContext.getLoginedAccount() != null && AppContext.getLoginedAccount().getUser() != null &&
                AppContext.getLoginedAccount().getUser().getId() == bean.getUser().getId()) {
            // 删除评论
            new CardMenuBuilder(getContext(), v, FundayUtils.getCardMenuOptions(R.style.AppTheme))
                    .add(deleteId, R.string.delete_comment)
                    .setOnCardMenuCallback(new CardMenuBuilder.OnCardMenuCallback() {

                        @Override
                        public boolean onCardMenuItemSelected(MenuItem menuItem) {
                            if (menuItem.getItemId() == deleteId) {
                                cancelComfire(bean);
                            }
                            return true;
                        }

                    })
                    .show();
        }
        // 举报评论
        else {
            CardMenuBuilder cardMenuBuilder = new CardMenuBuilder(getContext(), v, FundayUtils.getCardMenuOptions(R.style.AppTheme));
            SubMenu subMenu = cardMenuBuilder.addSubMenu(1, reportId, 1, getContext().getString(R.string.report_comment));
            String[] subItems = getContext().getResources().getStringArray(R.array.report_content_type);
            for (int i = 0; i < subItems.length; i++) {
                cardMenuBuilder.addSubMenuItem(subMenu, 1, i, 1, subItems[i]);
            }
            cardMenuBuilder.getOptions().setGravity(Gravity.NO_GRAVITY);
            cardMenuBuilder.setOnCardMenuCallback(new CardMenuBuilder.OnCardMenuCallback() {

                @Override
                public boolean onCardMenuItemSelected(final MenuItem menuItem) {
                    if (menuItem.getItemId() == reportId) {
                        // 点击了SubMenu
                    }
                    else {
                        // 检查用户权限
                        BizFragment.createBizFragment(getContext()).checkUserPermission(new BizFragment.OnUserPermissionCallback() {

                            @Override
                            public void onSuccess(AccountBean account) {
                                reportComment(menuItem.getItemId(), bean);
                            }

                            @Override
                            public void onFaild() {

                            }

                        });
                    }
                    return true;
                }

            })
            .show();
        }
    }
    private void cancelComfire(final CommentBean bean){
        new MaterialDialog.Builder(getContext())
                .content(R.string.cancel_comment_tip)
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (owner != null) {
                            owner.destoryComment(bean);
                        }
                    }
                })
                .show();
    }

    private void reportComment(final int typeIndex, final CommentBean bean){
        final String[] reportTypeArr = new String[]{ "RI", "PV" };

        if (typeIndex >= reportTypeArr.length) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("type", reportTypeArr[typeIndex]);
        TmaAgent.onEvent(getContext(), Consts.Event.Event_comment_report, args);

        new WorkTask<Void,Void, BaseBean>(){

            @Override
            protected void onPrepare() {
                super.onPrepare();

                ViewUtils.createProgressDialog(getContext(), getContext().getString(R.string.reporting_post), FundayUtils.getThemeColor(getContext())).show();
            }

            @Override
            public BaseBean workInBackground(Void... voids) throws TaskException {

                return FundaySDK.newInstance().doReportComment( bean.getId(), reportTypeArr[typeIndex]);

            }
            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                ViewUtils.showMessage(getContext(), FundayExceptionDelegate.getMessage(getContext(), exception, R.string.report_comment_failure));
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                ViewUtils.dismissProgressDialog();
            }

            @Override
            protected void onSuccess(BaseBean s) {
                super.onSuccess(s);

                ViewUtils.showMessage(getContext(), R.string.report_comment_success);
            }

        }.execute();
    }

}
