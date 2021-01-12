package com.github.SnowFlakes.Software;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.File.FastaFile.FastaFile;
import com.github.SnowFlakes.File.FastaFile.FastaItem;
import com.github.SnowFlakes.System.CommandLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    public FastaItem[] FindSimilarSequences(FastaFile file, AbstractFile<?> stat_file, float threshold)
            throws IOException, InterruptedException {
        FastaFile MsaFile = new FastaFile(file.getPath() + ".msa");
        StringBuilder SimSeq = new StringBuilder();
        ArrayList<char[]> MsaStat = new ArrayList<>();
        ArrayList<float[]> BaseFreq = new ArrayList<>();
        int[] CountArrays = new int[255];
        FastaItem[] ResItems;
        // ----------------------------------------------------------------------
        String ComLine = FullExe() + " " + file.getPath();
        PrintWriter msa = new PrintWriter(MsaFile);
        if (DeBugLevel < 1) {
            new CommandLine().run(ComLine, msa, null);
        } else {
            new CommandLine().run(ComLine, msa, new PrintWriter(System.err));
        }
        msa.close();
        MsaFile.ReadOpen();
        FastaItem item;
        while ((item = MsaFile.ReadItem()) != null) {
            MsaStat.add(item.Sequence.toString().toCharArray());
        }
        int SeqNum = MsaStat.size();
        MsaFile.ReadClose();
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
            for (char base : new char[] { 'A', 'T', 'C', 'G', '-' }) {
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
        ResItems = new FastaItem[SplitSeq.length];
        for (int i = 0; i < ResItems.length; i++) {
            ResItems[i] = new FastaItem(">seq" + i);
            ResItems[i].Sequence.append(SplitSeq[i]);
        }
        if (stat_file != null) {
            BufferedWriter writer = stat_file.WriteOpen();
            writer.write("Position\tA\tT\tC\tG\t-\n");
            for (int i = 0; i < BaseFreq.size(); i++) {
                writer.write(String.valueOf(i + 1));
                for (char base : new char[] { 'A', 'T', 'C', 'G', '-' }) {
                    writer.write("\t" + String.format("%.2f", BaseFreq.get(i)[base]));
                }
                writer.write("\n");
            }
            stat_file.WriteClose();
        }
        return ResItems;
    }
}
