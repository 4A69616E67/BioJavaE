package com.github.SnowFlakes.IO;

import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.IOUtil;

import java.io.*;

public class FastaWriterExtension extends BufferedWriter implements HTSWriter<ReferenceSequence> {
    protected int SeqLen = 60;

    public FastaWriterExtension(File file) {
        this(IOUtil.openFileForBufferedWriting(file));
    }

    public FastaWriterExtension(File file, boolean append) {
        this(IOUtil.openFileForBufferedWriting(file, append));
    }

    public FastaWriterExtension(Writer out) {
        super(out);
    }

    @Override
    public void WriterRecord(ReferenceSequence o) {
        try {
            write(">" + o.getName() + "\n");
            String s = o.getBaseString();
            for (int i = 0; i < s.length(); i += SeqLen) {
                write(s.substring(i, Math.min(i + SeqLen, s.length())) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void WriterRecordln(ReferenceSequence o) {
        WriterRecord(o);
        try {
            write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSeqLen(int seqLen) {
        SeqLen = seqLen;
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
