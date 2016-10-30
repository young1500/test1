package com.hawk.funday.support.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hawk.funday.ui.widget.FundayFooterItemView;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.ui.fragment.itemview.AFooterItemView;
import org.aisen.android.ui.fragment.itemview.IITemView;
import org.aisen.android.ui.fragment.itemview.IItemViewCreator;

import java.io.Serializable;

/**
 * Created by wangdan on 16/9/12.
 */
public class ViewUtils {

    public static void showMessage(Activity context, TaskException exception) {
        try {
            if (TaskException.TaskError.noneNetwork.toString().equals(exception.getCode()) ||
                    TaskException.TaskError.socketTimeout.toString().equals(exception.getCode()) ||
                    TaskException.TaskError.timeout.toString().equals(exception.getCode()) ||
                    TaskException.TaskError.failIOError.toString().equals(exception.getCode()) ||
                    TaskException.TaskError.resultIllegal.toString().equals(exception.getCode())) {

            }
            else if (TaskException.TaskError.socketTimeout.toString().equals(exception.getCode())) {

            } else if (TaskException.TaskError.timeout.toString().equals(exception.getCode())) {

            } else {

            }
        } catch (Exception e) {
            Logger.printExc(ViewUtils.class, e);
        }
    }

    public static void showMessage(Activity context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showMessage(Activity context, int messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    public static void setupSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if (GlobalContext.getInstance() != null) {
            int color = GlobalContext.getInstance().getResources().getColor(org.aisen.android.R.color.comm_black);

            swipeRefreshLayout.setColorSchemeColors(color, color, color, color);
        }
    }

    public static  <T extends Serializable> IItemViewCreator<T> configFooterViewCreator(final Activity activity, final AFooterItemView.OnFooterViewCallback callback) {
        return new IItemViewCreator<T>() {

            @Override
            public View newContentView(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return inflater.inflate(FundayFooterItemView.LAYOUT_RES, parent, false);
            }

            @Override
            public IITemView<T> newItemView(View convertView, int viewType) {
                return new FundayFooterItemView<T>(activity, convertView, callback);
            }

        };
    }

}
