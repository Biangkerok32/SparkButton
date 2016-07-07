package com.varunest.sparkbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.varunest.sparkbutton.heplers.CircleView;
import com.varunest.sparkbutton.heplers.DotsView;
import com.varunest.sparkbutton.heplers.Utils;

/**
 * @author varun 7th July 2016
 */
public class SparkButton extends FrameLayout implements View.OnClickListener {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private static final int INVALID_RESOURCE_ID = -1;
    private static final float DOTVIEW_SIZE_FACTOR = 3;
    private static final float DOTS_SIZE_FACTOR = .08f;
    private static final float CIRCLEVIEW_SIZE_FACTOR = 1.4f;

    ImageView imageView;
    int imageSize;
    int imageResourceIdActive = INVALID_RESOURCE_ID;
    int imageResourceIdDisabled = INVALID_RESOURCE_ID;

    DotsView dotsView;
    int dotsSize;

    CircleView circleView;
    int circleSize;

    int secondaryColor = 0xFFFF5722;
    int primaryColor = 0xFFFFC107;

    boolean pressOnTouch = true;
    boolean isChecked = true;

    private AnimatorSet animatorSet;
    private SparkEventListener listener;

    SparkButton(Context context) {
        super(context);
    }

    public SparkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        getStuffFromXML(attrs);
        init();
    }

    public SparkButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getStuffFromXML(attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SparkButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getStuffFromXML(attrs);
        init();
    }

    public void setColors(int startColor, int endColor) {
        this.secondaryColor = startColor;
        this.primaryColor = endColor;
    }

    private void getStuffFromXML(AttributeSet attr) {
        TypedArray a = getContext().obtainStyledAttributes(attr, R.styleable.sparkbutton);
        imageSize = a.getDimensionPixelOffset(R.styleable.sparkbutton_sparkbutton_size, Utils.dpToPx(getContext(), 50));
        imageResourceIdActive = a.getResourceId(R.styleable.sparkbutton_sparkbutton_activeImage, INVALID_RESOURCE_ID);
        imageResourceIdDisabled = a.getResourceId(R.styleable.sparkbutton_sparkbutton_disabledImage, INVALID_RESOURCE_ID);
        primaryColor = a.getResourceId(R.styleable.sparkbutton_sparkbutton_primaryColor, getResources().getColor(R.color.spark_primary_color));
        secondaryColor = a.getResourceId(R.styleable.sparkbutton_sparkbutton_secondaryColor, getResources().getColor(R.color.spark_secondary_color));
        pressOnTouch = a.getBoolean(R.styleable.sparkbutton_sparkbutton_pressOnTouch, true);
    }

    void init() {
        circleSize = (int) (imageSize * CIRCLEVIEW_SIZE_FACTOR);
        dotsSize = (int) (imageSize * DOTVIEW_SIZE_FACTOR);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_spark_button, this, true);
        circleView = (CircleView) findViewById(R.id.vCircle);
        circleView.setColors(secondaryColor, primaryColor);
        circleView.getLayoutParams().height = circleSize;
        circleView.getLayoutParams().width = circleSize;

        dotsView = (DotsView) findViewById(R.id.vDotsView);
        dotsView.getLayoutParams().width = dotsSize;
        dotsView.getLayoutParams().height = dotsSize;
        dotsView.setColors(secondaryColor, primaryColor);
        dotsView.setMaxDotSize((int) (imageSize * DOTS_SIZE_FACTOR));

        imageView = (ImageView) findViewById(R.id.ivImage);

        imageView.getLayoutParams().height = imageSize;
        imageView.getLayoutParams().width = imageSize;
        if (imageResourceIdActive != INVALID_RESOURCE_ID) {
            imageView.setImageResource(imageResourceIdActive);
        }
        setOnTouchListener();
        setOnClickListener(this);
    }

    private void setOnTouchListener() {
        if (pressOnTouch) {
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            imageView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(150).setInterpolator(DECCELERATE_INTERPOLATOR);
                            setPressed(true);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            float x = event.getX();
                            float y = event.getY();
                            boolean isInside = (x > 0 && x < getWidth() && y > 0 && y < getHeight());
                            if (isPressed() != isInside) {
                                setPressed(isInside);
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            imageView.animate().scaleX(1).scaleY(1).setInterpolator(DECCELERATE_INTERPOLATOR);
                            if (isPressed()) {
                                performClick();
                                setPressed(false);
                            }
                            break;
                    }
                    return true;
                }
            });
        } else {
            setOnTouchListener(null);
        }
    }

    public void setChecked(boolean flag) {
        isChecked = flag;
        imageView.setImageResource(isChecked ? imageResourceIdActive : imageResourceIdDisabled);
    }

    public void setEventListener(SparkEventListener listener) {
        this.listener = listener;
    }

    public void pressOnTouch(boolean pressOnTouch) {
        this.pressOnTouch = pressOnTouch;
        init();
    }

    public void playAnimation() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        imageView.animate().cancel();
        imageView.setScaleX(0);
        imageView.setScaleY(0);
        circleView.setInnerCircleRadiusProgress(0);
        circleView.setOuterCircleRadiusProgress(0);
        dotsView.setCurrentProgress(0);

        animatorSet = new AnimatorSet();

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(circleView, CircleView.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        outerCircleAnimator.setDuration(250);
        outerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(circleView, CircleView.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        innerCircleAnimator.setDuration(200);
        innerCircleAnimator.setStartDelay(200);
        innerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(imageView, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration(350);
        starScaleYAnimator.setStartDelay(250);
        starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(imageView, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration(350);
        starScaleXAnimator.setStartDelay(250);
        starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(dotsView, DotsView.DOTS_PROGRESS, 0, 1f);
        dotsAnimator.setDuration(900);
        dotsAnimator.setStartDelay(50);
        dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

        animatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator,
                starScaleYAnimator,
                starScaleXAnimator,
                dotsAnimator
        );

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                circleView.setInnerCircleRadiusProgress(0);
                circleView.setOuterCircleRadiusProgress(0);
                dotsView.setCurrentProgress(0);
                imageView.setScaleX(1);
                imageView.setScaleY(1);
            }
        });

        animatorSet.start();
    }

    @Override
    public void onClick(View v) {
        if (imageResourceIdDisabled != INVALID_RESOURCE_ID) {
            isChecked = !isChecked;

            imageView.setImageResource(isChecked ? imageResourceIdActive : imageResourceIdDisabled);

            if (animatorSet != null) {
                animatorSet.cancel();
            }
            if (isChecked) {
                playAnimation();
            }
        } else {
            playAnimation();
        }
        if (listener != null) {
            listener.onEvent(imageView, isChecked);
        }
    }
}