package com.hawk.funday.ui.fragment.detail;

import android.app.Activity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hawk.funday.R;
import com.hawk.funday.base.Consts;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.support.sdk.bean.PostBean;
import com.hawk.funday.ui.fragment.posts.PostImageItemView;
import com.wcc.framework.util.DeviceManager;

import org.aisen.android.common.utils.Logger;
import org.aisen.android.common.utils.Utils;
import org.aisen.android.support.inject.ViewInject;
import org.aisen.android.ui.fragment.APagingFragment;


/**
 * 评论界面的Header
 *
 * Created by wangdan on 16/8/24.
 */
public class CommentHeaderView extends PostImageItemView {

    @ViewInject(id = R.id.layWeb)
    View layWeb;
    @ViewInject(id = R.id.layImage)
    View layImage;
    @ViewInject(id = R.id.webview)
    WebView webView;
    @ViewInject(id = R.id.btn_more)
    LinearLayout moreBtn;
    @ViewInject(id = R.id.txtCmtsHint)
    TextView txtCmtsHint;

    public CommentHeaderView(Activity context, View itemView, APagingFragment ownerFragment) {
        super(context, itemView, ownerFragment);
        mContext = context;
    }

    @Override
    public void onBindData(View view, PostBean postBean, int l) {
        super.onBindData(view, postBean, l);

        if (postBean.getCommentCount() > 0) {
            txtCmtsHint.setText(R.string.cmts_hints);
        }
        else {
            txtCmtsHint.setText(R.string.cmts_hint);
        }
    }

    @Override
    protected void onImageResize(PostBean bean) {
        // 预先调整控件的大小
        if (bean.getResourceType() == Consts.MediaType.image ||
                bean.getResourceType() == Consts.MediaType.gif ||
                bean.getResourceType() == Consts.MediaType.video) {
            float width =0f;
            float height=0f;
            if( bean.getResourceType() == Consts.MediaType.video
                    &&bean.getThumbnailUrls() != null && bean.getThumbnailUrls().length > 0){
                width=bean.getThumbnailUrls()[0].getWidth()-Utils.dip2px(getContext(), 8);
                height=bean.getThumbnailUrls()[0].getHeight();
            }else if (bean.getUrls() != null && bean.getUrls().length > 0) {
                 width = bean.getUrls()[0].getWidth() - Utils.dip2px(getContext(), 8);
                 height = bean.getUrls()[0].getHeight();
            }
            float newWidth = DeviceManager.getScreenWidth(getContext());
            float newHeight = Math.abs((height / width) * newWidth);
            gifImageView.setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, (int) newHeight));
        }else {
            Logger.e("onImageResize","不知道是什么资源类型");
        }
    }

    @Override
    protected void onBindImage(PostBean bean) {

        if (bean.getResourceType() == Consts.MediaType.ariticle) {
            layImage.setVisibility(View.GONE);
            layWeb.setVisibility(View.VISIBLE);

            WebSettings setting = webView.getSettings();
            setting.setJavaScriptEnabled(true);
            setting.setDomStorageEnabled(true);
            setting.setDefaultTextEncodingName("utf-8") ;
            String html = bean.getContent();

            webView.loadDataWithBaseURL("", reSizeImage(html), "text/html", "UTF-8", "");

        }
        else {
            DisplayImageOptions.Builder bulider = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .showImageOnLoading(R.mipmap.bg_timeline_loading)
                    .showImageOnFail(R.mipmap.bg_timeline_fail);
            try {
                // 规避url为空报错
                final String image;
                if (bean.getResourceType() == Consts.MediaType.video) {
                    image = bean.getThumbnailUrls()[0].getUrl();
                }
                else {
                    image = bean.getUrls()[0].getUrl();
                }
                ImageLoader.getInstance().displayImage(image, gifImageView, bulider.build());
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }

            layImage.setVisibility(View.VISIBLE);
            layWeb.setVisibility(View.GONE);
        }

        moreBtn.setOnClickListener(this);
    }

    @Override
    protected void onImageClicked(PostBean bean) {

    }

    @Override
    protected void onCmt(View v, PostBean bean) {

    }

    private String reSizeImage(String originalHtml){

        if (!originalHtml.contains("<head>")){
            return "<head><style>img{max-width:100%; height:auto; !important;}</style></head>"+originalHtml;
        }else {
            String[] splited = originalHtml.split("</head>");
            String result = splited[0] + "<style>img{max-width:100%; height:auto; !important;}</style></head>" + splited[1];
            return result;
        }
//        int screenWidth = (int)DeviceManager.getScreenWidth(mContext);
//
//        String js;
//        try {
//            js = JsUtils.readFromAssets(mContext, "js/webview_utils.js");
//            js = "var newscript = document.getElementById('tcl_inject_script');" +
//                    "if (newscript == null || newscript == undefined) {" +
//                    "var newscript = document.createElement('script');" +
//                    "newscript.id='tcl_inject_script';" +
//                    "newscript.text=\"" + js + "\";" +
//                    "document.body.appendChild(newscript); }";
//            js = "javascript: " + js;
//            webView.loadUrl(js);
//            webView.loadUrl("javascript: reSizeImage(" + screenWidth + ");");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
