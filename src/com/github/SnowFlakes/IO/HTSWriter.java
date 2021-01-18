package com.github.SnowFlakes.IO;


public interface HTSWriter<E> {
    void WriterRecord(E o);

    void WriterRecordln(E o);

    void close();
}
