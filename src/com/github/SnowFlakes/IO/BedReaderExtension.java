package com.github.SnowFlakes.IO;

import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.File.BedFile.BedFile;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.bed.BEDCodec;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BedReaderExtension extends BufferedLineReader implements HTSReader<BedItem> {
    protected BEDCodec codec = new BEDCodec();
    protected ArrayList<String> header;
    protected BedFile.Format format = BedFile.Format.BED6;

    public BedReaderExtension(InputStream is) {
        super(is);
        try {
            header = ReadHeader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BedReaderExtension(File file) {
        this(IOUtil.openFileForReading(file));
    }


    @Override
    public BedItem ReadRecord() {
        String[] tokens = readLine().split("\\s+");
        BedItem item;
        switch (format) {
            case BED6:
                item = new BedItem(codec.decode(Arrays.copyOfRange(tokens, 0, Math.min(6, tokens.length))));
                item.Extends = Arrays.copyOfRange(tokens, 6, tokens.length);
                break;
            case BED12:
                item = new BedItem(codec.decode(Arrays.copyOfRange(tokens, 0, Math.min(12, tokens.length))));
                item.Extends = Arrays.copyOfRange(tokens, 12, tokens.length);
                break;
            default:
                return null;
        }
        return item;
    }

    /**
     * read bed file header, stop when line not start with "#" , "track" or "browser"
     *
     * @return a list include file header
     * @throws IOException
     */

    private ArrayList<String> ReadHeader() throws IOException {
        ArrayList<String> list = new ArrayList<>();
        String line = null;
        mark(1000);
        while ((line = readLine()) != null) {
            if (line.startsWith("#") || line.startsWith("track") || line.startsWith("browser")) {
                list.add(line);
                mark(1000);
            } else {
                reset();
                break;
            }
        }
        return list;
    }

    public void setFormat(BedFile.Format format) {
        this.format = format;
    }
}
