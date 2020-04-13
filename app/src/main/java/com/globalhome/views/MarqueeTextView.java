package com.globalhome.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;

public class MarqueeTextView extends AppCompatTextView implements Runnable {

	private final static String TAG = "MarqueeTextView";
	
	private int currentScrollX; // 当前滚动的位置
	private boolean isStop = false;
	private int textWidth;
	private boolean isMeasure = false;
	private float speed = 2;

	public MarqueeTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		currentScrollX += speed; // 滚动速度
		scrollTo(currentScrollX, 0);
		if (isStop) {
			return;
		}
		int width = this.getWidth() > textWidth ? this.getWidth() : textWidth;
		if (getScrollX() >= width) {
//			scrollTo(this.getWidth(), 0);
//			currentScrollX = -textWidth;
//			currentScrollX = textWidth;

//			currentScrollX = -this.getWidth() - 100;
			currentScrollX = -this.getWidth() / 2;
		}
//		postDelayed(this, 5);
		postDelayed(this, 50);
//		Log.d(TAG, "currentScrollX = " + currentScrollX);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (!isMeasure) { // 文字宽度只需获取一次就可以了
			getTextWidth();
			isMeasure = true;
		}
	}

	/*
	 * 获取文字宽度
	 */
	private void getTextWidth() {
		Paint paint = this.getPaint();
		String str = this.getText().toString();
		textWidth = (int) paint.measureText(str);
		Log.d(TAG, "textWidth = " + textWidth);
	}

	// 开始滚动
	public void startScroll() {
		isStop = false;
		this.removeCallbacks(this);
		post(this);
	}

	// 停止滚动
	public void stopScroll() {
		isStop = true;
	}

	// 从头开始滚动
	public void startFor0() {
		currentScrollX = 0;
		startScroll();
	}
}