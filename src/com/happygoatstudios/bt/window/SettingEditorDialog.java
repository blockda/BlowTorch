package com.happygoatstudios.bt.window;

import java.util.ArrayList;
import java.util.List;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IBaardTERMService;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SettingEditorDialog extends Dialog {
	
	ArrayList<BaseSetting> settings = new ArrayList<BaseSetting>();
	ConnectionAdapter adapter = null;
	
	IBaardTERMService service = null;
	public SettingEditorDialog(Context context,IBaardTERMService serv) {
		super(context);
		// TODO Auto-generated constructor stub
		service = serv;
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		
		this.setContentView(R.layout.settings_editor_layout);
		
		Button done = (Button)findViewById(R.id.settings_editor_done);
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				SettingEditorDialog.this.dismiss();
			}
		});
		
		//construct fake list;
		BaseSetting test1 = new NumberSetting("FOO","The Setting for foo is:",300);
		BaseSetting test2 = new NumberSetting("BAR","Bar is (max 3):",240);
		BaseSetting test4 = new NumberSetting("BAZ","Cha cha cha.", 4);
		
		settings.add(test1);
		settings.add(test2);
		settings.add(test4);
		
		adapter = new ConnectionAdapter(this.getContext(),0,settings);
		
		ListView lv = (ListView)findViewById(R.id.settings_list);
		lv.setAdapter(adapter);
	}
	
	private class ConnectionAdapter extends ArrayAdapter<BaseSetting> {

		List<BaseSetting> entries;
		
		public ConnectionAdapter(Context context,
				int textViewResourceId, List<BaseSetting> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
			entries = objects;
		}
		
		public View getView(int position,View convertView,ViewGroup parent) {
			
			View v = convertView;
			if(v == null) {
				v = entries.get(position).getView(this.getContext());
			}
			
			return v;
		}
		
	}
	
	private abstract class BaseSetting {
		abstract void showEditor(Context context);
		abstract View getView(Context context);
	}
	
	private class NumberSetting extends BaseSetting {

		private String name;
		private String desc;
		private Integer value;
		
		public NumberSetting(String pName,String pDesc,Integer pVal) {
			name = pName;
			desc = pDesc;
			value = pVal;
		}
		
		@Override
		View getView(Context context) {
			// TODO Auto-generated method stub
			LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = li.inflate(R.layout.number_setting_row, null);
			TextView title = (TextView) v.findViewById(R.id.number_setting_title);
			TextView descr = (TextView) v.findViewById(R.id.number_setting_description);
			TextView valuer = (TextView) v.findViewById(R.id.number_setting_value);
			title.setText(name);
			descr.setText(desc);
			valuer.setText(value.toString());
			return v;
		}

		@Override
		void showEditor(Context context) {
			
		}
		
	}

}
