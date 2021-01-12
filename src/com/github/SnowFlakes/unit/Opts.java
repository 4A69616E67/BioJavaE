package com.github.SnowFlakes.unit;

import java.io.File;

/**
 * Created by snowf on 2019/10/14.
 */

public class Opts {
    /**
     * 文件类型枚举类
     */
    public enum FileFormat {
        ErrorFormat, EmptyFile, BedpePointFormat, BedpeRegionFormat, Phred33, Phred64, ShortReads, LongReads, Undefine,
        Valid
    }

    public static final String OsName = System.getProperty("os.name");
    public static final File JarFile = new File(
            Opts.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    public static final Float Version = 1.0F;
    public static final String Author = "Snowflakes";
    public static final String Email = "john-jh@foxmail.com";
    public static final long MaxMemory = Runtime.getRuntime().maxMemory();// java能获取的最大内存
}
