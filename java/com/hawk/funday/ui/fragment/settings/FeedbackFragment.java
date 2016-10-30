package com.hawk.funday.ui.fragment.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.BaseBean;
import com.hawk.funday.support.utils.FundayExceptionDelegate;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;

import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;

/**
 * @author Liyang Sun
 * @Description:
 * @date 2016/8/25 10:14
 * @copyright HAWK
 */

public class FeedbackFragment extends ABaseFragment {
    @ViewInject (id = R.id.text_input_ly)
    TextInputLayout mTextInputLayout;
    @ViewInject (id = R.id.feedback_input_et)
    TextInputEditText mTextInputEditText;

    private static final String TAG = "FeedbackFragment";
    private int mTotalCharCountNum = 200;
    private String mFeedbackString;

    public static void launch(Activity from) {
        ContainerActivity.launch(from, FeedbackFragment.class, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setTitle(R.string.title_feedback);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (TextUtils.isEmpty(mFeedbackString)) {
            menu.findItem(R.id.toolbar_feedback_submit).setEnabled(false);
        } else if (mFeedbackString.length() > mTotalCharCountNum) {
            menu.findItem(R.id.toolbar_feedback_submit).setEnabled(false);
        } else {
            menu.findItem(R.id.toolbar_feedback_submit).setEnabled(true);
        }
    }

    @Override
    public boolean onBackClick() {
        if (isFeedbackValid()) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_feedback_back_click_saved_reminder_title)
                    .positiveText(R.string.dialog_feedback_back_click_saved_reminder_ok)
                    .negativeText(R.string.dialog_feedback_back_click_saved_reminder_cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            uploadFeedback(mFeedbackString);

//                            MainStats.logClick(StatEvent.FeedbackFragment.SUBMIT_FEEDBACK_FROM_BACK_CLICK_REMINDER);

                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }
                    }).show();
        } else {
            return super.onBackClick();
        }
        return true;
    }

    @Override
    public boolean onHomeClick() {
        return super.onHomeClick();
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_feedback;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

        setHasOptionsMenu(true);

        mTextInputLayout.setCounterEnabled(true);
        mTextInputLayout.setCounterMaxLength(mTotalCharCountNum);
        mTextInputLayout.setHintEnabled(false);

        mTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence typing, int start, int before, int count) {
                if (getActivity() != null) {
                    getActivity().invalidateOptionsMenu();
                }
            }

            @Override
            public void afterTextChanged(Editable typed) {
                mFeedbackString = typed.toString();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_feedback, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_feedback_submit:

                if (isFeedbackValid()) {
                    uploadFeedback(mFeedbackString);
                }
//                else {
//                    showMessage(error);
//                }
                break;

            default:
                break;
        }

        return true;
    }

    private void uploadFeedback(final String content) {
        new WorkTask<Void, Void, BaseBean>() {
            @Override
            protected void onPrepare() {
                super.onPrepare();

                if (getActivity() != null) {
                    ViewUtils.createProgressDialog(getActivity(), getResources().getString(R.string.dialog_submit_feedback_title), FundayUtils.getThemeColor(getActivity())).show();
                }
            }

            @Override
            public BaseBean workInBackground(Void... voids) throws TaskException {
                long userId = 0;
                if (AppContext.getLoginedAccount() != null && AppContext.getLoginedAccount().getUser() != null
                        && AppContext.getLoginedAccount().getUser().getId() != 0) {
                    userId = AppContext.getLoginedAccount().getUserId();
                }

                return FundaySDK.newInstance().uploadFeedback(userId, content);
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                showMessage(FundayExceptionDelegate.getMessage(getActivity(), exception, R.string.submit_feedback_failed));
            }

            @Override
            protected void onSuccess(BaseBean aVoid) {
                super.onSuccess(aVoid);

                if (getActivity() != null) {
                    getActivity().finish();
                }

                showMessage(R.string.submit_feedback_successful);
            }

            @Override
            protected void onFinished() {
                super.onFinished();

                if (getActivity() != null) {
                    ViewUtils.dismissProgressDialog();
                }
            }

        }.execute();
    }

    /**
     * 检测表单是否合法
     *
     * @return
     */
    private boolean isFeedbackValid() {
        String error = checkFormValid();

        return TextUtils.isEmpty(error) && !TextUtils.isEmpty(mFeedbackString);
    }

    /**
     * 返回检测表单错误信息
     *
     * @return
     */
    private String checkFormValid() {
        if (TextUtils.isEmpty(mFeedbackString)) {
            return getResources().getString(R.string.submit_feedback_null_error);
        } else if (mFeedbackString.length() > mTotalCharCountNum) {
            return getResources().getString(R.string.submit_feedback_char_length_error);
        }

        return null;
    }

}
