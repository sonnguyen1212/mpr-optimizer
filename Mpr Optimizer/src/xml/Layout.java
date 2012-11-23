package xml;

import java.util.ArrayList;

public class Layout {
	private int layoutNumber;
	private double  length, width;
	private ArrayList<MprFile> mprFiles;
	
	public Layout(int number, double length, double width) {
		this.layoutNumber = number;
		this.length = length;
		this.width = width;
		this.mprFiles = new ArrayList<>();
	}
	
	public int getNumber() {
		return layoutNumber;
	}
	
	public double getLength() {
		return length;
	}
	
	public double getWidth() {
		return width;
	}
	
	public ArrayList<MprFile> getMprFiles() {
		return mprFiles;
	}
	
	public void addMprFile(MprFile mprFile) {
		mprFiles.add(mprFile);
	}
}
