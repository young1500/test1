package com.hawk.funday.ui.fragment.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.AppContext;
import com.hawk.funday.base.Consts;
import com.hawk.funday.base.file.DirType;
import com.hawk.funday.base.file.FileContext;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.FundaySDK;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.FundayUserBean;
import com.hawk.funday.support.sdk.bean.UploadImageResultsBean;
import com.hawk.funday.support.utils.FundayUtils;
import com.hawk.funday.support.utils.TokenUtils;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.context.GlobalContext;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.ViewUtils;
import org.aisen.android.network.task.TaskException;
import org.aisen.android.network.task.WorkTask;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.fragment.ABaseFragment;

import java.io.File;

/**
 * @author Liyang Sun
 * @Description: 登陆完成后用户编辑个人信息的页面
 * @date 2016/10/8 17:28
 * @copyright HAWK
 */

public class RegisterProfileEditFragment extends ABaseFragment {

    @ViewInject(id = R.id.profile_detail_user_icon)
    ImageView mUserAvatar;
    @ViewInject (id = R.id.profile_text_input_ly)
    TextInputLayout mTextInputLayout;
    @ViewInject (id = R.id.profile_user_name_et)
    TextInputEditText mUserName;
    @ViewInject (id = R.id.profile_detail_user_email)
    TextView mUserEmail;
    @ViewInject (id = R.id.profile_detail_user_icon_ly)
    RelativeLayout mUserIconLy;
    @ViewInject (id = R.id.profile_edit_done)
    Button mEditDone;

    private static final int REQUEST_CODE_IMAGE_SELECT = 10;
    private static final int REQUEST_CODE_EDIT_PIC = 11;
    private static final String TAG = "RegisterProfileEditFragment";
    private static final int USER_NAME_MAX_LENGTH = 20;

    private Uri destinationUri;
    private Uri localUri;
    private String userName;
    private String userNameOld;
    private static AccountBean mAccountBean;

    private FundayUserBean mUser;

    public static void launch(Activity from, AccountBean accountBean) {
        mAccountBean = accountBean;
        ContainerActivity.launch(from, RegisterProfileEditFragment.class, null);
    }

    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_register_profile_edit;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.title_register_profile_edit);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void refreshDoneBtn() {
        if (TextUtils.isEmpty(userName)
                || userName.length() > USER_NAME_MAX_LENGTH) {
            mEditDone.setBackgroundResource(R.drawable.bg_register_edit_profile_done_no_ok_btn);
            mEditDone.setEnabled(false);
        } else {
            mEditDone.setBackgroundResource(R.drawable.bg_register_edit_profile_done_ok_btn);
            mEditDone.setEnabled(true);
        }
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);

//        userNameOld = mAccountBean.getUser().getName();
//        userName = userNameOld;

        setupTextInputLayout();

        if (AppContext.getLoginedAccount() == null) {
            if (getActivity() != null) {
                getActivity().finish();
            }

            return;
        }

        mUser = mAccountBean.getUser();

        if (!TextUtils.isEmpty(mUser.getAvatar())) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .showImageOnLoading(getResources().getDrawable(R.mipmap.ic_funday_profile_detail_head))
                    .build();

            ImageLoader.getInstance().displayImage(mUser.getAvatar(), mUserAvatar, options);
        }

        if (!TextUtils.isEmpty(mUser.getName())) {
            mUserName.requestFocus();
        }
        if (!TextUtils.isEmpty(mUser.getEmail())) {
            mUserEmail.setText(mUser.getEmail());
        }

        mUserIconLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDoneBtn();

                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                try {
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_SELECT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                userName = mUserName.getText().toString().trim(); // 将userName修改为用户修改后的

                refreshDoneBtn();
                setupTextInputLayout();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEditDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyUserInfo(userName);
                if (getActivity() != null) {
                    TmaAgent.onEvent(getActivity(), Consts.Event.Event_login_profile_modify_done);
                }
            }
        });
    }

    private void setupTextInputLayout() {
        mTextInputLayout.setCounterEnabled(false);
        mTextInputLayout.setCounterMaxLength(USER_NAME_MAX_LENGTH);
        mTextInputLayout.setHintEnabled(false);
        mTextInputLayout.setError(getResources().getString(R.string.user_name_length_error));
        if (!TextUtils.isEmpty(userName)) {
            if (userName.length() > USER_NAME_MAX_LENGTH) {
                mTextInputLayout.setErrorEnabled(true);
            } else {
                mTextInputLayout.setErrorEnabled(false);
            }
        } else {
            mTextInputLayout.setErrorEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMAGE_SELECT: // 选择照片
                    if (data != null) {
                        Logger.d(TAG + "_Account", "Selected Picture");
                        File cacheDir = FileContext.get().getDirectoryManager().getDir(DirType.cache.value()); // chache
                        String tempFile = cacheDir.getPath() + File.separator + System.currentTimeMillis() + "_avatar.jpg";
                        destinationUri = Uri.fromFile(new File(tempFile));
                        refreshDoneBtn();
                        localUri = data.getData();
                        String scheme = localUri.getScheme();
                        String imagePath = "";
                        if ("content".equals(scheme)) {
                            String[] filePathColumns = {MediaStore.Images.Media.DATA};
                            Cursor c = getActivity().getContentResolver().query(localUri, filePathColumns, null, null, null);
                            c.moveToFirst();
                            int columnIndex = c.getColumnIndex(filePathColumns[0]);
                            imagePath = c.getString(columnIndex);
                            Uri originUri = Uri.fromFile(new File(imagePath));
                            editPic(localUri, destinationUri);
                            c.close();
                        }
                    } else {
                        Logger.d(TAG, "Activity resultCode == RESULT_OK, but Intent data is null");
                    }
                    break;

                case REQUEST_CODE_EDIT_PIC: // 编辑照片
                    if (destinationUri != null && new File(destinationUri.getPath()).exists()) {
                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                .cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnLoading(getResources().getDrawable(R.mipmap.ic_funday_profile_detail_head))
                                .build();

                        ImageLoader.getInstance().displayImage(destinationUri.toString(), mUserAvatar, options); // 将头像设置为修改后的图片
                        refreshDoneBtn();
                        Logger.d(TAG + "_Account", "Edited Picture: " + destinationUri);
                    }
                    break;

                default:
                    break;
            }
        } else {
            Logger.d(TAG, "Activity resultCode != RESULT_OK");
        }

    }

    @Override
    public boolean onBackClick() {
        return true;
    }

    @Override
    public boolean onHomeClick() {
        return super.onHomeClick();
    }

    private void modifyUserInfo(final String newUserName) {
        final Activity context = getActivity();

        new WorkTask<Void, Void, Boolean>() {
            @Override
            protected void onPrepare() {
                super.onPrepare();

                if (getActivity() != null) {
                    ViewUtils.createProgressDialog(getActivity(), getResources().getString(R.string.dialog_save_profile_detail_info), FundayUtils.getThemeColor(getActivity())).show();
                }
            }

            @Override
            public Boolean workInBackground(Void... voids) throws TaskException {
                String token = AppContext.getLoginedAccount().getToken();
                long userId = mUser.getId();
                String oldUserName = mUser.getName();

                // 名称发生改变
                if (!TextUtils.isEmpty(newUserName) && !newUserName.equals(oldUserName)) {
                    TmaAgent.onEvent(context, Consts.Event.Event_login_profile_modify_name);

                    FundaySDK.newInstance().uploadUserName(newUserName, userId, token);
                    mUser.setName(newUserName);
                }
                // 上传图片
                if (destinationUri != null) {
                    TmaAgent.onEvent(context, Consts.Event.Event_login_profile_modify_avatar);

                    final String avatarPath = destinationUri.getPath();
                    UploadImageResultsBean result = FundaySDK.newInstance().uploadImage(new File(avatarPath), null);
                    // 更新头像
                    FundaySDK.newInstance().uploadAvatar(userId, token, result.getData().get(0).getUrl()); // 将修改后的URL上传至服务器
                    mUser.setAvatar(result.getData().get(0).getUrl());
                }

                AccountBean account = AppContext.getLoginedAccount();
                // 上下文
                account.setUser(mUser);
                // 持久化
                if (GlobalContext.getInstance() != null) {
                    new TokenUtils(GlobalContext.getInstance()).saveAccountBean(account);
                }

                return true; // 从服务器拿回来新头像Url
            }

            @Override
            protected void onFailure(TaskException exception) {
                super.onFailure(exception);

                showMessage(exception.getMessage());
            }

            @Override
            protected void onSuccess(Boolean aBoolean) {
                super.onSuccess(aBoolean);

                if (getActivity() != null) {
                    getActivity().finish();
                }
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

    private void editPic(Uri origin, Uri destination) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(origin, "image/*");
        setIntentExtra(intent);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, destination);
        try {
            startActivityForResult(intent, REQUEST_CODE_EDIT_PIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setIntentExtra(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", convertDIP2PX(getActivity(), 200));
        intent.putExtra("outputY", convertDIP2PX(getActivity(), 200));
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    }

    private int convertDIP2PX(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    @Override
    public void onResume() {
        super.onResume();

        TmaAgent.onPageStart(Consts.Page.Page_login_profile_modify);
        TmaAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        TmaAgent.onPageEnd(Consts.Page.Page_login_profile_modify);
        TmaAgent.onPause(getActivity());
    }
}
