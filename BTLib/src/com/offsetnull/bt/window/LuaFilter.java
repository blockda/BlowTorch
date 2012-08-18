package com.offsetnull.bt.window;

import android.widget.Filter;

public class LuaFilter extends Filter {

	private FilterProxy mProxy;
	
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		if(mProxy != null) {
			ResultsProxy tmp = new ResultsProxy();
			return mProxy.performFiltering(constraint, tmp);
		}
		return null;
	}

	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		if(mProxy != null) {
			mProxy.publishResults(constraint, results);
		}
	}
	
	public void setProxy(FilterProxy proxy) {
		mProxy = proxy;
	}
	
	public interface FilterProxy {
		FilterResults performFiltering(CharSequence constraint,FilterResults results);
		void publishResults(CharSequence constraint, FilterResults results);
	}
	
	private class ResultsProxy extends Filter.FilterResults {
		public void setCount(int count) {
			this.count = count;
		}
		
		public void setObject(Object o) {
			this.values = o;
		}
		
	}

}
