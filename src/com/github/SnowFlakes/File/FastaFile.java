package com.github.SnowFlakes.File;


import com.github.SnowFlakes.IO.FastaReaderExtension;
import com.github.SnowFlakes.IO.FastaWriterExtension;
import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.IO.HTSWriter;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.IOUtil;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;
import org.biojava.nbio.core.sequence.io.PlainFastaHeaderParser;
import org.biojava.nbio.core.sequence.io.template.SequenceCreatorInterface;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.biojava.nbio.core.sequence.template.Compound;
import org.biojava.nbio.core.sequence.template.Sequence;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;


/**
 * Created by snowf on 2019/2/17.
 */
public class FastaFile extends AbstractFile<ReferenceSequence> {


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

    public void write(Collection<Sequence<?>> sequences) {
        try {
            FastaWriterHelper.writeSequences(IOUtil.openFileForWriting(this), sequences);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class HeardComparator implements Comparator<ReferenceSequence> {

        @Override
        public int compare(ReferenceSequence o1, ReferenceSequence o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
