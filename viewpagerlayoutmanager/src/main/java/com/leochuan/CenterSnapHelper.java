package com.leochuan;

import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Class intended to support snapping for a {@link RecyclerView}
 * which use {@link ViewPagerLayoutManager} as its {@link androidx.recyclerview.widget.RecyclerView.LayoutManager}.
 * <p>
 * The implementation will snap the center of the target child view to the center of
 * the attached {@link RecyclerView}.
 */
public class CenterSnapHelper extends RecyclerView.OnFlingListener {

    /**
     * Fling时, 只允许一个一个滚动
     */
    public boolean snapScrollOne = false;
    protected RecyclerView mRecyclerView;
    protected Scroller mGravityScroller;
    /**
     * when the dataSet is extremely large
     * {@link #snapToCenterView(ViewPagerLayoutManager, ViewPagerLayoutManager.OnPageChangeListener)}
     * may keep calling itself because the accuracy of float
     */
    protected boolean snapToCenter = false;
    protected boolean mScrolled = false;

    protected int scrollStartPosition = RecyclerView.NO_POSITION;

    // Handles the snap on scroll case.
    protected final RecyclerView.OnScrollListener mScrollListener =
            new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    RecyclerView.LayoutManager viewLayoutManager = recyclerView.getLayoutManager();

                    if (viewLayoutManager instanceof ViewPagerLayoutManager) {

                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            //开始滚动, 记录当前的布局位置
                            scrollStartPosition = ((ViewPagerLayoutManager) viewLayoutManager).getCurrentPositionOffset();
                        }

                        final ViewPagerLayoutManager layoutManager = (ViewPagerLayoutManager) viewLayoutManager;
                        final ViewPagerLayoutManager.OnPageChangeListener onPageChangeListener =
                                layoutManager.onPageChangeListener;
                        if (onPageChangeListener != null) {
                            onPageChangeListener.onPageScrollStateChanged(newState);
                        }

                        if (newState == RecyclerView.SCROLL_STATE_IDLE && mScrolled) {
                            mScrolled = false;
                            if (!snapToCenter) {
                                snapToCenter = true;
                                snapToCenterView(layoutManager, onPageChangeListener);
                            } else {
                                snapToCenter = false;
                            }
                        }
                    }


                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    if (dx != 0 || dy != 0) {
                        mScrolled = true;
                    }
                }
            };

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        RecyclerView.LayoutManager viewLayoutManager = mRecyclerView.getLayoutManager();
        if (viewLayoutManager instanceof ViewPagerLayoutManager) {

        } else {
            return false;
        }

        ViewPagerLayoutManager layoutManager = (ViewPagerLayoutManager) viewLayoutManager;
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            return false;
        }

        if (!layoutManager.getInfinite() &&
                (layoutManager.mOffset == layoutManager.getMaxOffset()
                        || layoutManager.mOffset == layoutManager.getMinOffset())) {
            return false;
        }

        final int minFlingVelocity = mRecyclerView.getMinFlingVelocity();
        mGravityScroller.fling(0, 0, velocityX, velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

        int currentPosition = scrollStartPosition;
        if (currentPosition == RecyclerView.NO_POSITION) {
            currentPosition = layoutManager.getCurrentPositionOffset();
        }

        int offsetPosition = RecyclerView.NO_POSITION;
        int targetPosition = RecyclerView.NO_POSITION;

        if (layoutManager.mOrientation == ViewPagerLayoutManager.VERTICAL
                && Math.abs(velocityY) > minFlingVelocity) {

            if (snapScrollOne) {
                if (velocityY > 0) {
                    offsetPosition = 1;
                } else {
                    offsetPosition = -1;
                }
            } else {
                offsetPosition = (int) (mGravityScroller.getFinalY() /
                        layoutManager.mInterval / layoutManager.getDistanceRatio());
            }

        } else if (layoutManager.mOrientation == ViewPagerLayoutManager.HORIZONTAL
                && Math.abs(velocityX) > minFlingVelocity) {

            if (snapScrollOne) {
                if (velocityX > 0) {
                    offsetPosition = 1;
                } else {
                    offsetPosition = -1;
                }
            } else {
                offsetPosition = (int) (mGravityScroller.getFinalX() /
                        layoutManager.mInterval / layoutManager.getDistanceRatio());
            }
        }

        targetPosition = layoutManager.getReverseLayout() ?
                -currentPosition - offsetPosition : currentPosition + offsetPosition;

        ScrollHelper.smoothScrollToPosition(mRecyclerView, layoutManager, targetPosition);

        Log.i("angcyo", currentPosition + "->" + targetPosition);

        return true;
    }

    /**
     * Please attach after {{@link androidx.recyclerview.widget.RecyclerView.LayoutManager} is setting}
     * Attaches the {@link CenterSnapHelper} to the provided RecyclerView, by calling
     * {@link RecyclerView#setOnFlingListener(RecyclerView.OnFlingListener)}.
     * You can call this method with {@code null} to detach it from the current RecyclerView.
     *
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     *                     {@code null} if you want to remove CenterSnapHelper from the current
     *                     RecyclerView.
     * @throws IllegalArgumentException if there is already a {@link RecyclerView.OnFlingListener}
     *                                  attached to the provided {@link RecyclerView}.
     */
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView)
            throws IllegalStateException {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (!(layoutManager instanceof ViewPagerLayoutManager)) return;

            setupCallbacks();
            mGravityScroller = new Scroller(mRecyclerView.getContext(),
                    new DecelerateInterpolator());

            snapToCenterView((ViewPagerLayoutManager) layoutManager,
                    ((ViewPagerLayoutManager) layoutManager).onPageChangeListener);
        }
    }

    protected void snapToCenterView(ViewPagerLayoutManager layoutManager,
                                    ViewPagerLayoutManager.OnPageChangeListener listener) {
        final int delta = layoutManager.getOffsetToCenter();
        if (delta != 0) {
            if (layoutManager.getOrientation()
                    == ViewPagerLayoutManager.VERTICAL) {
                mRecyclerView.smoothScrollBy(0, delta);
            } else {
                mRecyclerView.smoothScrollBy(delta, 0);
            }
        } else {
            // set it false to make smoothScrollToPosition keep trigger the listener
            snapToCenter = false;
        }

        if (listener != null) {
            listener.onPageSelected(layoutManager.getCurrentPosition());
        }
    }

    /**
     * Called when an instance of a {@link RecyclerView} is attached.
     */
    protected void setupCallbacks() throws IllegalStateException {
        if (mRecyclerView.getOnFlingListener() != null) {
            throw new IllegalStateException("An instance of OnFlingListener already set.");
        }
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.setOnFlingListener(this);
    }

    /**
     * Called when the instance of a {@link RecyclerView} is detached.
     */
    protected void destroyCallbacks() {
        mRecyclerView.removeOnScrollListener(mScrollListener);
        mRecyclerView.setOnFlingListener(null);
    }
}
