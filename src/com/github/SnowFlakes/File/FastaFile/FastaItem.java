package com.github.SnowFlakes.File.FastaFile;

import com.github.SnowFlakes.File.AbstractItem;

import java.util.Comparator;

/**
 * Created by snowf on 2019/2/17.
 */

public class FastaItem extends AbstractItem {
    public String Title;
    public StringBuilder Sequence = new StringBuilder();
    public int SeqLen = 80;

    public FastaItem(String title) {
        Title = title;
    }

    public static class TitleComparator implements Comparator<FastaItem> {

        @Override
        public int compare(FastaItem o1, FastaItem o2) {
            return o1.Title.compareTo(o2.Title);
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder str = new StringBuilder(">" + Title + "\n");
        for (int i = 0; i < this.Sequence.length(); i += SeqLen) {
            if (i + SeqLen > this.Sequence.length()) {
                str.append(this.Sequence.substring(i));
            } else {
                str.append(this.Sequence.substring(i, i + SeqLen)).append("\n");
            }
        }
        return str.toString();
    }
}
