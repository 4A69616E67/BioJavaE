package com.github.SnowFlakes.File.GffFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.IO.GFF3ReaderExtension;
import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.IO.HTSWriter;
import com.github.SnowFlakes.unit.ChrRegion;
import com.github.SnowFlakes.unit.Gene;
import htsjdk.tribble.gff.Gff3BaseData;
import htsjdk.tribble.gff.Gff3Feature;
import htsjdk.tribble.gff.Gff3FeatureImpl;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/5/4.
 */

public class GFF3File extends AbstractFile<Gff3FeatureImpl> {
    public GFF3File(String pathname) {
        super(pathname);
    }

    @Override
    public GFF3ReaderExtension getReader() {
        return new GFF3ReaderExtension(this);
    }

    @Override
    public HTSWriter<Gff3FeatureImpl> getWriter() {
        return null;
    }

    @Override
    public HTSWriter<Gff3FeatureImpl> getWriter(boolean append) {
        return null;
    }

    public static void main(String[] args) throws IOException {
        GFF3File<Gff3FeatureImpl> file = new GFF3File("GRCh37.p13_genomic.gff");
        Gene gene;
        file.ReadOpen();
        ArrayList<Gene> list = new ArrayList<>();
        while ((gene = file.ReadItem()) != null) {
            list.add(gene);
        }
        System.out.println(list.size());
    }



//    @Override
//    public synchronized String[] ReadItemLine() throws IOException {
//        ArrayList<String> list = new ArrayList<>();
//        String line = reader.readLine();
//        if (line == null) {
//            return null;
//        }
//        String[] columns = line.split("\\t");
//        while (line.matches("^#.*") || columns[2].compareToIgnoreCase("gene") != 0) {
//            line = reader.readLine();
//            columns = line.split("\\s+");
//        }
//        list.add(line);
//        reader.mark(1000);
//        while ((line = reader.readLine()) != null) {
//            if (line.matches("^#.*")) {
//                continue;
//            }
//            columns = line.split("\\t");
//            if (columns[2].compareToIgnoreCase("gene") == 0) {
//                reader.reset();
//                break;
//            }
//            list.add(line);
//            reader.mark(1000);
//        }
//        return list.size() > 0 ? list.toArray(new String[0]) : null;
//    }

    /**
     * @apiNote list must been sorted.
     */
    public static Gene Search(ArrayList<Gene> list, ChrRegion item) {
        if (list == null) {
            return null;
        }
        int i = 0, j = list.size() - 1;
        int p = 0;
        Gene tempGene;
        //二分法查找
        while (i < j) {
            p = (i + j) / 2;
            tempGene = list.get(p);
            if (item.IsOverlap(tempGene.GeneRegion)) {
                return tempGene;
            } else {
                if (item.compareTo(tempGene.GeneRegion) > 0) {
                    i = p + 1;
                } else {
                    j = p - 1;
                }
            }
        }
        if (i >= j) {
            p = i;
        }
        int MinLen = Integer.MAX_VALUE, MinIndex = p;
        for (int k = p - 1; k <= p + 1; k++) {
            if (k >= 0 && k < list.size()) {
                tempGene = list.get(k);
                if (tempGene.GeneRegion.IsOverlap(item)) {
                    return tempGene;
                } else {
                    int len;
                    if (tempGene.GeneRegion.compareTo(item) > 0) {
                        len = tempGene.GeneRegion.region.Start - item.region.End;
                    } else {
                        len = item.region.Start - tempGene.GeneRegion.region.End;
                    }
                    if (len < MinLen) {
                        MinLen = len;
                        MinIndex = k;
                    }
                }
            }
        }
        return list.get(MinIndex);
    }
}
