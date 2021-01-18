package com.github.SnowFlakes.IO;

import htsjdk.samtools.fastq.BasicFastqWriter;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.util.IOUtil;

import java.io.*;

public class FastqWriterExtension<E extends FastqRecord> extends BasicFastqWriter implements HTSWriter<E> {
    public FastqWriterExtension(File file) {
        this(file, false);
    }

    public FastqWriterExtension(File file, boolean append) {
        super(new PrintStream(IOUtil.openFileForWriting(file, append)));
    }

    public FastqWriterExtension(PrintStream writer) {
        super(writer);
    }


    @Override
    public synchronized void WriterRecord(E o) {
        write(o);
    }

    @Override
    public void WriterRecordln(E o) {
        WriterRecord(o);
    }

    @Override
    public void close() {
        super.close();
    }
}
