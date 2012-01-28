package com.happygoatstudios.bt.window;

import java.util.ArrayList;

import com.happygoatstudios.bt.window.LayerManager.Border;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class BorderLayer extends View {

	LayerManager mLayerManager = null;
	private Paint borderPaint = new Paint();
	public BorderLayer(Context context,LayerManager manager) {
		super(context);
		mLayerManager = manager;
		borderPaint.setStrokeWidth(5);
		borderPaint.setColor(0xFF444488);
	}
	
	public void onDraw(Canvas c) {
		ArrayList<Border> borders = mLayerManager.borders;
		
		for(int i = 0;i<borders.size();i++) {
			Border b = borders.get(i);
			//c.drawLine(b.p1.x, b.p1.y, b.p2.x, b.p2.y, borderPaint);
		}
	}

}
