package com.hawk.funday.support.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.hawk.funday.component.imageloader.core.assist.LoadedFrom;
import com.hawk.funday.component.imageloader.core.display.BitmapDisplayer;
import com.hawk.funday.component.imageloader.core.imageaware.ImageAware;

/**
 * Created by wangdan on 16/8/27.
 */
public class PostListDisplayer implements BitmapDisplayer {

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        imageAware.setImageDrawable(new RoundedDrawable(bitmap, 0, 0));
    }

    public static class RoundedDrawable extends Drawable {

        protected final float cornerRadius;
        protected final int margin;

        protected  RectF mRect = new RectF(),
                mBitmapRect;
        protected final BitmapShader bitmapShader;
        protected final Paint paint;
        protected Bitmap mBitmap;

        public RoundedDrawable(Bitmap bitmap, int cornerRadius, int margin) {
            this.cornerRadius = cornerRadius;
            this.margin = margin;
            mBitmap = bitmap;

            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapRect = new RectF(margin, margin, bitmap.getWidth() - margin, bitmap.getHeight() - margin);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);

            // Resize the original bitmap to fit the new bound
            Matrix shaderMatrix = new Matrix();
//            shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
            int width = bounds.right - bounds.left;
            int height = bounds.bottom - bounds.top;

            float scale = width * 1.0f / mBitmap.getWidth();
            // 如果根据宽度缩放后，高度小于targetHeight
            if (scale * mBitmap.getHeight() < height) {
                scale = height * 1.0f / mBitmap.getHeight();
            }
            int outWidth = Math.round(scale * mBitmap.getWidth());
            int outHeight = Math.round(scale * mBitmap.getHeight());

            shaderMatrix.postScale(scale, scale);

            int left = 0;
            int top = 0;
            if (outWidth == width) {
                top = (outHeight - height) * -1 / 2;
            }
            else {
                left = (outWidth - width) * -1 / 2;
            }

            shaderMatrix.postTranslate(left, top);
            bitmapShader.setLocalMatrix(shaderMatrix);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paint);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }
    }

}
