package com.hawk.funday.support.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.hawk.funday.R;
import com.hawk.funday.component.imageloader.core.assist.LoadedFrom;
import com.hawk.funday.component.imageloader.core.display.BitmapDisplayer;
import com.hawk.funday.component.imageloader.core.display.SimpleBitmapDisplayer;
import com.hawk.funday.component.imageloader.core.imageaware.ImageAware;

import org.aisen.android.common.context.GlobalContext;

/**
 * Created by wangdan on 16/9/11.
 */
public class FadeInDisplayer implements BitmapDisplayer {

    static Drawable loadingDrawable = null;

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (loadingDrawable == null && GlobalContext.getInstance() != null) {
            loadingDrawable = GlobalContext.getInstance().getResources().getDrawable(R.mipmap.bg_timeline_loading);
        }

        if (imageAware.getWrappedView() instanceof ImageView && loadingDrawable != null) {
            ImageView imageView = (ImageView) imageAware.getWrappedView();

            try {
                if (imageView.getDrawable() instanceof TransitionDrawable) {
                    TransitionDrawable drawable = (TransitionDrawable) imageView.getDrawable();
                    if (drawable.getNumberOfLayers() == 2 && drawable.getDrawable(1) instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable.getDrawable(1);
                        if (bitmapDrawable.getBitmap() == bitmap) {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
            }

            final TransitionDrawable td = new TransitionDrawable(new Drawable[] { loadingDrawable, new BitmapDrawable(bitmap)});
            imageView.setImageDrawable(td);
            td.startTransition(300);
        }
        else {
            new SimpleBitmapDisplayer().display(bitmap, imageAware, loadedFrom);
        }
    }

}
