package mpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class OptionsManager {
//	public static final String configFilePath="c:\\\\config.ini";
	private static final String CONFIG_FILE = "\\config.ini";
	private File configFile;
	private HashMap<String, String> settings = new HashMap<>();
	public OptionsManager(File homeFolder) throws IOException
	{
		configFile = new File(homeFolder.getAbsolutePath() + CONFIG_FILE);
		if(!configFile.exists()) {
			createFile();
		}
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		String currentLine;
		String splittedLine[];
		while (reader.ready()){
			currentLine = reader.readLine();
			splittedLine = currentLine.split("=");
			if (splittedLine.length > 1) {
				settings.put(splittedLine[0].trim(), splittedLine[1].trim());
			}
		}
		
		reader.close();
	}
	
	private void createFile() throws IOException {
		writeProperty("mprMergePath", "\"c:\\Program Files (x86)\\Homag Group\\woodWOP6\\mprmerge.exe\"" );
		writeProperty("mprMergeArgs", "-v -kok");
		writeProperty("componentsDir", "\"c:\\Program Files (x86)\\Homag Group\\woodWOP6\\a1\\ml4\"");
		writeProperty("a1path", "\"c:\\Program Files (x86)\\Homag Group\\woodWOP6\\a1\"");
	}

	public void writeProperty(String key, String value) throws IOException {
		if (settings.put(key, value) == null) {
			PrintWriter writer = new PrintWriter(new FileWriter(configFile, true));
			writer.println(key + "=" + value);
			writer.close();
		}
	}
	
	public String getProperty(String key){
		return (settings.get(key));
	}
}
