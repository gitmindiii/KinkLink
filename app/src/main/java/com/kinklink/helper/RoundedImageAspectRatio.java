package com.kinklink.helper;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;

import com.github.siyamed.shapeimageview.RoundedImageView;
import com.kinklink.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RoundedImageAspectRatio extends RoundedImageView {
    public static final int DEFAULT_RATIO = 1;
    private int aspect;
    private float aspectRatio;

    public RoundedImageAspectRatio(Context context) {
        super(context);
    }

    public RoundedImageAspectRatio(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoundedImageAspectRatio(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        switch (aspect) {
            case Aspect.AUTO:
                if (height > width) {
                    if (width == 0) {
                        return;
                    }
                    aspect = Aspect.WIDTH;
                    aspectRatio = Math.round((double) height / width);
                    setMeasuredDimensionByHeight(height);
                } else {
                    if (height == 0) {
                        return;
                    }
                    aspect = Aspect.HEIGHT;
                    aspectRatio = Math.round((double) width / height);
                    setMeasuredDimensionByWidth(width);
                }
                break;
            case Aspect.WIDTH:
                setMeasuredDimensionByHeight(height);
                break;
            case Aspect.HEIGHT:
            default:
                setMeasuredDimensionByWidth(width);
                break;
        }
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
        try {
            aspect = a.getInt(R.styleable.AspectRatioImageView_ari_aspect, Aspect.HEIGHT);
            aspectRatio = a.getFloat(R.styleable.AspectRatioImageView_ari_ratio, DEFAULT_RATIO);
        } finally {
            a.recycle();
        }
    }

    private void setMeasuredDimensionByWidth(int width) {
        setMeasuredDimension(width, (int) (width * aspectRatio));
    }

    private void setMeasuredDimensionByHeight(int height) {
        setMeasuredDimension((int) (height * aspectRatio), height);
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
        requestLayout();
    }

    @Aspect
    public int getAspect() {
        return aspect;
    }

    public void setAspect(@Aspect int aspect) {
        this.aspect = aspect;
        requestLayout();
    }

    @IntDef({Aspect.WIDTH, Aspect.HEIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Aspect {

        int WIDTH = 0;
        int HEIGHT = 1;
        int AUTO = 2;
    }
}
