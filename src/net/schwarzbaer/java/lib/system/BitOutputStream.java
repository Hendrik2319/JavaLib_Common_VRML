package net.schwarzbaer.java.lib.system;

import java.util.Vector;

public class BitOutputStream {
	private final Vector<Byte> bytes;
	private int bitIndex;

	public BitOutputStream() {
		bytes = new Vector<>();
		bitIndex = 0;
	}

	public synchronized byte[] getByteArray() {
		Byte[] array1 = bytes.toArray(new Byte[bytes.size()]);
		byte[] array2 = new byte[bytes.size()];
		for (int i=0; i<array1.length; i++) array2[i] = array1[i];
		return array2;
		//return bytes.stream().toArray(Byte[]::new);
	}

	public synchronized void writeBits(int value, int bitCount) {
		if (bitCount<0)  throw new IllegalArgumentException("Can't write less than 0 bits.");
		if (bitCount>31) throw new IllegalArgumentException("Can't write more than 31 bits at once.");
		while (bitCount>0) {
			if (bitIndex==0) bytes.add((byte) 0);
			if (bitIndex+bitCount>=8) {
				int bitCount_ = 8-bitIndex;
				copyBits(value, bitCount_);
				value     = value>>bitCount_;
				bitIndex  = 0;
				bitCount -= bitCount_;
			} else {
				copyBits(value, bitCount);
				value     = value>>bitCount;
				bitIndex += bitCount;
				bitCount  = 0;
			}
		}
	}

	private void copyBits(int value, int bitCount) {
		if (bitIndex+bitCount>8) throw new IllegalArgumentException();
		if (bytes.isEmpty()) throw new IllegalStateException();
		int b = bytes.lastElement();
		int bitMask1 = (1<<bitIndex)-1;
		int bitMask2 = (1<<bitCount)-1;
		b = (b&bitMask1) | ((value&bitMask2)<<bitIndex);
		bytes.set(bytes.size()-1, (byte)(b&0x0ff));
	}
}
