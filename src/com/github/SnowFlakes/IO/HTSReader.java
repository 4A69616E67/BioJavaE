package com.github.SnowFlakes.IO;


public interface HTSReader<E> {
    E ReadRecord();
    void close();
}
