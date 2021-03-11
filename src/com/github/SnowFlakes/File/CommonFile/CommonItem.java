package com.github.SnowFlakes.File.CommonFile;


import java.util.Comparator;

/**
 * Created by snowf on 2019/8/28.
 */

public class CommonItem {
    public String item;

    public CommonItem(String s) {
        item = s;
    }

    public static class CommonComparator implements Comparator<CommonItem> {

        @Override
        public int compare(CommonItem o1, CommonItem o2) {
            return o1.item.compareTo(o2.item);
        }
    }

}
