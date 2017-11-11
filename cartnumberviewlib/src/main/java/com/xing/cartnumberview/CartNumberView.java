package com.xing.cartnumberview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Administrator on 2017/11/11.
 */

public class CartNumberView extends View {

    private final int DEFAULT_HEIGHT = dp2Px(24);  // default height = 10dp

    private final int DEFAULT_GAP = dp2Px(50);   // default gap between add and sub button

    private int mWidth;  // view width

    private int mHeight;  // view height

    private float buttonRadius;   // button radius

    private int buttonColor;


    private Paint buttonPaint;

    private Paint textPaint;

    private Paint operatorPaint;

    private Paint numberPaint;


    private RectF rect;

    private float textSize;

    private String text;
    private float currentX;
    private boolean isAnimatorEnd = false;
    private float expandX;
    private boolean isExpandEnd;  // 展开动画结束
    private String unit;

    private String numberText;

    private int number = 1;

    private float circleAngle = dp2Px(5);

    private boolean isShrinkAnimEnd = false;

    private boolean isAnimating = false;

    /**
     * 动画集合
     */
    private AnimatorSet animatorSet;
    /**
     * 圆角矩形收缩动画
     */
    private ValueAnimator rectToCircleAnim;

    /**
     * 圆角矩形变圆形动画
     */
    private ValueAnimator rectToCircleRectAnim;
    /**
     * 圆形展开动画
     */
    private ValueAnimator circleExpandAnim;


    public CartNumberView(Context context) {
        this(context, null);

    }

    public CartNumberView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartNumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(context, attrs);
        init();
    }

    private void init() {

        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(buttonColor);
        buttonPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        operatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        operatorPaint.setColor(Color.WHITE);
        operatorPaint.setStrokeWidth(dp2Px(3));
        operatorPaint.setStrokeCap(Paint.Cap.ROUND);


        numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setColor(Color.BLACK);
        numberPaint.setTextSize(textSize);
        numberPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * 获取自定义属性
     *
     * @param context
     * @param attrs
     */
    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CartNumberView);
        buttonColor = typedArray.getColor(R.styleable.CartNumberView_buttonColor, 0xfff7e008);
        textSize = typedArray.getDimension(R.styleable.CartNumberView_textSize, sp2Px(14));
        text = typedArray.getString(R.styleable.CartNumberView_text);
        unit = typedArray.getString(R.styleable.CartNumberView_unit);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasureWidth(widthMeasureSpec), getMeasureHeight(heightMeasureSpec));
    }

    private int getMeasureHeight(int heightMeasureSpec) {
        int measureSize;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            measureSize = size;
        } else {
            measureSize = DEFAULT_HEIGHT;
        }
        return measureSize;
    }

    private int getMeasureWidth(int widthMeasureSpec) {
        int measureSize;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            measureSize = size;
        } else {
            measureSize = 2 * DEFAULT_HEIGHT + DEFAULT_GAP;
        }
        return measureSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        rect = new RectF();
        buttonRadius = mHeight / 2f;

        initAnimations();

    }

    private void initAnimations() {
        // 初始化圆角矩形收缩动画
        initRectToCircleAnimation();

        // 初始化圆角矩形变圆形动画
        initRectToCircleRectAnimation();

        // 圆形展开动画
        initCircleExpandAnimation();

        // 初始化动画集合
        animatorSet = new AnimatorSet();

        animatorSet.play(rectToCircleAnim)
                .before(circleExpandAnim)
                .after(rectToCircleRectAnim);
    }


    /**
     * 圆形展开动画
     */
    private void initCircleExpandAnimation() {
        expandX = mWidth - buttonRadius;
        circleExpandAnim = ValueAnimator.ofFloat(mWidth - buttonRadius, buttonRadius);
        circleExpandAnim.setInterpolator(new LinearInterpolator());
        circleExpandAnim.setDuration(300);
        circleExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                expandX = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        circleExpandAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isExpandEnd = true;
                isAnimating = false;
            }
        });
    }

    /**
     * 初始化矩形变圆形矩形动画
     */
    private void initRectToCircleRectAnimation() {
        rectToCircleRectAnim = ValueAnimator.ofFloat(dp2Px(5), mHeight / 2f);
        rectToCircleRectAnim.setDuration(300);
        rectToCircleRectAnim.setInterpolator(new LinearInterpolator());
        rectToCircleRectAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // circleAngle 圆形矩形的圆角半径,从初始值变化到高度的一半(相当于半圆)
                circleAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
    }


    /**
     * 圆角矩形 -> 圆形
     */
    private void initRectToCircleAnimation() {
        rectToCircleAnim = ValueAnimator.ofFloat(0, mWidth - mHeight);
        rectToCircleAnim.setDuration(300);
        rectToCircleAnim.setInterpolator(new LinearInterpolator());
        rectToCircleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentX = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        rectToCircleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isShrinkAnimEnd = true;
            }
        });


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(0, buttonRadius);
        drawRoundRect(canvas);
        if (isShrinkAnimEnd) {
            drawAddSubButton(canvas);
        }
        canvas.restore();
    }

    /**
     * 绘制圆角矩形
     *
     * @param canvas
     */
    private void drawRoundRect(Canvas canvas) {
        rect.set(currentX, -mHeight / 2f, mWidth, mHeight / 2f);
        canvas.drawRoundRect(rect, circleAngle, circleAngle, buttonPaint);
        float baseLine = -(textPaint.descent() + textPaint.ascent()) / 2;
        if (currentX == 0) {
            canvas.drawText(text, mWidth / 2f, baseLine, textPaint);
        }
    }

    private void drawAddSubButton(Canvas canvas) {
        canvas.drawLine(mWidth - buttonRadius - dp2Px(6), 0, mWidth - buttonRadius + dp2Px(6),
                0, operatorPaint);
        canvas.drawLine(mWidth - buttonRadius, -dp2Px(6), mWidth - buttonRadius, dp2Px(6), operatorPaint);
        canvas.drawCircle(expandX, 0, buttonRadius, buttonPaint);
        canvas.drawLine(expandX - dp2Px(6), 0,
                expandX + dp2Px(6), 0, operatorPaint);

        if (isExpandEnd) {
            String text = "";
            if (TextUtils.isEmpty(unit)) {
                text = String.valueOf(number);
            } else {
                text = number + "  " + unit;
            }
            float baseLine = -(numberPaint.descent() + numberPaint.ascent()) / 2;
            canvas.drawText(text, mWidth / 2f, baseLine, numberPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isAnimating) {
                    return false;
                }
                if (isExpandEnd) {
                    if (event.getX() < mHeight) {
                        if (number > 1) {
                            number--;
                        }
                    } else if (event.getX() > mWidth - mHeight) {
                        number++;
                    }
                    if (listener != null) {
                        listener.onNumberChanged(number);
                    }
                    invalidate();
                    return true;
                }
                animatorSet.start();
                isAnimating = true;

                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }
        return true;
    }

    private OnNumberChangedListener listener;

    public interface OnNumberChangedListener {
        void onNumberChanged(int number);
    }

    public void setOnNumberChangedListener(OnNumberChangedListener listener) {
        this.listener = listener;
    }

    public int getNumber() {
        return number;
    }


    private int dp2Px(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                getResources().getDisplayMetrics());
    }


    private int sp2Px(int spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, getResources().getDisplayMetrics());
    }
}
