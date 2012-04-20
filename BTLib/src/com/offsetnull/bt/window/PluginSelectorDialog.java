package com.offsetnull.bt.window;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import com.offsetnull.bt.R;
import com.offsetnull.bt.service.plugin.settings.PluginDescription;
import com.offsetnull.bt.service.plugin.settings.QuickPluginParser;

import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class PluginSelectorDialog extends Dialog {

	Pattern xmlpattern = Pattern.compile("^.+\\.[xX][mM][lL]$");
	final Matcher xmlmatch = xmlpattern.matcher("");
	PluginSearchAdapter adapter = null;
	//ListView list = null;
	Stack<InfoStackItem> infoCacheStack = new Stack<InfoStackItem>();
	
	private InfoStackItem current_item = null;
	//HashMap<String,PluginDescription[]> infoCache = new HashMap<String,PluginDescription[]>();
	public PluginSelectorDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout tmp = new RelativeLayout(this.getContext());
		
		View root = (RelativeLayout) li.inflate(R.layout.options_dialog, tmp);
		
		this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		
		
		RelativeLayout content = (RelativeLayout) li.inflate(R.layout.options_dialog_content, null);
		
		//list = (ListView) content.findViewById(R.id.list);
		TextView title = (TextView) content.findViewById(R.id.title);
		
		//build list
		
		title.setText("/mnt/sdcard/BlowTorch/plugins");
		
		//ViewFlipper flipper = (ViewFlipper) root.findViewById(R.id.flipper);
		//flipper.addView(content);
		
		
		
		//adapter = new PluginSearchAdapter(this.getContext(),0,new File[] { });
		//list.setAdapter(adapter);
		//adapter.notifyDataSetInvalidated();
		
		this.setContentView(root);
		

		
		//launch the real list building.
		addPage("/mnt/sdcard/BlowTorch/plugins");
	}
	
	private void addPage(String path) {
		
		if(infoCacheStack.size() > 0) {
			HashMap<String,PluginDescription[]> map = infoCacheStack.peek().getInfoMap();
			if(map != null) {
				PluginDescription[] info = map.get(path);
				
				if(info != null) {
					//build the info page. for the current infostack
					//PluginDescription[] info = infoCacheStack.peek().getInfoMap();
					
					
					LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					
					RelativeLayout newContent = (RelativeLayout) li.inflate(R.layout.plugin_info_dialog_content, null);

					TextView title = (TextView) newContent.findViewById(R.id.title);
					
					title.setText(path);
					
					
					ListView list = (ListView) newContent.findViewById(R.id.list);
					
					//TextView test = new TextView(this.getContext());
					//test.setText(info[0].getName());
					//test.setLayoutParams(list.getLayoutParams());
					//test.setId(list.getId());
					//newContent.removeView(list);
					
					//newContent.addView(list);
					
					PluginInfoAdapter adapter = new PluginInfoAdapter(this.getContext(),0,info);
					list.setAdapter(adapter);
					adapter.notifyDataSetInvalidated();
					
					InfoStackItem i = new InfoStackItem();
					i.setInfoPage(newContent);
					infoCacheStack.push(i);
					
					ViewFlipper f = (ViewFlipper) PluginSelectorDialog.this.findViewById(R.id.flipper);
					f.addView(newContent);
					TranslateAnimation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
					TranslateAnimation inAnim  = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
					outAnim.setDuration(500);
					inAnim.setDuration(500);
					f.setInAnimation(inAnim);
					f.setOutAnimation(outAnim);
					
					f.showNext();
					return;
				}
			}
		}
		
		InfoStackItem item = new InfoStackItem();
		item.setInfoMap(new HashMap<String,PluginDescription[]>());
		infoCacheStack.push(item);
		
		PluginSearchAdapter adapter = buildList(path);
		
		
		
		LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		
		RelativeLayout newContent = (RelativeLayout) li.inflate(R.layout.options_dialog_content, null);

		TextView title = (TextView) newContent.findViewById(R.id.title);
		
		title.setText(path);
		
		
		ListView list = (ListView) newContent.findViewById(R.id.list);
		
		list.setAdapter(adapter);
		item.setInfoPage(newContent);
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				PluginSearchAdapter adapt = (PluginSearchAdapter) arg0.getAdapter();
				File item = adapt.getItem(pos);
				if(item.isDirectory()) {
					addPage(item.getAbsolutePath());
				} else {
					addPage(item.getAbsolutePath());
				}
			}
		});
		
		ViewFlipper f = (ViewFlipper) PluginSelectorDialog.this.findViewById(R.id.flipper);
		f.addView(newContent);
		if(infoCacheStack.size() == 1) {
			TranslateAnimation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			TranslateAnimation inAnim  = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			outAnim.setDuration(500);
			inAnim.setDuration(500);
			f.setInAnimation(inAnim);
			f.setOutAnimation(outAnim);
		}
		f.showNext();
	}
	
	private PluginSearchAdapter buildList(String path) {
		//View v = 
		//HashMap<String,PluginDescription[]> infoCache = new HashMap<String,PluginDescription[]>();
		//infoCacheStack.push(infoCache);
		
		File file = new File(path);
		
		File[] files = file.listFiles(new PluginFileFilter());
		
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File a, File b) {
				return a.compareTo(b);
				//return 0;
			}
			
		});
		
		PluginSearchAdapter adapter = new PluginSearchAdapter(this.getContext(),0,files);
		
		return adapter;
		//list.setAdapter(adapter);
		//adapter.notifyDataSetInvalidated();
		
	}
	
	@Override
	public void onBackPressed() {
		if(infoCacheStack.size() == 1) {
			//try {
			//	service.saveSettings();
			//} catch (RemoteException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			this.dismiss();
		} else {
			InfoStackItem prev = infoCacheStack.pop();
			//if(backStack.size() == 0) {
			//	selectedPlugin = "main";
			//}
			//Option key = (Option) v.getTag();
			//backStack.push(mCurrent);
			//mCurrent = (SettingsGroup) key;
			/*primeAdapter = new OptionsAdapter(mCurrent);
			primeList.setAdapter(primeAdapter);
			primeAdapter.notifyDataSetInvalidated();
			
			LinearLayout group = (LinearLayout) primeList.getParent();
			TextView title = (TextView) group.findViewById(R.id.title);
			title.setText(key.getTitle());*/
			ViewFlipper f = (ViewFlipper) PluginSelectorDialog.this.findViewById(R.id.flipper);
			
			//int amount = altList.getWidth();
			//int amount = 600;
			//TranslateAnimation outAnim = new TranslateAnimation(0,-amount,0,0);
			//TranslateAnimation inAnim = new TranslateAnimation(amount,0,0,0);
			TranslateAnimation outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			TranslateAnimation inAnim  = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
			
			outAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					ViewFlipper f = (ViewFlipper) PluginSelectorDialog.this.findViewById(R.id.flipper);
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
	
	private class PluginSearchAdapter extends ArrayAdapter<File> {

		LayoutInflater inflater = null;
		//Pl
		
		public PluginSearchAdapter(Context context, int textViewResourceId,
				File[] objects) {
			
			super(context, textViewResourceId, objects);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
		}
		
		@Override
		public View getView(int pos,View view,ViewGroup root)  {
			
			if(view == null) {
				view = inflater.inflate(R.layout.options_list_row, null);
			}
			
			TextView title = (TextView) view.findViewById(R.id.infoTitle);
			TextView extra = (TextView) view.findViewById(R.id.infoExtended);
			
			//get the path
			File file = this.getItem(pos);
			
			PluginDescription[] info = infoCacheStack.peek().getInfoMap().get(file.getAbsolutePath());
			if(info == null) {
				title.setText(file.getName());
				extra.setText("");
			} else {
				if(info.length > 1) {
					title.setText(file.getName());
					extra.setText(info.length + " plugins.");
				} else {
					title.setText(file.getName());
					extra.setText(info[0].getName() + " written by " + info[0].getAuthor() + ".");
				}
			}
			return view;
		}
		
	}
	
	private class PluginFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			if(pathname.isDirectory()) return true;
			
			xmlmatch.reset(pathname.getAbsolutePath());
			//else try and parse it with the level 1 parser.
			if(xmlmatch.matches()) {
				//attempt the info parse.
				QuickPluginParser p = new QuickPluginParser(pathname.getAbsolutePath(),PluginSelectorDialog.this.getContext());
				PluginDescription[] info;
				try {
					info = p.load();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				if(info == null) {
					return false;
				} else {
					//cache the info.
					HashMap<String,PluginDescription[]> infoCache = infoCacheStack.peek().getInfoMap();
					infoCache.put(pathname.getAbsolutePath(), info);
					return true;
				}
			}
			return false;
		
		}
		
	}
	
	private class InfoStackItem {
		private View infoPage;
		private HashMap<String,PluginDescription[]> infoMap;

		private HashMap<String,PluginDescription[]> getInfoMap() {
			return infoMap;
		}
		
		private void setInfoMap(HashMap<String,PluginDescription[]> map) {
			infoMap = map;
		}
		
		public View getInfoPage() {
			return infoPage;
		}
		public void setInfoPage(View infopage) {
			this.infoPage = infopage;
		}
		
	}
	
	private class PluginInfoAdapter extends ArrayAdapter<PluginDescription> {

		private LayoutInflater inflater = null;
		public PluginInfoAdapter(Context context, int resource, PluginDescription[] objects) {
			super(context, resource, objects);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
		}
		
		@Override
		public View getView(int pos,View view,ViewGroup root) {
			if(view == null) {
				view = inflater.inflate(R.layout.plugin_info_dialog_list_row, null);
			}
			
			((LinearLayout)view.findViewById(R.id.icon).getParent()).setVisibility(View.GONE);
			view.findViewById(R.id.widget_frame).setVisibility(View.GONE);
			
			PluginDescription desc = this.getItem(pos);
			
			TextView title = (TextView) view.findViewById(R.id.infoTitle);
			
			WebView content = (WebView) view.findViewById(R.id.infoExtended);
			
			title.setText(desc.getName());
			content.setBackgroundColor(0xFF000000);
			//content.setFo
			
			content.loadDataWithBaseURL("/mnt/sdcard/BlowTorch/plugins/aardwolf/", desc.getDescription(), null, null, null);
			
			return view;
		}
		
	}
	
}
