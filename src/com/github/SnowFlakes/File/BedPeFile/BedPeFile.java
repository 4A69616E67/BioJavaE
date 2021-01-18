package com.github.SnowFlakes.File.BedPeFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.File.BedFile.BedFile;
import com.github.SnowFlakes.File.BedFile.BedItem;
import com.github.SnowFlakes.IO.*;
import com.github.SnowFlakes.unit.*;

import java.io.*;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 */
public class BedPeFile extends AbstractFile<BEDPEItem> {

    public BedPeFile(String pathname) {
        super(pathname);
    }

    public BedPeFile(File f) {
        super(f);
    }

    @Override
    public BedPeReaderExtension getReader() {
        try {
            return new BedPeReaderExtension(new FileInputStream(this));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BedPeWriterExtension getWriter() {
        return getWriter(false);
    }

    @Override
    public BedPeWriterExtension getWriter(boolean append) {
        return new BedPeWriterExtension(this, append);
    }

//    public void SplitSortFile(BEDPEFile OutFile, Comparator<BedpeItem> comparator) throws IOException {
//        int splitItemNum = 1000000;
//        this.ItemNum = getItemNum();
//        if (this.ItemNum > splitItemNum) {
//            splitItemNum = (int) Math.ceil(this.ItemNum / Math.ceil((double) this.ItemNum / splitItemNum));
//            ArrayList<CommonFile> TempSplitFile = this.SplitFile(this.getPath(), splitItemNum);
//            BEDPEFile[] TempSplitSortFile = new BEDPEFile[TempSplitFile.size()];
//            for (int i = 0; i < TempSplitFile.size(); i++) {
//                TempSplitSortFile[i] = new BEDPEFile(TempSplitFile.get(i).getPath() + ".sort");
//                new BEDPEFile(TempSplitFile.get(i).getPath()).SortFile(TempSplitSortFile[i], comparator);
//            }
//            OutFile.MergeSortFile(TempSplitSortFile, comparator);
//            for (int i = 0; i < TempSplitFile.size(); i++) {
//                AbstractFile.delete(TempSplitFile.get(i));
//                AbstractFile.delete(TempSplitSortFile[i]);
//            }
//        } else {
//            this.SortFile(OutFile, comparator);
//        }
//    }

    public void BEDToBEDPE(BedFile file1, BedFile file2) throws IOException {
        BedReaderExtension reader1 = file1.getReader();
        BedReaderExtension reader2 = file2.getReader();
        BedPeWriterExtension writer = getWriter();
        ItemNum = 0;
        BedItem item1 = reader1.ReadRecord();
        BedItem item2 = reader2.ReadRecord();
        if (item1 == null || item2 == null) {
            writer.close();
            reader1.close();
            reader2.close();
            return;
        }
        Comparator<BedItem> location_comparator = new BedItem.LocationComparator();
        Comparator<BedItem> title_comparator = new BedItem.TitleComparator();
        while (item1 != null && item2 != null) {
            int res = title_comparator.compare(item1, item2);
            if (res == 0) {
                if (location_comparator.compare(item1, item2) > 0) {
                    writer.WriterRecord(BedItem.ToBEDPE(item2, item1));
                } else {
                    writer.WriterRecord(BedItem.ToBEDPE(item1, item2));
                }
                ItemNum++;
                item1 = reader1.ReadRecord();
                item2 = reader2.ReadRecord();
            } else if (res > 0) {
                item2 = reader2.ReadRecord();
            } else {
                item1 = reader1.ReadRecord();
            }
        }
        reader1.close();
        reader2.close();
        writer.close();
    }

//    public int DistanceCount(int min, int max, int thread) throws IOException {
//        if (thread <= 0) {
//            thread = 1;
//        }
//        final int[] Count = {0};
//        ReadOpen();
//        Thread[] t = new Thread[thread];
//        for (int i = 0; i < t.length; i++) {
//            t[i] = new Thread(() -> {
//                try {
//                    String[] Lines;
//                    while ((Lines = ReadItemLine()) != null) {
//                        InterAction action = new InterAction(Lines[0].split("\\s+"));
//                        int dis = action.Distance();
//                        if (dis <= max && dis >= min) {
//                            synchronized (t) {
//                                Count[0]++;
//                            }
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            t[i].start();
//        }
//        Tools.ThreadsWait(t);
//        ReadClose();
//        return Count[0];
//    }

    /**
     * @return {short range, long range}
     */
//    public long[] RangeCount(Region ShortRange, Region LongRange, int thread) throws IOException {
//        ItemNum = 0;
//        thread = thread > 0 ? thread : 1;
//        long[] Count = new long[2];
//        ReadOpen();
//        Thread[] t = new Thread[thread];
//        for (int i = 0; i < t.length; i++) {
//            t[i] = new Thread(() -> {
//                try {
//                    BedpeItem item;
//                    while ((item = ReadItem()) != null) {
//                        if (ShortRange.IsContain(item.getLocation().Distance())) {
//                            synchronized (ShortRange) {
//                                Count[0]++;
//                            }
//                        } else if (LongRange.IsContain(item.getLocation().Distance())) {
//                            synchronized (LongRange) {
//                                Count[1]++;
//                            }
//                        }
//                        ItemNum++;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            t[i].start();
//        }
//        Tools.ThreadsWait(t);
//        ReadClose();
//        return Count;
//    }

//    public HashMap<String, HashMap<String, long[]>> Annotation(GffF3ile gffFile, BEDPEFile outFile, int thread)
//            throws IOException {
//        HashMap<String, HashMap<String, long[]>> Stat = new HashMap<>();
//        HashMap<String, ArrayList<Gene>> gffList = new HashMap<>();
//        // HashMap<String, Integer> AttributeMap = new HashMap<>();
//        Gene item;
//        gffFile.ReadOpen();
//        System.out.println(new Date() + "\tCreate index ......");
//        BufferedWriter out = outFile.WriteOpen();
//        while ((item = gffFile.ReadItem()) != null) {
//            if (!gffList.containsKey(item.GeneRegion.Chr)) {
//                gffList.put(item.GeneRegion.Chr, new ArrayList<>());
//            }
//            gffList.get(item.GeneRegion.Chr).add(item);
//        }
//        for (String key : gffList.keySet()) {
//            gffList.get(key).sort(new Gene.RegionComparator());
//        }
//        gffFile.ReadClose();
//        System.out.println(new Date() + "\tAnnotation begin ......");
//        this.ReadOpen();
//        Thread[] t = new Thread[thread];
//        for (int i = 0; i < t.length; i++) {
//            t[i] = new Thread(() -> {
//                try {
//                    BedpeItem temp;
//                    while ((temp = this.ReadItem()) != null) {
//                        Gene g1 = GffF3ile.Search(gffList.get(temp.getLocation().getLeft().Chr),
//                                temp.getLocation().getLeft());
//                        Gene g2 = GffF3ile.Search(gffList.get(temp.getLocation().getRight().Chr),
//                                temp.getLocation().getRight());
//                        String[] extra1 = new String[]{"-"}, extra2 = new String[]{"-"};
//                        if (g1 != null) {
//                            extra1 = Gene.GeneDistance(g1, temp.getLocation().getLeft());
//                        }
//                        if (g2 != null) {
//                            extra2 = Gene.GeneDistance(g2, temp.getLocation().getRight());
//                        }
//                        synchronized (t) {
//                            if (!Stat.containsKey(extra1[0])) {
//                                MapInit(Stat, extra1[0]);
//                            }
//                            if (!Stat.containsKey(extra2[0])) {
//                                MapInit(Stat, extra2[0]);
//                            }
//                            Stat.get(extra1[0]).get(extra2[0])[0]++;
//                            out.write(temp + "\t" + String.join(":", extra1) + "\t" + String.join(":", extra2) + "\n");
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            t[i].start();
//        }
//        Tools.ThreadsWait(t);
//        out.close();
//        this.ReadClose();
//        System.out.println(new Date() + "\tAnnotation finish");
//        return Stat;
//    }

    private void MapInit(HashMap<String, HashMap<String, long[]>> map, String key) {
        Set<String> keys = map.keySet();
        map.put(key, new HashMap<>());
        map.get(key).put(key, new long[]{0});
        for (String k : keys) {
            map.get(key).put(k, new long[]{0});
            map.get(k).put(key, new long[]{0});
        }
    }
}
