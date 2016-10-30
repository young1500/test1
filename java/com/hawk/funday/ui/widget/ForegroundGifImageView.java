package com.hawk.funday.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by wangdan on 16/9/11.
 */
public class ForegroundGifImageView extends GifImageView {

    private Drawable mForeground;

    private final Rect mSelfBounds = new Rect();
    private final Rect mOverlayBounds = new Rect();

    private int mForegroundGravity = Gravity.FILL;

    protected boolean mForegroundInPadding = true;

    boolean mForegroundBoundsChanged = false;

    public ForegroundGifImageView(Context context) {
        super(context);
    }

    public ForegroundGifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForegroundGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, org.aisen.android.R.styleable.ForegroundView,
                defStyle, 0);

        mForegroundGravity = a.getInt(
                org.aisen.android.R.styleable.ForegroundView_android_foregroundGravity, mForegroundGravity);

        final Drawable d = a.getDrawable(org.aisen.android.R.styleable.ForegroundView_android_foreground);
        if (d != null) {
            setForeground(d);
        }

        mForegroundInPadding = a.getBoolean(
                org.aisen.android.R.styleable.ForegroundLinearLayout_foregroundInsidePadding, true);

        a.recycle();
    }

    /**
     * Describes how the foreground is positioned.
     *
     * @return foreground gravity.
     *
     * @see #setForegroundGravity(int)
     */
    public int getForegroundGravity() {
        return mForegroundGravity;
    }

    /**
     * Describes how the foreground is positioned. Defaults to START and TOP.
     *
     * @param foregroundGravity See {@link android.view.Gravity}
     *
     * @see #getForegroundGravity()
     */
    public void setForegroundGravity(int foregroundGravity) {
        if (mForegroundGravity != foregroundGravity) {
            if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.START;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            mForegroundGravity = foregroundGravity;


            if (mForegroundGravity == Gravity.FILL && mForeground != null) {
                Rect padding = new Rect();
                mForeground.getPadding(padding);
            }

            requestLayout();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForeground);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mForeground != null) mForeground.jumpToCurrentState();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForeground != null && mForeground.isStateful()) {
            mForeground.setState(getDrawableState());
        }
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.  Any padding in the Drawable will be taken
     * into account by ensuring that the children are inset to be placed
     * inside of the padding area.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        if (mForeground != drawable) {
            if (mForeground != null) {
                mForeground.setCallback(null);
                unscheduleDrawable(mForeground);
            }

            mForeground = drawable;

            if (drawable != null) {
                setWillNotDraw(false);
                drawable.setCallback(this);
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                if (mForegroundGravity == Gravity.FILL) {
                    Rect padding = new Rect();
                    drawable.getPadding(padding);
                }
            }  else {
                setWillNotDraw(true);
            }
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the drawable used as the foreground of this FrameLayout. The
     * foreground drawable, if non-null, is always drawn on top of the children.
     *
     * @return A Drawable or null if no foreground was set.
     */
    public Drawable getForeground() {
        return mForeground;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mForegroundBoundsChanged = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundBoundsChanged = true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mForeground != null) {
            final Drawable foreground = mForeground;

            if (mForegroundBoundsChanged) {
                mForegroundBoundsChanged = false;
                final Rect selfBounds = mSelfBounds;
                final Rect overlayBounds = mOverlayBounds;

                final int w = getRight() - getLeft();
                final int h = getBottom() - getTop();

                if (mForegroundInPadding) {
                    selfBounds.set(0, 0, w, h);
                } else {
                    selfBounds.set(getPaddingLeft(), getPaddingTop(),
                            w - getPaddingRight(), h - getPaddingBottom());
                }

                Gravity.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                        foreground.getIntrinsicHeight(), selfBounds, overlayBounds);
                foreground.setBounds(overlayBounds);
            }

            foreground.draw(canvas);
        }
    }

    @TargetApi(21)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (mForeground != null) {
            mForeground.setHotspot(x, y);
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
////                changeLight(25);
//                startLightAnim();
//                break;
//            case MotionEvent.ACTION_MOVE:
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                if (anim != null) {
//                    anim.cancel();
//                    anim = null;
//                }
//                changeLight(0);
//                break;
//            default:
//                break;
//        }
//
//        return super.onTouchEvent(event);
//    }
//
//    private ValueAnimator anim;
//    private void startLightAnim() {
//        if (anim != null) {
//            anim.cancel();
//        }
//        anim = ValueAnimator.ofInt(0, 32);
//        anim.setDuration(350);
//        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                changeLight(Integer.parseInt(valueAnimator.getAnimatedValue().toString()));
//            }
//
//        });
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                anim = null;
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//
//        });
//        anim.start();
//    }
//
//    private void changeLight(int brightness) {
//        ColorMatrix matrix = new ColorMatrix();
//        matrix.set(new float[] {
//                1,0,0,0,brightness,
//                0,1,0,0,brightness,
//                0,0,1,0,brightness,
//                0,0,0,1,0,
//                0,0,0,0,1
//        });
//        setColorFilter(new ColorMatrixColorFilter(matrix));
//    }

}
