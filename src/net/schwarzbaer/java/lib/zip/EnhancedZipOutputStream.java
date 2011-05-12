/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.java.lib.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author hscholtz
 */
public class EnhancedZipOutputStream extends ZipOutputStream {

    private ZipEntry currentEntry;
    private CRC32 crc;
    private long stored_size;

    public EnhancedZipOutputStream(OutputStream out) {
        super(out);
        currentEntry = null;
        crc = new CRC32();
        stored_size = 0;
    }

    private void fixSize( int len ) {
        if (currentEntry.getMethod() == ZipEntry.STORED) {
            currentEntry.setSize(stored_size + len + 10);
            currentEntry.setCompressedSize(stored_size + len + 10);
        }
    }

    private void setCrcOfLastEntry() {
        if (currentEntry!=null) {
            if (currentEntry.getMethod()==ZipEntry.STORED) {
                currentEntry.setCrc(crc.getValue());
                currentEntry.setSize(stored_size);
                currentEntry.setCompressedSize(stored_size);
            }
        }
    }

    @Override
    public void close() throws IOException {
        setCrcOfLastEntry();
        currentEntry = null;
        super.close();
    }

    @Override
    public void closeEntry() throws IOException {
        setCrcOfLastEntry();
        currentEntry = null;
        super.closeEntry();
    }

    @Override
    public void finish() throws IOException {
        setCrcOfLastEntry();
        currentEntry = null;
        super.finish();
    }

    @Override
    public void putNextEntry(ZipEntry e) throws IOException {
        setCrcOfLastEntry();

        currentEntry = e;
        crc.reset();
        stored_size=0;
        
        fixSize(0);
        currentEntry.setCrc(0);

        super.putNextEntry(e);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        fixSize( len );
        super.write(b, off, len);
        if (currentEntry.getMethod()==ZipEntry.STORED) {
            crc.update(b, off, len);
            stored_size += len;
        }

    }

    @Override
    public void write(int b) throws IOException {
        fixSize(1);
        super.write(b);
        if (currentEntry.getMethod()==ZipEntry.STORED) {
            crc.update(b);
            stored_size ++;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        fixSize(1);
        super.write(b);
        if (currentEntry.getMethod()==ZipEntry.STORED) {
            crc.update(b);
            stored_size ++;
        }
    }




}
