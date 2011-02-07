package com.happygoatstudios.bt.button;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.graphics.Path.Direction;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class ColorPickerDialog extends Dialog {

    public interface OnColorChangedListener {
        void colorChanged(int color,ButtonEditorDialog.COLOR_FIELDS which);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;
    private ButtonEditorDialog.COLOR_FIELDS whichfield;

    private static class ColorPickerView extends View implements SeekBar.OnSeekBarChangeListener {
        private Paint mPaint;
        private Paint mCenterPaint;
        private Paint mCenterIndicator;
        private int[] mColors;
        private OnColorChangedListener mListener;
        private ButtonEditorDialog.COLOR_FIELDS thefield;
        private Path circle_path;
        private Paint mCenterCircle;

        ColorPickerView(Context c, OnColorChangedListener l, int color,ButtonEditorDialog.COLOR_FIELDS usethisfield) {
            super(c);
            mListener = l;
            thefield = usethisfield;
            int alphapart = (0xFF000000&color);
            mColors = new int[] {
                (alphapart|0x00FF0000), (alphapart|0x00FF00FF), (alphapart|0x000000FF), (alphapart|0x0000FFFF), (alphapart|0x0000FF00),
                		(alphapart|0x00FFFF00), (alphapart|0x00FF0000)
            };
            Shader s = new SweepGradient(0, 0, mColors, null);

            //get the screen density.
            float scale = this.getContext().getResources().getDisplayMetrics().density;
            
            //CENTER_X = 125;
            //CENTER_Y = 125;
            //CENTER_RADIUS = 32;
            
            CENTER_X = (int) (83*scale);
            CENTER_Y = (int) (83*scale);
            CENTER_RADIUS = (int) (21*scale);
            
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            //mPaint.setStrokeWidth(42);
            mPaint.setStrokeWidth(28*scale);

            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(3*scale);
            
            mCenterIndicator = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterIndicator.setColor(0xFFAAAAAA);
            mCenterIndicator.setStrokeWidth(2);
            mCenterIndicator.setTextSize(10.0f * this.getContext().getResources().getDisplayMetrics().density);
            
            mCenterCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterCircle.setColor(0xFFAAAAAA);
            mCenterCircle.setStrokeWidth(2);
            mCenterCircle.setStyle(Paint.Style.STROKE);
            //circle_path = new Path();
           // circle_path.addCircle(0, 0, (float) (CENTER_X - mPaint.getStrokeWidth()*0.5f*0.5), Direction.CW);
            //Matrix m = new Matrix();
            //m.reset();
           // m.postRotate(-90);
           // circle_path.transform(m);
        }

        private boolean mTrackingCenter;
        private boolean mHighlightCenter;

        @Override 
        protected void onDraw(Canvas canvas) {
        	//float scale = this.getContext().getResources().getDisplayMetrics().density;
        	//Log.e("COLORPICK","CENTER_X:" + CENTER_X + " || mPaint.strokewidth" + mPaint.getStrokeWidth() );
        	
            float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;
        	
        	//CENTER_RADIUS = (int) (21*scale);
        	//float r = CENTER_RADIUS;
            circle_path = new Path();
            //circle_path.addCircle(0, 0, (float) (r*0.5), Direction.CW);
            float nr = (float) (mCenterPaint.getStrokeWidth() + CENTER_RADIUS + 5 * this.getContext().getResources().getDisplayMetrics().density);
            circle_path.addOval(new RectF(-nr, -nr, nr, nr), Direction.CW);
            Matrix m = new Matrix();
            m.reset();
            m.postRotate(-190);
            circle_path.transform(m);

            canvas.translate(CENTER_X, CENTER_X);

            canvas.drawTextOnPath("Select Inside To Confirm.", circle_path, 0, -3, mCenterIndicator);
            canvas.drawPath(circle_path, mCenterCircle);
            
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);            
            canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

            if (mTrackingCenter) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);

                if (mHighlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                } else {
                    mCenterPaint.setAlpha(0x80);
                }
                canvas.drawCircle(0, 0,
                                  CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                                  mCenterPaint);

                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        	float scale = this.getContext().getResources().getDisplayMetrics().density;
            setMeasuredDimension((int)(83*scale)*2, (int)(83*scale)*2);
            
        }

        private  int CENTER_X = 125;
        private int  CENTER_Y = 125;
        private int CENTER_RADIUS = 32;

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    if (inCenter) {
                        mHighlightCenter = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter;
                            invalidate();
                        }
                    } else {
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle/(2*PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        mCenterPaint.setColor(interpColor(mColors, unit));
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        if (inCenter) {
                            mListener.colorChanged(mCenterPaint.getColor(),thefield);
                        }
                        mTrackingCenter = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            return true;
        }

		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			doUpdate(arg0.getProgress(),mCenterPaint.getColor());
		}
		
		public void doUpdate(int newAlpha,int color) {
			//Log.e("COLORPICKER","SEEKBAR UPDATE WITH" + newAlpha);
			int modalpha = newAlpha << 24;
			//Log.e("COLORPICKER","MOD ALPHA IS 0x" + Integer.toHexString(modalpha));
            mColors = new int[] {
                    (modalpha|0x00FF0000), (modalpha|0x00FF00FF), (modalpha|0x000000FF), (modalpha|0x0000FFFF), (modalpha|0x0000FF00),
                    (modalpha|0x00FFFF00), (modalpha|0x00FF0000)
                };
                Shader s = new SweepGradient(0, 0, mColors, null);

                //mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                //mPaint.setShader(s);
                //mPaint.setStyle(Paint.Style.STROKE);
                //mPaint.setStrokeWidth(42);

                //mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                //mCenterPaint.setColor((modalpha|(color&0x00FFFFFF)));
                //mCenterPaint.setStrokeWidth(5);
                
                float scale = this.getContext().getResources().getDisplayMetrics().density;
                
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setShader(s);
                mPaint.setStyle(Paint.Style.STROKE);
                //mPaint.setStrokeWidth(42);
                mPaint.setStrokeWidth(28*scale);

                mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mCenterPaint.setColor((modalpha|(color&0x00FFFFFF)));
                mCenterPaint.setStrokeWidth(3*scale);
                
                this.invalidate();
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			
		}
    }

    public ColorPickerDialog(Context context,
                             OnColorChangedListener listener,
                             int initialColor,ButtonEditorDialog.COLOR_FIELDS fieldtouse) {
        super(context);

        

        
        mListener = listener;
        mInitialColor = initialColor;
        whichfield = fieldtouse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color,ButtonEditorDialog.COLOR_FIELDS whichfield) {
                mListener.colorChanged(color,whichfield);
                dismiss();
            }
        };
        
        float scale = this.getContext().getResources().getDisplayMetrics().density;
        
        this.getWindow().setBackgroundDrawableResource(com.happygoatstudios.bt.R.drawable.dialog_window_crawler1);
        this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //strip off the alpha part.
        int alphapart = ((mInitialColor&0xFF000000)>>24)&0x000000FF;
			//Log.e("COLORPICKER","AlphaPart is:"+alphapart);
			
	    RelativeLayout relay = new RelativeLayout(getContext());
	    RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	    relay.setLayoutParams(lparams);
	    //relay.setBackgroundResource(R.drawable.dialog_frame);
	    setContentView(relay);
	    
	    
		RelativeLayout.LayoutParams titlep = new RelativeLayout.LayoutParams((int) (166.66*scale),LayoutParams.WRAP_CONTENT);
		titlep.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		TextView title = new TextView(this.getContext());
		title.setText("COLOR PICKER");
		title.setBackgroundColor(0xFF999999);
		title.setTextColor(0xFF333333);
		title.setLayoutParams(titlep);
		title.setId(0x01);
		title.setGravity(Gravity.CENTER);
		title.setTextSize(15*this.getContext().getResources().getDisplayMetrics().density);
		title.setTypeface(Typeface.DEFAULT_BOLD);
		relay.addView(title);
        

        
        ColorPickerView view = new ColorPickerView(getContext(), l, mInitialColor,whichfield);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW,0x01);
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.topMargin =  (int) (5 * this.getContext().getResources().getDisplayMetrics().density);
        view.setLayoutParams(params);
        view.setId(0x02);
        relay.addView(view);
        //params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);

       
        
        RelativeLayout.LayoutParams barparams = new RelativeLayout.LayoutParams((int) (166.66*scale),LayoutParams.WRAP_CONTENT);
        //barparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,1);
        barparams.addRule(RelativeLayout.BELOW,0x02);
        barparams.addRule(RelativeLayout.ALIGN_LEFT,1);
        barparams.topMargin = (int) (5 * this.getContext().getResources().getDisplayMetrics().density);
        //barparams.leftMargin = barparams.topMargin;
        //barparams.rightMargin = barparams.topMargin;
        //barparams.bottomMargin = barparams.topMargin;
        //barparams.width = LayoutParams.FILL_PARENT;
        //barparams.height = LayoutParams.WRAP_CONTENT;
        
        SeekBar sb = new SeekBar(getContext());
        sb.setMax(255);
        sb.setProgress(alphapart);
       
        
        sb.setOnSeekBarChangeListener(view);
        
        sb.setLayoutParams(barparams);
        
        
        relay.addView(sb);
        
        relay.forceLayout();
        relay.invalidate();
        

        //setTitle("Color Picker:");
    }
}
