package com.github.SnowFlakes.test;

import java.io.IOException;
import java.util.ArrayList;

import com.github.SnowFlakes.File.CommonFile.CommonFile;
import com.github.SnowFlakes.File.FastaFile.FastaFile;
import com.github.SnowFlakes.File.FastaFile.FastaItem;
import com.github.SnowFlakes.unit.Opts;

/**
 * Created by snowf on 2019/10/14.
 */

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.err.println("jar file: " + Opts.JarFile);
        System.err.println("Author: " + Opts.Author);
        System.err.println("Email: " + Opts.Email);
        FastaFile file = new FastaFile(args[0]);
        CommonFile list_file = new CommonFile(args[1]);
        list_file.ReadOpen();
        ArrayList<char[]> list = list_file.Read();
        list_file.ReadClose();
        file.ReadOpen();
        FastaItem item;
        int LineNum = 0;
        while ((item = file.ReadItem()) != null) {
            System.err.println("read " + item.Title);
            for (int i = 0; i < list.size(); i++) {
                if (item.Title.matches(".*" + new String(list.get(i)) + ".*")) {
                    System.out.println(item);
                    list.remove(i);
                    break;
                }
            }
        }
        file.ReadClose();
    }
}
