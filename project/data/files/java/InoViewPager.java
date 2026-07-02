package com.android.rizxbyte;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import java.util.ArrayList;
import java.util.List;

public class InoViewPager extends ViewGroup {
	
	private Scroller scroller;
	private VelocityTracker velocityTracker;
	private int touchSlop;
	private int maxVelocity;
	private float lastX;
	private int currentItem = 0;
	private boolean isBeingDragged = false;
	
	private final List<OnPageChangeListener> pageChangeListeners = new ArrayList<>();
	
	public interface OnPageChangeListener {
		void onPageScrolled(int position, float positionOffset);
		void onPageSelected(int position);
	}
	
	public InoViewPager(Context context) {
		this(context, null);
	}
	
	public InoViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		scroller = new Scroller(getContext());
		ViewConfiguration vc = ViewConfiguration.get(getContext());
		touchSlop = vc.getScaledTouchSlop();
		maxVelocity = vc.getScaledMaximumFlingVelocity();
	}
	
	public void addOnPageChangeListener(OnPageChangeListener listener) {
		if (listener != null && !pageChangeListeners.contains(listener)) {
			pageChangeListeners.add(listener);
		}
	}
	
	public void removeOnPageChangeListener(OnPageChangeListener listener) {
		pageChangeListeners.remove(listener);
	}
	
	@Deprecated
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		pageChangeListeners.clear();
		if (listener != null) {
			pageChangeListeners.add(listener);
		}
	}
	
	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}
	
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new MarginLayoutParams(p);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof MarginLayoutParams;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int childCount = getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
				
				int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
						width - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
				int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
						height - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
				
				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			}
		}
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int width = r - l;
		
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
				
				int childLeft = (i * width) + lp.leftMargin;
				int childTop = lp.topMargin;
				int childRight = childLeft + child.getMeasuredWidth();
				int childBottom = childTop + child.getMeasuredHeight();
				
				child.layout(childLeft, childTop, childRight, childBottom);
			}
		}
	}
	
	public void setCurrentItem(int item, boolean smoothScroll) {
		item = Math.max(0, Math.min(item, getChildCount() - 1));
		
		if (currentItem != item) {
			currentItem = item;
			dispatchOnPageSelected(item);
		}
		
		int targetX = item * getWidth();
		
		if (smoothScroll) {
			int delta = targetX - getScrollX();
			scroller.startScroll(getScrollX(), 0, delta, 0, 280); 
			invalidate();
		} else {
			scrollTo(targetX, 0);
		}
	}
	
	private boolean canChildScroll(View v, boolean checkV, float dx, float x, float y) {
		if (v instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) v;
			int scrollX = v.getScrollX();
			int scrollY = v.getScrollY();
			
			for (int i = group.getChildCount() - 1; i >= 0; i--) {
				View child = group.getChildAt(i);
				if (x + scrollX >= child.getLeft()
				&& x + scrollX < child.getRight()
				&& y + scrollY >= child.getTop()
				&& y + scrollY < child.getBottom()) {
					
					if (canChildScroll(child, true, dx, 
					x + scrollX - child.getLeft(), 
					y + scrollY - child.getTop())) {
						return true;
					}
				}
			}
		}
		return checkV && v.canScrollHorizontally((int) -dx);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE && isBeingDragged) {
			return true;
		}
		
		float x = ev.getX();
		
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			lastX = x;
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
				isBeingDragged = true;
			} else {
				isBeingDragged = false;
			}
			break;
			
			case MotionEvent.ACTION_MOVE:
			float diffX = Math.abs(x - lastX);
			if (diffX > touchSlop) {
				float dx = x - lastX;
				if (canChildScroll(this, false, dx, ev.getX(), ev.getY())) {
					isBeingDragged = false;
					return false;
				}
				isBeingDragged = true;
				lastX = x;
			}
			break;
			
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			isBeingDragged = false;
			break;
		}
		return isBeingDragged;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);
		
		float x = event.getX();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}
			lastX = x;
			break;
			
			case MotionEvent.ACTION_MOVE:
			float deltaX = lastX - x;
			lastX = x;
			
			if (getScrollX() + deltaX < 0) {
				scrollTo(0, 0);
			} else if (getScrollX() + deltaX > (getChildCount() - 1) * getWidth()) {
				scrollTo((getChildCount() - 1) * getWidth(), 0);
			} else {
				scrollBy((int) deltaX, 0);
			}
			break;
			
			case MotionEvent.ACTION_UP:
			velocityTracker.computeCurrentVelocity(1000, maxVelocity);
			float velocity = velocityTracker.getXVelocity();
			int width = getWidth();
			
			int targetPage = (getScrollX() + width / 2) / width;
			
			if (Math.abs(velocity) > 600) {
				if (velocity > 0) {
					targetPage = currentItem - 1;
				} else {
					targetPage = currentItem + 1;
				}
			}
			
			targetPage = Math.max(0, Math.min(targetPage, getChildCount() - 1));
			
			if (targetPage != currentItem) {
				currentItem = targetPage;
				dispatchOnPageSelected(targetPage);
			}
			
			setCurrentItem(targetPage, true);
			isBeingDragged = false;
			
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}
			break;
			
			case MotionEvent.ACTION_CANCEL:
			isBeingDragged = false;
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}
			break;
		}
		return true;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		int width = getWidth();
		if (width > 0) {
			float scrollX = (float) l;
			int position = (int) (scrollX / width);
			float offset = (scrollX / width) - position;
			
			for (OnPageChangeListener listener : pageChangeListeners) {
				listener.onPageScrolled(position, offset);
			}
		}
	}
	
	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
	}
	
	private void dispatchOnPageSelected(int page) {
		for (OnPageChangeListener listener : pageChangeListeners) {
			listener.onPageSelected(page);
		}
	}
	
	public int getCurrentItem() {
		return currentItem;
	}
}
