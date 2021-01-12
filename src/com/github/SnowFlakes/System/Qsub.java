package com.github.SnowFlakes.System;

import com.github.SnowFlakes.File.CommonFile.CommonFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by snowf on 2019/6/15.
 */

public class Qsub extends Pbs {
    public static final String ExeName = "qsub";
    private String Queue;

    public Qsub(String nodes, int threads, long memory, String jobname) {
        super(nodes, threads, memory, jobname);
    }

    @Override
    public void CreateSubmitFile(String command, CommonFile file) throws IOException {
        StringBuilder Header = new StringBuilder();
        Header.append("#PBS -d ./\n");
        this.Nodes = this.Nodes == null ? "1" : this.Nodes;
        this.Threads = this.Threads <= 0 ? 1 : this.Threads;
        Header.append("#PBS -l nodes=").append(Nodes).append(":ppn=").append(Threads);
        if (Memory > 0) {
            Header.append(",mem=").append((int) Math.ceil(Memory / Math.pow(10, 9))).append("g");
        }
        Header.append("\n");
        if (JobName != null) {
            Header.append("#PBS -N ").append(JobName).append("\n");
        }
        if (Queue != null) {
            Header.append("#PBS -q ").append(Queue).append("\n");
        }
        BufferedWriter writer = file.WriteOpen();
        writer.write(Header.toString() + "\n");
        writer.write(command + "\n");
        writer.close();
    }

    @Override
    public String run(CommonFile file) throws InterruptedException, IOException {
        StringWriter Out = new StringWriter();
        int ExitValue = new CommandLine().run(ExeName + " " + file, new PrintWriter(Out), new PrintWriter(System.err));
        if (ExitValue != 0) {
            throw new InterruptedException("qsub error");
        }
        return Out.getBuffer().toString();
    }

    public void setQueue(String q) {
        Queue = q;
    }
}
