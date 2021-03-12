package com.github.SnowFlakes.File.GffFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.File.FastaFile;
import com.github.SnowFlakes.IO.FastaReaderExtension;
import com.github.SnowFlakes.IO.GFF3ReaderExtension;
import com.github.SnowFlakes.IO.HTSWriter;
import com.github.SnowFlakes.tool.Tools;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.gff.Gff3Feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
//        File outfile = new File("test.out.gff3");
        FastaReaderExtension fastaReaderExtension = new FastaFile("test.fna").getReader();
        ReferenceSequence seq;
        ArrayList<ReferenceSequence> ref_list = new ArrayList<>();
        while ((seq = fastaReaderExtension.ReadRecord()) != null) {
//            seq = new ReferenceSequence(seq.getName().split("\\s+")[0],0, seq.getBases());
            ref_list.add(seq);
        }
        fastaReaderExtension.close();
        GFF3ReaderExtension reader = new GFF3File("XENTR_10.0_GCF.Chr2.gff3").getReader();
        Gff3Feature feature ;
        ArrayList<Gff3Feature> gene_list = new ArrayList<>();
        while ((feature = reader.ReadRecord()) != null) {
            if (feature.getBaseData().getType().equals("gene")){
//                gene_list.add(feature);
                for (Gff3Feature mRNA: feature.getChildren()){
                    ArrayList<byte[]> ORF_Seq=new ArrayList<>();
                    for (Gff3Feature CDS : mRNA.getChildren()){
                        if (CDS.getBaseData().getType().compareToIgnoreCase("CDS")==0){
                            ReferenceSequence cds=null;
                            for (ReferenceSequence ref : ref_list){
                                if (ref.getName().equals(CDS.getContig())){
                                    cds = Tools.GetSubSeq(ref,CDS.getBaseData());
                                    break;
                                }
                            }
                            ORF_Seq.add(cds.getBases());
//                            if(CDS.getBaseData().getStrand()== Strand.POSITIVE){
//                            }else {
//                                ORF_Seq.add(0, cds.getBases());
//                            }
                        }
                    }
                    StringBuilder Join_ORF_Seq = new StringBuilder();
                    for (byte[] orf_seq : ORF_Seq){
                        Join_ORF_Seq.append(StringUtil.bytesToString(orf_seq));
                    }
                    ReferenceSequence mRNA_Seq = new ReferenceSequence(mRNA.getName(), 0,Join_ORF_Seq.toString().getBytes());
                    System.out.println(">"+mRNA_Seq.getName()+"\n"+mRNA_Seq.getBaseString());
                }
            }
        }
        reader.close();

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
