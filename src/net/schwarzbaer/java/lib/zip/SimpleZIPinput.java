/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.java.lib.zip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Hendrik
 */
public class SimpleZIPinput {

    private ZipInputStream zip_input;
    private BufferedReader input;
    private ZipEntry currentZipEntry;

    public SimpleZIPinput( File zipFile ) throws FileNotFoundException {
        zip_input = null;
        input = null;
        currentZipEntry = null;

        zip_input = new ZipInputStream(new FileInputStream( zipFile ) );

        try { input = new BufferedReader(new InputStreamReader( zip_input, "UTF-8" ) ); }
        catch (UnsupportedEncodingException ex) {
            input = new BufferedReader(new InputStreamReader( zip_input ) );
        }
    }

    public void closeZIP() {
        try {
            zip_input.close();
        } catch (IOException ex) {
            System.out.println("IOException while zip_input.close()");
            ex.printStackTrace();
        }
    }

    public boolean openEntry( String filename ) {
        currentZipEntry = null;
        ZipEntry nextZipEntry = null;
        try {
            nextZipEntry = zip_input.getNextEntry();
        } catch (IOException ex) {
            System.out.println("IOException while zip_input.getNextEntry()");
            ex.printStackTrace();
            return false;
        }
        if (nextZipEntry==null) return false;
        if (!nextZipEntry.getName().equals(filename)) return false;

        currentZipEntry = nextZipEntry;
        return true;
    }

    public void closeEntry() {
        currentZipEntry = null;
        try {
            zip_input.closeEntry();
        } catch (IOException ex) {
            System.out.println("IOException while zip_input.closeEntry() on \"" + currentZipEntry.getName() + "\"");
            ex.printStackTrace();
        }
    }

    public String readLine() {
        try {
            //if (zip_input.available() == 0) { return null; }
            return input.readLine();
        }
        catch (IOException ex) {
            System.out.println("IOException while SimpleZIPinput.readLine() on \"" + currentZipEntry.getName() + "\"");
            ex.printStackTrace();
            return null;
        }
    }



}
