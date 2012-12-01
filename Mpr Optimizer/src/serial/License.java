package serial;

import java.io.Serializable;
import java.net.UnknownHostException;

public class License implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String serial;

	public License(String serial) throws UnknownHostException {
		this.serial = serial;

	}

	
	public String getSerial() {
		return serial;
	}
}
