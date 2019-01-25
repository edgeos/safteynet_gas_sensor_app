package com.wearables.ge.safteynet_gas_sensor.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractCollection;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.NonNull;

public class LogCollection {

    private static final String DIRECTORY_NAME = "gas_sensor";
    private static final File downloadsPath = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), DIRECTORY_NAME);


    // Bookkeeping for the file and it's size
    private File file;
    private FileWriter writer;
    private long size = 0;

    public LogCollection(String fileName) throws IOException {
        // For now just hardcode the path to be the downloads directory, plus the filename
        this.file = new File(downloadsPath.getPath(), fileName);

        // Make sure the file exists
        if (!file.exists()) {
            file.createNewFile();
        }

        final FileReader fileReader = new FileReader(file);
        final LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
        this.writer = new FileWriter(file, true);

        // Get an initial count of the number of lines in the file.
        // After this we will keep track of the lines ourselves
        while (lineNumberReader.readLine() != null) size++;
    }

    public void write(String logLine) throws IOException {
        size++;
        writer.append(logLine);
        writer.flush();  // This is a performance hit, but should not matter right now.
    }

    public String read(long index) throws IOException {
        if (index > size) {
            return "";
        }

        String line = "";
        FileReader fReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fReader);
        for (long i = 0; i < index; i++) {
            line = reader.readLine();
        }
        return line;
    }

    public long size() {
        return size;
    }

    public File getFile() {
        return file;
    }

    public File[] getAllLogs() {
        return file.getParentFile().listFiles();
    }
}
