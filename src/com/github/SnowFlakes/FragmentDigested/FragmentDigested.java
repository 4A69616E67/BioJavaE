package com.github.SnowFlakes.FragmentDigested;

import com.github.SnowFlakes.File.BedFile.BedFile;
import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.File.CommonFile.CommonFile;
import com.github.SnowFlakes.File.FastaFile;
import com.github.SnowFlakes.IO.BedWriterExtension;
import com.github.SnowFlakes.IO.FastaReaderExtension;
import com.github.SnowFlakes.IO.FastqReaderExtension;
import com.github.SnowFlakes.IO.HTSWriter;
import com.github.SnowFlakes.tool.Tools;
import com.github.SnowFlakes.unit.ChrRegion;
import com.github.SnowFlakes.unit.Chromosome;
import com.github.SnowFlakes.unit.ThreadIndex;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.compress.utils.ByteUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by snowf on 2019/5/27.
 */

public class FragmentDigested {
    private File OutDir;
    private Chromosome[] Chrs;
    private RestrictionEnzyme Enzyme;
    private String Prefix;
    private BedFile[] ChrsFragmentFile;
    private BedFile AllChrsFragmentFile;
    private CommonFile ChrSizeFile;
    public int Threads = 1;

    public FragmentDigested(File outDir, Chromosome[] chrs, RestrictionEnzyme enzyme, String prefix) {
        OutDir = outDir;
        Chrs = chrs;
        Enzyme = enzyme;
        Prefix = prefix;
        CreateFile();
    }

    public void run(FastaFile genomeFile) throws IOException {
        System.out.println(new Date() + "\tCreate restriction fragment");
        if (!OutDir.isDirectory() && !OutDir.mkdir()) {
            System.err.println(new Date() + "\tCreate " + OutDir + " false !");
        }
        ArrayList<ReferenceSequence> GenomeList = new ArrayList<>();
        //--------------------------------------------------
        FastaReaderExtension reader = genomeFile.getReader();
        ReferenceSequence item;
        while ((item = reader.ReadRecord()) != null) {
            int chrIndex = ContainChromosome(item.getName());
            if (chrIndex != -1) {
                GenomeList.add(item);
                Chrs[chrIndex].Size = item.length();
            }
        }
        reader.close();
        //------------------------------------------------
        Thread[] t = new Thread[Threads];
        ThreadIndex Index = new ThreadIndex(-1);
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                while (true) {
                    int index = Index.Add(1);
                    if (index >= GenomeList.size()) {
                        break;
                    }
                    int chrIndex = ContainChromosome(GenomeList.get(index).getName());
                    if (chrIndex != -1) {
                        ArrayList<BedItem> FragmentList = FindFragment(GenomeList.get(index), Enzyme);
                        BedWriterExtension writer = ChrsFragmentFile[chrIndex].getWriter();
                        for (BedItem frag : FragmentList) {
                            writer.WriterRecordln(frag);
                        }
                        writer.close();
                    }
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        //--------------------------------------------------
        HTSWriter<String> writer = ChrSizeFile.getWriter();
        for (int i = 0; i < ChrsFragmentFile.length; i++) {
            if (!ChrsFragmentFile[i].exists()) {
                System.err.println(new Date() + "\t[FindRestrictionFragment]\tWarning! No " + Chrs[i].Name + " in genomic file");
                ChrsFragmentFile[i].createNewFile();
            }
            writer.WriterRecord(Chrs[i].Name + "\t" + Chrs[i].Size + "\n");
        }
        writer.close();
        AllChrsFragmentFile.Merge(ChrsFragmentFile);
        System.out.println(new Date() + "\tCreate restriction fragment finished");
        //-------------------
    }

    private void CreateFile() {
        ChrsFragmentFile = new BedFile[Chrs.length];
        for (int i = 0; i < ChrsFragmentFile.length; i++) {
            ChrsFragmentFile[i] = new BedFile(OutDir + "/" + Prefix + "." + Enzyme.getSequence() + "." + Chrs[i].Name + ".bed");
        }
        AllChrsFragmentFile = new BedFile(OutDir + "/" + Prefix + "." + Enzyme.getSequence() + ".all.bed");
        ChrSizeFile = new CommonFile(OutDir + "/" + Prefix + "." + Enzyme.getSequence() + ".ChrSize.bed");
    }

    public BedFile[] getChrsFragmentFile() {
        CreateFile();
        return ChrsFragmentFile;
    }

    public BedFile getAllChrsFragmentFile() {
        CreateFile();
        return AllChrsFragmentFile;
    }

    public Chromosome[] getChromosomes() {
        return Chrs;
    }

    public CommonFile getChrSizeFile() {
        return ChrSizeFile;
    }

    private int ContainChromosome(String s) {
        for (int i = 0; i < Chrs.length; i++) {
            if (s.equals(Chrs[i].Name)) {
                return i;
            }
        }
        return -1;
    }

    private ArrayList<BedItem> FindFragment(ReferenceSequence refSeq, RestrictionEnzyme enzymeSeq) {
        String EnzySeq = enzymeSeq.getSequence();
        if (EnzySeq.length() < 1) {
            System.err.println("Null enzyme sequence!");
            System.exit(1);
        }
        Chromosome Chr = new Chromosome(refSeq.getName(), refSeq.length());
        ArrayList<BedItem> List = new ArrayList<>();
        int Count = 1;
        BedItem item = new BedItem(Chr.Name, 1,0);
        item.setScore(Count);
        item.setDescription("fragment" + Count);
        List.add(item);
        String seq = refSeq.getBaseString();
        for (int i = 0; i < Chr.Size - EnzySeq.length() + 1; i++) {
            if (seq.substring(i, i + EnzySeq.length()).compareToIgnoreCase(EnzySeq) == 0) {
                int EndIndex = i + enzymeSeq.getCutSite();
                if (EndIndex != 0) {
                    List.get(List.size() - 1).setEnd(EndIndex);
                }
                if (EndIndex < Chr.Size) {
                    Count++;
                    item = new BedItem(Chr.Name, 1,0);
                    item.setScore(Count);
                    item.setDescription("fragment" + Count);
                    List.add(item);
                }
            }
        }
        if (List.get(List.size() - 1).getEnd() == 0) {
            List.get(List.size() - 1).setEnd(Chr.Size);
        }
        return List;
    }
}
