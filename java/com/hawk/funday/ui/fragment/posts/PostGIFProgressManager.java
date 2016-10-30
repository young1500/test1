package com.hawk.funday.ui.fragment.posts;

import android.graphics.Bitmap;
import android.view.View;

import com.hawk.funday.R;
import com.hawk.funday.component.imageloader.core.DisplayImageOptions;
import com.hawk.funday.component.imageloader.core.ImageLoader;
import com.hawk.funday.component.imageloader.core.assist.FailReason;
import com.hawk.funday.component.imageloader.core.listener.ImageLoadingListener;
import com.hawk.funday.component.imageloader.core.listener.ImageLoadingProgressListener;

import org.aisen.android.common.utils.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by yijie.ma on 2016/8/23.
 */
public class PostGIFProgressManager  {

    private static final String TAG = "GIFProgress";

    private static final Hashtable<String, GIFProgress> mProgressMap = new Hashtable<>();

    public static void loadImage(final String image, final OnGIFProgressListener listener) {
        Logger.v(TAG, "loadImage ---> " + image);

        final GIFProgress gifProgress = new GIFProgress();
        mProgressMap.put(image, gifProgress);
        gifProgress.mItems.add(listener);

        DisplayImageOptions.Builder bulider = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .showImageOnLoading(R.mipmap.bg_timeline_loading)
                .showImageOnFail(R.mipmap.bg_timeline_fail);

        ImageLoader.getInstance().loadImage(image,
                new PostImageItemView.GIFImageSize(100, 100),
                bulider.build(),
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        Logger.d(TAG, "onLoadingStarted ---> " + imageUri);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Logger.w(TAG, "onLoadingFailed ---> " + imageUri + ", failReason = " + failReason.getCause());

                        mProgressMap.remove(imageUri);

                        gifProgress.onFailed(imageUri);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Logger.d(TAG, "onLoadingComplete ---> " + imageUri);

                        mProgressMap.remove(imageUri);

                        gifProgress.onSuccess(imageUri);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Logger.w(TAG, "onLoadingCancelled ---> " + imageUri);

                        gifProgress.onFailed(imageUri);
                    }

                },
                new ImageLoadingProgressListener() {

                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        Logger.v(TAG, "onProgressUpdate ---> " + imageUri + ", current = " + current + ", total = " + total);

                        gifProgress.onProgressUpdate(imageUri, view, current, total);
                    }

                });
    }

    public static boolean bindLoading(String image, OnGIFProgressListener listener) {
        if (mProgressMap.contains(image)) {
            GIFProgress gifProgress = mProgressMap.get(image);

            gifProgress.mItems.add(listener);

            return true;
        }

        return false;
    }

    static class GIFProgress implements ImageLoadingProgressListener {

        List<OnGIFProgressListener> mItems = new ArrayList<>();

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            for (OnGIFProgressListener listener : mItems) {
                if (imageUri.equals(listener.image())) {
                    listener.onGifProgressUpdate(imageUri, view, current, total);
                }
            }
        }

        public void onSuccess(String imageUri) {
            for (OnGIFProgressListener listener : mItems) {
                if (imageUri.equals(listener.image())) {
                    listener.onGifSuccess(imageUri);
                }
            }
        }

        public void onFailed(String imageUri) {
            for (OnGIFProgressListener listener : mItems) {
                if (imageUri.equals(listener.image())) {
                    listener.onGifFailed(imageUri);
                }
            }
        }

    }

    public interface OnGIFProgressListener {

        String image();

        void onGifProgressUpdate(String imageUri, View view, int current, int total);

        void onGifSuccess(String imageUri);

        void onGifFailed(String imageUri);

    }

}
