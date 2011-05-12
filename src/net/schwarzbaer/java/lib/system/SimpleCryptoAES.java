package net.schwarzbaer.java.lib.system;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SimpleCryptoAES {
	public static final int INIT_VECTOR = 16;
	public static final int BLOCKSIZE = 16;
	public static final String algorithm = "AES";
	public static final String fullAlgorithmStr = algorithm+"/CBC/PKCS5Padding";
	
	private final Cipher cipher;
	private SecretKey key;
	
	private SimpleCryptoAES() throws CryptoException {
		try {
			cipher = Cipher.getInstance(fullAlgorithmStr);
		} catch (NoSuchPaddingException e) {
			throw new CryptoException(CryptoException.Reason.Constructor_NoSuchPadding,e);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoException.Reason.Constructor_NoSuchAlgorithm,e);
		}
	}
	
	public static byte[] decrypt(String password, byte[] initVector, byte[] inputBytes, boolean processFinally) throws CryptoException {
		SimpleCryptoAES crypto = new SimpleCryptoAES();
		crypto.setKey(password);
		crypto.initDecrypt(initVector);
		byte[] outputBytes;
		if (processFinally) outputBytes = crypto.processFinal(inputBytes);
		else                outputBytes = crypto.processPreliminary(inputBytes);
		return outputBytes;
	}

	public static Result encrypt(String password, byte[] inputBytes, boolean processFinally) throws CryptoException {
		SimpleCryptoAES crypto = new SimpleCryptoAES();
		crypto.setKey(password);
		Result result = new Result();
		result.initVector = crypto.initEncrypt();
		if (processFinally) result.outputBytes = crypto.processFinal(inputBytes);
		else                result.outputBytes = crypto.processPreliminary(inputBytes);
		return result;
	}

	private byte[] initEncrypt() throws CryptoException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoException.Reason.Init_InvalidKey,e);
		}
		return cipher.getIV();
	}
	
	private void initDecrypt(byte[] initVector) throws CryptoException {
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(initVector));
		} catch (InvalidKeyException e) {
			throw new CryptoException(CryptoException.Reason.Init_InvalidKey,e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new CryptoException(CryptoException.Reason.Init_InvalidAlgorithmParameter,e);
		} 
	}
	
	private byte[] processFinal(byte[] bytes) throws CryptoException {
		try {
			return cipher.doFinal(bytes);
		} catch (IllegalBlockSizeException e) {
			throw new CryptoException(CryptoException.Reason.FinalProcessing_IllegalBlockSize,e);
		} catch (BadPaddingException e) {
			throw new CryptoException(CryptoException.Reason.FinalProcessing_BadPadding,e);
		}
	}

	private byte[] processPreliminary(byte[] bytes) {
		return cipher.update(bytes);
	}

	private void setKey(String password) throws CryptoException {
		byte[] keybytes = password.getBytes(StandardCharsets.UTF_8);
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			keybytes = messageDigest.digest(keybytes);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoException(CryptoException.Reason.KeyGeneration_NoSHA256,e);
		}
		
		key = new SecretKeySpec(keybytes, algorithm);
	}
	
	public static class Result {
		public byte[] initVector = null;
		public byte[] outputBytes = null;
	}

	public static class CryptoException extends Exception {
		private static final long serialVersionUID = -7060546970874881994L;
		
		public enum Reason {
			KeyGeneration_NoSHA256, FinalProcessing_IllegalBlockSize, FinalProcessing_BadPadding, Init_InvalidKey, Init_InvalidAlgorithmParameter, Constructor_NoSuchAlgorithm, Constructor_NoSuchPadding
		};
		
		public final Reason reason;
		CryptoException(Reason reason, Throwable cause) {
			super(String.format("[Reason: %s] %s", reason, cause.getMessage()),cause);
			this.reason = reason;
		}
	}
}
