package com.happygoatstudios.bt.window;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.launcher.BaardTERMLauncher;
import com.happygoatstudios.bt.launcher.MudConnection;


import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class AliasEditorDialog extends Dialog implements NewAliasDialogDoneListener {
	
	private ArrayList<String> aliases;
	private ConnectionAdapter apdapter;
	
	ListView lv = null;
	
	AliasDialogDoneListener reporto = null;

	public AliasEditorDialog(Context context,Map<String,String> input,AliasDialogDoneListener useme) {
		super(context);
		setContentView(R.layout.alias_dialog);
		reporto = useme;
		if(input != null) {
			//load aliases
			
			
			aliases = new ArrayList<String>();
			
			lv = (ListView)findViewById(R.id.alias_list);
			
			
			
			//Object[] pres = input.keySet().toArray();
			//Object[] posts = input.
			//Set<Entry<String,String>> list = input.entrySet();
			
			Object[] keys = input.keySet().toArray();
			Object[] values = input.values().toArray();
			
			if(keys.length != values.length) {
				Log.e("ALIAS","UNEQUAL ALIAS TARGETS");
			}
			
			for(int i=0;i<keys.length;i++) {
				aliases.add((String)keys[i] + "[||]" + (String)values[i]);
			}
			
			apdapter = new ConnectionAdapter(lv.getContext(),R.layout.alias_row,aliases);
			lv.setAdapter(apdapter);
			lv.setTextFilterEnabled(true);
		}
		
		Button butt = (Button)findViewById(R.id.new_alias_button);
		
		butt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				NewAliasDialog diag = new NewAliasDialog(AliasEditorDialog.this.getContext(),AliasEditorDialog.this);
				diag.setTitle("New Alias:");
				diag.show();
			}
		});
		
		Button done = (Button)findViewById(R.id.alias_dialog_done);
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				reporto.aliasDialogDone(AliasEditorDialog.this.aliases);
				AliasEditorDialog.this.dismiss();
			}
		});

	}
	
	private class ConnectionAdapter extends ArrayAdapter<String> {
		private ArrayList<String> items;
		
		public ConnectionAdapter(Context context, int txtviewresid, ArrayList<String> objects) {
			super(context, txtviewresid, objects);
			// TODO Auto-generated constructor stub
			this.items = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.alias_row, null);
			}
			
			String m = items.get(position);
			
			String[] parts = m.split("\\Q[||]\\E");
			
			if(m != null) {
				TextView pre = (TextView)v.findViewById(R.id.alias_pre);
				TextView post = (TextView)v.findViewById(R.id.alias_post);
				//TextView port = (TextView)v.findViewById(R.id.port);
				if(pre != null) {
					//title.setText(" " + m.getDisplayName());
					pre.setText("PRE:" + parts[0]);
				}
				if(post != null) {
					//host.setText("\t"  + m.getHostName() + ":" + m.getPortString());
					post.setText("POST:" + parts[1]);
				}
				//if(port != null) {
				//	port.setText(" Port: " + m.getPortString());
				//}
			}
			return v;
		}
		
		
	}

	public void newAliasDialogDone(String pre, String post) {
		apdapter.add(pre + "[||]" + post);
		apdapter.notifyDataSetChanged();
	}

}