package com.github.SnowFlakes.File;


import com.github.SnowFlakes.IO.FastaReaderExtension;
import com.github.SnowFlakes.IO.FastaWriterExtension;
import com.github.SnowFlakes.IO.HTSWriter;
import htsjdk.samtools.reference.ReferenceSequence;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * Created by snowf on 2019/2/17.
 */
public class FastaFile extends AbstractFile<ReferenceSequence> {

    public static void main(String[] args) {
        FastaFile infile = new FastaFile("test.fna");
        FastaReaderExtension reader = infile.getReader();
        ReferenceSequence seq;
        ArrayList<ReferenceSequence> list = new ArrayList<>();
        while ((seq = reader.ReadRecord()) != null) {
//            seq = new ReferenceSequence(seq.getName().split("\\s+")[0],0, seq.getBases());
            list.add(seq);
        }
        System.out.println(list.size());
    }

    public FastaFile(File file) {
        super(file);
    }

    public FastaFile(String pathname) {
        super(pathname);
    }


    public FastaReaderExtension getReader() {
        return new FastaReaderExtension(this);
    }

    @Override
    public HTSWriter<ReferenceSequence> getWriter() {
        return new FastaWriterExtension(this);
    }

    @Override
    public HTSWriter<ReferenceSequence> getWriter(boolean append) {
        return new FastaWriterExtension(this, append);
    }

//    public void write(Collection<Sequence<?>> sequences) {
//        try {
//            FastaWriterHelper.writeSequences(IOUtil.openFileForWriting(this), sequences);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public class HeardComparator implements Comparator<ReferenceSequence> {

        @Override
        public int compare(ReferenceSequence o1, ReferenceSequence o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
