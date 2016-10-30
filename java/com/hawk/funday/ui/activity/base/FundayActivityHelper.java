package com.hawk.funday.ui.activity.base;

import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;

/**
 * Created by wangdan on 16/9/21.
 */
public class FundayActivityHelper extends TCLLoginActivityHelper {

    public void login(final OnUserLoginCallback callback, boolean ignore) {
        if (ignore) {
            login(callback);
        }
        else {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_login_title)
                    .positiveText(R.string.dialog_login_ok)
                    .negativeText(R.string.dialog_login_cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            login(callback);
                        }
                    }).show();
        }
    }

}
