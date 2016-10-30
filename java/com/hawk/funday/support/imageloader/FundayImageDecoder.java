package com.hawk.funday.support.imageloader;

import android.graphics.Bitmap;

import com.hawk.funday.BuildConfig;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.ImageSize;
import com.hawk.funday.component.imageloader.core.decode.BaseImageDecoder;
import com.hawk.funday.component.imageloader.core.decode.ImageDecodingInfo;
import com.hawk.funday.ui.fragment.posts.PostImageItemView;

import java.io.File;
import java.io.IOException;

/**
 * Created by yijie.ma on 2016/8/23.
 */
public class FundayImageDecoder extends BaseImageDecoder {

    public FundayImageDecoder() {
        super(BuildConfig.LOG_DEBUG);
    }

    @Override
    public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
        ImageSize imageSize = decodingInfo.getTargetSize();
        // 解析GIF图片，那么就解析GIF的第一帧
        if (imageSize instanceof PostImageItemView.GIFImageSize) {
            if (ImageLoader.getInstance() != null) {
                File file = ImageLoader.getInstance().getDiskCache().get(decodingInfo.getOriginalImageUri());
                if (file != null && file.exists()) {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
                }
            }
        }

        return super.decode(decodingInfo);
    }

}
