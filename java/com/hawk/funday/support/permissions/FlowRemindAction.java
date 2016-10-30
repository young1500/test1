package com.hawk.funday.support.permissions;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppSettings;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.support.action.IAction;

/**
 * Created by yijie.ma on 2016/9/14.
 */
public class FlowRemindAction extends IAction {

    private long length;

    public FlowRemindAction(Activity context, long length) {
        super(context, null);

        this.length = length;
    }

    @Override
    protected boolean interrupt() {
        if (AppSettings.isFlowRemind(getContext()) &&
//                length >= AppSettings.getAlowMaxFlowLength() &&
                SystemUtils.getNetworkType(getContext()) == SystemUtils.NetWorkType.mobile) {
            doInterrupt();
            return true;
        } else {
            return super.interrupt();
        }
    }

    @Override
    public void doInterrupt() {
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getContext());

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // 允许
                                getChild().doAction();
                                AppSettings.setFlowRemind(getContext(), false);
                            }

                        });


        builder.onNegative(new MaterialDialog.SingleButtonCallback() {

                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // 取消播放

                            }

                        });
        builder.positiveText(R.string.video_play);
        builder.negativeText(R.string.video_play_cancel);
        builder.content(R.string.video_play_tips);
        builder.contentColor(getContext().getResources().getColor(R.color.comm_black));
        builder.build().show();

    }

}
