package serial;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

public class License implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int EXPIRATION_TIME = 30;

	private String serial;
	private Date expireDate;

	public License(String serial) throws UnknownHostException {
		this.serial = serial;
		expireDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, EXPIRATION_TIME);
		expireDate = calendar.getTime();
	}

	public Date getExpirationDate()
	{
		return expireDate;
	}
	public String getSerial() {
		return serial;
	}
}
