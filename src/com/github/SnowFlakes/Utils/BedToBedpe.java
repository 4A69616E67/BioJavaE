package com.github.SnowFlakes.Utils;


import com.github.SnowFlakes.File.BedFile.BedFile;
import com.github.SnowFlakes.File.BedPeFile.BedPeFile;
import com.github.SnowFlakes.unit.Opts;

import java.io.IOException;

/**
 * Created by snowf on 2019/2/24.
 */

public class BedToBedpe {
    public static void main(String[] args) throws IOException {
//        new com.github.SnowFlakes.tool.BedToBedpe(new BedFile(args[0]), new BedFile(args[1]), new BedpeFile(args[2]), 4, "");
        if (args.length <= 3) {
            System.err.println("usage: java -cp " + Opts.JarFile + " " + BedToBedpe.class.getName() + " <bed file 1> <bed file 2> <bedpe file>");
            System.exit(1);
        }
        BedPeFile file = new BedPeFile(args[2]);
        file.BEDToBEDPE(new BedFile(args[0]), new BedFile(args[1]));
        System.out.println(file.getItemNum());
    }
}
