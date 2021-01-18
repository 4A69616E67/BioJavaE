package com.github.SnowFlakes.unit;

import com.github.SnowFlakes.File.BedFile.BedItem;

public class BEDPEItem {
    protected BedItem locate1;
    protected BedItem locate2;
    private String Name;
    private float Score;
    public String[] Extends;

    public BEDPEItem(BedItem l1, BedItem l2) {
        String n1 = l1.getName().split("\\s+")[0];
        String n2 = l2.getName().split("\\s+")[0];
        if (!n1.equals(n2)) {
            throw new UnsupportedOperationException("find different name when convert bed to bedpe");
        }
        this.locate1 = l1;
        this.locate2 = l2;
        Name = n1;
        Score = (locate1.getScore() + locate2.getScore()) / 2;
    }

    public BEDPEItem(String[] s) {
        String[] l1;
        String[] l2;
        if (s.length < 6) {
            throw new UnsupportedOperationException("column less than 6");
        } else if (s.length < 7) {
            l1 = new String[]{s[0], s[1], s[2]};
            l2 = new String[]{s[3], s[4], s[5]};
        } else if (s.length < 8) {
            l1 = new String[]{s[0], s[1], s[2], s[6]};
            l2 = new String[]{s[3], s[4], s[5], s[6]};
        } else if (s.length < 10) {
            l1 = new String[]{s[0], s[1], s[2], s[6], s[7]};
            l2 = new String[]{s[3], s[4], s[5], s[6], s[7]};
        } else {
            l1 = new String[]{s[0], s[1], s[2], s[6], s[7], s[8]};
            l2 = new String[]{s[3], s[4], s[5], s[6], s[7], s[9]};
        }
        locate1 = new BedItem(BedItem.codec.decode(l1));
        locate2 = new BedItem(BedItem.codec.decode(l2));
        if (s.length > 10) {
            System.arraycopy(s, 10, Extends, 0, s.length - 10);
        }
    }

    public BedItem[] ToBED() {
        return ToBED(false);
    }

    public BedItem[] ToBED(boolean diff_name) {
        if (diff_name) {
            locate1.setName(locate1.getName() + "/1");
        }
        return new BedItem[]{locate1, locate2};
    }

    public float getScore() {
        return Score;
    }

    public String getName() {
        return Name;
    }

    public BedItem getLocate1() {
        return locate1;
    }

    public BedItem getLocate2() {
        return locate2;
    }
}
