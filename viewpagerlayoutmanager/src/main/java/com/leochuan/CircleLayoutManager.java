package com.leochuan;

import android.content.Context;
import android.view.View;

/**
 * An implementation of {@link ViewPagerLayoutManager}
 * which layouts item in a circle
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class CircleLayoutManager extends ViewPagerLayoutManager {

    /**
     * 布局的Gravity
     */
    public static final int LEFT = 10;
    public static final int RIGHT = 11;
    public static final int TOP = 12;
    public static final int BOTTOM = 13;

    /**
     * 左边的Item在最上面显示
     */
    public static final int LEFT_ON_TOP = 4;
    /**
     * 右边的Item在最上面显示
     */
    public static final int RIGHT_ON_TOP = 5;
    /**
     * 居中的Item在最上面显示
     */
    public static final int CENTER_ON_TOP = 6;

    private int radius;
    private int angleInterval;
    private float moveSpeed;
    private float maxRemoveAngle;
    private float minRemoveAngle;
    private int gravity;
    private boolean flipRotate;
    private int zAlignment;

    public CircleLayoutManager(Context context) {
        this(new Builder(context));
    }

    public CircleLayoutManager(Context context, boolean reverseLayout) {
        this(new Builder(context).setReverseLayout(reverseLayout));
    }

    public CircleLayoutManager(Context context, int gravity, boolean reverseLayout) {
        this(new Builder(context).setGravity(gravity).setReverseLayout(reverseLayout));
    }

    public CircleLayoutManager(Builder builder) {
        this(builder.context, builder.radius, builder.angleInterval, builder.moveSpeed, builder.maxRemoveAngle,
                builder.minRemoveAngle, builder.gravity, builder.zAlignment, builder.flipRotate,
                builder.maxVisibleItemCount, builder.distanceToBottom, builder.reverseLayout);
    }

    private CircleLayoutManager(Context context, int radius, int angleInterval, float moveSpeed,
                                float max, float min, int gravity, int zAlignment, boolean flipRotate,
                                int maxVisibleItemCount, int distanceToBottom, boolean reverseLayout) {
        super(context, (gravity == LEFT || gravity == RIGHT) ? VERTICAL : HORIZONTAL, reverseLayout);
        setEnableBringCenterToFront(true);
        setMaxVisibleItemCount(maxVisibleItemCount);
        setDistanceToBottom(distanceToBottom);
        this.radius = radius;
        this.angleInterval = angleInterval;
        this.moveSpeed = moveSpeed;
        this.maxRemoveAngle = max;
        this.minRemoveAngle = min;
        this.gravity = gravity;
        this.flipRotate = flipRotate;
        this.zAlignment = zAlignment;
    }

    public static void assertGravity(int gravity) {
        if (gravity != LEFT && gravity != RIGHT && gravity != TOP && gravity != BOTTOM) {
            throw new IllegalArgumentException("gravity must be one of LEFT RIGHT TOP and BOTTOM");
        }
    }

    public static void assertZAlignmentState(int zAlignment) {
        if (zAlignment != LEFT_ON_TOP && zAlignment != RIGHT_ON_TOP && zAlignment != CENTER_ON_TOP) {
            throw new IllegalArgumentException("zAlignment must be one of LEFT_ON_TOP RIGHT_ON_TOP and CENTER_ON_TOP");
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        assertNotInLayoutOrScroll(null);
        if (this.radius == radius) {
            return;
        }
        this.radius = radius;
        removeAllViews();
    }

    public int getAngleInterval() {
        return angleInterval;
    }

    public void setAngleInterval(int angleInterval) {
        assertNotInLayoutOrScroll(null);
        if (this.angleInterval == angleInterval) {
            return;
        }
        this.angleInterval = angleInterval;
        removeAllViews();
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        assertNotInLayoutOrScroll(null);
        if (this.moveSpeed == moveSpeed) {
            return;
        }
        this.moveSpeed = moveSpeed;
    }

    public float getMaxRemoveAngle() {
        return maxRemoveAngle;
    }

    public void setMaxRemoveAngle(float maxRemoveAngle) {
        assertNotInLayoutOrScroll(null);
        if (this.maxRemoveAngle == maxRemoveAngle) {
            return;
        }
        this.maxRemoveAngle = maxRemoveAngle;
        requestLayout();
    }

    public float getMinRemoveAngle() {
        return minRemoveAngle;
    }

    public void setMinRemoveAngle(float minRemoveAngle) {
        assertNotInLayoutOrScroll(null);
        if (this.minRemoveAngle == minRemoveAngle) {
            return;
        }
        this.minRemoveAngle = minRemoveAngle;
        requestLayout();
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        assertNotInLayoutOrScroll(null);
        assertGravity(gravity);
        if (this.gravity == gravity) {
            return;
        }
        this.gravity = gravity;
        if (gravity == LEFT || gravity == RIGHT) {
            setOrientation(VERTICAL);
        } else {
            setOrientation(HORIZONTAL);
        }
        requestLayout();
    }

    public boolean getFlipRotate() {
        return flipRotate;
    }

    public void setFlipRotate(boolean flipRotate) {
        assertNotInLayoutOrScroll(null);
        if (this.flipRotate == flipRotate) {
            return;
        }
        this.flipRotate = flipRotate;
        requestLayout();
    }

    public int getZAlignment() {
        return zAlignment;
    }

    public void setZAlignment(int zAlignment) {
        assertNotInLayoutOrScroll(null);
        assertZAlignmentState(zAlignment);
        if (this.zAlignment == zAlignment) {
            return;
        }
        this.zAlignment = zAlignment;
        requestLayout();
    }

    @Override
    protected float getInterval() {
        return angleInterval;
    }

    @Override
    protected void setUpOnLayout() {
        radius = radius == Builder.INVALID_VALUE ? mDecoratedMeasurementInOther : radius;
    }

    @Override
    protected float maxRemoveOffset() {
        return maxRemoveAngle;
    }

    @Override
    protected float minRemoveOffset() {
        return minRemoveAngle;
    }

    @Override
    protected int calItemLeft(View itemView, float targetOffset) {
        switch (gravity) {
            case LEFT:
                return (int) (radius * Math.sin(Math.toRadians(90 - targetOffset)) - radius);
            case RIGHT:
                return (int) (radius - radius * Math.sin(Math.toRadians(90 - targetOffset)));
            case TOP:
            case BOTTOM:
            default:
                return (int) (radius * Math.cos(Math.toRadians(90 - targetOffset)));
        }
    }

    @Override
    protected int calItemTop(View itemView, float targetOffset) {
        switch (gravity) {
            case LEFT:
            case RIGHT:
                return (int) (radius * Math.cos(Math.toRadians(90 - targetOffset)));
            case TOP:
                return (int) (radius * Math.sin(Math.toRadians(90 - targetOffset)) - radius);
            case BOTTOM:
            default:
                return (int) (radius - radius * Math.sin(Math.toRadians(90 - targetOffset)));
        }
    }

    @Override
    protected void setItemViewProperty(View itemView, float targetOffset) {
        switch (gravity) {
            case RIGHT:
            case TOP:
                if (flipRotate) {
                    itemView.setRotation(targetOffset);
                } else {
                    itemView.setRotation(360 - targetOffset);
                }
                break;
            case LEFT:
            case BOTTOM:
            default:
                if (flipRotate) {
                    itemView.setRotation(360 - targetOffset);
                } else {
                    itemView.setRotation(targetOffset);
                }
                break;
        }
    }

    @Override
    protected float getViewElevation(View itemView, float targetOffset) {
        if (zAlignment == LEFT_ON_TOP) {
            return (540 - targetOffset) / 72;
        } else if (zAlignment == RIGHT_ON_TOP) {
            return (targetOffset - 540) / 72;
        } else {
            return (360 - Math.abs(targetOffset)) / 72;
        }
    }

    @Override
    protected float getDistanceRatio() {
        if (moveSpeed != 0) {
            return 1 / moveSpeed;
        }
        return Float.MAX_VALUE;
    }

    public static class Builder {
        /**
         * 默认的半径
         */
        public static int DEFAULT_RADIUS = 1480;
        /**
         * 默认的间隔角度
         */
        public static int DEFAULT_INTERVAL_ANGLE = 30;
        private static int INTERVAL_ANGLE = 30;// The default mInterval angle between each items
        private static float DISTANCE_RATIO = 10f; // Finger swipe distance divide item rotate angle
        private static int INVALID_VALUE = Integer.MIN_VALUE;
        private static int MAX_REMOVE_ANGLE = 90;
        private static int MIN_REMOVE_ANGLE = -90;
        private int radius;
        private int angleInterval;
        private float moveSpeed;
        private float maxRemoveAngle;
        private float minRemoveAngle;
        private boolean reverseLayout;
        private Context context;
        private int gravity;
        private boolean flipRotate;
        private int zAlignment;
        private int maxVisibleItemCount;
        private int distanceToBottom;

        public Builder(Context context) {
            this.context = context;
            //总体布局在多少半径的圆上
            radius = DEFAULT_RADIUS;
            //每个item之间, 相隔多少角度
            angleInterval = DEFAULT_INTERVAL_ANGLE;
            //移动时的速度比率, 值越小,滑动越慢.反之.
            moveSpeed = 1 / DISTANCE_RATIO;
            maxRemoveAngle = MAX_REMOVE_ANGLE;
            minRemoveAngle = MIN_REMOVE_ANGLE;
            reverseLayout = false;
            flipRotate = false;
            gravity = BOTTOM;
            //z轴
            zAlignment = CENTER_ON_TOP;
            maxVisibleItemCount = ViewPagerLayoutManager.DETERMINE_BY_MAX_AND_MIN;
            //与底部的距离
            distanceToBottom = ViewPagerLayoutManager.INVALID_SIZE;
        }

        public Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder setAngleInterval(int angleInterval) {
            this.angleInterval = angleInterval;
            return this;
        }

        public Builder setMoveSpeed(int moveSpeed) {
            this.moveSpeed = moveSpeed;
            return this;
        }

        public Builder setMaxRemoveAngle(float maxRemoveAngle) {
            this.maxRemoveAngle = maxRemoveAngle;
            return this;
        }

        public Builder setMinRemoveAngle(float minRemoveAngle) {
            this.minRemoveAngle = minRemoveAngle;
            return this;
        }

        public Builder setReverseLayout(boolean reverseLayout) {
            this.reverseLayout = reverseLayout;
            return this;
        }

        public Builder setGravity(int gravity) {
            assertGravity(gravity);
            this.gravity = gravity;
            return this;
        }

        public Builder setFlipRotate(boolean flipRotate) {
            this.flipRotate = flipRotate;
            return this;
        }

        public Builder setZAlignment(int zAlignment) {
            assertZAlignmentState(zAlignment);
            this.zAlignment = zAlignment;
            return this;
        }

        public Builder setMaxVisibleItemCount(int maxVisibleItemCount) {
            this.maxVisibleItemCount = maxVisibleItemCount;
            return this;
        }

        public Builder setDistanceToBottom(int distanceToBottom) {
            this.distanceToBottom = distanceToBottom;
            return this;
        }

        public CircleLayoutManager build() {
            return new CircleLayoutManager(this);
        }
    }
}
