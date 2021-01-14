package com.github.SnowFlakes.File.FastQFile;

import com.github.SnowFlakes.IO.FastqReaderExtension;
import com.github.SnowFlakes.IO.FastqWriterExtension;
import com.github.SnowFlakes.tool.Tools;
import htsjdk.samtools.fastq.FastqRecord;

import java.io.*;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 */
public class FastqFile extends File {
    enum QualityFormat {
        PHRED33, PHRED64
    }

    public FastqFile(File file) {
        super(file.getPath());
    }

    public FastqFile(String s) {
        super(s);
    }

    public FastqFile(FastqFile file) {
        super(file.getPath());
    }


    public static QualityFormat FastqPhred(FastqFile file) throws IOException {
        FastqReaderExtension reader = new FastqReaderExtension(file);
        FastqRecord Item;
        int[] FormatEdge = new int[]{(int) '9', (int) 'K'};
        int[] Count = new int[2];
        int LineNum = 0;
        while ((Item = reader.ReadRecode()) != null && ++LineNum <= 100) {
            for (byte q : Item.getBaseQualities()) {
                if (q <= FormatEdge[0]) {
                    Count[0]++;
                } else if (q >= FormatEdge[1]) {
                    Count[1]++;
                }
            }
        }
        reader.close();
        return Count[0] >= Count[1] ? QualityFormat.PHRED33 : QualityFormat.PHRED64;
    }

    public ArrayList<FastqRecord> ExtractID(Collection<String> List) throws IOException {
        return ExtractID(List, 1);
    }

    public ArrayList<FastqRecord> ExtractID(Collection<String> List, int threads) throws IOException {
        ArrayList<FastqRecord> ResList = new ArrayList<>();
        FastqReaderExtension reader = new FastqReaderExtension(this);
        Thread[] t = new Thread[threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                FastqRecord item;
                while ((item = reader.ReadRecode()) != null) {
                    if (List.contains(item.getReadName())) {
                        synchronized (this) {
                            ResList.add(item);
                            List.remove(item.getReadName());
                            if (List.size() <= 0) {
                                break;
                            }
                        }
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        reader.close();
        return ResList;
    }

    public void ExtractID(Collection<String> List, int threads, FastqFile OutFile) throws IOException {
        FastqReaderExtension reader = new FastqReaderExtension(this);
        FastqWriterExtension writer = new FastqWriterExtension(OutFile);
        Thread[] t = new Thread[threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                FastqRecord item;
                while ((item = reader.ReadRecode()) != null) {
                    if (List.contains(item.getReadName())) {
                        synchronized (this) {
                            writer.WriterRecodeln(item);
                            List.remove(item.getReadName());
                            if (List.size() <= 0) {
                                break;
                            }
                        }
                    }
                }

            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        reader.close();
        writer.close();
    }
}
