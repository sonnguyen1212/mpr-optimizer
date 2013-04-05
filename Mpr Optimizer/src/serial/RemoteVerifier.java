package serial;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.swing.JOptionPane;

public class RemoteVerifier {

	private static final String URL_ADDRESS = "http://www.parkme.co.il/licenseOptimizer.php";
	private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private final String LICENSE_FOLDER = File.separator + "License";
	private final String KEY_FILE = File.separator + "license.key";
	private final String LICENSE_FILE = File.separator + "license.data";
	private String mac;
	private File homeFolder;
	private boolean isFull = false;
	private Date dateOfActivation;

	public RemoteVerifier(File homeFolder) throws UnknownHostException {
		this.mac = LocalVerifier.getCurrentMac();
		this.homeFolder = homeFolder;
	}

	private void createKey(String serial) {
		if (serial == null) {
			JOptionPane.showMessageDialog(null, "No Key Entered", "Error!", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		byte password[] = serial.getBytes();
		DESKeySpec desKeySpec;
		try {

			desKeySpec = new DESKeySpec(password);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
			File licenseFolder = new File(homeFolder.getAbsolutePath() + LICENSE_FOLDER);
			if (!licenseFolder.exists()) {
				licenseFolder.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(licenseFolder.getAbsolutePath() + KEY_FILE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(secretKey);
			oos.close();
		} catch (InvalidKeyException e) {
			JOptionPane.showMessageDialog(null, "Keys don't match", "Error!", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifies that the MAC of this machine and the serial matches.
	 * 
	 * @param serial
	 *            the serial to check
	 */
	public int verify(String serial) {
		String data;
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
				isFull = Boolean.parseBoolean(rd.readLine());
				if (Integer.parseInt(resultIntString) == 1) {
					String resultDateString = rd.readLine();
					SimpleDateFormat formatter = new SimpleDateFormat(TIME_PATTERN);
					dateOfActivation = formatter.parse(resultDateString);
					Calendar c = Calendar.getInstance();
					c.setTime(dateOfActivation);
					c.add(Calendar.DATE, 30);
					Date dateExpires = c.getTime();
					Date today = new Date();
					if (isFull) {
						return LocalVerifier.LICENSE_MATCH;
					}
					if (today.before(dateOfActivation) || today.after(dateExpires)) {
						return LocalVerifier.LICENSE_EXPIRED;
					}
				} else {
					return LocalVerifier.LICENSE_KEYS_DONT_MATCH;
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
		return LocalVerifier.LICENSE_MATCH;
	}

	public static String md5(String input) throws NoSuchAlgorithmException {
		String result = input;
		if (input != null) {
			MessageDigest md = MessageDigest.getInstance("MD5"); // or "SHA-1"
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
		}
		return result;
	}

	private SecretKey loadKey() {
		SecretKey key = null;
		try {
			File keyFile = new File(homeFolder.getAbsolutePath() + LICENSE_FOLDER + KEY_FILE);
			FileInputStream fis = new FileInputStream(keyFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);
			key = (SecretKey) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Couldn't Find Key File.", "Error!", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(-2);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return key;
	}

	public void createLicense(String serial) {
		createKey(serial);
		SecretKey key = loadKey();
		try {
			License license = new License(serial, dateOfActivation);
			license.setLimited(!isFull);
			
			// Create Cipher
			Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			desCipher.init(Cipher.ENCRYPT_MODE, key);

			// Create stream
			File licenseFolder = new File(homeFolder.getAbsolutePath() + LICENSE_FOLDER);
			if (!licenseFolder.exists()) {
				licenseFolder.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(licenseFolder.getAbsolutePath() + LICENSE_FILE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			CipherOutputStream cos = new CipherOutputStream(bos, desCipher);
			ObjectOutputStream oos = new ObjectOutputStream(cos);
			oos.writeObject(license);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}

	}
}
