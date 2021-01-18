package com.github.SnowFlakes.IO;

import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.util.IOUtil;

import java.io.*;

public class FastqReaderExtension extends FastqReader implements HTSReader<FastqRecord> {


    public FastqReaderExtension(File file) {
        super(IOUtil.openFileForBufferedReading(file));
    }

    public FastqReaderExtension(File file, boolean skipBlankLines) {
        super(file, IOUtil.openFileForBufferedReading(file), skipBlankLines);
    }

    @Override
    public FastqRecord ReadRecord() {
        if (hasNext()) {
            return next();
        }
        return null;
    }
}
