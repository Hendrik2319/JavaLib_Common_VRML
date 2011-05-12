/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.java.lib.zip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Hendrik
 */
public class SimpleZIPoutput {

    public static void main( String[] args ) {

        JFileChooser fch = new JFileChooser(".");
        fch.setFileFilter( new FileNameExtensionFilter("EPUB (*.epub,*.zip)", "epub", "zip") );
        fch.setMultiSelectionEnabled(false);
        fch.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fch.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.out.println("File selection aborted");
            return;
        }

        final File zipFile = fch.getSelectedFile();

        SimpleZIPoutput zipoutput = null;
        try {
            zipoutput = new SimpleZIPoutput(zipFile);
        } catch (FileNotFoundException ex) {
            return;
        }

        zipoutput.writeFile("testfile1.txt", "\"testfile.txt\"",false);
        zipoutput.writeFile("testdir/testfile2.txt", "\"testfile.txt\"",false);
        zipoutput.writeFile("testdir/testfile3.c.txt", "\"testfile.txt\"",true);
        zipoutput.writeFile("testfile4.c.txt", "\"testfile.txt\"",true);
        zipoutput.writeFile("testdir/testfile5.txt", "\"testfile.txt\"",false);

        zipoutput.closeZIP();
    }

    private ZipOutputStream zip_output;
    private BufferedWriter output;
    private ZipEntry currentZipEntry;
    
    public SimpleZIPoutput( File zipFile ) throws FileNotFoundException {
        zip_output = null;
        output = null;
        currentZipEntry = null;

        zip_output = new ZipOutputStream(new FileOutputStream( zipFile));
        
        try { output = new BufferedWriter(new OutputStreamWriter( zip_output, "UTF-8" ) ); }
        catch (UnsupportedEncodingException ex) {
            output = new BufferedWriter(new OutputStreamWriter( zip_output ) );
        }
    }

    public void closeZIP() {
        try {
            zip_output.flush();
        } catch (IOException ex) {
            System.out.println("IOException while zip_output.flush()");
            ex.printStackTrace();
        }

        try {
            zip_output.finish();
        } catch (IOException ex) {
            System.out.println("IOException while zip_output.finish()");
            ex.printStackTrace();
        }
        try {
            zip_output.close();
        } catch (IOException ex) {
            System.out.println("IOException while zip_output.close()");
            ex.printStackTrace();
        }
    }

    public void writeFile( String filename, String content, boolean compressed ) {
        currentZipEntry = null;
        ZipEntry ze = new ZipEntry(filename);

        if (compressed) ze.setMethod( ZipEntry.DEFLATED );
        else {
            ze.setMethod( ZipEntry.STORED );
            ze.setSize( content.length() );
            ze.setCompressedSize( content.length() );

            CRC32 c = new CRC32();
            c.update( content.getBytes() );

            ze.setCrc( c.getValue() );
        }

        try {
            zip_output.putNextEntry(ze);
        } catch (IOException ex) {
            System.out.println("IOException while putNextEntry(\"" + ze.getName() + "\")");
            ex.printStackTrace();
            return;
        }
        try {
            output.write(content);
        } catch (IOException ex) {
            System.out.println("IOException while output.write([content]) to \"" + ze.getName() + "\"");
            ex.printStackTrace();
        }
        try {
            output.flush();
        } catch (IOException ex) {
            System.out.println("IOException while output.flush() on \"" + ze.getName() + "\"");
            ex.printStackTrace();
        }
        try {
            zip_output.closeEntry();
        } catch (IOException ex) {
            System.out.println("IOException while zip_output.closeEntry() on \"" + ze.getName() + "\"");
            ex.printStackTrace();
        }

    }

    public void CompressedFile_open( String filename ) {
        currentZipEntry = new ZipEntry(filename);
        currentZipEntry.setMethod( ZipEntry.DEFLATED );
        try {
            zip_output.putNextEntry(currentZipEntry);
        } catch (IOException ex) {
            System.out.println("IOException while putNextEntry(\"" + currentZipEntry.getName() + "\")");
            ex.printStackTrace();
            return;
        }
    }

    public void CompressedFile_close() {
        try {
            output.flush();
        } catch (IOException ex) {
            System.out.println("IOException while output.flush() on \"" + currentZipEntry.getName() + "\"");
            ex.printStackTrace();
        }
        try {
            zip_output.closeEntry();
        } catch (IOException ex) {
            System.out.println("IOException while zip_output.closeEntry() on \"" + currentZipEntry.getName() + "\"");
            ex.printStackTrace();
        }
    }

    public void CompressedFile_writeln( String str ) {
        CompressedFile_write_private( str, true );
    }

    public void CompressedFile_write( String str ) {
        CompressedFile_write_private( str, false );
    }

    private void CompressedFile_write_private( String str, boolean newline ) {
        if (currentZipEntry==null) return;

        try {
            output.write(str);
            if (newline) output.newLine();
        } catch (IOException ex) {
            System.out.println("IOException while output.write([content]) to \"" + currentZipEntry.getName() + "\"");
            ex.printStackTrace();
        }
        try {
            output.flush();
        } catch (IOException ex) {
            System.out.println("IOException while output.flush() on \"" + currentZipEntry.getName() + "\"");
            ex.printStackTrace();
        }
    }

}
