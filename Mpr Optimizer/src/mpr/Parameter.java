package mpr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parameter {
	private String name, value, regex;
	//public static final String parameterRegexStart ="(^|[+-*/\\s])(\\w)([+-*/\\s]|$)";
	public static final String parameterRegexStart ="(^|[\\+\\-\\*\\/\\s])";
	public static final String parameterRegexEnd ="([\\+\\-\\*\\/\\s]|$)";

	public Parameter(String name, String value){
		this.name = name;
		this.value = value;
		this.regex = parameterRegexStart + "(" + name + ")" + parameterRegexEnd;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	String replaceParameterByVal (String line)
	{
	        Pattern p = Pattern.compile(regex);
	        Matcher m = p.matcher(line);
	        StringBuffer sb = new StringBuffer();
	        while (m.find()) {
	            String rep = m.group(1) + value + m.group(3);
	            m.appendReplacement(sb, rep);
	        }
	        m.appendTail(sb);
	        return sb.toString();

	}
}
