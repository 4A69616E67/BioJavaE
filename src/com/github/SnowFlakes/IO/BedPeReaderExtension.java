package com.github.SnowFlakes.IO;

import com.github.SnowFlakes.unit.BEDPEItem;
import htsjdk.samtools.util.BufferedLineReader;

import java.io.InputStream;

public class BedPeReaderExtension extends BufferedLineReader implements HTSReader<BEDPEItem> {
    public BedPeReaderExtension(InputStream is) {
        super(is);
    }

    @Override
    public BEDPEItem ReadRecord() {
        String[] s = readLine().split("\\s+");
        return new BEDPEItem(s);
    }
}
