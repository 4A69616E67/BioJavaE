package com.github.SnowFlakes.tool;

import com.github.SnowFlakes.File.FastqFile;
import com.github.SnowFlakes.FragmentDigested.RestrictionEnzyme;
import com.github.SnowFlakes.Sequence.KmerStructure;
import com.github.SnowFlakes.Statistic.StatUtil;
import com.github.SnowFlakes.unit.*;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.cli.*;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.util.SequenceTools;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by snowf on 2019/2/17.
 */
public class LinkerDetection {
    public static RestrictionEnzyme Enzyme;

    public static void main(String[] args) throws IOException, ParseException {

        // ==============================================================================================================
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().desc("input file").required().build());
        Argument.addOption(Option.builder("p").hasArg().desc("prefix").build());
        Argument.addOption(Option.builder("s").hasArg().desc("cutoff start index (default 0)").build());
        Argument.addOption(Option.builder("t").hasArg()
                .desc("cutoff terminal index (default 70, if you want to remain full reads, please set a large number)")
                .build());
        Argument.addOption(
                Option.builder("n").hasArg().desc("sequence number use to processing (default 5000)").build());
        Argument.addOption(Option.builder("e").hasArg().desc(
                "restriction enzyme seq (example A^AGCTT or T^TAA. if you needn't enzyme, set this option \"no\")")
                .build());
        Argument.addOption(Option.builder("k").hasArg().desc("k-mer length (default 10)").build());
        Argument.addOption(Option.builder("f").hasArg().desc("threshold (default 0.05)").build());
        if (args.length == 0) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + LinkerDetection.class.getName(),
                    Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        FastqFile InPutFile = new FastqFile(Parameter.GetFileOpt(ComLine, "i", null));
        String Prefix = Parameter.GetStringOpt(ComLine, "p", "out");
        int Index1 = Parameter.GetIntOpt(ComLine, "s", 0);
        int Index2 = Parameter.GetIntOpt(ComLine, "t", 70);
        int SeqNum = Parameter.GetIntOpt(ComLine, "n", 5000);
        int KmerLen = Parameter.GetIntOpt(ComLine, "k", 10);
        float Threshold = Parameter.GetFloatOpt(ComLine, "f", 0.05f);
        RestrictionEnzyme enzyme = Parameter.GetStringOpt(ComLine, "e", null) == null ? null
                : new RestrictionEnzyme(Parameter.GetStringOpt(ComLine, "e", null));
        // --------------------------------------------------------------------------------------------------------------
        ArrayList<ReferenceSequence> result = run(InPutFile, new File(Prefix), Index1, Index2, SeqNum, enzyme, KmerLen, Threshold);
        for (ReferenceSequence d : result) {
            System.out.println(d.getBaseString() + "\t" + d.getContigIndex());
        }
    }

    public static ArrayList<ReferenceSequence> run(FastqFile InPutFile, File prefix, int start, int end, int seqNum, RestrictionEnzyme enzyme, int k_merLen, float threshold) throws IOException {
        ArrayList<ReferenceSequence> linkers = LinkerDetection.SimilarSeqDetection(InPutFile, new File("test"), start, end, seqNum, k_merLen, threshold);
        if (enzyme == null) {
            // find out restriction enzyme
            int[] Count = new int[RestrictionEnzyme.list.length];
            for (ReferenceSequence linker : linkers) {
                int minPosition = 1000;
                int minIndex = 0;
                boolean flag = false;
                for (int j = 0; j < RestrictionEnzyme.list.length; j++) {
                    String subEnzyme1 = RestrictionEnzyme.list[j].getSequence().substring(0, Math.max(
                            RestrictionEnzyme.list[j].getCutSite(),
                            RestrictionEnzyme.list[j].getSequence().length() - RestrictionEnzyme.list[j].getCutSite()));
                    int position = linker.getBaseString().indexOf(subEnzyme1);
                    if (position >= 0 && position <= 3 && position < minPosition) {
                        minPosition = position;
                        minIndex = j;
                        flag = true;
                    }
                }
                if (flag) {
                    Count[minIndex]++;
                }
            }
            int maxIndex = StatUtil.maxIndex(Count);
            if (Count[maxIndex] >= linkers.size() / 2) {
                enzyme = RestrictionEnzyme.list[maxIndex];
                Enzyme = enzyme;
            }
        } else if (enzyme.getSequence().compareToIgnoreCase("no") == 0) {
            enzyme = null;
        }
        if (enzyme == null) {
            System.out.println("Unknown enzyme");
        } else {
            System.out.println(enzyme);
        }
        // 修剪
        if (enzyme != null) {
            for (int i = 0; i < linkers.size(); i++) {
                String subEnzyme1 = enzyme.getSequence().substring(0, Math.max(enzyme.getCutSite(), enzyme.getSequence().length() - enzyme.getCutSite()));
                String subEnzyme2 = enzyme.getSequence().substring(Math.min(enzyme.getCutSite(), enzyme.getSequence().length() - enzyme.getCutSite()));
                int index1, index2;
                index1 = linkers.get(i).getBaseString().indexOf(subEnzyme1);
                index2 = linkers.get(i).getBaseString().lastIndexOf(subEnzyme2);
                if (index1 >= 0 && index2 >= 0 && index1 + subEnzyme1.length() < index2 && index1 <= 3 && linkers.get(i).length() - index2 - subEnzyme2.length() <= 3) {
                    ReferenceSequence s = new ReferenceSequence("", linkers.get(i).getContigIndex(), linkers.get(i).getBaseString().substring(index1 + subEnzyme1.length(), index2).getBytes());
                    if (Tools.ReverseComple(s.getBaseString()).equals(s.getBaseString())) {
                        linkers.set(i, s);
                    } else {
                        linkers.remove(i);
                        i--;
                    }
                } else {
                    linkers.remove(i);
                    i--;
                }
            }
        }
        // 去重
        Hashtable<String, Double> final_linkers = new Hashtable<>();
        for (ReferenceSequence d : linkers) {
            if (!final_linkers.contains(d.getBaseString())) {
                final_linkers.put(d.getBaseString(), (double) d.getContigIndex());
            } else {
                final_linkers.put(d.getBaseString(), final_linkers.get(d.getBaseString()) + d.getContigIndex());
            }
        }
        linkers.clear();
        for (String s : final_linkers.keySet()) {
            linkers.add(new ReferenceSequence("", final_linkers.get(s).intValue(), s.getBytes()));
        }
        return linkers;
    }

    public static ArrayList<ReferenceSequence> SimilarSeqDetection(FastqFile input_file, File prefix, int start, int end,
                                                                   int SeqNum, int k_merLen, float threshold) throws IOException {
        start = Math.max(start, 0);
        end = Math.max(end, start);
        SeqNum = SeqNum == 0 ? 5000 : SeqNum;
        k_merLen = k_merLen == 0 ? 10 : k_merLen;
        threshold = threshold == 0 ? 0.05f : threshold;
        ArrayList<FastqRecord> list = input_file.Extraction(SeqNum);
        for (FastqRecord item : list) {
            item = new FastqRecord(item.getReadName(), item.getReadString().substring(start, Math.min(end, item.getReadString().length())), item.getBaseQualityHeader(), item.getBaseQualityString().substring(start, Math.min(end, item.getReadString().length())));
        }
        ArrayList<KmerStructure> ValidKmerList = GetValidKmer(list, k_merLen, threshold * SeqNum);
        ArrayList<KmerStructure> assembly_list = Assembly(ValidKmerList);
        ArrayList<ReferenceSequence> final_assembly_list = AssemblyShow(assembly_list);
        return final_assembly_list;
    }

    private static ArrayList<ReferenceSequence> AssemblyShow(ArrayList<KmerStructure> input) {
        ArrayList<ReferenceSequence> result = new ArrayList<>();
        if (input == null || input.size() == 0) {
            result.add(new ReferenceSequence("", 0, new byte[0]));
        } else {
            for (int i = 0; i < input.size(); i++) {
                if (input.get(i).Visited) {
                    result.add(new ReferenceSequence("", i, new byte[0]));
                    continue;
                }
                input.get(i).Visited = true;
                ArrayList<ReferenceSequence> next_seq = AssemblyShow(input.get(i).next);
                for (ReferenceSequence s : next_seq) {
                    if (s.length() == 0) {
                        result.add(input.get(i).Seq);
                    } else {
                        result.add(new ReferenceSequence("", Math.min(input.get(i).Seq.getContigIndex(), s.getContigIndex()), new String(input.get(i).Seq.getBaseString() + s.getBaseString().substring(input.get(i).Seq.getBaseString().length() - 1)).getBytes()));
                    }
                }
                input.get(i).Visited = false;
            }
        }
        return result;
    }

    private static ArrayList<KmerStructure> Assembly(ArrayList<KmerStructure> origin) {
        ArrayList<KmerStructure> temp_list = new ArrayList<>(origin);
        ArrayList<KmerStructure> assembly_list = new ArrayList<>();
        ArrayList<String[]> subList = new ArrayList<>();
        for (KmerStructure kmerStructure : temp_list) {
            String s = kmerStructure.Seq.getBaseString();
            subList.add(new String[]{s.substring(0, s.length() - 1), s.substring(1)});
        }
        for (int i = 0; i < temp_list.size(); i++) {
            String[] sub1 = subList.get(i);
            for (int j = i; j < temp_list.size(); j++) {
                String[] sub2 = subList.get(j);
                if (sub1[0].equals(sub2[1])) {
                    temp_list.get(i).last.add(temp_list.get(j));
                    temp_list.get(j).next.add(temp_list.get(i));
                } else if (sub1[1].equals(sub2[0])) {
                    temp_list.get(i).next.add(temp_list.get(j));
                    temp_list.get(j).last.add(temp_list.get(i));
                }
            }
        }
        for (int i = 0; i < temp_list.size(); i++) {
            if (temp_list.get(i).last.size() == 0) {
                assembly_list.add(temp_list.get(i));
            }
        }
        return assembly_list;
    }

    private static ArrayList<KmerStructure> GetValidKmer(ArrayList<FastqRecord> list, int k, float threshold) {
        HashMap<String, int[]> KmerMap = new HashMap<>();
        for (FastqRecord item : list) {
            String[] kmer = Tools.GetKmer(item.getReadString(), k);
            for (String s : kmer) {
                if (!KmerMap.containsKey(s)) {
                    KmerMap.put(s, new int[]{0});
                }
                KmerMap.get(s)[0]++;
            }
        }
        int i = 0;
        ArrayList<KmerStructure> result = new ArrayList<>();
        for (String s : KmerMap.keySet()) {
            if (KmerMap.get(s)[0] > threshold) {
                result.add(new KmerStructure(new ReferenceSequence("Seq" + i, KmerMap.get(s)[0], s.getBytes())));
                i++;
            }
        }
        KmerMap.clear();
        return result;
    }
}

class DNASeq extends ReferenceSequence {

    /**
     * creates a fully formed ReferenceSequence
     *
     * @param name  the name of the sequence from the source file
     * @param index the zero based index of this contig in the source file
     * @param bases the bases themselves stored as one-byte characters
     */
    public DNASeq(String name, int index, byte[] bases) {
        super(name, index, bases);
    }
}