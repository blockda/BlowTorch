package com.happygoatstudios.bt.service.plugin.settings;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.button.ButtonEditorDialog.COLOR_FIELDS;
import com.happygoatstudios.bt.button.ColorPickerDialog;
import com.happygoatstudios.bt.service.IConnectionBinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class OptionsDialog extends Dialog {

	//ListView primeList;
	//ListView altList;
	IConnectionBinder service;
	//SettingsGroup mRoot;
	//SettingsGroup mCurrent;
	//OptionsAdapter primeAdapter;
	//OptionsAdapter altAdapter;
	SettingsGroup mCurrent;
	String selectedPlugin;
	String[] mEncodings = null;
	//FragmentManager mFragementManager;
	
	
	boolean toggle = true;
	
	Stack<SettingsGroup> backStack = new Stack<SettingsGroup>();
	
	public OptionsDialog(Context context,IConnectionBinder service,String plugin) {
		super(context);
		this.selectedPlugin = plugin;
		this.service = service;
		//this.mFragementManager = fragmentManager;
	}

	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		Vector<String> items = new Vector<String>();
		for(Charset set : Charset.availableCharsets().values()) {
			items.add(set.displayName());
		}
		
		mEncodings = null;
		if(items.size() > 0) {
			mEncodings = new String[items.size()];
			for(int z=0;z<items.size();z++) {
				mEncodings[z] = items.get(z);
			}
		}
		
		LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout tmp = new RelativeLayout(this.getContext());
		
		View root = (RelativeLayout) li.inflate(R.layout.options_dialog, tmp);
		//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(800,400);
		
		//params.width = RelativeLayout.LayoutParams.FILL_PARENT;
		//root.setLayoutParams(params);
		
		/*Button optionsbutton = (Button) root.findViewById(R.id.optionsbutton);
		optionsbutton.setVisibility(View.GONE);
		
		Button newbutton = (Button)root.findViewById(R.id.add);
		newbutton.setVisibility(View.GONE);
		
		Button donebutton = (Button)root.findViewById(R.id.done);
		donebutton.setVisibility(View.GONE);*/
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		
		
		LinearLayout content = (LinearLayout) li.inflate(R.layout.options_dialog_content, null);
		
		//LinearLayout prime = (LinearLayout) root.findViewById(R.id.primelistholder);
		
		
		//LinearLayout alt = (LinearLayout) root.findViewById(R.id.altlistholder);
		//altList = (ListView) alt.findViewById(R.id.list);
		
		ListView list = (ListView) content.findViewById(R.id.list);
		TextView title = (TextView) content.findViewById(R.id.title);
		
		//View empty = root.findViewById(R.id.empty);
		//list.setEmptyView(empty);
		
		try {
			mCurrent = service.getSettings();
			//mCurrent = mRoot;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		title.setText(mCurrent.getTitle());
		
		ViewFlipper flipper = (ViewFlipper) root.findViewById(R.id.flipper);
		//flipper.removeAllViews();
		flipper.addView(content);
		
		OptionsAdapter opt = new OptionsAdapter(mCurrent);
		list.setAdapter(opt);
		opt.notifyDataSetInvalidated();
		//primeList.invalidate();
		
		
		this.setContentView(root);
		
		//adapter.
		//list.re
	}
	
	class OptionsAdapter extends BaseAdapter {

		SettingsGroup group;
		
		public OptionsAdapter(SettingsGroup sg) {
			this.group = sg;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return group.getOptions().size();
		}

		@Override
		public Object getItem(int position) {
			return group.getOptions().get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater) OptionsDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.options_list_row, null);
				//android.R.layout.
			}
			
			ImageView iv = (ImageView) v.findViewById(R.id.icon);
			LinearLayout ivl = (LinearLayout) iv.getParent();
			ivl.setVisibility(View.GONE);
			
			Option o = (Option) this.getItem(position);
			
			TextView title = (TextView) v.findViewById(R.id.infoTitle);
			TextView ext = (TextView) v.findViewById(R.id.infoExtended);
			
			title.setText(o.getTitle());
			ext.setText(o.getDescription());
			
			LinearLayout widget = (LinearLayout) v.findViewById(R.id.widget_frame);
			//widget.setVisibility(View.GONE);
			
			//v.setOnClickListener(l)
			v.setTag(null);
			
			widget.removeAllViews();
			switch(o.type) {
			case BOOLEAN:
				CheckBox cb = new CheckBox(OptionsDialog.this.getContext());
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
				params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
				//params.setMargins(40, 10, 10, 10);
				cb.setLayoutParams(params);
				//widget.setPadding(left, top, right, bottom)
				//cb.setPadding(10, 10, 10, 10);
				BooleanOption bo = (BooleanOption)o;
				if(((Boolean)bo.getValue()).booleanValue() == false) {
					cb.setChecked(false);
				} else {
					cb.setChecked(true);
				}
				cb.setTag(o);
				cb.setOnCheckedChangeListener(new BooleanCheckChangeListener());
				widget.addView(cb);
				
				//must set up the on checkchange listener.
				break;
			case GROUP:
				v.setTag(o);
				v.setOnClickListener(new GroupClickedListener());
				break;
			case LIST:
				//set up list dialog clicker.
				v.setTag(o);
				v.setOnClickListener(new ListOptionClickedListener());
				break;
			case ENCODING:
				v.setTag(o);
				v.setOnClickListener(new EncodingOptionClickedListener());
				break;
			case INTEGER:
				
				v.setTag(o);
				IntegerOption integerOption = (IntegerOption)o;
				TextView indicator = new TextView(OptionsDialog.this.getContext());
				indicator.setTextSize(26);
				indicator.setTextColor(0xFFAAAAAA);
				LinearLayout.LayoutParams iparam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
				iparam.gravity = Gravity.CENTER;
				indicator.setText(Integer.toString((Integer)integerOption.getValue()));
				indicator.setLayoutParams(iparam);
				//indicator.setFocusable(false);
				//indicator.setFocusableInTouchMode(false);
				//widget.setFocusable(flase)
				widget.addView(indicator);
				widget.setTag(o);
				
				v.setOnClickListener(new IntegerOptionClickedListener(indicator));
				widget.setOnClickListener(new IntegerOptionClickedListener(indicator));
				//widget.setOnClickListener(new IntegerOptionClickedListener());
				
				break;
				
			case COLOR:
				v.setTag(o);
				ColorOption co = (ColorOption)o;
				LayoutInflater li = (LayoutInflater)OptionsDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View color_swatch_layout = li.inflate(R.layout.colorswatch_widget, null);
				Button swatch = (Button) color_swatch_layout.findViewById(R.id.colorswatch);
				//swatch.setTag(co);
				swatch.setBackgroundColor((Integer)co.getValue());
				
				v.setOnClickListener(new ColorOptionClickedListener(swatch));
				swatch.setTag(co);
				swatch.setOnClickListener(new ColorOptionClickedListener(swatch));
				
				widget.addView(color_swatch_layout);
				break;
			case FILE:
				v.setTag(o);
				v.setOnClickListener(new FileOptionClickedListener());
				break;
			}
			
			return v;
			
		}


		
	}
	
	private class FileOptionClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			FileOption o = (FileOption)v.getTag();
			
			//this is tricky. we have to build the list. in the right order.
			//first build up the actual file matches, sort them and insert the "items" at the top.
			//ArrayList<String> paths = new ArrayList<String>();
			StringBuilder str = new StringBuilder();
			ArrayList<String> extensions = o.extensions;
			for(int i=0;i<extensions.size();i++) {
				str.append("(^.+(\\Q"+extensions.get(i)+"\\E))");
				if(i != extensions.size()-1) {
					str.append("|");
				}
			}
			
			Pattern p = Pattern.compile(str.toString());
			Matcher m = p.matcher("");
			
			PatternFileNameFilter filter = new PatternFileNameFilter(m);
			
			ArrayList<String> foundFilePaths = new ArrayList<String>();
			ArrayList<String> foundFileNames = new ArrayList<String>();
			
			ArrayList<String> items = o.items;
			for(int i=0;i<items.size();i++) {
				foundFilePaths.add(items.get(i));
				foundFileNames.add(items.get(i));
			}
			ArrayList<String> paths = o.paths;
			for(int i=0;i<paths.size();i++) {
				String path = paths.get(i);
				
				if(path.startsWith("/")) {
					//use it directly
					File file = new File(path);
					for(File found : file.listFiles(filter)) {
						foundFilePaths.add(found.getPath());
						foundFileNames.add(found.getName());
					}
				} else {
					//get it from sdcard.
					String sdstate = Environment.getExternalStorageState();
					if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
						File tmp = Environment.getExternalStorageDirectory();
						File file = new File(tmp,"/"+path);
						for(File found : file.listFiles(filter)) {
							foundFilePaths.add(found.getPath());
							foundFileNames.add(found.getPath());
						}
					}
				}
			}
			
			String[] entries = new String[foundFileNames.size()];
			entries = foundFileNames.toArray(entries);
			
			int selectedIndex = -1;
			for(int i=0;i<foundFilePaths.size();i++) {
				String path = foundFilePaths.get(i);
				if(path.equals((String)o.getValue())) {
					selectedIndex = i;
					i=foundFilePaths.size();
				}
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsDialog.this.getContext());
			builder.setTitle(o.getTitle());
			builder.setSingleChoiceItems(entries, selectedIndex,new FileOptionItemClickListener((FileOption)o,foundFilePaths,foundFileNames));

			AlertDialog dialog = builder.create();
			dialog.show();
			
		}
		
	}
	
	private class FileOptionItemClickListener implements DialogInterface.OnClickListener {

		private ArrayList<String> paths;
		private ArrayList<String> names;
		private FileOption option;
		
		public FileOptionItemClickListener(FileOption option,ArrayList<String> paths,ArrayList<String> names) {
			this.paths = paths;
			this.names = names;
			this.option = option;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String path = paths.get(which);
			option.setValue(path);
			if(selectedPlugin.equals("main")) {
				try {
					service.updateStringSetting(option.getKey(), path);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					service.updatePluginStringSetting(selectedPlugin, option.getKey(), path);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private class PatternFileNameFilter implements FilenameFilter {

		Matcher m;
		public PatternFileNameFilter(Matcher matcher) {
			m = matcher;
		}
		
		@Override
		public boolean accept(File dir, String filename) {
			m.reset(filename);
			if(m.matches()) {
				return true;
			} else {
				return false;
			}
		}

	}
	
	private class ColorOptionClickedListener implements View.OnClickListener,ColorPickerDialog.OnColorChangedListener {

		private ColorOption option;
		Button widget;
		
		public ColorOptionClickedListener(Button widget) {
			this.widget = widget;
		}
		
		@Override
		public void onClick(View v) {
			option = (ColorOption) v.getTag();
			
			ColorPickerDialog dialog = new ColorPickerDialog(OptionsDialog.this.getContext(),this,(Integer)option.getValue());
			dialog.show();
		}

		@Override
		public void colorChanged(int color) {
			option.setValue(color);
			widget.setBackgroundColor(color);
			widget.invalidate();
			if(selectedPlugin.equals("main")) {
				try {
					service.updateIntegerSetting(option.getKey(), color);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					service.updatePluginIntegerSetting(selectedPlugin, option.getKey(), color);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private class IntegerOptionClickedListener implements View.OnClickListener {

		private TextView widget;
		
		public IntegerOptionClickedListener(TextView widget) {
			this.widget = widget;
		}
		
		@Override
		public void onClick(View v) {
			IntegerOption o = (IntegerOption) v.getTag();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsDialog.this.getContext());
			
			builder.setTitle(o.getTitle());
			EditText input = new EditText(OptionsDialog.this.getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			input.setLayoutParams(params);
			input.setTextSize(26);
			input.setText(Integer.toString((Integer)o.getValue()));
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setGravity(Gravity.RIGHT);
			builder.setView(input);
			
			//builder.setView(input);
			
			builder.setPositiveButton("Done", new IntegerOptionFinishedListener(o,input,widget));
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
		}
		
	}
	
	private class IntegerOptionFinishedListener implements DialogInterface.OnClickListener {

		private IntegerOption option;
		EditText input;
		TextView widget;
		
		
		public IntegerOptionFinishedListener(IntegerOption option,EditText input,TextView widget) {
			this.option = option;
			this.input = input;
			this.widget = widget;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			
			String text = input.getText().toString();
			
			try{
				Integer number = Integer.parseInt(text);
				option.setValue(number);
				widget.setText(text);
				if(selectedPlugin.equals("main")) {
					service.updateIntegerSetting(option.getKey(), number);
				} else {
					service.updatePluginIntegerSetting(selectedPlugin,option.getKey(), number);
				}
			
			} catch(NumberFormatException e) {
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			dialog.dismiss();
		}
		
	}
	
	private class EncodingOptionClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			EncodingOption o = (EncodingOption)v.getTag();
			
			int selected = -1;
			String current = (String) o.getValue();
			for(int i=0;i<mEncodings.length;i++) {
				String str = mEncodings[i];
				if(str.equals(current)) {
					selected = i;
					i = mEncodings.length;
				}
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsDialog.this.getContext());
			builder.setTitle("Select Encoding:");
			
			builder.setSingleChoiceItems(mEncodings, selected, new EncodingItemClickListener(o));
			
			AlertDialog dialog = builder.create();
			//GenericDialogFragment gdf = new GenericDialogFragment(dialog);
			//gdf.showWithTag("encoding_dialog");
			//gdf.show(mFragementManager, "encoding_dialog");
			//dialog.show();
			dialog.show();
		}
		
	}
	
	private class EncodingItemClickListener implements DialogInterface.OnClickListener {

		private EncodingOption option;
		
		public EncodingItemClickListener(EncodingOption option) {
			this.option = option;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			String encoding = mEncodings[which];
			String key = option.getKey();
			option.setValue(encoding);
			
			if(selectedPlugin.equals("main")) {
				try {
					service.updateStringSetting(key, encoding);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					service.updatePluginStringSetting(selectedPlugin, key, encoding);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			dialog.dismiss();
		}
		
	}
	
	private class GroupClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			//get the tag for this view, it will be the key.
			
			Option key = (Option) v.getTag();
			backStack.push(mCurrent);
			mCurrent = (SettingsGroup) key;
			
			LayoutInflater li = (LayoutInflater) OptionsDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			LinearLayout newContent = (LinearLayout) li.inflate(R.layout.options_dialog_content, null);
			ListView list = (ListView) newContent.findViewById(R.id.list);
			
			OptionsAdapter newAdapt = new OptionsAdapter(mCurrent);
			list.setAdapter(newAdapt);
			newAdapt.notifyDataSetInvalidated();
			
			//LinearLayout group = (LinearLayout) altList.getParent();
			TextView title = (TextView) newContent.findViewById(R.id.title);
			title.setText(key.getTitle());
			ViewFlipper f = (ViewFlipper) OptionsDialog.this.findViewById(R.id.flipper);
			f.addView(newContent);
			//int amount = altList.getWidth();
			//int amount = 600;
			//TranslateAnimation outAnim = new TranslateAnimation(0,-amount,0,0);
			//TranslateAnimation inAnim = new TranslateAnimation(amount,0,0,0);
			TranslateAnimation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			TranslateAnimation inAnim  = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			outAnim.setDuration(500);
			inAnim.setDuration(500);
			f.setInAnimation(inAnim);
			f.setOutAnimation(outAnim);
			
			
			
			f.showNext();
		}
		
	}
	
	private class ListOptionClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			ListOption o = (ListOption)v.getTag();
			
			ArrayList<String> items = o.getItems();
			String[] foo = new String[items.size()];
			
			foo = items.toArray(foo);
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsDialog.this.getContext());
			
			builder.setTitle(o.getTitle());
			//builder.setSin
			builder.setSingleChoiceItems(foo, ((Integer)o.getValue()).intValue(),new ListItemClickListener(o));
			
			AlertDialog d = builder.create();
			d.show();
			
		}
		
	}
	
	private class ListItemClickListener implements DialogInterface.OnClickListener {

		private ListOption option;
		
		public ListItemClickListener(ListOption option) {
			this.option = option;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String picked = option.getItems().get(which);
			
			option.setValue(which);
			
			if(selectedPlugin.equals("main")) {
				try {
					service.updateIntegerSetting(option.getKey(),which);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				//service.updatePluginSetting(
				try {
					service.updatePluginIntegerSetting(selectedPlugin,option.getKey(),which);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//service.updateSetting
			
			
			dialog.dismiss();
		}
		
	}
	
	private class BooleanCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton v,
				boolean isChecked) {
			BooleanOption o = (BooleanOption) v.getTag();
			
			if(selectedPlugin.equals("main")) {
				try {
					service.updateBooleanSetting(o.getKey(),isChecked);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					service.updatePluginBooleanSetting(selectedPlugin,o.getKey(),isChecked);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if(backStack.size() == 0) {
			try {
				service.saveSettings();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.dismiss();
		} else {
			SettingsGroup key = backStack.pop();
			//Option key = (Option) v.getTag();
			//backStack.push(mCurrent);
			mCurrent = (SettingsGroup) key;
			/*primeAdapter = new OptionsAdapter(mCurrent);
			primeList.setAdapter(primeAdapter);
			primeAdapter.notifyDataSetInvalidated();
			
			LinearLayout group = (LinearLayout) primeList.getParent();
			TextView title = (TextView) group.findViewById(R.id.title);
			title.setText(key.getTitle());*/
			ViewFlipper f = (ViewFlipper) OptionsDialog.this.findViewById(R.id.flipper);
			
			//int amount = altList.getWidth();
			//int amount = 600;
			//TranslateAnimation outAnim = new TranslateAnimation(0,-amount,0,0);
			//TranslateAnimation inAnim = new TranslateAnimation(amount,0,0,0);
			TranslateAnimation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			TranslateAnimation inAnim  = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			
			outAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					ViewFlipper f = (ViewFlipper) OptionsDialog.this.findViewById(R.id.flipper);
					f.removeViewAt(f.getChildCount()-1);
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
			outAnim.setDuration(500);
			inAnim.setDuration(500);
			f.setInAnimation(inAnim);
			f.setOutAnimation(outAnim);
			
			f.showPrevious();
		}
	}
	
}
