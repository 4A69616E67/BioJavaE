package com.github.SnowFlakes.Sequence;

import htsjdk.samtools.reference.ReferenceSequence;

import java.util.ArrayList;

/**
 * Created by snowf on 2019/4/23.
 */

public class KmerStructure {
    public ReferenceSequence Seq;
    public ArrayList<KmerStructure> next = new ArrayList<>();
    public ArrayList<KmerStructure> last = new ArrayList<>();
    public boolean Visited = false;

    public KmerStructure(ReferenceSequence seq) {
        Seq = seq;
    }
}
