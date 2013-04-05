package serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RemoteVerifier {

	private static final String URL_ADDRESS = "http://www.parkme.co.il/licenseOptimizer.php";
	private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private String mac;

	public RemoteVerifier() throws UnknownHostException {
		this.mac = LocalVerifier.getCurrentMac();
		// this.mac = "1";
	}

	/**
	 * Verifies that the MAC of this machine and the serial matches.
	 * 
	 * @param serial
	 *            the serial to check
	 * @return 1 if match. 0 if not, -1 if couldn't check
	 */
	public int verify(String serial) {
		String data;
		int result = -1;
		try {
			if (serial != null) {
				data = URLEncoder.encode("mac", "UTF-8") + "=" + URLEncoder.encode(mac, "UTF-8");
				data += "&" + URLEncoder.encode("serial", "UTF-8") + "=" + URLEncoder.encode(serial, "UTF-8");
				data += "&" + URLEncoder.encode("mode", "UTF-8") + "=" + URLEncoder.encode("VERIFY", "UTF-8");
				URL url = new URL(URL_ADDRESS);
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String resultIntString = rd.readLine();
				if (Integer.parseInt(resultIntString) == 1) {
					String resultDateString = rd.readLine();
					SimpleDateFormat formatter = new SimpleDateFormat(TIME_PATTERN);
					Date dateOfActivation = formatter.parse(resultDateString);
					Date today = new Date();
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
