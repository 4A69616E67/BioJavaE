package com.github.SnowFlakes.File.BedFile;


import com.github.SnowFlakes.unit.BEDPEItem;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.tribble.bed.FullBEDFeature;

import java.util.Comparator;

/**
 * Created by snowf on 2019/2/24.
 */

public class BedItem extends FullBEDFeature {
    public static BEDCodec codec = new BEDCodec();
    public String[] Extends;

    public BedItem(String chr, int start, int end) {
        super(chr, start, end);

    }

    public BedItem(BEDFeature b) {
        super(b.getContig(), b.getStart(), b.getEnd());
        this.setScore(b.getScore());
        this.setName(b.getName());
        this.setStrand(b.getStrand());
        this.setColor(b.getColor());
        this.setDescription(b.getDescription());
        this.setExons(b.getExons());
        this.setLink(b.getLink());
        this.setType(b.getType());
    }


//    @Override
//    public String toString() {
//        StringBuilder s = new StringBuilder();
//        s.append(chr).append("\t").append(start).append("\t").append(end).append("\t").append(getName()).append("\t").append(getScore()).append("\t").append(getStrand());
//        return s.toString();
//    }


    public static BEDPEItem ToBEDPE(BedItem a, BedItem b) {
        return new BEDPEItem(a, b);
    }

//    public ChrRegion getLocation() {
//        return new ChrRegion(chr, start, end, strand);
//    }

    public static class LocationComparator implements Comparator<BedItem> {

        @Override
        public int compare(BedItem o1, BedItem o2) {
            int res = o1.chr.compareTo(o2.chr);
            if (res == 0) {
                res = o1.start - o2.start;
                if (res == 0) {
                    return o1.end - o2.end;
                } else {
                    return res;
                }
            } else {
                return res;
            }
        }
    }

    public static class TitleComparator implements Comparator<BedItem> {

        @Override
        public int compare(BedItem o1, BedItem o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
