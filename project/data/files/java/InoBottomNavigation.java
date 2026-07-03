package com.android.rizxbyte;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;

import java.util.ArrayList;
import java.util.List;

public class InoBottomNavigation extends View {
	
	private final float density;
	private final ArgbEvaluator colorEval = new ArgbEvaluator();
	private final PathInterpolator m3Interpolator = new PathInterpolator(0.2f, 0f, 0f, 1f);
	
	private final List<String> tabs = new ArrayList<>();
	private final List<Drawable> tabIcons = new ArrayList<>();
	
	private int barBackgroundColor = 0xFF1A1A1A;
	private int activeColor = 0xFF8A8A8A;
	private int inactiveColor = 0xFFFFFFFF;
	private int indicatorColor = 0xFF3A3A3A;
	
	private TextPaint textPaint;
	private Paint indicatorPaint;
	private Paint barBgPaint;
	
	private int selectedPosition = 0;
	private float scrollPosition = 0f;
	
	private int pressedTabIndex = -1;
	private InoViewPager associatedViewPager;
	
	private float[] tabPositionsLeft;
	private float[] tabPositionsRight;
	
	private final RectF rectF = new RectF();
	
	public InoBottomNavigation(Context context) {
		this(context, null);
	}
	
	public InoBottomNavigation(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = getResources().getDisplayMetrics().density;
		init();
	}
	
	private void init() {
		setClickable(true);
		
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(12f * density);
		textPaint.setTextAlign(Paint.Align.LEFT); 
		textPaint.setFakeBoldText(true);
		
		indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		indicatorPaint.setStyle(Paint.Style.FILL);
		
		barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		barBgPaint.setStyle(Paint.Style.FILL);
	}
	
	public InoBottomNavigation tab(String title) {
		tabs.add(title);
		tabIcons.add(null);
		updateArrays();
		requestLayout();
		return this;
	}
	
	public InoBottomNavigation tab(String title, Drawable icon) {
		tabs.add(title);
		tabIcons.add(icon);
		updateArrays();
		requestLayout();
		return this;
	}
	
	public void clearTabs() {
		tabs.clear();
		tabIcons.clear();
		selectedPosition = 0;
		scrollPosition = 0f;
		updateArrays();
		requestLayout();
	}
	
	private void updateArrays() {
		tabPositionsLeft = new float[tabs.size()];
		tabPositionsRight = new float[tabs.size()];
	}
	
	public void setupWithViewPager(InoViewPager viewPager) {
		associatedViewPager = viewPager;
		if (viewPager != null) {
			viewPager.addOnPageChangeListener(new InoViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset) {
					scrollPosition = position + positionOffset;
					invalidate(); 
				}
				
				@Override
				public void onPageSelected(int position) {
					selectedPosition = position;
					invalidate();
				}
			});
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredHeight = (int) ((45f + 12f) * density); 
		
		int desiredWidth = (int) (280f * density); 
		
		int resolvedWidth = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY 
		? MeasureSpec.getSize(widthMeasureSpec) : desiredWidth;
		
		setMeasuredDimension(resolvedWidth, resolveSize(desiredHeight, heightMeasureSpec));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (tabs.isEmpty() || tabPositionsLeft == null) return false;
		
		float touchX = event.getX();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			pressedTabIndex = -1;
			for (int i = 0; i < tabs.size(); i++) {
				if (touchX >= tabPositionsLeft[i] && touchX <= tabPositionsRight[i]) {
					pressedTabIndex = i;
					break;
				}
			}
			break;
			
			case MotionEvent.ACTION_UP:
			if (pressedTabIndex != -1) {
				int upIndex = -1;
				for (int i = 0; i < tabs.size(); i++) {
					if (touchX >= tabPositionsLeft[i] && touchX <= tabPositionsRight[i]) {
						upIndex = i;
						break;
					}
				}
				
				if (upIndex == pressedTabIndex && upIndex >= 0 && upIndex < tabs.size()) {
					selectedPosition = upIndex;
					if (associatedViewPager != null) {
						associatedViewPager.setCurrentItem(selectedPosition, true);
					}
				}
			}
			pressedTabIndex = -1;
			invalidate();
			break;
			
			case MotionEvent.ACTION_CANCEL:
			pressedTabIndex = -1;
			break;
		}
		return true;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tabs.isEmpty() || tabPositionsLeft == null) return;
		
		float w = getWidth();
		float h = getHeight();
		
		float cornerRadius = h / 2f; 
		float padding = 6f * density;
		
		float iconSize = 24f * density;
		float spacing = 6f * density;
		
		barBgPaint.setColor(barBackgroundColor);
		rectF.set(0, 0, w, h);
		canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, barBgPaint);
		
		float availableWidth = w - (padding * 2f);
		
		float totalWeights = 0f;
		float[] weights = new float[tabs.size()];
		
		for (int i = 0; i < tabs.size(); i++) {
			float distance = Math.abs(scrollPosition - i);
			float activeWeight = Math.max(0f, Math.min(1f, 1f - distance));
			float interpWeight = m3Interpolator.getInterpolation(activeWeight);
			
			weights[i] = 1.0f;
			totalWeights += weights[i];
		}
		
		float currentX = padding;
		for (int i = 0; i < tabs.size(); i++) {
			float calculatedTabWidth = (weights[i] / totalWeights) * availableWidth;
			
			tabPositionsLeft[i] = currentX;
			tabPositionsRight[i] = currentX + calculatedTabWidth;
			currentX += calculatedTabWidth;
		}

		int leftIndex = (int) Math.floor(scrollPosition);
		int rightIndex = (int) Math.ceil(scrollPosition);
		leftIndex = Math.max(0, Math.min(leftIndex, tabs.size() - 1));
		rightIndex = Math.max(0, Math.min(rightIndex, tabs.size() - 1));
		
		float fraction = scrollPosition - leftIndex;
		float segmentWeight = m3Interpolator.getInterpolation(fraction);
		
		float finalLeft = tabPositionsLeft[leftIndex] + ((tabPositionsLeft[rightIndex] - tabPositionsLeft[leftIndex]) * segmentWeight);
		float finalRight = tabPositionsRight[leftIndex] + ((tabPositionsRight[rightIndex] - tabPositionsRight[leftIndex]) * segmentWeight);
		
		indicatorPaint.setColor(indicatorColor);
		rectF.set(finalLeft, padding, finalRight, h - padding);
		
		float indicatorRadius = (h - (padding * 2f)) / 2f;
		canvas.drawRoundRect(rectF, indicatorRadius, indicatorRadius, indicatorPaint);
		
		for (int i = 0; i < tabs.size(); i++) {
			float tLeft = tabPositionsLeft[i];
			float tRight = tabPositionsRight[i];
			float cx = (tLeft + tRight) / 2f;
			float cy = h / 2f;
			
			float distance = Math.abs(scrollPosition - i);
			float activeWeight = Math.max(0f, Math.min(1f, 1f - distance));
			
			int currentTextColor = (Integer) colorEval.evaluate(activeWeight, inactiveColor, activeColor);
			Drawable icon = (i < tabIcons.size()) ? tabIcons.get(i) : null;
			
			if (icon != null) {
				icon.setTint(currentTextColor);
				
	            float iconLeft = cx - (iconSize / 2f);
				
				int finalIconTop = (int) (cy - (iconSize / 2f));
				icon.setBounds((int) iconLeft, finalIconTop, (int) (iconLeft + iconSize), (int) (finalIconTop + iconSize));
				icon.draw(canvas);
				
				textPaint.setColor(currentTextColor);
				if (activeWeight > 0.8f) {
					textPaint.setAlpha(255);
				} else {
					textPaint.setAlpha(0);
				}
				
				Paint.FontMetrics fm = textPaint.getFontMetrics();
				float textY = cy - ((fm.ascent + fm.descent) / 2f);
				// canvas.drawText(title, textLeft, textY, textPaint);
			}
		}
	}
	
	public void setActiveColor(int color) {
		this.activeColor = color;
		invalidate();
	}
	
	public void setInactiveColor(int color) {
		this.inactiveColor = color;
		invalidate();
	}
	
	public void setIndicatorColor(int color) {
		this.indicatorColor = color;
		invalidate();
	}
	
	public void setBarBackgroundColor(int color) {
		this.barBackgroundColor = color;
		invalidate();
	}
	
}
