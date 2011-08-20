package com.happygoatstudios.bt.window;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WindowFragment extends Fragment {
	ByteView view = null;
	public WindowFragment() {
		super();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = new ByteView(this.getActivity()); 
		return view;
		
	}
	
	public void onResume() {
		Log.e("FRAG","Fragment resuming.");
		//this.getView().
	}
	
}
