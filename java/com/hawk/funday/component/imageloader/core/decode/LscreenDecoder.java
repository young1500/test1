package com.hawk.funday.component.imageloader.core.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.ImageSize;
import com.hawk.funday.component.imageloader.utils.BitmapDecoder;

import org.aisen.android.common.utils.Logger;

import java.io.File;
import java.io.IOException;

public class LscreenDecoder extends BaseImageDecoder {

    static final String TAG = "LscreenDecoder";

    public LscreenDecoder(boolean loggingEnabled) {
        super(loggingEnabled);
    }

    @Override
    public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
        String origImageUrl = decodingInfo.getOriginalImageUri();

        // 如果是gif图片，压缩
        if (!TextUtils.isEmpty(origImageUrl) && origImageUrl.toLowerCase().endsWith(".gif")) {
            Logger.d("解析一张gif图 ---> " + origImageUrl);
            try {
                File file = ImageLoader.getInstance().getDiskCache().get(origImageUrl);
                if (file != null && file.exists()) {
                    return BitmapFactory.decodeFile(file.getAbsolutePath(), null);
                }
            } catch (Throwable e) {
                Logger.printExc(LscreenDecoder.class, e);
            }
        }

        ImageSize imageSize = decodingInfo.getTargetSize();
        if (imageSize instanceof WallpaperItemSize) {
            int reqWidth = imageSize.getWidth();
            int reqHeight = imageSize.getHeight();

            if (reqWidth > 0 && reqHeight > 0) {
                try {
                    File file = ImageLoader.getInstance().getDiskCache().get(origImageUrl);
                    if (file != null && file.exists()) {
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                        Logger.v(TAG, origImageUrl);

                        Logger.v(TAG, "原始 width = %d, height = %d, 内存%sMb", options.outWidth, options.outHeight,
                                String.valueOf(Math.abs(options.outWidth * options.outHeight * 4.0f / 1024 / 1024)));

                        Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(file.getAbsolutePath(), reqWidth, reqHeight);
                        float scale = bitmap.getHeight() * 1.0f / reqHeight;
                        int outWidth = Math.round(reqWidth * 1.0f / scale);
                        if (outWidth < bitmap.getWidth()) {
                            bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - outWidth) / 2, 0, outWidth, bitmap.getHeight());
                        }

                        Logger.d(TAG, "压缩后 width = %d, height = %d, 内存%sMb", bitmap.getWidth(), bitmap.getHeight(),
                                String.valueOf(Math.abs(bitmap.getWidth() * bitmap.getHeight() * 4.0f / 1024 / 1024)));

                        return bitmap;
                    }
                } catch (Throwable e) {
                    Logger.printExc(LscreenDecoder.class, e);
                }
            }
        }
        return super.decode(decodingInfo);
    }

    public static class WallpaperItemSize extends ImageSize {

        public WallpaperItemSize(int width, int height) {
            super(width, height);
        }

    }
    public static class EBookImageSize extends ImageSize {

        public EBookImageSize(int width, int height) {
            super(width, height);
        }

    }

}
