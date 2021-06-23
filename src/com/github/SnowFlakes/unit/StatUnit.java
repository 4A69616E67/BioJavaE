package com.github.SnowFlakes.unit;

import com.github.SnowFlakes.File.FastaFile;
import com.github.SnowFlakes.IO.FastaReaderExtension;
import htsjdk.samtools.reference.ReferenceSequence;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * biology statistic class, include much item about statistic
 */
public class StatUnit {
    private final int interval = 100000;
    public FastaFile fastaFile;
    public HashMap<String, ArrayList<char[]>> Base = new HashMap<>();


    public void analys(FastaFile file) {
        ReferenceSequence seq;
        FastaReaderExtension fasta_reader = file.getReader();
        while ((seq = fasta_reader.ReadRecord()) != null) {
            if (!Base.containsKey(seq.getName())) {
                Base.put(seq.getName(), new ArrayList<>());
            }
            ArrayList<char[]> count = Base.get(seq.getName());
            String s = seq.getBaseString();
            for (int i = 0; i * interval < s.length(); i++) {
                count.add(StringCount(s.substring(i*interval,Math.min((i+1)*interval,s.length()))));
            }
        }
    }

    public char[] StringCount(String s) {
        char[] count = new char[256];
        for (char b : s.toCharArray()) {
            count[b]++;
        }
        return count;
    }
}