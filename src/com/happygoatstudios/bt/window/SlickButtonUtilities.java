package com.happygoatstudios.bt.window;

import java.util.Vector;

import com.happygoatstudios.bt.R.id;

public class SlickButtonUtilities {
	
	public Vector<ButtonSet> sets = new Vector<ButtonSet>();
	
	public int selectedButtonSet = 0;
	
	public SlickButtonUtilities() {
		//add one set to work with
		sets.add(new ButtonSet());
	}
	
	public void addNewSet(String name) {
		if(name == null || name == "") {
			name = "Not Set Yet";
		}
		
		sets.add(new ButtonSet(null,name));
	}
	
	public void removeSet(int location) {
		sets.remove(location);
	}
	
	public static boolean saveButtons(SlickButton[] input,String filename) {
		
		return true;
	}
	
	public void addButtonToSelectedSet(SlickButton b) {
		sets.get(selectedButtonSet).addButton(b);
	}
	
	public void removeButtonFromSelectedSet(SlickButton b) {
		sets.get(selectedButtonSet).remobeButton(b);
	}
	
	private class ButtonSet {
		public Vector<SlickButton> buttons = new Vector<SlickButton>();
		public String name = new String("NAME NOT SET");
		
		public ButtonSet() {
			
		}
		
		public ButtonSet(SlickButton[] input,String usethisname) {
			if(input != null) {
				for(int i = 0;i<input.length;i++) {
					buttons.add(input[i]);
				}
			}
			name = usethisname;
		}
		
		public void addButton(SlickButton b) {
			buttons.add(b);
		}
		
		public void remobeButton(SlickButton b) {
			buttons.remove(b);
		}
		
		public void clearAllButtons() {
			buttons.removeAllElements();
		}
	}
}
