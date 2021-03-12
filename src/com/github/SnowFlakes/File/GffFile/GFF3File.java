package com.github.SnowFlakes.File.GffFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.IO.GFF3ReaderExtension;
import com.github.SnowFlakes.IO.HTSWriter;
import htsjdk.tribble.gff.Gff3Feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/5/4.
 */

public class GFF3File extends AbstractFile<Gff3Feature> {
    public GFF3File(String pathname) {
        super(pathname);
    }

    @Override
    public GFF3ReaderExtension getReader() {
        return new GFF3ReaderExtension(this);
    }

    @Override
    public HTSWriter<Gff3Feature> getWriter() {
        return null;
    }

    @Override
    public HTSWriter<Gff3Feature> getWriter(boolean append) {
        return null;
    }

    public static void main(String[] args) throws IOException {
//        GFF3File file = new GFF3File("XENTR_10.0_GCF.Chr2.gff3");
        GFF3File file = new GFF3File("XENTR_10.0_GCF.gff3");
        File outfile = new File("test.out.gff3");
        GFF3ReaderExtension reader = file.getReader();
        Gff3Feature feature ;
        ArrayList<Gff3Feature> list = new ArrayList<>();
        while ((feature = reader.ReadRecord()) != null) {
            if (feature.getBaseData().getType().equals("gene")){
                list.add(feature);
            }
        }
        reader.close();
        System.out.println(list.size());
    }

    /**
     * @apiNote list must been sorted.
     */
//    public static Gene Search(ArrayList<Gene> list, ChrRegion item) {
//        if (list == null) {
//            return null;
//        }
//        int i = 0, j = list.size() - 1;
//        int p = 0;
//        Gene tempGene;
//        //二分法查找
//        while (i < j) {
//            p = (i + j) / 2;
//            tempGene = list.get(p);
//            if (item.IsOverlap(tempGene.GeneRegion)) {
//                return tempGene;
//            } else {
//                if (item.compareTo(tempGene.GeneRegion) > 0) {
//                    i = p + 1;
//                } else {
//                    j = p - 1;
//                }
//            }
//        }
//        p = i;
//        int MinLen = Integer.MAX_VALUE, MinIndex = p;
//        for (int k = p - 1; k <= p + 1; k++) {
//            if (k >= 0 && k < list.size()) {
//                tempGene = list.get(k);
//                if (tempGene.GeneRegion.IsOverlap(item)) {
//                    return tempGene;
//                } else {
//                    int len;
//                    if (tempGene.GeneRegion.compareTo(item) > 0) {
//                        len = tempGene.GeneRegion.region.Start - item.region.End;
//                    } else {
//                        len = item.region.Start - tempGene.GeneRegion.region.End;
//                    }
//                    if (len < MinLen) {
//                        MinLen = len;
//                        MinIndex = k;
//                    }
//                }
//            }
//        }
//        return list.get(MinIndex);
//    }
}
