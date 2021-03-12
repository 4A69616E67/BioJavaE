package com.github.SnowFlakes.IO;

import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.gff.Gff3Codec;
import htsjdk.tribble.gff.Gff3Feature;
import htsjdk.tribble.gff.Gff3FeatureImpl;
import htsjdk.tribble.readers.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class GFF3ReaderExtension implements HTSReader<Gff3Feature> {
    protected AbstractFeatureReader<Gff3Feature, LineIterator> reader;
    protected Iterator<Gff3Feature> iterator;

    public GFF3ReaderExtension(File file) {
        reader = AbstractFeatureReader.getFeatureReader(file.getAbsolutePath(), null, new Gff3Codec(), false);
        try {
            iterator = reader.iterator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Gff3Feature ReadRecord() {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
