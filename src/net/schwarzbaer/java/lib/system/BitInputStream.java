package net.schwarzbaer.java.lib.system;

public class BitInputStream {
	private final byte[] bytes;
	private int byteIndex;
	private int bitIndex;

	public BitInputStream(byte[] bytes) {
		this.bytes = bytes;
		byteIndex = 0;
		bitIndex = 0;
		if (bytes==null)
			throw new IllegalArgumentException("Byte array must not be NULL.");
	}

	public synchronized int readBits(int bitCount) {
		if (bitCount<0 ) throw new IllegalArgumentException("Can't read less than 0 bits.");
		if (bitCount>31) throw new IllegalArgumentException("Can't read more than 31 bits at once.");
		if (byteIndex>=bytes.length) return -1;
		int result = 0;
		int resultBitIndex = 0;
		while (bitCount>0 && byteIndex<bytes.length) {
			if (bitIndex+bitCount>=8) {
				int bitCount_ = 8-bitIndex;
				result = copyBits(result, resultBitIndex, bitCount_);
				resultBitIndex += bitCount_;
				bitIndex        = 0;
				bitCount       -= bitCount_;
				byteIndex      ++;
			} else {
				result = copyBits(result, resultBitIndex, bitCount);
				resultBitIndex += bitCount;
				bitIndex       += bitCount;
				bitCount        = 0;
			}
		}
		return result;
	}

	private int copyBits(int result, int resultBitIndex, int bitCount) {
		if (bitIndex+bitCount>8) throw new IllegalArgumentException();
		int bitMask = ((1<<bitCount)-1)<<bitIndex;
		int value = (bytes[byteIndex]&bitMask)>>bitIndex;
		return result | (value<<resultBitIndex);
	}
}
