package com.happygoatstudios.bt.alias;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.service.IStellarService;
import com.happygoatstudios.bt.validator.Validator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class AliasEditorDialog extends Dialog implements NewAliasDialogDoneListener {
	
	//private ArrayList<String> aliases;
	private ArrayList<AliasEntry> entries;
	private ConnectionAdapter apdapter;
	
	ListView lv = null;
	
	//AliasDialogDoneListener reporto = null;
	HashMap<String,AliasData> input;
	
	IStellarService service;

	public AliasEditorDialog(Context context,HashMap<String,AliasData> pinput,IStellarService pService) {
		super(context);
		//reporto = useme;
		input = pinput;
		service = pService;
	}
	
	public void onCreate(Bundle b) {
		
		if(input != null) {
			//load aliases
			this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setBackgroundDrawableResource(R.drawable.hyperframe);
			
			setContentView(R.layout.alias_dialog);
			
			//aliases = new ArrayList<String>();
			entries = new ArrayList<AliasEntry>();
			
			
			lv = (ListView)findViewById(R.id.alias_list);
			lv.setScrollbarFadingEnabled(false);
			
			lv.setOnItemLongClickListener(new listItemLongClicked());
			
			lv.setEmptyView(findViewById(R.id.alias_empty));
			
			/*Object[] keys = input.keySet().toArray();
			Object[] values = input.values().toArray();
			
			for(int i=0;i<keys.length;i++) {
				aliases.add((String)keys[i] + "[||]" + (String)values[i]);
			}*/
			for(AliasData a : input.values()) {
				entries.add(new AliasEntry(a.getPre(),a.getPost()));
			}
			
			apdapter = new ConnectionAdapter(lv.getContext(),R.layout.alias_row,entries);
			lv.setAdapter(apdapter);
			lv.setTextFilterEnabled(true);
			
			apdapter.sort(new AliasComparator());
			//apdapter.sort(String.CASE_INSENSITIVE_ORDER);
		}
		
		Button butt = (Button)findViewById(R.id.new_alias_button);
		
		butt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				NewAliasDialog diag = new NewAliasDialog(AliasEditorDialog.this.getContext(),AliasEditorDialog.this,service,computeNames(""));
				diag.setTitle("NEW ALIAS");
				diag.show();
			}
		});
		
		/*Button done = (Button)findViewById(R.id.alias_dialog_done);
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				if(offenders.size() > 0) {
					String message = "Cannot save alias list with circular dependencies.";
					Validator v = new Validator();
					v.showMessageNoDecoration(AliasEditorDialog.this.getContext(), message);
					return;
				}
				//make new list.
				ArrayList<AliasData> tmp = new ArrayList<AliasData>();
				for(int i = 0;i<apdapter.getCount();i++) {
					AliasEntry e = apdapter.getItem(i);
					tmp.add(new AliasData(e.pre,e.post));
				}
				reporto.aliasDialogDone(tmp);
				//reporto.aliasDialogDone(AliasEditorDialog.this.aliases);
				AliasEditorDialog.this.dismiss();
			}
		});*/
		
		Button cancel = (Button)findViewById(R.id.alias_cancel_done);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				AliasEditorDialog.this.dismiss();
			}
		});

	}
	
	private class ConnectionAdapter extends ArrayAdapter<AliasEntry> {
		private ArrayList<AliasEntry> items;
		
		public ConnectionAdapter(Context context, int txtviewresid, ArrayList<AliasEntry> objects) {
			super(context, txtviewresid, objects);
			this.items = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.alias_row, null);
			}
			
			//String m = items.get(position);
			
			//String[] parts = m.split("\\Q[||]\\E");
			AliasEntry a = items.get(position);
			
			if(a != null) {
				TextView pre = (TextView)v.findViewById(R.id.alias_pre);
				TextView post = (TextView)v.findViewById(R.id.alias_post);
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
			return v;
		}
		
		
	}
	
	private class listItemLongClicked implements ListView.OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			//Log.e("LAUNCHER","List item long clicked!");
			AliasEntry muc = apdapter.getItem(arg2);
			
			
			Message delmsg = aliasModifier.obtainMessage(MSG_DELETEALIAS);
			delmsg.obj = new AliasData(muc.pre,muc.post);
			delmsg.arg1 = arg2; //add position
			
			Message modmsg = aliasModifier.obtainMessage(MSG_MODIFYALIAS);
			modmsg.obj = new AliasData(muc.pre,muc.post);
			modmsg.arg1 = arg2; //add position
			
			AlertDialog.Builder build = new AlertDialog.Builder(AliasEditorDialog.this.getContext())
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
				NewAliasDialog diag = new NewAliasDialog(AliasEditorDialog.this.getContext(),AliasEditorDialog.this,((AliasData)msg.obj).getPre(),((AliasData)msg.obj).getPost(),position,(AliasData)msg.obj,service,computeNames(((AliasData)msg.obj).getPre()));
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
			
			for(String key : existingAliases.keySet()) {
				if(!key.equals(name)) {
					names.add(key);
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

	public void newAliasDialogDone(String pre, String post) {

		
		/****
		 * 
		 * CHECK FOR CIRCULUAR REFERENCES
		 * 
		 */


		AliasEntry tmp = new AliasEntry(pre,post);
		apdapter.add(tmp);
		apdapter.notifyDataSetChanged();
		apdapter.sort(new AliasComparator());
		
		try {
			HashMap<String,AliasData> existingAliases = (HashMap<String, AliasData>) service.getAliases();
			AliasData newAlias = new AliasData();
			newAlias.setPost(post);
			newAlias.setPre(pre);
			String newKey = newAlias.getPre();
			if(newKey.startsWith("^")) newKey = newKey.substring(1,newKey.length());
			if(newKey.endsWith("$")) newKey = newKey.substring(0,newKey.length()-1);
			
			existingAliases.put(newKey, newAlias);
			service.setAliases(existingAliases);
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
	
	public void editAliasDialogDone(String pre,String post,int pos,AliasData orig) {
		apdapter.remove(apdapter.getItem(pos));
		AliasEntry tmp = new AliasEntry(pre,post);
		apdapter.insert(tmp,pos);
		apdapter.notifyDataSetChanged();
		apdapter.sort(new AliasComparator());
		pos = apdapter.getPosition(tmp);
		
		//remove from the list and add the new one.
		try {
			HashMap<String,AliasData> existingAliases = (HashMap<String, AliasData>) service.getAliases();
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
			existingAliases.put(newKey, newAlias);
			service.setAliases(existingAliases);
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
		/*public AliasEntry() {
			pre = "";
			post = "";
		}*/
		
		public AliasEntry(String pPre,String pPost) {
			pre = pPre;
			post = pPost;
		}
		
		/*public AliasEntry(AliasData i) {
			pre = i.getPre();
			post = i.getPost();
		}*/
	}

}