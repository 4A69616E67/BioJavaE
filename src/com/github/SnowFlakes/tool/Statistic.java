package com.github.SnowFlakes.tool;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import com.github.SnowFlakes.File.BedPeFile.BedPeFile;
import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.unit.BEDPEItem;
import com.github.SnowFlakes.unit.LinkerSequence;
import org.apache.commons.math3.distribution.*;

/**
 * Created by snowf on 2019/2/17.
 */
public class Statistic {

    public static double[] ReadsLengthDis(File FastqFile, File OutFile) throws IOException {
        ArrayList<Double> Distribution = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(FastqFile));
        String Line;
        int LineNum = 0;
        while ((Line = reader.readLine()) != null) {
            if (++LineNum % 4 == 2) {
                int len = Line.length();
                if (len + 1 > Distribution.size()) {
                    for (int i = Distribution.size(); i <= len; i++) {
                        Distribution.add(0D);
                    }
                }
                Distribution.set(len, Distribution.get(len) + 1);
            }
        }
        reader.close();
        if (OutFile != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
            writer.write("Length\tCount\n");
            for (int i = 0; i < Distribution.size(); i++) {
                writer.write(i + "\t" + Distribution.get(i) + "\n");
            }
            writer.close();
        }
        double[] dis = new double[Distribution.size()];
        for (int i = 0; i < dis.length; i++) {
            dis[i] = Distribution.get(i);
        }
        return dis;
    }

    public static double[] CalculateLinkerCount(File InFile, LinkerSequence[] LinkerList, int MinScore, int Threads)
            throws IOException, InterruptedException {
        double[] Count = new double[LinkerList.length];
        BufferedReader reader = new BufferedReader(new FileReader(InFile));
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                try {
                    String Line;
                    String[] Str;
                    while ((Line = reader.readLine()) != null) {
                        Str = Line.split("\\t+");
                        if (Integer.parseInt(Str[6]) >= MinScore) {
                            for (int j = 0; j < LinkerList.length; j++) {
                                if (Str[5].equals(LinkerList[j].getType())) {
                                    synchronized (LinkerList[j]) {
                                        Count[j]++;
                                    }
                                    break;
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            t[i].start();
        }
        reader.close();
        for (Thread aT : t) {
            aT.join();
        }
        return Count;
    }

    public static int[] CalculatorBinSize(int[] ChrSize, int Resolution) {
        int[] ChrBinSize = new int[ChrSize.length];
        for (int i = 0; i < ChrSize.length; i++) {
            ChrBinSize[i] = ChrSize[i] / Resolution + 1;
        }
        return ChrBinSize;
    }

    public static Hashtable<String, Integer> GetChromosomeSize(String InFile) throws IOException {
        BufferedReader infile = new BufferedReader(new FileReader(InFile));
        Hashtable<String, Integer> ChrSizeList = new Hashtable<>();
        String line;
        String[] str;
        if (InFile.matches(".+\\.ann")) {
            infile.readLine();
            while ((line = infile.readLine()) != null) {
                String Chr = line.split("\\s+")[1];
                line = infile.readLine();
                int Size = Integer.parseInt(line.split("\\s+")[1]);
                ChrSizeList.put(Chr, Size);
            }
        } else if (InFile.matches(".+\\.(fna|fa|fasta)")) {
            String Chr = "";
            int Size = 0;
            while ((line = infile.readLine()) != null) {
                if (line.matches("^>.+")) {
                    Chr = line.split("\\s+")[0].replace(">", "");
                    break;
                }
            }
            while ((line = infile.readLine()) != null) {
                if (line.matches("^>.+")) {
                    ChrSizeList.put(Chr, Size);
                    Chr = line.split("\\s+")[0].replace(">", "");
                    Size = 0;
                } else {
                    Size += line.length();
                }
            }
        } else if (InFile.matches(".+\\.sam")) {
            while ((line = infile.readLine()).matches("^@.+")) {
                if (line.matches("^@SQ.+")) {
                    str = line.split("\\s+");
                    ChrSizeList.put(str[1].replace("SN:", ""), Integer.parseInt(str[2].replace("LN:", "")));
                }
            }
        }
        infile.close();
        return ChrSizeList;
    }

    public static Hashtable<String, Integer> FindRestrictionSite(String FastFile, String Restriction, String Prefix)
            throws IOException {
        BufferedReader fastfile = new BufferedReader(new FileReader(FastFile));
        BufferedWriter chrwrite;
        Hashtable<String, Integer> ChrSize = new Hashtable<>();
        StringBuilder Seq = new StringBuilder();
        String line;
        String Chr = "";
        int Site = Restriction.indexOf("^");
        Restriction = Restriction.replace("^", "");
        int ResLength = Restriction.length();
        // 找到第一个以 ">" 开头的行
        while ((line = fastfile.readLine()) != null) {
            if (line.matches("^>.+")) {
                Chr = line.split("\\s+")[0].replace(">", "");
                break;
            }
        }
        while ((line = fastfile.readLine()) != null) {
            if (line.matches("^>.+")) {
                int Count = 0;
                int len = Seq.length();
                chrwrite = new BufferedWriter(new FileWriter(Prefix + "." + Chr + ".txt"));
                chrwrite.write(Count + "\t+\t" + Chr + "\t0\n");
                ChrSize.put(Chr, len);
                for (int i = 0; i <= len - ResLength; i++) {
                    if (Seq.substring(i, i + ResLength).equals(Restriction)) {
                        Count++;
                        chrwrite.write(Count + "\t+\t" + Chr + "\t" + String.valueOf(i + Site) + "\n");
                    }
                }
                chrwrite.write(++Count + "\t+\t" + Chr + "\t" + len + "\n");
                chrwrite.close();
                Seq.setLength(0);
                Chr = line.split("\\s+")[0].replace(">", "");
            } else {
                Seq.append(line);
            }
        }
        fastfile.close();
        // ========================================打印最后一条染色体=========================================
        int Count = 0;
        int len = Seq.length();
        chrwrite = new BufferedWriter(new FileWriter(Prefix + "." + Chr + ".txt"));
        chrwrite.write(Count + "\t+\t" + Chr + "\t0\n");
        ChrSize.put(Chr, len);
        for (int i = 0; i <= len - ResLength; i++) {
            if (Seq.substring(i, i + ResLength).equals(Restriction)) {
                Count++;
                chrwrite.write(Count + "\t+\t" + Chr + "\t" + String.valueOf(i + Site) + "\n");
            }
        }
        chrwrite.write(++Count + "\t+\t" + Chr + "\t" + len + "\n");
        chrwrite.close();
        Seq.setLength(0);
        return ChrSize;
    }

    public static ArrayList<long[]> PowerLaw(BedPeFile bedPeFile, int StepLength, File OutFile) throws IOException {
        ArrayList<long[]> List = new ArrayList<>();
        List.add(new long[]{0, StepLength, 0});
//        BufferedReader infile = new BufferedReader(new FileReader(bedPeFile));
        HTSReader<BEDPEItem> reader = bedPeFile.getReader();
        BEDPEItem item;
        String[] str;
        int distant;
//        byte[] index;
//        switch () {
//            case BedpePointFormat:
//                index = new byte[] { 1, 1, 3, 3 };
//                break;
//            case BedpeRegionFormat:
//                index = new byte[] { 5, 4, 2, 1 };
//                break;
//            default:
//                System.err.println("Error format!");
//                infile.close();
//                return List;
//        }
        while ((item = reader.ReadRecord()) != null) {
//            str = line.split("\\s+");
            distant = Math.abs((item.getLocate2().getEnd() + item.getLocate2().getStart() - item.getLocate1().getEnd() - item.getLocate1().getStart()) / 2);
            int i = 0;
            while (i < List.size()) {
                if (distant > List.get(i)[1]) {
                    i++;
                } else {
                    List.get(i)[2]++;
                    break;
                }
            }
            if (i == List.size()) {
                List.add(new long[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                while (List.get(i)[1] < distant) {
                    i++;
                    List.add(new long[]{List.get(i - 1)[1] + 1, List.get(i - 1)[1] + StepLength, 0});
                }
                List.get(i)[2]++;
            }
        }
        reader.close();
        if (OutFile != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
            writer.write("Distant/(M)\tCount/(log10)\n");
            for (long[] aList : List) {
                writer.write((double) aList[1] / 1000000 + "\t" + Math.log10((double) aList[2]) + "\n");
            }
            writer.close();
        }
        return List;
    }

    public static long Factorial(int k) {
        return k == 0 ? 1 : k * Factorial(k - 1);
    }

    public static void main(String[] args) {
        double s;
        int num = 7;
        PoissonDistribution p = new PoissonDistribution(4.56);
        s = p.cumulativeProbability(num);
        System.out.println(s);
    }

    public static int[] CalculateLinkerScoreDistribution(File PastFile, int MaxScore, File OutFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(PastFile));
        String Line;
        int[] Distribution = new int[MaxScore + 1];
        while ((Line = reader.readLine()) != null) {
            try {
                Distribution[Integer.parseInt(Line.split("\\s+")[6])]++;
            } catch (NumberFormatException e) {
                Distribution[0]++;
            }
        }
        reader.close();
        if (OutFile != null) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(OutFile));
            writer.write("Score\tCount\n");
            for (int i = 0; i < Distribution.length; i++) {
                writer.write(i + "\t" + Distribution[i] + "\n");
            }
            writer.close();
        }
        return Distribution;
    }

}
