package com.hawk.funday.ui.widget;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hawk.funday.R;

import org.aisen.android.network.task.TaskException;
import org.aisen.android.support.inject.InjectUtility;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.ABaseFragment;
import org.aisen.android.ui.fragment.APagingFragment;
import org.aisen.android.ui.fragment.itemview.AFooterItemView;

import java.io.Serializable;

/**
 * Created by wangdan on 16/8/30.
 */
public class FundayFooterItemView<T extends Serializable> extends AFooterItemView<T> {

    public static final int LAYOUT_RES = R.layout.lay_footerview;

    private View footerView;

    @ViewInject(id = R.id.layBtn)
    View layBtn;
    @ViewInject(id = R.id.btnMore)
    TextView btnMore;
    @ViewInject(id = R.id.progressLoading)
    View progressLoading;


    public FundayFooterItemView(Activity context, View itemView, OnFooterViewCallback callback) {
        super(context, itemView, callback);

        this.footerView = itemView;

        InjectUtility.initInjectedView(getContext(), this, getConvertView());

        progressLoading.setVisibility(View.VISIBLE);
        btnMore.setVisibility(View.GONE);

        btnMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getCallback() != null && getCallback().canLoadMore()) {
                    getCallback().onLoadMore();
                }
            }

        });
    }

    @Override
    public void onBindView(View convertView) {

    }

    @Override
    public void onBindData(View convertView, T data, int position) {
    }

    @Override
    public View getConvertView() {
        return footerView;
    }

    @Override
    public void onTaskStateChanged(AFooterItemView<?> footerItemView, ABaseFragment.ABaseTaskState state, TaskException exception, APagingFragment.RefreshMode mode) {
        if (state == ABaseFragment.ABaseTaskState.finished) {
            if (getCallback().canLoadMore()) {
                int height = getContext().getResources().getDimensionPixelSize(R.dimen.comm_footer_height);
                if (layBtn.getHeight() != height) {
                    layBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
                }
            }
            else {
                layBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
            }
        }
        else if (state == ABaseFragment.ABaseTaskState.prepare) {
            if (getCallback().canLoadMore()) {
                progressLoading.setVisibility(View.VISIBLE);
                btnMore.setVisibility(View.GONE);
            }
        }
        else if (state == ABaseFragment.ABaseTaskState.success) {

        }
        else if (state == ABaseFragment.ABaseTaskState.falid) {
            if (mode == APagingFragment.RefreshMode.update) {
                if (getCallback().canLoadMore()) {
                    btnMore.setText(faildText());
                    btnMore.setVisibility(View.VISIBLE);
                    progressLoading.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void setFooterViewToRefreshing() {
        if (getCallback().canLoadMore()) {
            getCallback().onLoadMore();
        }
    }

    protected String faildText() {
        return getContext().getString(org.aisen.android.R.string.comm_footer_faild);
    }

}
