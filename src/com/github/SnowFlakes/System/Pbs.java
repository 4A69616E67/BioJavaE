package com.github.SnowFlakes.System;

import com.github.SnowFlakes.File.CommonFile.CommonFile;

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

    public abstract void CreateSubmitFile(String command, CommonFile file) throws Exception;

    public abstract String run(CommonFile file) throws Exception;
}
