package com.github.SnowFlakes.IO;

import htsjdk.samtools.util.IOUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class HTSWriterImpl extends BufferedWriter implements HTSWriter<String> {

    public HTSWriterImpl(Writer out) {
        super(out);
    }

    public HTSWriterImpl(File out) {
        this(out, false);
    }

    public HTSWriterImpl(File out, boolean append) {
        super(IOUtil.openFileForBufferedWriting(out, append));
    }

    @Override
    public void WriterRecord(String o) {
        try {
            write(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void WriterRecordln(String o) {
        try {
            write(o + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
