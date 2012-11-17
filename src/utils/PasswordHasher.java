package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class providing a simple way of password hashing
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public abstract class PasswordHasher {
	
	private final static String ALGORITHM = "SHA";
	
	/**
	 * Hash the given string using the default algorithm, returning the
	 * resulting digest as a hex string
	 * 
	 * @param password
	 * @return
	 */
	public static String hashPassword(String password) {
		String hashed = null;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(ALGORITHM);
			 
			md.update(password.getBytes());
			return digestToString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hashed;
	}
	
	/**
	 * Covert a byte array to a hex string
	 * 
	 * @param digest
	 * @return
	 */
	private static String digestToString(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < digest.length; i++) {
			String hex = Integer.toHexString(0xFF & digest[i]);
			if (hex.length() == 1) {
			    sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}
	
}
