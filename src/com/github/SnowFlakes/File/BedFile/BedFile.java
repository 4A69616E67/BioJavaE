package com.github.SnowFlakes.File.BedFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.IO.BedReaderExtension;
import com.github.SnowFlakes.IO.BedWriterExtension;

import java.io.*;
import java.util.Comparator;

/**
 * Created by snowf on 2019/2/17.
 */
public class BedFile extends AbstractFile<BedItem> {
    public enum Format {
        BED6, BED12
    }

    public short DeBugLevel = 0;

    public BedFile(String pathname) {
        super(pathname);
    }

    @Override
    public BedReaderExtension getReader() {
        try {
            return new BedReaderExtension(new FileInputStream(this));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BedWriterExtension getWriter() {
        return getWriter(false);
    }

    @Override
    public BedWriterExtension getWriter(boolean append) {
        try {
            return new BedWriterExtension(new FileWriter(this));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BedFile(BedFile file) {
        super(file);
    }


    public class NameComparator implements Comparator<BedItem> {

        @Override
        public int compare(BedItem o1, BedItem o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public class LocationComparator implements Comparator<BedItem> {

        @Override
        public int compare(BedItem o1, BedItem o2) {
            int res = o1.getContig().compareToIgnoreCase(o2.getContig());
            if (res == 0) {
                res = o1.getStart() - o2.getStart();
                if (res == 0) {
                    return o1.getEnd() - o2.getEnd();
                } else {
                    return res;
                }
            } else {
                return res;
            }
        }
    }

}