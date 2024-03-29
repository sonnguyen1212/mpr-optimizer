package serial;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;

public class LocalVerifier {

	private final String LICENSE_FOLDER = File.separator + "License";
	private final String KEY_FILE = File.separator + "license.key";
	private final String LICENSE_FILE = File.separator + "license.data";
	public static final int LICENSE_EXPIRED = -1;
	public static final int LICENSE_KEYS_DONT_MATCH = -2;
	public static final int LICENSE_MATCH = 0;

	private File homeFolder;

	public LocalVerifier(File homeFolder) {
		this.homeFolder = homeFolder;
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

	public int verify() {
		License license = loadLicense();
		if (license != null) {
			try {
				if (license.isLimited()) {
					if (license.getExpirationDate().before(new Date())
							|| license.getCreatingDate().after(new Date())) {
						return LICENSE_EXPIRED;
					}
				}
				ArrayList<String> macs = getAllAdresses();
				for (String mac : macs) {
					String md5 = md5(mac);
					if(md5.equals(license.getSerial())) {
						return LICENSE_MATCH;
					}
				}

			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		return LICENSE_KEYS_DONT_MATCH;
	}

	private License loadLicense() {
		SecretKey key = loadKey();
		License license = null;
		try {
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			FileInputStream fis = new FileInputStream(
					homeFolder.getAbsolutePath() + LICENSE_FOLDER
							+ LICENSE_FILE);
			BufferedInputStream bis = new BufferedInputStream(fis);
			CipherInputStream cis = new CipherInputStream(bis, cipher);
			ObjectInputStream ois = new ObjectInputStream(cis);
			license = (License) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return license;
	}

	private SecretKey loadKey() {
		SecretKey key = null;
		try {
			File keyFile = new File(homeFolder.getAbsolutePath()
					+ LICENSE_FOLDER + KEY_FILE);
			FileInputStream fis = new FileInputStream(keyFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);
			key = (SecretKey) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Couldn't Find Key File.",
					"Error!", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(-2);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return key;
	}

	public static String getCurrentMac() throws UnknownHostException {
		InetAddress ip;
		StringBuilder sb = null;
		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network;
			network = NetworkInterface.getByInetAddress(ip);
			if (network == null) {
				return "1234456";
			}
			byte[] mac = network.getHardwareAddress();
			if (mac == null) {
				return "1234456";
			}

			sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i],
						(i < mac.length - 1) ? "-" : ""));
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private static ArrayList<String> getAllAdresses() throws SocketException {
		ArrayList<String> returnList = new ArrayList<String>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface
				.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {
			NetworkInterface nif = interfaces.nextElement();
			byte[] lBytes = nif.getHardwareAddress();
			StringBuffer lStringBuffer = new StringBuffer();

			if (lBytes != null) {
				for (int i = 0; i < lBytes.length; i++) {
					lStringBuffer.append(String.format("%02X%s", lBytes[i],
							(i < lBytes.length - 1) ? "-" : ""));
				}
			}

			if (lStringBuffer.length() > 1) {
				returnList.add(lStringBuffer.toString());
			}
		}
		return returnList;
	}
}
