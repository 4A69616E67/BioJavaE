package com.github.SnowFlakes.test;

import java.io.IOException;

import com.github.SnowFlakes.unit.Opts;

/**
 * Created by snowf on 2019/10/14.
 */

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.err.println("jar file: " + Opts.JarFile);
        System.err.println("Author: " + Opts.Author);
        System.err.println("Email: " + Opts.Email);
    }
}
