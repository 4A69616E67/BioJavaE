package com.github.SnowFlakes.System;

import com.github.SnowFlakes.File.CommonFile.CommonFile;

import java.io.File;

/**
 * Created by snowf on 2019/6/15.
 */

public abstract class Pbs {
    // protected CommonFile SubmitFile;
    protected String Nodes;
    protected int Threads;
    protected long Memory;
    protected String JobName;

    public Pbs(String nodes, int threads, long memory, String jobname) {
        // SubmitFile = file;
        Nodes = nodes;
        Threads = threads;
        Memory = memory;
        JobName = jobname;
    }

    public abstract void CreateSubmitFile(String command, File file) throws Exception;

    public abstract String run(CommonFile file) throws Exception;
}
