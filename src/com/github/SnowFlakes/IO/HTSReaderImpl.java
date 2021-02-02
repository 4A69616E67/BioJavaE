package com.github.SnowFlakes.IO;

import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.IOUtil;

import java.io.File;
import java.io.InputStream;

public class HTSReaderImpl extends BufferedLineReader implements HTSReader<String> {
    public HTSReaderImpl(InputStream is) {
        super(is);
    }

    public HTSReaderImpl(File file) {
        super(IOUtil.openFileForReading(file));
    }

    @Override
    public String ReadRecord() {
        return null;
    }
}
