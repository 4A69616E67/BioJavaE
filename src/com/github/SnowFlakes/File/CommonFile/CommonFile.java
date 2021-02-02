package com.github.SnowFlakes.File.CommonFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.IO.HTSReaderImpl;
import com.github.SnowFlakes.IO.HTSWriter;
import com.github.SnowFlakes.IO.HTSWriterImpl;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.IOUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by snowf on 2019/2/17.
 */
public class CommonFile extends AbstractFile<String> {
    public String Regex = "\\s+";

    public CommonFile(String pathname) {
        super(pathname);
    }

    public CommonFile(File f) {
        super(f);
    }

    @Override
    public HTSReaderImpl getReader() {
        return new HTSReaderImpl(this);
    }

    @Override
    public HTSWriter<String> getWriter() {
        return new HTSWriterImpl(this);
    }

    @Override
    public HTSWriter<String> getWriter(boolean append) {
        return null;
    }
}
