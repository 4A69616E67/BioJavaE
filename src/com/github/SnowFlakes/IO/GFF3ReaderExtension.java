package com.github.SnowFlakes.IO;

import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.gff.Gff3Codec;
import htsjdk.tribble.gff.Gff3Feature;
import htsjdk.tribble.readers.LineIterator;

import java.io.File;
import java.io.IOException;

public class GFF3ReaderExtension implements HTSReader<Gff3Feature> {
    AbstractFeatureReader<Gff3Feature, LineIterator> reader;
    public GFF3ReaderExtension(File file) {
        reader = AbstractFeatureReader.getFeatureReader(file.getAbsolutePath(), null, new Gff3Codec(), false);
    }


    @Override
    public Gff3Feature ReadRecord() {
        try {
            if (reader.iterator().hasNext()) {
                return reader.iterator().next();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
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
