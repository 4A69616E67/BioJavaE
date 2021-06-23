import com.github.SnowFlakes.File.BedFile.BedFile;
import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.File.FastaFile;
import com.github.SnowFlakes.File.GffFile.GFF3File;
import com.github.SnowFlakes.IO.BedReaderExtension;
import com.github.SnowFlakes.IO.FastaReaderExtension;
import com.github.SnowFlakes.IO.GFF3ReaderExtension;
import com.github.SnowFlakes.tool.Tools;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.gff.Gff3Feature;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.RNASequence;

import java.io.File;
import java.util.*;

public class App {
    public static void main(String[] args) throws Exception {
        File genome_file = new File(args[0]);
        File anno_file = new File(args[1]);
        File snv_file = new File(args[2]);
        FastaReaderExtension fastaReaderExtension = new FastaFile(genome_file).getReader();
        ReferenceSequence seq;
        HashMap<String, ArrayList<Gff3Feature>> AnnotationList = new HashMap<>();
        HashMap<String, DNASequence> RefList = new HashMap<>();
        while ((seq = fastaReaderExtension.ReadRecord()) != null) {
            RefList.put(seq.getName(), new DNASequence(seq.getBaseString()));
        }
        fastaReaderExtension.close();
        System.err.println("read genome file finished");
        GFF3ReaderExtension reader = new GFF3File(anno_file.getPath()).getReader();
        Gff3Feature feature;
        while ((feature = reader.ReadRecord()) != null) {
            if (feature.isTopLevelFeature()) {
                if (!AnnotationList.containsKey(feature.getContig())) {
                    AnnotationList.put(feature.getContig(), new ArrayList<>());
                }
                AnnotationList.get(feature.getContig()).add(feature);
            }
        }
        reader.close();
        System.err.println("read gff file finished");
        //----------------------------------------------------------------------------------------------------
        //read vcf file
        BedReaderExtension reader1 = new BedReaderExtension(snv_file);
        reader1.setFormat(BedFile.Format.BED6);
        reader1.setCodec(new BEDCodec(BEDCodec.StartOffset.ZERO));
        BedItem item;
        int count = 0;
        while ((item = reader1.ReadRecord()) != null) {
            for (String snv : item.Extends.get(0).split(",")) {
                if (snv.matches("[^ATCGatcg]")) {
                    continue;
                }
                //--------------------------------------------------------------------------------------
                String[] Extends = new String[7];
                Extends[0] = Extends[3] = Extends[4] = Extends[5] = ".";
                Extends[1] = "Intergenic";
                String ori_seq = RefList.get(item.getContig()).getSequenceAsString(item.getStart(), item.getEnd(), org.biojava.nbio.core.sequence.Strand.POSITIVE).toUpperCase();
                Extends[2] = ori_seq + "->" + snv;
                try {
                    Extends[6] = RefList.get(item.getContig()).getSequenceAsString(item.getStart()-10, item.getEnd()+10, org.biojava.nbio.core.sequence.Strand.POSITIVE).toUpperCase();
                }catch (IndexOutOfBoundsException e){
                    Extends[6] = ".";
                }
                item.Extends = new ArrayList<>(Arrays.asList(Extends));
                //------------------------------------------------------------------------------------
                for (Gff3Feature gff3Feature : AnnotationList.get(item.getContig())) {
                    if (gff3Feature.contains(item)) {
                        if (SearchMutationSNV(RefList.get(item.getContig()), gff3Feature, item, snv)) {
                            break;
                        }
                    }
                }
                System.out.println(item.getContig() + "\t" + item.getStart() + "\t" + item.getEnd() + "\t" + item.getName() + "\t" + item.getScore() + "\t" + item.getStrand() + "\t" + String.join("\t", item.Extends));
            }

            count++;
            if (count % 1000000 == 0) {
                System.err.println(count / 1000000 + " Million process");
            }
        }
        System.err.println("process " + count + " snv");
        reader1.close();

    }

    //查找变异在基因组的情况，包括SNV和Indel（Indel还未完成）
    public static boolean SearchMutationSNV(DNASequence ref, Gff3Feature feature, BedItem region, String snv) throws CompoundNotFoundException {
        boolean flag = false;
        String ori_seq;
        String new_seq;
        region.Extends.set(0, feature.getName());
        region.Extends.set(1, feature.getType());
        if (feature.getStrand() == Strand.NEGATIVE) {
            ori_seq = ref.getSequenceAsString(region.getStart(), region.getEnd(), org.biojava.nbio.core.sequence.Strand.NEGATIVE).toUpperCase();
            new_seq = Tools.ReverseComplement(snv);
            region.Extends.set(2,ori_seq + "->" + new_seq);
            region.setStrand(Strand.NEGATIVE);
            try {
                region.Extends.set(6,ref.getSequenceAsString(region.getStart()-10, region.getEnd()+10, org.biojava.nbio.core.sequence.Strand.NEGATIVE).toUpperCase());
            }catch (IndexOutOfBoundsException ignored){

            }
        }
        //---------------------------------------------------------------
        //----------------------------------------------------------------
        for (Gff3Feature m_rna : feature.getChildren()) {
            if (m_rna.contains(region)) {
                int len = 0;
                for (Gff3Feature cds : m_rna.getChildren()) {
                    if (cds.getType().compareToIgnoreCase("CDS") == 0) {
                        if (cds.contains(region)) {
                            if (cds.getStrand() == Strand.FORWARD) {
                                len += region.getStart() - cds.getStart();
                            } else {
                                len += cds.getEnd() - region.getEnd();
                            }
                            region.Extends.set(1,cds.getType());
                            flag = true;
                            break;
                        } else {
                            len += cds.getLengthOnReference();
                        }
                    }
                }
                if (flag) {
                    RNASequence rna = Tools.GetRNASeq(ref, m_rna);
                    int l = len % 3;
                    ori_seq = rna.getSubSequence(len + 1 - l, len + 3 - l).getSequenceAsString().toUpperCase();
                    if (m_rna.getStrand() == Strand.NEGATIVE) {
                        new_seq = ori_seq.substring(0, l) + Tools.ReverseComplement(snv) + ori_seq.substring(l + 1);
                    } else {
                        new_seq = ori_seq.substring(0, l) + snv + ori_seq.substring(l + 1);
                    }
                    new_seq = new_seq.replaceAll("T", "U");
                    region.Extends.set(3,ori_seq + "->" + new_seq);
                    region.Extends.set(4, new RNASequence(ori_seq).getProteinSequence().getSequenceAsString() + "->" + new RNASequence(new_seq).getProteinSequence().getSequenceAsString());
                    region.Extends.set(5,  String.valueOf(len / 3 + 1));
                    break;
                } else {
                    region.Extends.set(1,m_rna.getType());
                }
            }
        }
        return flag;
    }

    public static boolean SearchMutationIndel(DNASequence ref, Gff3Feature feature, BedItem region, String snv) throws CompoundNotFoundException {
        boolean flag = false;
        String ori_seq;
        String new_seq;
        region.Extends.set(0, feature.getName());
        region.Extends.set(1, feature.getType());
        if (feature.getStrand() == Strand.NEGATIVE) {
            ori_seq = ref.getSequenceAsString(region.getStart(), region.getEnd(), org.biojava.nbio.core.sequence.Strand.NEGATIVE).toUpperCase();
            new_seq = Tools.ReverseComplement(snv);
            region.Extends.set(2,ori_seq + "->" + new_seq);
            region.setStrand(Strand.NEGATIVE);
            try {
                region.Extends.set(6,ref.getSequenceAsString(region.getStart()-10, region.getEnd()+10, org.biojava.nbio.core.sequence.Strand.NEGATIVE).toUpperCase());
            }catch (IndexOutOfBoundsException ignored){

            }
        }
        //---------------------------------------------------------------
        //----------------------------------------------------------------
        for (Gff3Feature m_rna : feature.getChildren()) {
            if (m_rna.contains(region)) {
                int len = 0;
                for (Gff3Feature cds : m_rna.getChildren()) {
                    if (cds.getType().compareToIgnoreCase("CDS") == 0) {
                        if (cds.contains(region)) {
                            if (cds.getStrand() == Strand.FORWARD) {
                                len += region.getStart() - cds.getStart();
                            } else {
                                len += cds.getEnd() - region.getEnd();
                            }
                            region.Extends.set(1,cds.getType());
                            flag = true;
                            break;
                        } else {
                            len += cds.getLengthOnReference();
                        }
                    }
                }
                if (flag) {
                    RNASequence rna = Tools.GetRNASeq(ref, m_rna);
                    int l = len % 3;
                    ori_seq = rna.getSubSequence(len + 1 - l, len + 3 - l).getSequenceAsString().toUpperCase();
                    if (m_rna.getStrand() == Strand.NEGATIVE) {
                        new_seq = ori_seq.substring(0, l) + Tools.ReverseComplement(snv) + ori_seq.substring(l + 1);
                    } else {
                        new_seq = ori_seq.substring(0, l) + snv + ori_seq.substring(l + 1);
                    }
                    new_seq = new_seq.replaceAll("T", "U");
                    region.Extends.set(3,ori_seq + "->" + new_seq);
                    region.Extends.set(4, new RNASequence(ori_seq).getProteinSequence().getSequenceAsString() + "->" + new RNASequence(new_seq).getProteinSequence().getSequenceAsString());
                    region.Extends.set(5,  String.valueOf(len / 3 + 1));
                    break;
                } else {
                    region.Extends.set(1,m_rna.getType());
                }
            }
        }
        return flag;
    }

}



