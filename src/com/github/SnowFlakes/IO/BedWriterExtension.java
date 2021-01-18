package com.github.SnowFlakes.IO;

import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.File.BedFile.BedFile;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.FullBEDFeature;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;


public class BedWriterExtension extends BufferedWriter implements HTSWriter<BedItem> {
    BedFile.Format format = BedFile.Format.BED6;

    public BedWriterExtension(Writer out) {
        super(out);
    }

    public BedWriterExtension(Writer out, int sz) {
        super(out, sz);
    }

    @Override
    public synchronized void WriterRecord(BedItem o) {
        StringBuilder out_string = new StringBuilder(o.getContig() + "\t" + o.getStart() + "\t" + o.getEnd() + "\t" + o.getName() + "\t" + o.getScore() + "\t" + o.getStrand());
        if (format == BedFile.Format.BED12) {
            if (o.getExons().size() > 0) {
                FullBEDFeature.Exon exon = o.getExons().get(0);
                out_string.append("\t").append(exon.getCdStart() - BEDCodec.StartOffset.ONE.value()).append("\t").append(exon.getCdEnd());
            } else {
                out_string.append("\t").append(o.getStart()).append("\t").append(o.getEnd());
            }

            if (o.getColor() != null) {
                Color c = o.getColor();
                out_string.append("\t").append(c.getRed()).append(",").append(c.getGreen()).append(",").append(c.getBlue());
            } else {
                out_string.append("\t255,255,255");
            }

//        if (o.getExons().size() > 0) {
//            out_string.append("\t" + o.getExons().size());
//            String[] exon_sizes = new String[o.getExons().size()];
//            String[] starts_buffer = new String[o.getExons().size()];
//            for (int i = 0; i < o.getExons().size(); i++) {
//                FullBEDFeature.Exon exon = o.getExons().get(i);
//                exon_sizes[i]=exon.
//            }
//        }
        }

        try {
            write(out_string.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void WriterRecordln(BedItem o) {
        WriterRecord(o);
        try {
            write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFormat(BedFile.Format format) {
        this.format = format;
    }
}
