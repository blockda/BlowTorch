package com.happygoatstudios.bt.window;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
        private int[] mColors;
        private OnColorChangedListener mListener;
        private ButtonEditorDialog.COLOR_FIELDS thefield;

        ColorPickerView(Context c, OnColorChangedListener l, int color,ButtonEditorDialog.COLOR_FIELDS usethisfield) {
            super(c);
            mListener = l;
            thefield = usethisfield;
            mColors = new int[] {
                0xAAFF0000, 0xAAFF00FF, 0xAA0000FF, 0xAA00FFFF, 0xAA00FF00,
                0xAAFFFF00, 0xAAFF0000
            };
            Shader s = new SweepGradient(0, 0, mColors, null);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32);

            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(5);
        }

        private boolean mTrackingCenter;
        private boolean mHighlightCenter;

        @Override 
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;

            canvas.translate(CENTER_X, CENTER_X);

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
            setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
        }

        private static final int CENTER_X = 100;
        private static final int CENTER_Y = 100;
        private static final int CENTER_RADIUS = 32;

        private int floatToByte(float x) {
            int n = java.lang.Math.round(x);
            return n;
        }
        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }

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

        private int rotateColor(int color, float rad) {
            float deg = rad * 180 / 3.1415927f;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();

            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);

            final float[] a = cm.getArray();

            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

            return Color.argb(Color.alpha(color), pinToByte(ir),
                              pinToByte(ig), pinToByte(ib));
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

		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar arg0) {
			//doUpdate()
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

                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setShader(s);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(32);

                mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mCenterPaint.setColor((modalpha|(color&0x00FFFFFF)));
                mCenterPaint.setStrokeWidth(5);
                
                this.invalidate();
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
        
        RelativeLayout relay = new RelativeLayout(getContext());
        ColorPickerView view = new ColorPickerView(getContext(), l, mInitialColor,whichfield);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        
        RelativeLayout.LayoutParams barparams = new RelativeLayout.LayoutParams(250,LayoutParams.WRAP_CONTENT);
        barparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,1);
        barparams.addRule(RelativeLayout.BELOW,view.getId());
        barparams.leftMargin = 10;
        barparams.rightMargin = 10;
        //barparams.width = LayoutParams.FILL_PARENT;
        //barparams.height = LayoutParams.WRAP_CONTENT;
        
        SeekBar sb = new SeekBar(getContext());
        sb.setMax(255);
        sb.setProgress(0xAA);
        
        sb.setOnSeekBarChangeListener(view);
        
        sb.setLayoutParams(barparams);
        
        view.setLayoutParams(params);
        
        relay.setLayoutParams(params);
        relay.addView(view);
        relay.addView(sb);

        setContentView(relay);
        
        setTitle("Pick a Color");
    }
}
