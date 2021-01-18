package com.github.SnowFlakes.Software;


import com.github.SnowFlakes.File.FastaFile;

import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.System.CommandLine;
import htsjdk.samtools.reference.ReferenceSequence;


import java.io.*;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/6/14.
 */

public class MAFFT extends AbstractSoftware {

    public int DeBugLevel = 0;

    public MAFFT(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {
        if (Execution.trim().equals("")) {
            System.err.println("[mafft]\tNo execute file");
        } else {
            if (!Path.isDirectory()) {
                FindPath();
            }
            getVersion();
        }
    }

    @Override
    protected String getVersion() {
        try {
            StringWriter buffer = new StringWriter();
            new CommandLine().run(FullExe() + " --version", null, new PrintWriter(buffer));
            Version = buffer.toString().split("\\n")[0];
        } catch (IOException | InterruptedException e) {
            Valid = false;
        }
        return Version;
    }

    public ReferenceSequence[] FindSimilarSequences(FastaFile file, File stat_file, float threshold) throws IOException, InterruptedException {
        FastaFile MsaFile = new FastaFile(file);
        StringBuilder SimSeq = new StringBuilder();
        ArrayList<char[]> MsaStat = new ArrayList<>();
        ArrayList<float[]> BaseFreq = new ArrayList<>();
        int[] CountArrays = new int[255];
        ReferenceSequence[] ResItems;
        // ----------------------------------------------------------------------
        String ComLine = FullExe() + " " + file.getPath();
        PrintWriter msa = new PrintWriter(MsaFile);
        if (DeBugLevel < 1) {
            new CommandLine().run(ComLine, msa, null);
        } else {
            new CommandLine().run(ComLine, msa, new PrintWriter(System.err));
        }
        msa.close();
        HTSReader<ReferenceSequence> reader = MsaFile.getReader();
        ReferenceSequence item;
        while ((item = reader.ReadRecord()) != null) {
            MsaStat.add(item.getBaseString().toCharArray());
        }
        int SeqNum = MsaStat.size();
        reader.close();
        for (int i = 0; i < MsaStat.get(0).length; i++) {
            CountArrays['A'] = 0;
            CountArrays['T'] = 0;
            CountArrays['C'] = 0;
            CountArrays['G'] = 0;
            CountArrays['-'] = 0;
            for (char[] aMsaStat : MsaStat) {
                CountArrays[Character.toUpperCase(aMsaStat[i])]++;
            }
            int MaxValue = 0;
            char MaxBase = '-';
            BaseFreq.add(new float[255]);
            for (char base : new char[]{'A', 'T', 'C', 'G', '-'}) {
                BaseFreq.get(i)[base] = (float) CountArrays[base] / SeqNum;
                if (CountArrays[base] > MaxValue) {
                    MaxValue = CountArrays[base];
                    MaxBase = base;
                }
            }
            if (MaxValue > SeqNum * threshold) {
                SimSeq.append(MaxBase);
            } else {
                SimSeq.append('N');
            }
        }
        String[] SplitSeq = SimSeq.toString().replace("-", "").split("N+");
        ResItems = new ReferenceSequence[SplitSeq.length];
        for (int i = 0; i < ResItems.length; i++) {
            ResItems[i] = new ReferenceSequence(">seq" + i, i, SplitSeq[i].getBytes());
        }
        if (stat_file != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(stat_file));
            writer.write("Position\tA\tT\tC\tG\t-\n");
            for (int i = 0; i < BaseFreq.size(); i++) {
                writer.write(String.valueOf(i + 1));
                for (char base : new char[]{'A', 'T', 'C', 'G', '-'}) {
                    writer.write("\t" + String.format("%.2f", BaseFreq.get(i)[base]));
                }
                writer.write("\n");
            }
            writer.close();
        }
        return ResItems;
    }
}
