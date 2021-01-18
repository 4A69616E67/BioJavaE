package com.github.SnowFlakes.IO;

import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.unit.BEDPEItem;
import htsjdk.samtools.util.IOUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class BedPeWriterExtension extends BufferedWriter implements HTSWriter<BEDPEItem> {
    public BedPeWriterExtension(Writer out) {
        super(out);
    }

    public BedPeWriterExtension(File file, boolean append) {
        this(IOUtil.openFileForBufferedWriting(file, append));
    }

    public BedPeWriterExtension(File file) {
        this(file, false);
    }


    @Override
    public synchronized void WriterRecord(BEDPEItem o) {
        BedItem l1 = o.getLocate1();
        BedItem l2 = o.getLocate2();
        try {
            write(l1.getContig() + "\t" + l1.getStart() + "\t" + l1.getEnd());
            write(l2.getContig() + "\t" + l2.getStart() + "\t" + l2.getEnd());
            write("\t" + o.getName());
            write("\t" + l1.getScore() + "\t" + l2.getScore());
            write("\t" + l1.getStrand() + "\t" + l2.getStrand());
            if (o.Extends != null && o.Extends.length > 0) {
                write("\t" + String.join("\t", o.Extends));
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Override
    public synchronized void WriterRecordln(BEDPEItem o) {
        this.WriterRecord(o);
        try {
            write("\n");
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
