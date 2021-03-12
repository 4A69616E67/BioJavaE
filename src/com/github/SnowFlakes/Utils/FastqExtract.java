package com.github.SnowFlakes.Utils;

import com.github.SnowFlakes.File.CommonFile.CommonFile;
import com.github.SnowFlakes.File.FastqFile;
import com.github.SnowFlakes.IO.FastqReaderExtension;
import com.github.SnowFlakes.IO.FastqWriterExtension;
import com.github.SnowFlakes.unit.Opts;
import com.github.SnowFlakes.unit.Parameter;

import htsjdk.samtools.fastq.FastqRecord;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by snowf on 2019/3/4.
 */

public class FastqExtract {
    public static void main(String[] args) throws IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").desc("input file").required().build());// 输入文件
        Argument.addOption(Option.builder("list").hasArg().argName("file").desc("read id file").build());// 配置文件
        Argument.addOption(Option.builder("n").hasArg().argName("int").desc("item number").build());// 配置文件
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("thread").build());// 配置文件
        Argument.addOption(Option.builder("f").hasArg().argName("file").desc("out file").build());// 配置文件
        final String helpFooter = "Note: use \"java -jar " + Opts.JarFile.getName()
                + " install\" when you first use!\n      JVM can get "
                + String.format("%.2f", Opts.MaxMemory / Math.pow(10, 9)) + "G memory";
        if (args.length == 0) {
            // 没有参数时打印帮助信息
            new HelpFormatter().printHelp("java -jar Path/" + Opts.JarFile.getName(), "", Argument, helpFooter, true);
            System.exit(1);
        }
        CommandLine ComLine = null;
        try {
            ComLine = new DefaultParser().parse(Argument, args);
        } catch (ParseException e) {
            // 缺少参数时打印帮助信息
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("java -jar Path/" + Opts.JarFile.getName(), "", Argument, helpFooter, true);
            System.exit(1);
        }
        FastqFile inputFile = new FastqFile(Parameter.GetStringOpt(ComLine, "i", null));
        FastqFile outputFile = ComLine.hasOption("f") ? new FastqFile(Parameter.GetFileOpt(ComLine, "f", null))
                : new FastqFile(inputFile.getPath() + ".out");
        CommonFile listfile = ComLine.hasOption("list") ? new CommonFile(Parameter.GetStringOpt(ComLine, "list", null))
                : null;
        int ItemNum = Parameter.GetIntOpt(ComLine, "n", 0);
        int threads = Parameter.GetIntOpt(ComLine, "t", 1);
        FastqFile TempFile;
        HashSet<String> IDList = new HashSet<>();
        FastqRecord item;
        String line;
        if (listfile != null) {
            BufferedReader list_reader = new BufferedReader(new FileReader(listfile));
            while ((line = list_reader.readLine()) != null) {
                IDList.add(line);
            }
            list_reader.close();
        }
        if (ItemNum <= 0) {
            TempFile = inputFile;
        } else {
            TempFile = new FastqFile(inputFile.getPath() + ".temp");
            FastqWriterExtension writer = new FastqWriterExtension(TempFile);
            FastqReaderExtension reader = new FastqReaderExtension(inputFile);
            int count = 1;
            while ((item = reader.ReadRecord()) != null && count <= ItemNum) {
                writer.WriterRecordln(item);
                count++;
            }
            writer.close();
            reader.close();
        }
        if (listfile == null) {
            FileUtils.moveFile(TempFile, outputFile);
        } else {
            FastqWriterExtension writer = new FastqWriterExtension(outputFile);
            for (FastqRecord i : TempFile.ExtractID(IDList, threads)) {
                writer.WriterRecordln(i);
            }
            writer.close();
            if (ItemNum > 0) {
                TempFile.delete();
            }
        }

    }
}
