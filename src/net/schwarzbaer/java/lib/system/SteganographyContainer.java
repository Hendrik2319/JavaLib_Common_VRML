package net.schwarzbaer.java.lib.system;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class SteganographyContainer {
	
	public final BufferedImage image;
	public final int width;
	public final int height;

	public SteganographyContainer(BufferedImage image) {
		this.image = image;
		width = this.image.getWidth();
		height = this.image.getHeight();
	}
	
	public static class SteganographyException extends Exception {
		private static final long serialVersionUID = -3899205850556348509L;
		
		public enum Reason { WrongBlockFormat, Decryption, Encryption }
		public final Reason reason;
		private SteganographyException(Reason reason, Throwable cause) {
			super(String.format("[Reason: %s] %s", reason, cause.getMessage()), cause);
			this.reason = reason;
		}
		private SteganographyException(Reason reason) {
			super(String.format("[Reason: %s]", reason));
			this.reason = reason;
		}
	}

	public static int computeMinImgSize_Px(int bytesLength, int bitsPerPx, boolean isEncrypted, boolean asByteBlock) {
		if (asByteBlock) bytesLength = 4 + bytesLength + 3;
		if (isEncrypted) bytesLength = SimpleCryptoAES.INIT_VECTOR + bytesLength + SimpleCryptoAES.BLOCKSIZE + 1;
		return (int) Math.ceil(bytesLength*8/3f/bitsPerPx);
	}

	public static int computeCapacity_B(int imgWidth, int imgHeight, int bitsPerPx, boolean isEncrypted, boolean asByteBlock) {
		int imgCap = (imgWidth*imgHeight*3*bitsPerPx)/8;
		if (isEncrypted) imgCap = ((imgCap - SimpleCryptoAES.INIT_VECTOR)/SimpleCryptoAES.BLOCKSIZE-1)*SimpleCryptoAES.BLOCKSIZE;
		if (asByteBlock) imgCap = imgCap - (4+3);
		return imgCap;
	}

	public static byte[] encrypt(String password, byte[] plainBytes, int imgWidth, int imgHeight, int bitsPerPx) throws SimpleCryptoAES.CryptoException {
		
		int origSize = plainBytes.length;
		int imgSize = (int) Math.ceil(imgWidth*imgHeight*3*bitsPerPx/8f);
		int paddedSize = (int) (Math.ceil(imgSize/(float)SimpleCryptoAES.BLOCKSIZE+3)*SimpleCryptoAES.BLOCKSIZE); 
		if (paddedSize>origSize) {
			plainBytes = Arrays.copyOf(plainBytes,paddedSize);
			Arrays.fill(plainBytes, origSize, paddedSize, (byte)new Random().nextInt());
		}
		
		SimpleCryptoAES.Result result = SimpleCryptoAES.encrypt(password, plainBytes, false);
		
		byte[] resultBytes = Arrays.copyOf(result.initVector, result.initVector.length+result.outputBytes.length);
		for (int i=0; i<result.outputBytes.length; i++)
			resultBytes[result.initVector.length+i] = result.outputBytes[i];
		
		return resultBytes;
	}

	public static byte[] decrypt(String password, byte[] storedBytes) throws SimpleCryptoAES.CryptoException {
		
		byte[] initVector = Arrays.copyOfRange(storedBytes,0,SimpleCryptoAES.INIT_VECTOR);
		byte[] encodedBytes = Arrays.copyOfRange(storedBytes,SimpleCryptoAES.INIT_VECTOR,storedBytes.length);
		
		byte[] decodedBytes = SimpleCryptoAES.decrypt(password, initVector, encodedBytes, false);
		
		return decodedBytes;
	}

	@SuppressWarnings("unused")
	private String typeToString(int type) {
		switch (type) {
		case BufferedImage.TYPE_3BYTE_BGR     : return "TYPE_3BYTE_BGR";
		case BufferedImage.TYPE_4BYTE_ABGR    : return "TYPE_4BYTE_ABGR";
		case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
		case BufferedImage.TYPE_BYTE_BINARY   : return "TYPE_BYTE_BINARY";
		case BufferedImage.TYPE_BYTE_GRAY     : return "TYPE_BYTE_GRAY";
		case BufferedImage.TYPE_BYTE_INDEXED  : return "TYPE_BYTE_INDEXED";
		case BufferedImage.TYPE_CUSTOM        : return "TYPE_CUSTOM";
		case BufferedImage.TYPE_INT_ARGB      : return "TYPE_INT_ARGB";
		case BufferedImage.TYPE_INT_ARGB_PRE  : return "TYPE_INT_ARGB_PRE";
		case BufferedImage.TYPE_INT_BGR       : return "TYPE_INT_BGR";
		case BufferedImage.TYPE_INT_RGB       : return "TYPE_INT_RGB";
		case BufferedImage.TYPE_USHORT_555_RGB: return "TYPE_USHORT_555_RGB";
		case BufferedImage.TYPE_USHORT_565_RGB: return "TYPE_USHORT_565_RGB";
		case BufferedImage.TYPE_USHORT_GRAY   : return "TYPE_USHORT_GRAY";
		}
		return "Unknown Type ["+type+"]";
	}

	public void writeRawBytes(byte[] bytes, int bitsPerPx) {
		writeRawBytes(bytes, bitsPerPx, false); // true: old behaviour
	}
	
	public void writeRawBytes(byte[] bytes, int bitsPerPx, boolean writeZerosAtEnd) {
		int bitmask = (1<<bitsPerPx)-1;
		BitInputStream bitIn = new BitInputStream(bytes);
		WritableRaster raster = image.getRaster();
		//System.out.println("[writeBytesToImage]  imageType: "+typeToString(image.getType()));
		int[] pixelRGBA = new int[] { 12,12,12,12 };
		boolean endOfStream = false;
		for (int y=0; y<height && !endOfStream; y++)
			for (int x=0; x<width && !endOfStream; x++) {
				raster.getPixel(x,y,pixelRGBA);
				//System.out.printf("[writeBytesToImage]  Pixel(%d,%d): %s%n",x,y,Arrays.toString(pixelRGBA));
				for (int i=0; i<3; i++) {
					int bits = bitIn.readBits(bitsPerPx);
					if (bits<0) { // no more bits in stream
						if (!writeZerosAtEnd) {
							endOfStream = true;
							break;
						}
						bits=0;
					}
					pixelRGBA[i] = (pixelRGBA[i]&(~bitmask)) | (bits&bitmask);
				}
				raster.setPixel(x,y,pixelRGBA);
			}
	}

	public byte[] readRawBytes(int bitsPerPx) {
		int bitmask = (1<<bitsPerPx)-1;
		BitOutputStream bitOut = new BitOutputStream();
		WritableRaster raster = image.getRaster();
		//System.out.println("[writeBytesToImage]  imageType: "+typeToString(image.getType()));
		int[] pixelRGBA = new int[] { 12,12,12,12 };
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++) {
				raster.getPixel(x,y,pixelRGBA);
				//System.out.printf("[writeBytesToImage]  Pixel(%d,%d): %s%n",x,y,Arrays.toString(pixelRGBA));
				for (int i=0; i<3; i++)
					bitOut.writeBits(pixelRGBA[i]&bitmask,bitsPerPx);
			}
		return bitOut.getByteArray();
	}

	public void writeEncryptedBytes(byte[] bytes, int bitsPerPx, String password) throws SteganographyException {
		try { bytes = encrypt(password, bytes, width, height, bitsPerPx); }
		catch (SimpleCryptoAES.CryptoException e) { throw new SteganographyException(SteganographyException.Reason.Encryption,e); }
		writeRawBytes(bytes, bitsPerPx);
	}

	public byte[] readEncryptedBytes(int bitsPerPx, String password) throws SteganographyException {
		byte[] bytes = readRawBytes(bitsPerPx);
		try { return decrypt(password, bytes); }
		catch (SimpleCryptoAES.CryptoException e) { throw new SteganographyException(SteganographyException.Reason.Decryption,e); }
	}

	public void writeByteBlock(byte[] bytes, int bitsPerPx, boolean withEncryption, String password) throws SteganographyException {
		int length = bytes.length;
		byte[] byteBlock = new byte[4 + length + 3];
		byteBlock[0] = (byte) ((length>>(0*8))&0x0ff);
		byteBlock[1] = (byte) ((length>>(1*8))&0x0ff);
		byteBlock[2] = (byte) ((length>>(2*8))&0x0ff);
		byteBlock[3] = (byte) ((length>>(3*8))&0x0ff);
		for (int i=0; i<length; i++)
			byteBlock[4+i] = bytes[i];
		byteBlock[4 + length + 0] = 'E';
		byteBlock[4 + length + 1] = 'N';
		byteBlock[4 + length + 2] = 'D';
		
		if (withEncryption) writeEncryptedBytes(byteBlock, bitsPerPx, password);
		else                writeRawBytes      (byteBlock, bitsPerPx);
	}

	public byte[] readByteBlock(int bitsPerPx, boolean withEncryption, String password) throws SteganographyException {
		byte[] byteBlock;
		if (withEncryption) byteBlock = readEncryptedBytes(bitsPerPx, password);
		else                byteBlock = readRawBytes      (bitsPerPx);
		if (byteBlock.length < 4+0+3) throw new SteganographyException(SteganographyException.Reason.WrongBlockFormat);
		int length =
				(byteBlock[0]&0x0ff)<<(0*8) |
				(byteBlock[1]&0x0ff)<<(1*8) |
				(byteBlock[2]&0x0ff)<<(2*8) |
				(byteBlock[3]&0x0ff)<<(3*8);
		if (length<0 || byteBlock.length < 4+length+3 ||
			byteBlock[4+length+0] != 'E' ||
			byteBlock[4+length+1] != 'N' ||
			byteBlock[4+length+2] != 'D')
			throw new SteganographyException(SteganographyException.Reason.WrongBlockFormat);
		return Arrays.copyOfRange(byteBlock, 4, 4+length);
	}

	public BufferedImage readPixelInPixelImage(int bitsPerPx) {
		int bitmask = (1<<bitsPerPx)-1;
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		WritableRaster newRaster = newImage.getRaster();
		int[] pixelRGBA = new int[] { 12,12,12,12 };
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++) {
				raster.getPixel(x,y,pixelRGBA);
				for (int i=0; i<3; i++)
					pixelRGBA[i] = ((pixelRGBA[i]&bitmask)*255)/bitmask;
				pixelRGBA[3] = 255;
				newRaster.setPixel(x,y,pixelRGBA);
			}
		return newImage;
	}

	public void writeText(String text, Charset charSet, Integer bitsPerPx, boolean withEncryption, String password) throws SteganographyException {
		writeByteBlock(text.getBytes(charSet),bitsPerPx,withEncryption,password);
	}

	public String readText(Charset charSet, Integer bitsPerPx, boolean withEncryption, String password) throws SteganographyException {
		byte[] bytes = readByteBlock(bitsPerPx,withEncryption,password);
		if (bytes==null) return null;
		return new String(bytes,charSet);
	}
}
