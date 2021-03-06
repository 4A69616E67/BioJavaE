package com.github.SnowFlakes.Utils;

import com.github.SnowFlakes.File.BedPeFile.BedPeFile;
import com.github.SnowFlakes.File.MatrixFile.MatrixFile;
import com.github.SnowFlakes.tool.Statistic;
import com.github.SnowFlakes.tool.Tools;
import com.github.SnowFlakes.unit.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public class CreateMatrix {
    private BedPeFile BedpeFile;
    private Chromosome[] Chromosomes;
    private ChrRegion Region1, Region2;
    private int Resolution = 100000;
    private String Prefix;
    private MatrixFile DenseMatrixFile, SpareMatrixFile;
    private File RegionFile;
    private File BinSizeFile;
    private int Threads;
    private boolean UseCount = false;

    private double MaxBinNum = 1e6;

    public CreateMatrix(BedPeFile BedpeFile, Chromosome[] Chrs, int Resolution, String Prefix, int Threads) {
        this.BedpeFile = BedpeFile;
        this.Chromosomes = Chrs;
        this.Resolution = Resolution;
        this.Prefix = Prefix;
        this.Threads = Threads;
        Init();
    }

    private CreateMatrix(String[] args) throws IOException {
        Options Argument = new Options();
        Argument.addOption(
                Option.builder("f").hasArg().argName("file").required().desc("[required] bedpefile").build());
        Argument.addOption(
                Option.builder("s").hasArg().longOpt("size").argName("file").desc("Chromosomes size file").build());
        Argument.addOption(Option.builder("chr").hasArgs().argName("strings")
                .desc("The chromosome name which you want to calculator").build());
        Argument.addOption(Option.builder("res").hasArg().argName("int").desc("Resolution (default 1M)").build());
        Argument.addOption(Option.builder("region").hasArgs().argName("strings").desc(
                "(sample chr1:0:100 chr4:100:400) region you want to calculator, if not set, will calculator chromosome size")
                .build());
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("Threads (default 1)").build());
        Argument.addOption(
                Option.builder("p").hasArg().argName("string").desc("out prefix (default bedpefile)").build());
        Argument.addOption(Option.builder("count").hasArg(false).desc("use count value").build());
        final String helpHeader = "Author: " + Opts.Author;
        final String helpFooter = "Note:\n"
                + "you can set -chr like \"Chr:ChrSize\" or use -s to define the \"ChrSize\"\n"
                + "If you set -s, you can set -chr like \"Chr\"\n"
                + "The file format of option -s is \"Chromosomes    Size\" for each row\n"
                + "We will calculate all chromosome in Chromosomes size file if you don't set -chr\n"
                + "You needn't set -s and -chr if you set -region";
        if (args.length == 0) {
            new HelpFormatter().printHelp(
                    "java -cp Path/" + Opts.JarFile.getName() + " " + CreateMatrix.class.getName(), helpHeader,
                    Argument, helpFooter, true);
            System.exit(1);
        }
        CommandLine ComLine = null;
        try {
            ComLine = new DefaultParser().parse(Argument, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp(
                    "java -cp Path/" + Opts.JarFile.getName() + " " + CreateMatrix.class.getName(), helpHeader,
                    Argument, helpFooter, true);
            System.exit(1);
        }
        BedpeFile = new BedPeFile(ComLine.getOptionValue("f"));
        String[] Chr = ComLine.hasOption("chr") ? ComLine.getOptionValues("chr") : null;
        if (Chr != null) {
            Chromosomes = new Chromosome[Chr.length];
            for (int i = 0; i < Chr.length; i++) {
                Chromosomes[i] = new Chromosome(Chr[i].split(":"));
            }
        }
        String SizeFile = ComLine.hasOption("size") ? ComLine.getOptionValue("size") : null;
        Resolution = ComLine.hasOption("res") ? Integer.parseInt(ComLine.getOptionValue("res")) : Resolution;
        Prefix = ComLine.hasOption("p") ? ComLine.getOptionValue("p") : BedpeFile.getPath();
        Threads = ComLine.hasOption("t") ? Integer.parseInt(ComLine.getOptionValue("t")) : 1;
        Region1 = ComLine.hasOption("region") ? new ChrRegion(ComLine.getOptionValue("region").split(":")) : null;
        Region2 = ComLine.hasOption("region") && ComLine.getOptionValues("region").length > 1
                ? new ChrRegion(ComLine.getOptionValues("region")[1].split(":"))
                : Region1;
        UseCount = ComLine.hasOption("count");
        if (SizeFile != null) {
            List<String> ChrSizeList = FileUtils.readLines(new File(SizeFile), StandardCharsets.UTF_8);
            if (Chromosomes == null) {
                Chromosomes = new Chromosome[ChrSizeList.size()];
                for (int i = 0; i < Chromosomes.length; i++) {
                    Chromosomes[i] = new Chromosome(ChrSizeList.get(i).split("\\s+"));
                }
            } else {
                for (String aChrSizeList : ChrSizeList) {
                    for (Chromosome aChromosome : Chromosomes) {
                        if (aChromosome.Name.equals(aChrSizeList.split("\\s+")[0])) {
                            aChromosome.Size = Integer.parseInt(aChrSizeList.split("\\s+")[1]);
                            break;
                        }
                    }
                }
            }
        }
        Init();
    }

    private void Init() {
        DenseMatrixFile = new MatrixFile(Prefix + ".dense.matrix");
        SpareMatrixFile = new MatrixFile(Prefix + ".spare.matrix");
        RegionFile = new File(Prefix + ".matrix.Region");
        BinSizeFile = new File(Prefix + ".matrix.BinSize");
    }

    public static void main(String[] args) throws IOException {

        new CreateMatrix(args).Run();

    }

    public Array2DRowRealMatrix Run() throws IOException {
        if (Region1 != null) {
            return Run(Region1, Region2);
        }
        if (Chromosomes == null) {
            System.err.println("Error! no -chr  argument");
            System.exit(1);
        }
        int[] ChrSize = new int[Chromosomes.length];
        System.out.println(new Date() + "\tBegin to create interaction matrix " + BedpeFile.getName() + " Resolution="
                + Resolution + " Threads=" + Threads);
        for (int i = 0; i < Chromosomes.length; i++) {
            ChrSize[i] = Chromosomes[i].Size;
        }
        int SumBin = 0;
        int[] ChrBinSize = Statistic.CalculatorBinSize(ChrSize, Resolution);
        Hashtable<String, Integer> IndexBias = new Hashtable<>();
        // 计算bin的总数
        for (int i = 0; i < ChrBinSize.length; i++) {
            IndexBias.put(Chromosomes[i].Name, SumBin);
            SumBin = SumBin + ChrBinSize[i];
        }
        if (SumBin > MaxBinNum) {
            System.err.println("Error ! too many bins, there are " + SumBin + " bins.");
            System.exit(1);
        }
        double[][] intermatrix = new double[SumBin][SumBin];
        int[] DataIndex = IndexParse(BedpeFile);
        BufferedReader infile = new BufferedReader(new FileReader(BedpeFile));
        Thread[] Process = new Thread[Threads];
        // ----------------------------------------------------------------------------
        for (int i = 0; i < Threads; i++) {
            int finalSumBin = SumBin;
            Process[i] = new Thread(() -> {
                try {
                    String line;
                    String[] str;
                    while ((line = infile.readLine()) != null) {
                        str = line.split("\\s+");
                        int row = (Integer.parseInt(str[DataIndex[1]]) + Integer.parseInt(str[DataIndex[2]])) / 2
                                / Resolution;
                        int col = (Integer.parseInt(str[DataIndex[4]]) + Integer.parseInt(str[DataIndex[5]])) / 2
                                / Resolution;
                        row += IndexBias.get(str[DataIndex[0]]);
                        if (row >= finalSumBin) {
                            continue;
                        }
                        col += IndexBias.get(str[DataIndex[3]]);
                        if (col >= finalSumBin) {
                            continue;
                        }
                        synchronized (Process) {
                            if (UseCount) {
                                intermatrix[row][col] += Integer.parseInt(str[DataIndex[5] + 2]);
                            } else {
                                intermatrix[row][col]++;
                            }
                            if (row != col) {
                                intermatrix[col][row] = intermatrix[row][col];
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Process[i].start();
        }
        // -------------------------------------------------
        Tools.ThreadsWait(Process);
        infile.close();
        // --------------------------------------------------------
        // 打印矩阵
        Array2DRowRealMatrix InterMatrix = new Array2DRowRealMatrix(intermatrix);
        Tools.PrintMatrix(InterMatrix, DenseMatrixFile, SpareMatrixFile);
        System.out.println(new Date() + "\tEnd to create interaction matrix");
        // --------------------------------------------------------------------
        int temp = 0;
        BufferedWriter outfile = new BufferedWriter(new FileWriter(BinSizeFile));
        for (int i = 0; i < Chromosomes.length; i++) {
            temp = temp + 1;
            outfile.write(Chromosomes[i].Name + "\t" + temp + "\t");
            temp = temp + ChrBinSize[i] - 1;
            outfile.write(temp + "\n");
        }
        outfile.close();
        return InterMatrix;
    }// OK

    public Array2DRowRealMatrix Run(ChrRegion reg1, ChrRegion reg2) throws IOException {
        System.out.println(new Date() + "\tBegin to creat interaction matrix " + reg1.toString().replace("\t", ":")
                + " " + reg2.toString().replace("\t", ":"));
        int[] ChrBinSize;
        ChrBinSize = Statistic.CalculatorBinSize(new int[] { reg1.region.getLength(), reg2.region.getLength() },
                Resolution);
        if (Math.max(ChrBinSize[0], ChrBinSize[1]) > MaxBinNum) {
            System.err.println("Error ! too many bins, there are " + Math.max(ChrBinSize[0], ChrBinSize[1]) + " bins.");
            System.exit(0);
        }
        Array2DRowRealMatrix InterMatrix = new Array2DRowRealMatrix(ChrBinSize[0], ChrBinSize[1]);
        for (int i = 0; i < InterMatrix.getRowDimension(); i++) {
            for (int j = 0; j < InterMatrix.getColumnDimension(); j++) {
                InterMatrix.setEntry(i, j, 0);// 数组初始化为0
            }
        }
        BufferedReader infile = new BufferedReader(new FileReader(BedpeFile));
        int[] DataIndex = IndexParse(BedpeFile);
        Thread[] Process = new Thread[Threads];
        // ----------------------------------------------------------------------------
        for (int i = 0; i < Threads; i++) {
            Process[i] = new Thread(() -> {
                try {
                    String line;
                    String[] str;
                    while ((line = infile.readLine()) != null) {
                        str = line.split("\\s+");
                        ChrRegion left = new ChrRegion(
                                new String[] { str[DataIndex[0]], str[DataIndex[1]], str[DataIndex[2]] });
                        ChrRegion right = new ChrRegion(
                                new String[] { str[DataIndex[3]], str[DataIndex[4]], str[DataIndex[5]] });
                        int[] index1 = Bedpe2Index(reg1, reg2, left, right, Resolution);
                        if (index1[0] >= 0) {
                            synchronized (InterMatrix) {
                                InterMatrix.addToEntry(index1[0], index1[1],
                                        UseCount ? Integer.parseInt(str[DataIndex[5] + 2]) : 1);
                            }
                        }
                        int[] index2 = Bedpe2Index(reg1, reg2, right, left, Resolution);
                        if (index2[0] >= 0 && index2[0] != index1[0]) {
                            synchronized (InterMatrix) {
                                InterMatrix.addToEntry(index2[0], index2[1],
                                        UseCount ? Integer.parseInt(str[DataIndex[5] + 2]) : 1);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Process[i].start();
        }
        // -------------------------------------------------
        Tools.ThreadsWait(Process);
        infile.close();
        // --------------------------------------------------------
        // 打印矩阵
        Tools.PrintMatrix(InterMatrix, DenseMatrixFile, SpareMatrixFile);
        System.out.println(new Date() + "\tEnd to create interaction matrix");
        // --------------------------------------------------------------------
        BufferedWriter outfile = new BufferedWriter(new FileWriter(RegionFile));
        outfile.write(reg1.toString() + "\n");
        outfile.write(reg2.toString() + "\n");
        outfile.close();
        return InterMatrix;
    }

    public static int[] Bedpe2Index(ChrRegion reg1, ChrRegion reg2, ChrRegion left, ChrRegion right, int resolution) {
        int[] index = new int[] { -1, -1 };
        if (left.IsBelong(reg1) && right.IsBelong(reg2)) {
            index[0] = (left.region.Start - reg1.region.Start) / resolution;
            index[1] = (right.region.Start - reg2.region.Start) / resolution;
        }
        return index;
    }

    public ArrayList<Array2DRowRealMatrix> Run(List<InterAction> list) throws IOException {
        // 初始化矩阵列表
        ArrayList<Array2DRowRealMatrix> MatrixList = new ArrayList<>();
        for (InterAction aList : list) {
            Array2DRowRealMatrix aMatrix = new Array2DRowRealMatrix(
                    (aList.getLeft().region.End - aList.getLeft().region.Start + 1) / Resolution + 1,
                    (aList.getRight().region.End - aList.getRight().region.Start + 1) / Resolution + 1);
            MatrixList.add(aMatrix);
            for (int i = 0; i < aMatrix.getRowDimension(); i++) {
                for (int j = 0; j < aMatrix.getColumnDimension(); j++) {
                    aMatrix.setEntry(i, j, 0);
                }
            }
        }
        BufferedReader reader = new BufferedReader(new FileReader(BedpeFile));
        // 多线程构建矩阵
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                String Line;
                String[] Str;
                try {
                    int[] DataIndex = IndexParse(BedpeFile);
                    while ((Line = reader.readLine()) != null) {
                        Str = Line.split("\\s+");
                        ChrRegion left = new ChrRegion(
                                new String[] { Str[DataIndex[0]], Str[DataIndex[1]], Str[DataIndex[2]] });
                        ChrRegion right = new ChrRegion(
                                new String[] { Str[DataIndex[3]], Str[DataIndex[4]], Str[DataIndex[5]] });
                        for (int j = 0; j < list.size(); j++) {
                            if (left.IsBelong(list.get(j).getLeft()) && right.IsBelong(list.get(j).getRight())) {
                                synchronized (MatrixList.get(j)) {
                                    MatrixList.get(j).addToEntry(
                                            ((left.region.Start + left.region.End) / 2
                                                    - list.get(j).getLeft().region.Start + 1) / Resolution,
                                            ((right.region.Start + right.region.End) / 2
                                                    - list.get(j).getRight().region.Start + 1) / Resolution,
                                            1);
                                }
                                break;
                            } else if (right.IsBelong(list.get(j).getLeft()) && left.IsBelong(list.get(j).getRight())) {
                                synchronized (MatrixList.get(j)) {
                                    MatrixList.get(j).addToEntry(
                                            ((right.region.Start + right.region.End) / 2
                                                    - list.get(j).getLeft().region.Start + 1) / Resolution,
                                            ((left.region.Start + left.region.End) / 2
                                                    - list.get(j).getRight().region.Start + 1) / Resolution,
                                            1);
                                }
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        reader.close();
        return MatrixList;
    }

    public ArrayList<Array2DRowRealMatrix> Run(List<InterAction> list, ArrayList<Integer> Resolution)
            throws IOException {
        // 初始化矩阵列表
        ArrayList<Array2DRowRealMatrix> MatrixList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Array2DRowRealMatrix aMatrix = new Array2DRowRealMatrix(
                    (list.get(i).getLeft().region.End - list.get(i).getLeft().region.Start) / Resolution.get(i) + 1,
                    (list.get(i).getRight().region.End - list.get(i).getRight().region.Start) / Resolution.get(i) + 1);
            MatrixList.add(aMatrix);
            for (int j = 0; j < aMatrix.getRowDimension(); j++) {
                for (int k = 0; k < aMatrix.getColumnDimension(); k++) {
                    aMatrix.setEntry(j, k, 0);
                }
            }
        }
        BufferedReader reader = new BufferedReader(new FileReader(BedpeFile));
        // 多线程构建矩阵
        Thread[] t = new Thread[Threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                String Line;
                String[] Str;
                try {
                    int[] DataIndex = IndexParse(BedpeFile);
                    while ((Line = reader.readLine()) != null) {
                        Str = Line.split("\\s+");
                        ChrRegion left = new ChrRegion(
                                new String[] { Str[DataIndex[0]], Str[DataIndex[1]], Str[DataIndex[2]] });
                        ChrRegion right = new ChrRegion(
                                new String[] { Str[DataIndex[3]], Str[DataIndex[4]], Str[DataIndex[5]] });
                        for (int j = 0; j < list.size(); j++) {
                            if (left.IsBelong(list.get(j).getLeft()) && right.IsBelong(list.get(j).getRight())) {
                                synchronized (MatrixList.get(j)) {
                                    MatrixList.get(j).addToEntry(
                                            ((left.region.Start + left.region.End) / 2
                                                    - list.get(j).getLeft().region.Start) / Resolution.get(j),
                                            ((right.region.Start + right.region.End) / 2
                                                    - list.get(j).getRight().region.Start) / Resolution.get(j),
                                            1);
                                }
                                // break;
                            } else if (right.IsBelong(list.get(j).getLeft()) && left.IsBelong(list.get(j).getRight())) {
                                synchronized (MatrixList.get(j)) {
                                    MatrixList.get(j).addToEntry(
                                            ((right.region.Start + right.region.End) / 2
                                                    - list.get(j).getLeft().region.Start) / Resolution.get(j),
                                            ((left.region.Start + left.region.End) / 2
                                                    - list.get(j).getRight().region.Start) / Resolution.get(j),
                                            1);
                                }
                                // break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
        reader.close();
        return MatrixList;
    }

    public MatrixFile getSpareMatrixFile() {
        return SpareMatrixFile;
    }

    public MatrixFile getDenseMatrixFile() {
        return DenseMatrixFile;
    }

    public File getBinSizeFile() {
        return BinSizeFile;
    }

    private int[] IndexParse(BedPeFile file) throws IOException {
        int[] Index = new int[6];
        Index = new int[] { 0, 1, 2, 3, 4, 5 };
//        switch (file.BedpeDetect()) {
//            case BedpePointFormat:
//                Index = new int[] { 0, 1, 1, 2, 3, 3 };
//                break;
//            case BedpeRegionFormat:
//                break;
//            case EmptyFile:
//                break;
//            default:
//                System.err.println(new Date() + "\t" + "[" + CreateMatrix.class.getName() + "]\tError format!");
//                System.exit(1);
//        }
        return Index;
    }
}
