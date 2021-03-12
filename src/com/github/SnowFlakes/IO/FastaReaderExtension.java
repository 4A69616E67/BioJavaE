package com.github.SnowFlakes.IO;

import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;

import java.io.File;

public class FastaReaderExtension implements HTSReader<ReferenceSequence> {
    private FastaSequenceFile file;

    public FastaReaderExtension(File file) {
        this.file = new FastaSequenceFile(file, true);
    }

    @Override
    public synchronized ReferenceSequence ReadRecord() {
        return file.nextSequence();
    }

    @Override
    public void close() {
        file.close();
    }
}
