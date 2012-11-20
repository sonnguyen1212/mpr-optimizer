package xml;

import java.util.ArrayList;

public class Layout {
	private int number, length, width;
	private ArrayList<MprFile> mprFiles;
	
	public Layout(int number, int length, int width) {
		this.number = number;
		this.length = length;
		this.width = width;
		this.mprFiles = new ArrayList<>();
	}
	
	public int getNumber() {
		return number;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getWidth() {
		return width;
	}
	
	public ArrayList<MprFile> getMprFiles() {
		return mprFiles;
	}
	
	public void addMprFile(MprFile mprFile) {
		mprFiles.add(mprFile);
	}
}
