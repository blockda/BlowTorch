package com.happygoatstudios.bt.alias;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IConnectionBinder;
import com.happygoatstudios.bt.validator.Validator;
import com.happygoatstudios.bt.window.AnimatedRelativeLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class AliasSelectionDialog extends Dialog implements AliasEditorDialogDoneListener {
	
	//private ArrayList<String> aliases;
	private ArrayList<AliasEntry> entries;
	private AliasAdapter apdapter;
	
	ListView list = null;
	
	private int lastSelectedIndex = -1;
	private RelativeLayout targetHolder = null;
	private int targetIndex = -1;
	String currentPlugin = "main";
	ListView mOptionsList;
	boolean mOptionsListToggle = false;
	int toolbarLength = 0;
	
	LayoutAnimationController animateInController = null;
	TranslateAnimation animateOut = null;
	TranslateAnimation animateOutNoTransition = null;
	
	//View lastSelectedToolbar = null;
	//AliasDialogDoneListener reporto = null;
	HashMap<String,AliasData> input;
	
	IConnectionBinder service;
	
	LinearLayout theToolbar = null;

	public AliasSelectionDialog(Context context,HashMap<String,AliasData> pinput,IConnectionBinder pService) {
		super(context);
		//reporto = useme;
		input = pinput;
		service = pService;
	}
	
	public class CustomAnimationEndListener implements AnimatedRelativeLayout.OnAnimationEndListener {

		@Override
		public void onCustomAnimationEnd() {
			
			RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
			if(rl == null) {
				return;
			}
			rl.removeAllViews();

			if(targetHolder != null) {
				//set the image view.
				AliasEntry data = apdapter.getItem(targetIndex);
				if(data.enabled) {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
				} else {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
				}
				targetHolder.setLayoutAnimation(animateInController);
				
				targetHolder.addView(theToolbar);
			}
			lastSelectedIndex = targetIndex;
		}
		
	}
	
	public CustomAnimationEndListener mCustomAnimationListener = new CustomAnimationEndListener();
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		//if(input != null) {
			//load aliases
			this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
			
			setContentView(R.layout.editor_selection_dialog);
			
			//aliases = new ArrayList<String>();
			entries = new ArrayList<AliasEntry>();
			
			
			list = (ListView)findViewById(R.id.list);
			list.setScrollbarFadingEnabled(false);
			
			//list.setOnItemLongClickListener(new listItemLongClicked());
			
			list.setEmptyView(findViewById(R.id.empty));
			
			list.setScrollbarFadingEnabled(false);
			
			list.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			/*list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					arg0.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
					for(int i = 0;i<apdapter.getCount();i++) {
						int first = arg0.getFirstVisiblePosition();
						int last = arg0.getLastVisiblePosition();
						int index = i;
						boolean dostuff = false;
						if(first <= index && index <= last) {
							index = index - first;
							dostuff = true;
						} else if (index >= last) {
							//dont care.
						} 
						//if(arg0.getChildAt(i) != null) {
						if(dostuff) {
							arg0.getChildAt(index).findViewById(R.id.toolbar_tab).setFocusable(false);
							//arg0.getChildAt(i).findViewById(R.id.toolbar_tab).s(false);
							if(i==arg2) {
								arg0.getChildAt(index).findViewById(R.id.toolbar_tab).setFocusable(true);
							}
						}
						//}
						
					}
					lastSelectedIndex = arg2;
					arg1.findViewById(R.id.toolbar_tab).requestFocus();
					
					Log.e("LIST","SELECTED ELEMENT:" + arg2);
				}

				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
					Log.e("LIST","NOTHING SELECTED");
					
				}
			});*/
			
			//list.setOnFocusChangeListener(new ListFocusFixerListener());
			
			
			/*list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int pos, long arg3) {
					
					TranslateAnimation animation = new TranslateAnimation(toolbarLength,0,0,0);
					animation.setDuration(300);
					AnimationSet set = new AnimationSet(true);
					set.addAnimation(animation);
					LayoutAnimationController lac = new LayoutAnimationController(set);
					RelativeLayout rl = (RelativeLayout)arg1.findViewById(R.id.toolbarholder);
					
					rl.setLayoutAnimation(lac);
					
					lastSelectedIndex = pos;
					
					rl.addView(theToolbar);
					return true;
				}
			});*/
			
//			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> arg0, View arg1,
//						int pos, long arg3) {
//					
//					if(lastSelectedIndex < 0) {
//						/*TranslateAnimation animation = new TranslateAnimation(toolbarLength,0,0,0);
//						animation.setDuration(300);
//						AnimationSet set = new AnimationSet(true);
//						set.addAnimation(animation);
//						LayoutAnimationController lac = new LayoutAnimationController(set);
//						
//						rl.setLayoutAnimation(lac);*/
//						
//						lastSelectedIndex = pos;
//						RelativeLayout rl = (RelativeLayout)arg1.findViewById(R.id.toolbarholder);
//						
//						rl.addView(theToolbar);
//					} else if(lastSelectedIndex != pos) {
//						
//						//TranslateAnimation animation = new TranslateAnimation(0,toolbarLength,0,0);
//						//animation.setDuration(300);
//						/*AnimationSet set = new AnimationSet(true);
//						set.addAnimation(animation);
//						LayoutAnimationController lac = new LayoutAnimationController(set,0.0f);
//						
//						
//						
//						RelativeLayout p = (RelativeLayout)lastSelectedToolbar.getParent();
//						p.setLayoutAnimation(lac);
//						p.removeView(lastSelectedToolbar);*/
//						animateOut = new TranslateAnimation(0,toolbarLength,0,0);
//						animateOut.setDuration(300);
//						animateOut.setAnimationListener(new AnimationListener() {
//
//							@Override
//							public void onAnimationEnd(Animation animation) {
//								RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
//								rl.removeAllViews();
//
//								targetHolder.addView(theToolbar);
//								lastSelectedIndex = targetIndex;
//								
//							}
//
//							@Override
//							public void onAnimationRepeat(Animation animation) {
//								// TODO Auto-generated method stub
//								
//							}
//
//							@Override
//							public void onAnimationStart(Animation animation) {
//								// TODO Auto-generated method stub
//								
//							}
//							
//						});
//						
//						RelativeLayout holder = (RelativeLayout) arg1.findViewById(R.id.toolbarholder);
//						targetHolder = holder;
//						targetIndex = pos;
//						theToolbar.startAnimation(animateOut);
//						theToolbar.invalidate();
//					} else {
//						
//					}
//					
//				}
//			});
			
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					//we just want to have one
					arg1.performClick();
					Log.e("CLICK","CLICK CLICK CLICK CLICK");
				}
			});
			
			list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if(arg2 != lastSelectedIndex) {
						//arg0.is
						if(arg0.getFirstVisiblePosition() <= lastSelectedIndex && arg0.getLastVisiblePosition() >= lastSelectedIndex) {
							if(theToolbar.getParent() != null) {
								theToolbar.startAnimation(animateOutNoTransition);
							}
						} else {
							if(theToolbar.getParent() != null) {
								((RelativeLayout)theToolbar.getParent()).removeAllViews();
							}
						}
					}
					lastSelectedIndex = arg2;
					//check to see if the 
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					
				}
			});
			
			list.setItemsCanFocus(true);
			
			//list.setSelector(R.drawable.transparent);
			/*Object[] keys = input.keySet().toArray();
			Object[] values = input.values().toArray();
			
			for(int i=0;i<keys.length;i++) {
				aliases.add((String)keys[i] + "[||]" + (String)values[i]);
			}*/
			
//			list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//				public void onItemSelected(AdapterView<?> arg0, View arg1,
//						int arg2, long arg3) {
//					arg0.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
//					for(int i = 0;i<apdapter.getCount();i++) {
//						int first = arg0.getFirstVisiblePosition();
//						int last = arg0.getLastVisiblePosition();
//						int index = i;
//						boolean dostuff = false;
//						if(first <= index && index <= last) {
//							index = index - first;
//							dostuff = true;
//						} else if (index >= last) {
//							//dont care.
//						} 
//						//if(arg0.getChildAt(i) != null) {
//						if(dostuff) {
//							arg0.getChildAt(index).findViewById(R.id.toolbar_tab_close).setFocusable(false);
//							//arg0.getChildAt(i).findViewById(R.id.toolbar_tab).s(false);
//							if(i==arg2) {
//								arg0.getChildAt(index).findViewById(R.id.toolbar_tab_close).setFocusable(true);
//							}
//						}
//						//}
//						
//					}
//					lastSelectedIndex = arg2;
//					arg1.findViewById(R.id.toolbar_tab_close).requestFocus();
//					
//					Log.e("LIST","SELECTED ELEMENT:" + arg2);
//				}
//
//				public void onNothingSelected(AdapterView<?> arg0) {
//					// TODO Auto-generated method stub
//					
//					Log.e("LIST","NOTHING SELECTED");
//					
//				}
//			});
			
			
			try {
				input = (HashMap<String, AliasData>) service.getAliases();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(input != null) {
				for(AliasData a : input.values()) {
					entries.add(new AliasEntry(a.getPre(),a.getPost(),a.isEnabled()));
				}
			}
			
			apdapter = new AliasAdapter(list.getContext(),0,entries);
			list.setAdapter(apdapter);
			//list.setTextFilterEnabled(true);
			
			apdapter.sort(new AliasComparator());
			//apdapter.sort(String.CASE_INSENSITIVE_ORDER);
		//}
		
		Button butt = (Button)findViewById(R.id.add);
		
		//if(butt != null) {
		butt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				AliasEditorDialog diag = new AliasEditorDialog(AliasSelectionDialog.this.getContext(),AliasSelectionDialog.this,service,computeNames(""),currentPlugin);
				diag.setTitle("NEW ALIAS");
				diag.show();
			}
		});
		
		Button cancel = (Button)findViewById(R.id.done);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				AliasSelectionDialog.this.dismiss();
			}
		});
		
		TextView title = (TextView)findViewById(R.id.titlebar);
		title.setText("ALIASES");
		
		makeToolbar();
	}
	
	private class LineClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			int pos = v.getId() / 157;
			Log.e("CLICK","this is the clicker, clicked view:"+ pos);
			
			if(lastSelectedIndex < 0) {
				/*TranslateAnimation animation = new TranslateAnimation(toolbarLength,0,0,0);
				animation.setDuration(300);
				AnimationSet set = new AnimationSet(true);
				set.addAnimation(animation);
				LayoutAnimationController lac = new LayoutAnimationController(set);
				
				rl.setLayoutAnimation(lac);*/
				
				lastSelectedIndex = pos;
				RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.toolbarholder);
				rl.setLayoutAnimation(animateInController);
				AliasEntry data = apdapter.getItem(lastSelectedIndex);
				if(data.enabled) {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
				} else {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
				}
				rl.addView(theToolbar);
			} else if(lastSelectedIndex != pos) {
				Log.e("SLDF","AM I EVEN HERE");
				AnimatedRelativeLayout holder = (AnimatedRelativeLayout)theToolbar.getParent();
				if(holder != null) {
					if(list.getFirstVisiblePosition() <= lastSelectedIndex && list.getLastVisiblePosition() >= lastSelectedIndex) {
					
						holder.setAnimationListener(mCustomAnimationListener);
						holder.startAnimation(animateOut);
						targetIndex = pos;
						targetHolder = (RelativeLayout) v.findViewById(R.id.toolbarholder);
						
					} else {
						holder.removeAllViews();
						RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.toolbarholder);
						rl.setLayoutAnimation(animateInController);
						AliasEntry data = apdapter.getItem(pos);
						if(data.enabled) {
							((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
						} else {
							((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
						}
						rl.addView(theToolbar);
					}
				}
				//theToolbar.startAnimation(animateOut);
			} else {
				
				//lastSelectedIndex = -1;
				if(theToolbar.getParent() == null) {
					lastSelectedIndex = pos;
					RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
					holder.setLayoutAnimation(animateInController);
					AliasEntry data = apdapter.getItem(pos);
					if(data.enabled) {
						((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
					} else {
						((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
					}
					holder.addView(theToolbar);
				} else {
					targetIndex = pos;
					theToolbar.startAnimation(animateOutNoTransition);
					
				}
			}
		}
		
	}
	
	private LineClickedListener mLineClicker = new LineClickedListener();
	
	private void makeToolbar() {
		LayoutInflater li = (LayoutInflater)AliasSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		theToolbar = (LinearLayout) li.inflate(R.layout.editor_selection_list_row_toolbar, null);
		RelativeLayout.LayoutParams toolbarParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		theToolbar.setLayoutParams(toolbarParams);
		
		ImageButton toggle = new ImageButton(AliasSelectionDialog.this.getContext());
		ImageButton modify = new ImageButton(AliasSelectionDialog.this.getContext());
		ImageButton delete = new ImageButton(AliasSelectionDialog.this.getContext());
		
		LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		params.setMargins(0, 0, 0, 0);
		
		
		toggle.setLayoutParams(params);
		modify.setLayoutParams(params);
		delete.setLayoutParams(params);
		
		toggle.setPadding(0, 0, 0, 0);
		modify.setPadding(0, 0, 0, 0);
		delete.setPadding(0, 0, 0, 0);
		//AliasEntry a = entries.get(pos);
		//if(a.enabled) {
			toggle.setImageResource(R.drawable.toolbar_toggleon_button);
		//} else {
		//	toggle.setImageResource(R.drawable.toolbar_toggleoff_button);
		//}
		modify.setImageResource(R.drawable.toolbar_modify_button);
		delete.setImageResource(R.drawable.toolbar_delete_button);
		
		toggle.setBackgroundColor(0x0000000000);
		modify.setBackgroundColor(0);
		delete.setBackgroundColor(0);
		
		toggle.setOnKeyListener(theButtonKeyListener);
		modify.setOnKeyListener(theButtonKeyListener);
		delete.setOnKeyListener(theButtonKeyListener);
		
		toggle.setOnClickListener(new ToggleButtonListener());
		modify.setOnClickListener(new ModifyButtonListener());
		delete.setOnClickListener(new DeleteButtonListener());
		
		theToolbar.addView(toggle);
		theToolbar.addView(modify);
		theToolbar.addView(delete);
		
		
		ImageButton close = (ImageButton)theToolbar.findViewById(R.id.toolbar_tab_close);
		close.setOnKeyListener(theButtonKeyListener);
		
		toolbarLength = close.getDrawable().getIntrinsicWidth() + (toggle.getDrawable().getIntrinsicWidth() * 3); 
		
		TranslateAnimation animation2 = new TranslateAnimation(toolbarLength,0,0,0);
		animation2.setDuration(300);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(animation2);
		animateInController = new LayoutAnimationController(set);
		
		animateOut = new TranslateAnimation(0,toolbarLength,0,0);
		animateOut.setDuration(300);
		/*animateOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
				rl.removeAllViews();

				targetHolder.addView(theToolbar);
				lastSelectedIndex = targetIndex;
				
				//list.setSelection(lastSelectedIndex);
				//list.setFocusable(true);
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
		
		animateOutNoTransition = new TranslateAnimation(0,toolbarLength,0,0);
		animateOutNoTransition.setDuration(300);
		animateOutNoTransition.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
				rl.removeAllViews();

				//targetHolder.addView(theToolbar);
				lastSelectedIndex = targetIndex;
				lastSelectedIndex = -1;
				//list.setSelection(lastSelectedIndex);
				//list.setFocusable(true);
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	/*public void onCreate(Bundle b) {
		
	}*/
	
//	public class ListFocusFixerListener implements View.OnFocusChangeListener {
//		public void onFocusChange(View v, boolean hasFocus) {
//			if(hasFocus) {
//				for(int i=0;i<apdapter.getCount();i++) {
//					View view = list.getChildAt(i);
//					if(view != null)  {
//						view.findViewById(R.id.toolbar_tab_close).setFocusable(false);
//					}
//				}
//				if(lastSelectedIndex < 0) {
//					
//				} else {
//					Log.e("LIST","SETTING FOCUS ON:" + lastSelectedIndex);
//					int index = lastSelectedIndex;
//					int first = list.getFirstVisiblePosition();
//					int last = list.getLastVisiblePosition();
//					if(first <= index && index <= last) {
//						index = index - first;
//					} else {
//						index = list.getFirstVisiblePosition();
//					}
//					list.setSelection(lastSelectedIndex);
//					list.getChildAt(index).findViewById(R.id.toolbar_tab_close).setFocusable(true);
//					list.getChildAt(index).findViewById(R.id.toolbar_tab_close).requestFocus();
//				}
//				
//			}
//			Log.e("FOCUS","FOCUS CHANGE LISTENER FIRE, focus is " + hasFocus);
//		}
//	}
	
	private class AliasAdapter extends ArrayAdapter<AliasEntry> {
		private ArrayList<AliasEntry> items;
		
		public AliasAdapter(Context context, int txtviewresid, ArrayList<AliasEntry> objects) {
			super(context, txtviewresid, objects);
			this.items = objects;
		}
		
		public View getView(int pos, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.editor_selection_list_row, null);
				
				RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
				root.setOnClickListener(mLineClicker);
				
			}
			//RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
			
			v.setId(157*pos);
			//root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			
			//if(v.findViewById(theToolbar.getId()) != null) {
				
			//}
			
			RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
			//holder.setLayoutAnimation(animateInController);
			
			holder.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			//String m = items.get(position);
			
			if(holder.getChildCount() > 0) {
				holder.removeAllViews();
				lastSelectedIndex = -1;
			}
			
			//String[] parts = m.split("\\Q[||]\\E");
			AliasEntry a = items.get(pos);
			
			if(a != null) {
				TextView pre = (TextView)v.findViewById(R.id.infoTitle);
				TextView post = (TextView)v.findViewById(R.id.infoExtended);
				//TextView port = (TextView)v.findViewById(R.id.port);
				if(pre != null) {
					//title.setText(" " + m.getDisplayName());
					String str = a.pre;
					if(str.startsWith("^")) str = str.substring(1,str.length());
					if(str.endsWith("$")) str = str.substring(0,str.length()-1);
					pre.setText(str);
				}
				if(post != null) {
					//host.setText("\t"  + m.getHostName() + ":" + m.getPortString());
					post.setText(a.post);
				}
				//if(port != null) {
				//	port.setText(" Port: " + m.getPortString());
				//}
				
				/*Boolean isoffender = false;
				if(offenders.contains(new Integer(position))) {
					isoffender = true;
					//Log.e("ALIASEDITOR","POSITION " + position + " is a circular reference offender.");
				}
				if(isoffender) {
					//Log.e("ALIASEDITOR","POSITION " + position + " is a circular reference offender.");
					//activate view elements to notify the user that this is a circular reference offender.
					TextView tmp1 = (TextView)v.findViewById(R.id.alias_pre);
					TextView tmp2 = (TextView)v.findViewById(R.id.alias_post);
					tmp1.setBackgroundColor(0xAAFF0000);
					tmp2.setBackgroundColor(0xAAFF0000);
					tmp1.setTextColor(0xFF000000);
					tmp2.setTextColor(0xFF000000);
				} else {
					TextView tmp1 = (TextView)v.findViewById(R.id.alias_pre);
					TextView tmp2 = (TextView)v.findViewById(R.id.alias_post);
					tmp1.setBackgroundColor(0xAA630460);
					tmp2.setBackgroundColor(0xAAF7941D);
					tmp1.setTextColor(0xFFF7941D);
					tmp2.setTextColor(0xFF630460);
				}*/
			}
			
			ImageView iv = (ImageView) v.findViewById(R.id.icon);
			if(a.enabled) {
				iv.setImageResource(R.drawable.toolbar_mini_enabled);
			} else {
				iv.setImageResource(R.drawable.toolbar_mini_disabled);
			}
			
			/*ImageButton toggle = new ImageButton(AliasSelectionDialog.this.getContext());
			ImageButton modify = new ImageButton(AliasSelectionDialog.this.getContext());
			ImageButton delete = new ImageButton(AliasSelectionDialog.this.getContext());
			
			LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			params.setMargins(0, 0, 0, 0);
			
			
			toggle.setLayoutParams(params);
			modify.setLayoutParams(params);
			delete.setLayoutParams(params);
			
			toggle.setPadding(0, 0, 0, 0);
			modify.setPadding(0, 0, 0, 0);
			delete.setPadding(0, 0, 0, 0);
			if(a.enabled) {
				toggle.setImageResource(R.drawable.toolbar_toggleon_button);
			} else {
				toggle.setImageResource(R.drawable.toolbar_toggleoff_button);
			}
			modify.setImageResource(R.drawable.toolbar_modify_button);
			delete.setImageResource(R.drawable.toolbar_delete_button);
			
			toggle.setBackgroundColor(0x0000000000);
			modify.setBackgroundColor(0);
			delete.setBackgroundColor(0);
			
			toggle.setOnKeyListener(theButtonKeyListener);
			modify.setOnKeyListener(theButtonKeyListener);
			delete.setOnKeyListener(theButtonKeyListener);
			
			LinearLayout holder = (LinearLayout) v.findViewById(R.id.button_holder);
			holder.removeAllViews();
			holder.addView(toggle);
			holder.addView(modify);
			holder.addView(delete);
			
			int width = toggle.getDrawable().getIntrinsicWidth() + delete.getDrawable().getIntrinsicWidth() + modify.getDrawable().getIntrinsicWidth();
			
			toggle.setOnClickListener(new ToggleButtonListener(pos,iv,a.pre));
			modify.setOnClickListener(new ModifyButtonListener(pos));
			delete.setOnClickListener(new DeleteButtonListener(pos,(ViewFlipper)v.findViewById(R.id.flipper),width));
			
			v.findViewById(R.id.toolbar_tab).setOnClickListener(new ToolbarTabOpenListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,pos));
			
			v.findViewById(R.id.toolbar_tab_close).setOnClickListener(new ToolbarTabCloseListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,v.findViewById(R.id.toolbar_tab)));
			v.findViewById(R.id.toolbar_tab_close).setOnKeyListener(theButtonKeyListener);
			
			v.findViewById(R.id.toolbar_tab).setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus) {
						v.setFocusable(true);
						v.setFocusableInTouchMode(true);
					} else {
						v.setFocusable(false);
						v.setFocusableInTouchMode(false);
					}
				}
			});*/
			return v;
		}
		
		
	}
	
	public class DeleteButtonListener implements View.OnClickListener {

		//private int entry = -1;
		//ViewFlipper flip = null;
		//private int animateDistance = 0;
		public DeleteButtonListener() {
			//entry = element;
			//this.flip = flip;
			//this.animateDistance = animateDistance;
		}
		
		public void onClick(View v) {
			//int index = v.getId() / 160;
			//int index = lastSelectedIndex;
			AlertDialog.Builder builder = new AlertDialog.Builder(AliasSelectionDialog.this.getContext());
			builder.setTitle("Delete Trigger");
			builder.setMessage("Confirm Delete?");
			builder.setPositiveButton("Delete", new ReallyDeleteAliasListener());
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			AlertDialog d = builder.create();
			d.show();
			
			
			
			//arg0.dismiss();
		}
		
	}
	
	public class ReallyDeleteAliasListener implements DialogInterface.OnClickListener {
		//ViewFlipper flip = null;
		//int animateDistance = 0;
		//int entry = -1;
		public ReallyDeleteAliasListener() {
			//this.flip = flip;
			//this.animateDistance = animateDistance;
			//this.entry = entry;
		}
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			Animation a = new TranslateAnimation(0, toolbarLength, 0, 0);
			a.setDuration(300);
			a.setAnimationListener(new DeleteAnimationListener());
			
			theToolbar.startAnimation(a);
			
			//list.setOnFocusChangeListener(null);
			//list.setFocusable(false);
			//flip.setOutAnimation(a);
			//flip.showNext();
		}
		
	}
	
	public class DeleteAnimationListener implements Animation.AnimationListener {

		//int entry = -1;
		public DeleteAnimationListener() {
			//this.entry = entry;
		}
		
		public void onAnimationEnd(Animation animation) {
			//list.setOnFocusChangeListener(null);
			//list.setFocusable(false);
			try {
				if(currentPlugin.equals("main")) {
					service.deleteAlias(entries.get(lastSelectedIndex).pre);
				} else {
					service.deletePluginAlias(currentPlugin,entries.get(lastSelectedIndex).pre);
				}
				
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			//triggerModifier.sendMessageDelayed(triggerModifier.obtainMessage(104), 10);
			apdapter.remove(apdapter.getItem(lastSelectedIndex));
			apdapter.notifyDataSetInvalidated();
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class ModifyButtonListener implements View.OnClickListener {
		//private int index = -1;
		public ModifyButtonListener() {
			//this.index = entry;
		}
		public void onClick(View v) {
			//int index = v.getId() / 159;
			int index = lastSelectedIndex;
			AliasEntry entry = apdapter.getItem(index);
			//launch the trigger editor with this item.
			try {
				AliasData data = null;
				if(currentPlugin.equals("main")) {
					data = service.getAlias(entry.pre);
				} else {
					data = service.getPluginAlias(currentPlugin,entry.pre);
				}
				AliasEditorDialog editor = new AliasEditorDialog(AliasSelectionDialog.this.getContext(),AliasSelectionDialog.this,data.getPre(),data.getPost(),index,data,service,computeNames(data.getPre()),currentPlugin);
				editor.show();
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class ToggleButtonListener implements View.OnClickListener {

		//private int index = -1;
		//ImageView icon = null;
		//String key = null;
	
		public ToggleButtonListener() {
			//this.index = index;
			//this.icon = icon;
			//this.key = key;
		}
		
		public void onClick(View v) {
			int index = lastSelectedIndex;
			AliasEntry entry = apdapter.getItem(index);
			String key = entry.pre;
			//View top = list.getChildAt(index);
			//ViewFlipper flip = top.findViewById(R.id.flipper);
			ImageButton b = (ImageButton)v;
			if(entry.enabled) {
				b.setImageResource(R.drawable.toolbar_toggleoff_button);
				try {
					if(currentPlugin.equals("main")) {
						service.setAliasEnabled( false,key);
					} else {
						service.setPluginAliasEnabled( currentPlugin,false,key);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entry.enabled = false;
				RelativeLayout root = (RelativeLayout) v.getParent().getParent().getParent();
				((ImageView)root.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_disabled);
			} else {
				b.setImageResource(R.drawable.toolbar_toggleon_button);
				try {
					if(currentPlugin.equals("main")) {
						service.setAliasEnabled( true,key);
					} else {
						service.setPluginAliasEnabled( currentPlugin,true,key);
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entry.enabled = true;
				
				RelativeLayout root = (RelativeLayout) v.getParent().getParent().getParent();
				((ImageView)root.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_enabled);
			}
			
		}
		
	}
	
	public ToolBarButtonKeyListener theButtonKeyListener = new ToolBarButtonKeyListener();
	
	public class ToolBarButtonKeyListener implements View.OnKeyListener {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				int first = 0;
				//int last = list.getLastVisiblePosition();
				if(lastSelectedIndex - 1 >= first) {
					/*list.setSelection(lastSelectedIndex -1);
					RelativeLayout row = (RelativeLayout) list.getChildAt(lastSelectedIndex -1);
					row.performClick();*/
					list.setSelection(lastSelectedIndex - 1);
					return true;
				} else {
					return false;
				}
				//break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				int last = list.getCount() -1;
				if(lastSelectedIndex + 1 <= last) {
					/*list.setSelection(lastSelectedIndex +1);
					int childCount = list.getChildCount();
					//list.getAdapter().get
					RelativeLayout row = (RelativeLayout) list.getChildAt(lastSelectedIndex +1);
					row.performClick();*/
					list.setSelection(lastSelectedIndex + 1);
					return true;
				} else {
					return false;
				}
				//break;
			}

			return false;
		}
		
	}
	
	public class ToolbarTabOpenListener implements View.OnClickListener {
		View parent = null;
		ViewFlipper targetFlipper = null;
		int toolbarLength = 0;
		private int index;
		
		public ToolbarTabOpenListener(View parent, ViewFlipper targetFlipper, int toolBarWidth,int index) {
			this.parent = parent;
			this.targetFlipper = targetFlipper;
			toolbarLength = toolBarWidth;
			this.index = index;
		}
		
		public void onClick(View v) {
			//v.requestFocus();
			lastSelectedIndex = this.index;
			
			//int targetWidth = 100;
			Animation ai = new TranslateAnimation(toolbarLength, 0, 0, 0);
			ai.setDuration(800);
			
			targetFlipper.setInAnimation(ai);
			
			Animation ao = new TranslateAnimation(0, toolbarLength, 0, 0);
			ao.setDuration(800);
			
			targetFlipper.setOutAnimation(ao);
			
			targetFlipper.showNext();
			
			parent.findViewById(R.id.toolbar_tab_close).requestFocus();
		}
		
	}
	
	public class ToolbarTabCloseListener implements View.OnClickListener {
		View viewToFocus = null;
		View parent = null;
		ViewFlipper targetFlipper = null;
		int toolbarLength = 0;
		public ToolbarTabCloseListener(View parent, ViewFlipper targetFlipper, int toolBarWidth,View viewToFocus) {
			this.parent = parent;
			this.viewToFocus = viewToFocus;
			this.targetFlipper = targetFlipper;
			toolbarLength = toolBarWidth;
		}
		
		public void onClick(View v) {
			//int totalWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_holder).getWidth();
			//int tabWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_tab).getWidth();
			
			//int targetWidth = TriggerSelectionDialog.this.findViewById(R.id.button_holder).getWidth();
			
			Animation ao = new TranslateAnimation(0, toolbarLength, 0, 0);
			ao.setDuration(800);
			//a.setFillBefore(true);
			//a.setFillAfter(false);
			targetFlipper.setOutAnimation(ao);
			
			Animation ai = new TranslateAnimation(toolbarLength, 0, 0, 0);
			ai.setDuration(800);
			//a.setFillBefore(true);
			//a.setFillAfter(false);
			targetFlipper.setInAnimation(ai);
			targetFlipper.showNext();
			
			//parent.findViewById(R.id.toolbar_tab).requestFocus();
			viewToFocus.setFocusable(true);
			viewToFocus.requestFocus();
		}
		
	}
	
	private class listItemLongClicked implements ListView.OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			//Log.e("LAUNCHER","List item long clicked!");
			AliasEntry muc = apdapter.getItem(arg2);
			
			
			Message delmsg = aliasModifier.obtainMessage(MSG_DELETEALIAS);
			delmsg.obj = new AliasData(muc.pre,muc.post,muc.enabled);
			delmsg.arg1 = arg2; //add position
			
			Message modmsg = aliasModifier.obtainMessage(MSG_MODIFYALIAS);
			modmsg.obj = new AliasData(muc.pre,muc.post,muc.enabled);
			modmsg.arg1 = arg2; //add position
			
			AlertDialog.Builder build = new AlertDialog.Builder(AliasSelectionDialog.this.getContext())
				.setMessage("Edit or Delete Alias?");
			AlertDialog dialog = build.create();
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Edit", modmsg);
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Delete",delmsg);
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			
			dialog.show();
			return true;
		}
		
	}
	
	public final int MSG_DELETEALIAS = 101;
	public final int MSG_MODIFYALIAS = 102;
	public Handler aliasModifier = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_DELETEALIAS:
				AliasEntry tmp = apdapter.getItem(msg.arg1);
				apdapter.remove(apdapter.getItem(msg.arg1));
				//check to see if this is an offender
				for(int i=0;i<apdapter.getCount();i++) {
					//AliasEntry e = apdapter.getItem(i);
					validateList();
				}
				
				apdapter.notifyDataSetChanged();
				apdapter.sort(new AliasComparator());
				
				String oldKey = tmp.pre;
				if(oldKey.startsWith("^")) oldKey = oldKey.substring(1,oldKey.length());
				if(oldKey.endsWith("$")) oldKey = oldKey.substring(0,oldKey.length()-1);
				
				try {
					HashMap<String,AliasData> existingAliases = (HashMap<String, AliasData>) service.getAliases();
					existingAliases.remove(oldKey);
					service.setAliases(existingAliases);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Log.e("ALIASED","DELETING ALIAS");
				break;
			case MSG_MODIFYALIAS:
				//String tomodify = (String)msg.obj;
				//String[] parts = tomodify.split("\\Q[||]\\E");
				int position = msg.arg1;
				AliasEditorDialog diag = new AliasEditorDialog(AliasSelectionDialog.this.getContext(),AliasSelectionDialog.this,((AliasData)msg.obj).getPre(),((AliasData)msg.obj).getPost(),position,(AliasData)msg.obj,service,computeNames(((AliasData)msg.obj).getPre()),currentPlugin);
				diag.setTitle("Modify Alias:");
				diag.show();
				break;
			default:
				break;
			}
		}
	};
	
	ArrayList<String> names = new ArrayList<String>();
	
	private List<String> computeNames(String name) {
		names.clear(); 
		
		if(name.startsWith("^")) name = name.substring(1,name.length());
		if(name.endsWith("$")) name = name.substring(0,name.length()-1);
		
		try {
			HashMap<String,AliasData> existingAliases = (HashMap<String, AliasData>) service.getAliases();
			
			if(existingAliases != null) {
				for(String key : existingAliases.keySet()) {
					if(!key.equals(name)) {
						names.add(key);
					}
				}
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*for(int i=0;i<apdapter.getCount();i++) {
			if(!apdapter.getItem(i).pre.equals(name)) {
				//Log.e("FLOOP","COMPUTED " + apdapter.getItem(i).pre + " AS INVALID");
				names.add(apdapter.getItem(i).pre);
			}
		}*/
		
		return names;
	}

	public void newAliasDialogDone(String pre, String post,boolean enabled) {

		
		/****
		 * 
		 * CHECK FOR CIRCULUAR REFERENCES
		 * 
		 */

		lastSelectedIndex = -1;
		if(theToolbar.getParent() != null) {
			((RelativeLayout)theToolbar.getParent()).removeView(theToolbar);
		}
		AliasEntry tmp = new AliasEntry(pre,post,enabled);
		apdapter.add(tmp);
		apdapter.notifyDataSetChanged();
		apdapter.sort(new AliasComparator());
		
		try {
			HashMap<String,AliasData> existingAliases = null;
			if(currentPlugin.equals("main")) {
				existingAliases =(HashMap<String, AliasData>) service.getAliases();
			} else {
				existingAliases =(HashMap<String, AliasData>) service.getPluginAliases(currentPlugin);
			}
			
			AliasData newAlias = new AliasData();
			newAlias.setPost(post);
			newAlias.setPre(pre);
			newAlias.setEnabled(enabled);
			String newKey = newAlias.getPre();
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			
			existingAliases.put(newKey, newAlias);
			if(currentPlugin.equals("main")) {
				service.setAliases(existingAliases);
			} else {
				service.setPluginAliases(currentPlugin,existingAliases);
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//int pos = apdapter.getPosition(tmp);
		/*boolean validated = validateList();
		if(!validated) {
			//do some stuff to make the dialog better.
			apdapter.notifyDataSetChanged();
			apdapter.sort(new AliasComparator());
		} else {
			//Log.e("ALIASEDITOR","NEW ALIAS VALID!");
			offenders.removeAllElements();
			offenders.clear();
			apdapter.notifyDataSetChanged();
			apdapter.sort(new AliasComparator());
			//apdapter.
		}*/
		//apdapter.sort(String.CASE_INSENSITIVE_ORDER);

	}
	
	public void editAliasDialogDone(String pre,String post,boolean enabled,int pos,AliasData orig) {
		lastSelectedIndex = -1;
		if(theToolbar.getParent() != null) {
			((RelativeLayout)theToolbar.getParent()).removeView(theToolbar);
		}
		
		apdapter.remove(apdapter.getItem(pos));
		AliasEntry tmp = new AliasEntry(pre,post,enabled);
		apdapter.insert(tmp,pos);
		apdapter.notifyDataSetChanged();
		apdapter.sort(new AliasComparator());
		pos = apdapter.getPosition(tmp);
		
		//remove from the list and add the new one.
		try {
			HashMap<String,AliasData> existingAliases = null;
			if(currentPlugin.equals("main")) {
				existingAliases =(HashMap<String, AliasData>) service.getAliases();
			} else {
				existingAliases =(HashMap<String, AliasData>) service.getPluginAliases(currentPlugin);
			}
			String oldKey = orig.getPre();
			if(oldKey.startsWith("^")) oldKey = oldKey.substring(1,oldKey.length());
			if(oldKey.endsWith("$")) oldKey = oldKey.substring(0,oldKey.length()-1);
			existingAliases.remove(oldKey);
			
			String newKey = pre;
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			AliasData newAlias = new AliasData();
			newAlias.setPre(pre);
			newAlias.setPost(post);
			newAlias.setEnabled(enabled);
			existingAliases.put(newKey, newAlias);
			if(currentPlugin.equals("main")) {
				service.setAliases(existingAliases);
			} else {
				service.setPluginAliases(currentPlugin,existingAliases);
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*boolean validated = validateList();
		if(!validated) {
			//do some stuff to make the dialog better.
			apdapter.notifyDataSetChanged();
			//apdapter.sort(new AliasComparator());
		} else {
			//Log.e("ALIASEDITOR","EDITED ALIAS VALID!");
			offenders.removeAllElements();
			offenders.clear();
			apdapter.notifyDataSetChanged();
			//apdapter.sort(new AliasComparator());
		}*/
		//apdapter.sort(String.CASE_INSENSITIVE_ORDER);
	}
	
	Vector<Integer> offenders = new Vector<Integer>();
	
	public boolean validateList() {
		Boolean retval = true;
		int count = apdapter.getCount();
		
		offenders.removeAllElements();
		offenders.clear();
		
		
		
		for(int j=0;j<count;j++) {
			Integer offendingpos = j;
			AliasEntry test = apdapter.getItem(j);
			Pattern wb_pre = Pattern.compile("\\b"+apdapter.getItem(j).pre+"\\b");
			Matcher inc_pre = wb_pre.matcher("");
			for(int i=0;i<count;i++) {
				AliasEntry a = apdapter.getItem(i);
				//String[] parts = test.split("\\Q[||]\\E");
				//String test_pre = parts[0];
				//String test_post = parts[1];
				
				//test to see if the test post matches the new pre, afterstep test
				//Pattern wb_pre = Pattern.compile("\\b"+a.pre+"\\b");
				Pattern test_pre = Pattern.compile("\\b" + a.pre + "\\b");
				Matcher ma_pre = test_pre.matcher(test.post);
				//Matcher ma_pre = wb_pre.matcher(input)
				if(ma_pre.find()) {
					//it is circuluar only if the old pre is contained in the new pre.
					inc_pre.reset(a.post);
					if(inc_pre.find()) {
						//circular reference. flag accordingly.
						//Toast.makeText(this.getContext(), "CIRCULAR REFERENCES DETECTED", 2000);
						//Log.e("ALIASEDITOR","CIRCULAR ALIAS DETECTED!");
						offenders.add(new Integer(i));
						retval = false;
						
						if(!offenders.contains(offendingpos)) {
							offenders.add(offendingpos);
						}
					}
				}
			}
		}
		return retval;
	}
	
	private class AliasComparator implements Comparator<AliasEntry> {

		public int compare(AliasEntry a, AliasEntry b) {
			String a_str = a.pre;
			String b_str = b.pre;
			if(a_str.startsWith("^")) a_str = a_str.substring(1, a_str.length());
			if(a_str.endsWith("$")) a_str = a_str.substring(0, a_str.length()-1);
			
			if(b_str.startsWith("^")) b_str = b_str.substring(1, b_str.length());
			if(b_str.endsWith("$")) b_str = b_str.substring(0, b_str.length()-1);
			return a_str.compareToIgnoreCase(b_str);
		}
		
	}
	
	private class AliasEntry { 
		public String pre;
		public String post;
		boolean enabled = true;
		/*public AliasEntry() {
			pre = "";
			post = "";
		}*/
		
		public AliasEntry(String pPre,String pPost,boolean enabled) {
			pre = pPre;
			post = pPost;
			this.enabled = enabled;
		}		
		/*public AliasEntry(AliasData i) {
			pre = i.getPre();
			post = i.getPost();
		}*/
	}

}