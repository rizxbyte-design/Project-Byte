package com.android.rizxbyte;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class CustomFillView extends View {
	
	private Drawable iconDrawable;
	private Paint fillPaint;
	private float progress = 0f;
	private ValueAnimator animator;
	
	public CustomFillView(Context context, AttributeSet attrs) {
		super(context, attrs);
		fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaint.setColor(Color.BLUE);
		fillPaint.setStyle(Paint.Style.FILL);
	}
	
	// Menggunakan getResources() standar tanpa AppCompat
	public void setIcon(int drawableId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			this.iconDrawable = getResources().getDrawable(drawableId, null);
		} else {
			// Untuk support versi sangat lama
			this.iconDrawable = getResources().getDrawable(drawableId);
		}
		
		if (getWidth() > 0 && getHeight() > 0) {
			iconDrawable.setBounds(0, 0, getWidth(), getHeight());
		}
		invalidate();
	}
	
	public void setFillColor(int color) {
		fillPaint.setColor(color);
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (iconDrawable != null) {
			iconDrawable.setBounds(0, 0, w, h);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// 1. Buat off-screen buffer (layer) agar kita bisa melakukan operasi mask
		int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
		
		// 2. Gambar Ikon terlebih dahulu sebagai "Destination"
		if (iconDrawable != null) {
			iconDrawable.draw(canvas);
		}
		
		// 3. Atur Xfermode ke SRC_IN
		// Ini akan memotong gambar berikutnya (air) agar hanya muncul di area ikon
		fillPaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		
		// 4. Gambar kotak pengisi (Air)
		float fillTop = getHeight() - (getHeight() * progress);
		canvas.drawRect(0, fillTop, getWidth(), getHeight(), fillPaint);
		
		// 5. Reset Xfermode dan kembalikan layer
		fillPaint.setXfermode(null);
		canvas.restoreToCount(saveCount);
	}
	
	
	public void start(long duration) {
		if (animator != null && animator.isRunning()) {
			animator.cancel();
		}
		animator = ValueAnimator.ofFloat(0f, 1f);
		animator.setDuration(duration);
		animator.setInterpolator(new LinearInterpolator());
		animator.addUpdateListener(animation -> {
			progress = (float) animation.getAnimatedValue();
			invalidate();
		});
		animator.start();
	}
	
	public void setIconColor(int color) {
		if (iconDrawable != null) {
			// Menerapkan warna pada ikon
			iconDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
			invalidate(); // Gambar ulang view
		}
	}
	
}
