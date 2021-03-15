import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.File.FastaFile;
import com.github.SnowFlakes.File.GffFile.GFF3File;
import com.github.SnowFlakes.IO.FastaReaderExtension;
import com.github.SnowFlakes.IO.GFF3ReaderExtension;
import com.github.SnowFlakes.tool.Tools;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.annotation.Strand;
import htsjdk.tribble.gff.Gff3Feature;
import org.apache.commons.lang3.StringUtils;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.RNASequence;

import java.io.BufferedReader;
import java.io.File;
import java.util.*;

public class App {
    public static void main(String[] args) throws Exception {
        File genome_file = new File(args[0]);
        File anno_file = new File(args[1]);
        File snv_file = new File(args[2]);
        BedItem test_region = new BedItem("Chr2", 104659475, 104659475);
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
            }else if(feature.getType().compareToIgnoreCase("mRNA")==0 || feature.getType().compareToIgnoreCase("transcript")==0){
                if (!RefList.containsKey(feature.getContig())){
                    continue;
                }
                RNASequence rna = Tools.GetRNASeq(RefList.get(feature.getContig()),feature);
                if (rna!=null){
                    System.out.println(">"+ feature.getName());
                    System.out.println(rna.getProteinSequence().getSequenceAsString());
                }
            }
        }
        reader.close();
        System.err.println("read gff file finished");
        //----------------------------------------------------------------------------------------------------
        //read vcf file
//        BufferedReader reader1 = new BufferedReader(IOUtil.openFileForBufferedReading(snv_file));
//        String line;
//        int count = 0;
//        org.biojava.nbio.core.sequence.Strand strand;
//        while ((line = reader1.readLine()) != null) {
//            String[] strs = line.split("\\s+");
//            String[] snvs = strs[3].split(",");
//            test_region = new BedItem(strs[0], Integer.parseInt(strs[1]), Integer.parseInt(strs[2]));
//            test_region.setDescription(strs[4]);
//            for (String snv : snvs) {
//                if (snv.matches("[^ATCGatcg]")) {
//                    continue;
//                }
////                test_region.setName(snv);
//                //--------------------------------------------------------------------------------------
//                test_region.Extends = new String[5];
//                test_region.Extends[0] = "Intergenic";
//                test_region.Extends[1] = ".";
//                String ori_seq = RefList.get(test_region.getContig()).getSequenceAsString(test_region.getStart(), test_region.getEnd(), org.biojava.nbio.core.sequence.Strand.POSITIVE);
//                test_region.Extends[2] = ori_seq + "->" + snv;
//                test_region.Extends[3] = ".";
//                test_region.Extends[4] = ".";
//                //------------------------------------------------------------------------------------
//                for (Gff3Feature gff3Feature : AnnotationList.get(test_region.getContig())) {
//                    if (gff3Feature.contains(test_region)) {
//                        if(SearchMutation(RefList.get(test_region.getContig()), gff3Feature, test_region, snv)){
//                            break;
//                        }
//                    }
//                }
//                System.out.println(test_region.getContig() + "\t" + test_region.getStart() + "\t" + String.join("\t", test_region.Extends) + "\t" + test_region.getDescription());
//            }
//
//            count++;
//            if (count % 1000000 == 0) {
//                System.err.println(count / 1000000 + " Million process");
//            }
//        }
//        System.err.println("process " + count + " snv");
//        reader1.close();

    }

    public static boolean SearchMutation(DNASequence ref, Gff3Feature feature, BedItem region, String snv) throws CompoundNotFoundException {
        boolean flag = false;
        region.Extends[0] = feature.getName();
        region.Extends[1] = feature.getType();
        org.biojava.nbio.core.sequence.Strand strand;
        if (feature.getStrand() == Strand.NEGATIVE) {
            strand = org.biojava.nbio.core.sequence.Strand.NEGATIVE;
        } else {
            strand = org.biojava.nbio.core.sequence.Strand.POSITIVE;
        }
        //---------------------------------------------------------------
        String ori_seq = ref.getSequenceAsString(region.getStart(), region.getEnd(), strand);
        String new_seq = snv;
        if (strand == org.biojava.nbio.core.sequence.Strand.NEGATIVE) {
            new_seq = Tools.ReverseComplement(new_seq);
        }
        region.Extends[2] = ori_seq + "->" + new_seq;
        region.Extends[3] = ".";
        region.Extends[4] = ".";
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
                            region.Extends[1] = cds.getType();
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
                    ori_seq = rna.getSubSequence(len + 1 - l, len + 3 - l).getSequenceAsString();
                    new_seq = ori_seq;
                    if (strand == org.biojava.nbio.core.sequence.Strand.NEGATIVE) {
                        new_seq = new_seq.substring(0, l) + Tools.ReverseComplement(snv) + new_seq.substring(l + 1);
                    } else {
                        new_seq = new_seq.substring(0, l) + snv + new_seq.substring(l + 1);
                    }
                    new_seq = new_seq.replaceAll("T", "U");
                    region.Extends[2] = ori_seq + "->" + new_seq;
                    region.Extends[3] = new RNASequence(ori_seq).getProteinSequence().getSequenceAsString() + "->" + new RNASequence(new_seq).getProteinSequence().getSequenceAsString();
                    region.Extends[4] = String.valueOf(len / 3 + 1);
                    break;
                } else {
                    region.Extends[1] = m_rna.getType();
                }
            }
        }
        return flag;
    }

}



