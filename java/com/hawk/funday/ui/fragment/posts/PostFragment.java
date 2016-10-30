package com.hawk.funday.ui.fragment.posts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.db.PostPublisherDB;
import com.hawk.funday.support.permissions.SdcardPermissionAction;
import com.hawk.funday.support.sdk.bean.AccountBean;
import com.hawk.funday.support.sdk.bean.UploadBean;
import com.hawk.funday.support.utils.FileUtils;
import com.hawk.funday.sys.service.UploadService;
import com.hawk.funday.ui.activity.base.ContainerActivity;
import com.hawk.funday.ui.activity.image.PreviewImageActivity;
import com.hawk.funday.ui.fragment.base.BizFragment;
import com.tma.analytics.TmaAgent;

import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.support.action.IAction;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.activity.container.FragmentArgs;
import org.aisen.android.ui.fragment.ABaseFragment;

import java.io.File;

public class PostFragment extends ABaseFragment implements View.OnClickListener{

    public static void launch(Activity from) {
//        Stats.logEvent(StatEvent.Post.NEW_POST, new Bundle());
        FragmentArgs args=new FragmentArgs();
        //args.add("imgePath",mImagePath);
        ContainerActivity.launch(from, PostFragment.class, args);
    }
    private final long MAX_GIF_SIZE=5242880; ////最大gif限制5M
    @ViewInject(id=R.id.edt_title)
    private EditText mTitleEdt; ////标题编辑框

    @ViewInject(id=R.id.tv_char_length)
    private TextView mCharSizeTv;////标题字数显示
    @ViewInject(id = R.id.img_preview)
    private ImageView mImageView; ////图片预览

    @ViewInject(id = R.id.btn_upload)
    private Button mUploadBtn; ///内容提交按钮
    @ViewInject(id = R.id.btn_reselect)
    private Button mReselectBtn; ////重新选择按钮
    @ViewInject(id = R.id.rl_image)
    private RelativeLayout mAddPictureLayout;
    private  int PICTURE_CODE=1;

    private  String mImagePath; ////图片路径
    private ViewStub mTipView;
    private  final String TAG="PostFragment";
    @Override
    public int inflateContentView() {
        return R.layout.ui_fg_post;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        attchEvent();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImagePath = savedInstanceState == null ? "" : savedInstanceState.getString("ImagePath");

        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_funday_fab_back);
        activity.getSupportActionBar().setTitle(R.string.title_new_post);
        if (TextUtils.isEmpty(mImagePath)){
            startImageAlbum();
        }else {
            showImage();
            checkGifSize();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ImagePath", mImagePath);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICTURE_CODE )
        {
            String temp = handleImageResult(data);
            if ( resultCode== Activity.RESULT_OK&&data!=null &&temp!=null&&temp.length()>0) {
                mImagePath = temp;
                showImage();
                checkGifSize();
            }else {
                if ((mImagePath==null||mImagePath.length()<1)&&(temp==null||temp.length()<1)&& getActivity()!=null){
                    getActivity().finish();
                }
            }
        }
    }
    /**
      * @Description:
      * @params
      * @return
      */

    private void checkGifSize(){
        mUploadBtn.setEnabled(isUploadable());
        hideImageSizeTip();
        if (getResourceType(mImagePath)==GIF_CODE){
            File gifFile=new File(mImagePath);
            if (gifFile==null){
                mImagePath="";
                mUploadBtn.setEnabled(false);
                return;
            }
            if (gifFile.length()>MAX_GIF_SIZE){
                mUploadBtn.setEnabled(false);
                showImageSizeTip();
            }
        }
    }
    private synchronized  void showImageSizeTip(){
        if (mTipView==null){
            mTipView = (ViewStub) findViewById(R.id.img_size_tip);
            mTipView.inflate();
        }
        mTipView.setVisibility(View.VISIBLE);
    }

    private synchronized void hideImageSizeTip(){
        if (mTipView!=null)
        {
            mTipView.setVisibility(View.GONE);
        }
    }
    /**
      * @Description: 控件事件绑定
      * @return
      */
    private void attchEvent(){
        mTitleEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int charLen=editable.length();
                mCharSizeTv.setText(charLen+"");
                if (getActivity()==null)
                    return;
                if (charLen>140 ){
                    mUploadBtn.setEnabled(false);
                    mCharSizeTv.setTextColor(ContextCompat.getColor(getActivity(),R.color.img_size_tip_color));
                }else {
                    mCharSizeTv.setTextColor(ContextCompat.getColor(getActivity(),R.color.black_38));
                    mUploadBtn.setEnabled(true);
                }
            }
        });
        mImageView.setOnClickListener(this);
        mUploadBtn.setOnClickListener(this);
        mReselectBtn.setOnClickListener(this);
        mAddPictureLayout.setOnClickListener(this);
    }



    @Override
    public void onClick(final View view) {
        if (getActivity()==null)
            return;
        new IAction(getActivity(), new SdcardPermissionAction((BaseActivity) getActivity(), null)) {

            @Override
            public void doAction() {
                switch (view.getId())
                {
                    case R.id.img_preview:
                        hindSoftInput();
                        if (mImagePath==null||mImagePath.length()==0 ||mImageView.getDrawable()==null) {
                            // startImageAlbum();
                        } else {
                           // previewIamge();
                        }
                        break;
                    case R.id.imgbtn_delete:
                        hindSoftInput();
                        deleteImage();
                        break;
                    case R.id.btn_upload:
                        hindSoftInput();
                        if (isUploadable()&&getActivity()!=null){
                            handleUpload();
                        }
                        break;
                    case R.id.btn_reselect:
                        startImageAlbum();
                        break;
                    case R.id.rl_image:
                        startImageAlbum();
                        break;
                }
            }

        }.run();
    }
    /**
      * @Description: 检查上传的内容是否满足条件
      * @return
      */

    private boolean isUploadable(){

        return  new FileUtils().fileIsExists(mImagePath);
    }
    /**
      * @Description 删除已选择图片
      * @return  
      */
    
    private void deleteImage(){
        mImagePath="";
        if (mImageView == null || mImageView.getDrawable()==null) return;
        Bitmap leftBitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        mImageView.setImageDrawable(null);
        if (leftBitmap != null && !leftBitmap.isRecycled()){
            leftBitmap.recycle();
        }

        mUploadBtn.setEnabled(false);
    }
    /**
      * @Description:  调用系统相册获取图片
      * @params data 调用系统相册返回的数据
      * @return  
      */
    private String handleImageResult(Intent data){
        if (data==null)
            return "";

            Uri selectedImage = data.getData();
            if (selectedImage == null) {
                return "";
            }
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            if (getActivity() == null)
                return "";
            Cursor c = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c == null) {
                return "";
            }
            if (c.getCount() < 1) {
                c.close();
                return "";
            }
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
        try {
            String picturePath = c.getString(columnIndex);
            c.close();
            return picturePath;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
      * @Description:  打开系统相册
      * @return
      */
    private void startImageAlbum(){
       /// if (TextUtils.isEmpty(mImagePath)){
        try {
            Intent pictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pictureIntent, PICTURE_CODE);
        }catch (Exception e)
        {

        }
        /*}else {
            showImage();
            checkGifSize();
        }*/
    }

    /**
      * @Description: 显示预览图片
      * @params
      * @return
      */
    private void  showImage(){
        FileUtils utils=new FileUtils();
        if (mImagePath.length()>0 && utils.fileIsExists(mImagePath))
        {
            new IAction(getActivity(), new SdcardPermissionAction((BaseActivity) getActivity(), null)) {

                @Override
                public void doAction() {
                    super.doAction();
                    ImageLoader.getInstance().displayImage("file:///"+mImagePath,mImageView);
                }

            }.run();

            findViewById(R.id.imgbtn_delete).setVisibility(View.GONE);
            findViewById(R.id.tv_add_picture).setVisibility(View.GONE);

        }
    }
    private void  previewIamge(){
        if (mImagePath.length()>0 && new FileUtils().fileIsExists(mImagePath) &&getActivity()!=null) {
            PreviewImageActivity.launch(getActivity(), mImagePath);
        }
    }
    private void handleUpload(){
        BizFragment.createBizFragment(getActivity()).checkUserPermission(new BizFragment.OnUserPermissionCallback() {
            @Override
            public void onSuccess(AccountBean account) {
                showNetworkConfirm(account);
            }

            @Override
            public void onFaild() {

            }
        });
    }
    /**
     * @Description:  上传图片
     * @params 图片路径
     * @return
     */
    private void uploadImage(AccountBean accountBean){
        if (accountBean == null)
            return;

        if (getActivity()!=null) {
            String title=mTitleEdt.getText().toString().trim();
            UploadBean bean=new UploadBean();
            bean.setFilePath(mImagePath);
            bean.setResourceType(getResourceType(mImagePath));
            bean.setTitle(title);
            bean.setState(0);
            PostPublisherDB.insert(bean);
            Intent startServiceIntent = new Intent(getActivity(),UploadService.class);////5.0版本后要显示设置包名

            startServiceIntent.putExtra("title", title);
            startServiceIntent.putExtra("imagePath", mImagePath);
            startServiceIntent.putExtra("userId",accountBean.getUserId());
            startServiceIntent.putExtra("notifyId",bean.getId());
            getActivity().startService(startServiceIntent);
            //UploadService.luanchService(getActivity(),title,mImagePath,bean.getId(),accountBean.getUserId());
            getActivity().finish();
        }

    }
    /**
      * @Description:  显示网络类型确认
      * @param
      * @return
      */

    private void showNetworkConfirm(final AccountBean accountBean){
        if (getActivity()!=null) {
           if (SystemUtils.getNetworkType(getActivity()) == SystemUtils.NetWorkType.none) {

               showMessage(R.string.none_network_tip);
           }else {
               if (SystemUtils.getNetworkType(getActivity()) != SystemUtils.NetWorkType.wifi) {
                   new MaterialDialog.Builder(getActivity())
                           .content(R.string.internet_tip)
                           .positiveText(R.string.upload)
                           .negativeText(R.string.btn_cancel)
                           .onPositive(new MaterialDialog.SingleButtonCallback() {
                               @Override
                               public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                   uploadImage(accountBean);
                               }
                           })
                           .show();
               } else {
                   uploadImage(accountBean);
               }
           }
        }
    }

    @Override
    public boolean onBackClick() {
        if (checkExit()) {
            new MaterialDialog.Builder(getActivity())
                    .content(R.string.exit_post_tip)
                    .positiveText(R.string.btn_ok)
                    .negativeText(R.string.btn_cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (getActivity()!=null)
                                getActivity().finish();
                        }
                    })
                    .show();
            return true;
        }else {
            return super.onBackClick();
        }
    }

    private boolean checkExit(){
        if (mTitleEdt.getText().toString().trim().length()>0||mImagePath.length()>0) {
            if (getActivity() != null){
                return true;
            }

        }
        return false;
    }
    private final int  GIF_CODE=2;
    private int getResourceType(String fileName)
    {
        String temp=fileName.toLowerCase();
        if (temp.endsWith(".gif"))
        {
            return GIF_CODE;
        }else if (temp.endsWith(".jpg")||temp.endsWith(".png")||temp.endsWith(".jpeg")||temp.endsWith(".bmp"))
        {
            return PICTURE_CODE;
        }
        return 0;
    }
    private void hindSoftInput(){
        if (getActivity()==null ||getView()==null)
            return;
        InputMethodManager imm = (InputMethodManager)
                getActivity(). getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        TmaAgent.onPageStart(Consts.Page.Page_post_create);
        TmaAgent.onResume(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        TmaAgent.onPageEnd(Consts.Page.Page_post_create);
        TmaAgent.onPause(getActivity());
    }

}
