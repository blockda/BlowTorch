package com.happygoatstudios.bt.window;

import com.happygoatstudios.bt.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VitalsView extends View implements FloatingVitalMoveDialog.CustomListener {

	//View vitals = null;
	
	Bar health = null;
	Bar mana = null;
	Bar enemy = null;
	float density = 1.0f;
	Point origin = new Point();
	private int width;
	private int height;
	
	public VitalsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public VitalsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VitalsView(Context context) {
		super(context);
		init(context);
	}
	
	public void init(Context c) {
		density = c.getResources().getDisplayMetrics().density;
		//Log.e("VITALS","vitals engates");
		//LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//vitals = li.inflate(R.layout.vitals_widget, null);
		health = new Bar(this.getContext());
		mana = new Bar(this.getContext());
		enemy = new Bar(this.getContext());
		//this.addView(vitals);
		health.setColor(0xFF00FF00);
		mana.setColor(0xFF0000FF);
		enemy.setColor(0xFFFF0000);
		
		health.setMax(100);
		health.setValue(100);
		mana.setMax(100);
		mana.setValue(100);
		enemy.setValue(100);
		enemy.setMax(100);
		
		origin.x = (int) (c.getResources().getDisplayMetrics().widthPixels - enemy.getWidth() - 2*density);
		origin.y = 0;
	}
	
	public void onDraw(Canvas c) {
		//Log.e("VITALS","DRAWING VITALS");
		//c.translate(100, 100);
		//c.drawColor(0xFF3f3f3f);
		//vitals.draw(c);
		//c.save();
		c.translate(origin.x, origin.y);
		c.translate(2*density, 2*density);
		health.draw(c);
		//c.restore();
		//c.save();
		c.translate(0, enemy.getHeight());
		mana.draw(c);
		//c.restore();
		//c.save();
		c.translate(0, enemy.getHeight());
		enemy.draw(c);
		//c.restore();
		
	}
	
	public void updateVitals(int hp,int mana) {
		health.setValue(hp);
		this.mana.setValue(mana);
		//this.enemy.setValue(enemy);
		this.invalidate();
	}
	
	public void updateMaxVitals(int hp,int mana) {
		this.health.setMax(hp);
		this.mana.setMax(mana);
		this.invalidate();
	}

	public void updateEnemyVal(int arg1) {
		enemy.setValue(arg1);
		this.invalidate();
	}

	public void onMove(int dx, int dy) {
		origin.x += dx;
		origin.y += dy;
		this.invalidate();
	}

	public void onSize(int dx, int dy) {
		this.enemy.setRight(this.enemy.getRight() + dx);
		this.mana.setRight(this.mana.getRight() + dx);
		this.health.setRight(this.health.getRight() + dx);
		this.invalidate();
		
	}

	public void updateAllVitals(int hp, int mp, int maxhp, int maxmana,
			int enemy2) {
		this.enemy.setValue(enemy2);
		this.health.setMax(maxhp);
		this.mana.setMax(maxmana);
		this.health.setValue(hp);
		this.mana.setValue(mp);
		this.invalidate();
	}

	public void autoPosition() {
		// TODO Auto-generated method stub
		origin.x = (int) (this.getContext().getResources().getDisplayMetrics().widthPixels - enemy.getWidth() - 2*density);
		origin.y = 0;
	}

	public void savePosition(Editor ed) {
		ed.putInt("LEFT", origin.x);
		ed.putInt("TOP", origin.y);
		ed.putInt("BOTTOM", enemy.getHeight()*3);
		ed.putInt("RIGHT", origin.x + enemy.getWidth());
	}

	public void setRect(int left, int right, int top, int bottom) {
		this.origin.x = left;
		this.origin.y = top;
		this.width = right-left;
		this.height = bottom-top;
		enemy.setRight(width);
		health.setRight(width);
		mana.setRight(width);
	}
	
	public void saveSettings() {
		SharedPreferences.Editor ed = this.getContext().getSharedPreferences("VITALS_CONF", Context.MODE_PRIVATE).edit();
		ed.putInt("LEFT", origin.x);
		ed.putInt("TOP", origin.y);
		ed.putInt("BOTTOM", enemy.getHeight()*3);
		ed.putInt("RIGHT", origin.x + enemy.getWidth());
		ed.commit();
	}

	

}
