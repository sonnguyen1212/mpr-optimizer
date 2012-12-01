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

public class RemoteVerifier {

	private static final String URL_ADDRESS = "http://www.parkme.co.il/licenseOptimizer.php";
	private String mac;

	public RemoteVerifier() throws UnknownHostException {
		this.mac = LocalVerifier.getCurrentMac();
//		this.mac = "1";
	}

	/**
	 * Verifies that the MAC of this machine and the serial matches.
	 * @param serial the serial to check
	 * @return 1 if match. 0 if not, -1 if couldn't check
	 */
	public int verify(String serial) {
		String data;
		int result = -1;
		try {
			if (serial != null) {
				System.out.println("MAC: " + mac + " Serial: (" + serial + ")");
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
				String line;
				while ((line = rd.readLine()) != null) {
					result = Integer.parseInt(line);
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
