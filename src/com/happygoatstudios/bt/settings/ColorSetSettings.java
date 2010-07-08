package com.happygoatstudios.bt.settings;

import android.os.Parcel;
import android.os.Parcelable;

import com.happygoatstudios.bt.window.SlickButtonData;

public class ColorSetSettings implements Parcelable {
	
	private int selectedColor;
	private int primaryColor;
	private int flipColor;
	private int labelColor;
	private int buttonHeight;
	private int buttonWidth;
	private int labelSize;
	private int flipLabelColor;
	
	public ColorSetSettings() {
		toDefautls();
	}
	
	public ColorSetSettings copy() {
		ColorSetSettings tmp = new ColorSetSettings();
		tmp.selectedColor = this.selectedColor;
		tmp.primaryColor = this.primaryColor;
		tmp.flipColor = this.flipColor;
		tmp.labelColor = this.labelColor;
		tmp.buttonHeight = this.buttonHeight;
		tmp.buttonWidth = this.buttonWidth;
		tmp.labelSize = this.labelSize;
		tmp.flipLabelColor = this.flipLabelColor;
		return tmp;
	}
	
	public void toDefautls() {
		selectedColor = SlickButtonData.DEFAULT_SELECTED_COLOR;
		primaryColor = SlickButtonData.DEFAULT_COLOR;
		flipColor = SlickButtonData.DEFAULT_FLIP_COLOR;
		labelColor = SlickButtonData.DEFAULT_LABEL_COLOR;
		buttonWidth = SlickButtonData.DEFAULT_BUTTON_WDITH;
		buttonHeight = SlickButtonData.DEFAULT_BUTTON_HEIGHT;
		labelSize = SlickButtonData.DEFAULT_LABEL_SIZE;
		flipLabelColor = SlickButtonData.DEFAULT_FLIPLABEL_COLOR;
	}

	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
	}

	public int getSelectedColor() {
		return selectedColor;
	}

	public void setPrimaryColor(int primaryColor) {
		this.primaryColor = primaryColor;
	}

	public int getPrimaryColor() {
		return primaryColor;
	}

	public void setFlipColor(int flipColor) {
		this.flipColor = flipColor;
	}

	public int getFlipColor() {
		return flipColor;
	}

	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;
	}

	public int getLabelColor() {
		return labelColor;
	}

	public void setButtonHeight(int buttonHeight) {
		this.buttonHeight = buttonHeight;
	}

	public int getButtonHeight() {
		return buttonHeight;
	}

	public void setButtonWidth(int buttonWidth) {
		this.buttonWidth = buttonWidth;
	}

	public int getButtonWidth() {
		return buttonWidth;
	}
	
	public void setLabelSize(int labelSize) {
		this.labelSize = labelSize;
	}

	public int getLabelSize() {
		return labelSize;
	}
	
	public void setFlipLabelColor(int flipLabelColor) {
		this.flipLabelColor = flipLabelColor;
	}

	public int getFlipLabelColor() {
		return flipLabelColor;
	}

	public static final Parcelable.Creator<ColorSetSettings> CREATOR = new Parcelable.Creator<ColorSetSettings>() {

		public ColorSetSettings createFromParcel(Parcel arg0) {
			return new ColorSetSettings(arg0);
		}

		public ColorSetSettings[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new ColorSetSettings[arg0];
		}
	
	
	};
	
	public ColorSetSettings(Parcel p) {
		readFromParcel(p);
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel d, int arg1) {
		// TODO Auto-generated method stub
		d.writeInt(labelColor);
		d.writeInt(selectedColor);
		d.writeInt(flipColor);
		d.writeInt(primaryColor);
		d.writeInt(buttonHeight);
		d.writeInt(buttonWidth);
		d.writeInt(labelSize);
		d.writeInt(flipLabelColor);
	}
	
	public void readFromParcel(Parcel in) {
		labelColor = in.readInt();
		selectedColor = in.readInt();
		flipColor = in.readInt();
		primaryColor = in.readInt();
		buttonHeight = in.readInt();
		buttonWidth = in.readInt();
		labelSize = in.readInt();
		flipLabelColor = in.readInt();
	}



}