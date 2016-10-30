package com.hawk.funday.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.hawk.funday.R;
import com.hawk.funday.support.utils.FundayUtils;

/**
 * Created by yijie.ma on 2016/9/6.
 */
public class TabDownImage extends LinearLayout {

    private Paint mPaint;
    private int mDiamonNum;//锯齿个数
    private int mDiamonWidth;//锯齿宽度
    private int mDiamonHeight;//锯齿高度


    public TabDownImage(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabDownImage);
        mDiamonHeight = typedArray.getDimensionPixelOffset(R.styleable.TabDownImage_diamonHeight, FundayUtils.convertDIP2PX(context, 6));
        mDiamonWidth = typedArray.getDimensionPixelOffset(R.styleable.TabDownImage_diamonWidth, FundayUtils.convertDIP2PX(context, 18));

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(context.getResources().getColor(R.color.comm_white));
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        typedArray.recycle();


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b-(int)mDiamonHeight/2);//根据锯齿高度确定文字上移高度以居中
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDiamonNum = (int)(w/(float)mDiamonWidth + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = 0;
        for (int i = 0 ; i < mDiamonNum; i++){

            Path path = new Path();
            path.moveTo(left, getHeight());
            path.lineTo(left + mDiamonWidth/2, getHeight() - mDiamonHeight/2);
            path.lineTo(left + mDiamonWidth, getHeight());
//            path.lineTo(left + mDiamonWidth/2, getHeight() + mDiamonHeight/2);
            path.lineTo(left, getHeight());
            path.close();

            canvas.drawPath(path, mPaint);

            left = left + mDiamonWidth;
        }

    }


}
