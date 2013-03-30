package mpr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class OptionsManager {
	public static final String configFilePath="c:\\\\config.ini";
	private HashMap<String, String> settings = new HashMap<>();
	public OptionsManager() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
		String currentLine;
		String splittedLine[];
		while (reader.ready()){
			currentLine = reader.readLine();
			splittedLine = currentLine.split("=");
			settings.put(splittedLine[0], splittedLine[1]);
		}
		
		reader.close();
	}
	
	public String getProperty(String key){
		return (settings.get(key));
	}
}
