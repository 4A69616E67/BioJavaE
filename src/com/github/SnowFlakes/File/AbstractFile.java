package com.github.SnowFlakes.File;

import com.github.SnowFlakes.File.CommonFile.CommonFile;
import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.IO.HTSWriter;

import java.io.*;
import java.util.*;

/**
 * Created by snowf on 2019/2/17.
 */
public abstract class AbstractFile<E> extends File {
    public long ItemNum = 0;
    private boolean sorted = false;
    private String description = getPath();

    public AbstractFile(String pathname) {
        super(pathname);
    }

    public AbstractFile(File file) {
        super(file.getPath());
    }

    public AbstractFile(AbstractFile<?> file) {
        super(file.getPath());
        ItemNum = file.ItemNum;
        sorted = file.sorted;
        description = file.description;
    }

    public void CalculateItemNumber() throws IOException {
        ItemNum = 0;
        if (!isFile()) {
            return;
        }
        HTSReader<E> reader = getReader();
        while (reader.ReadRecord() != null) {
            ItemNum++;
        }
        reader.close();
    }

    public ArrayList<E> Extraction(int num) throws IOException {
        ArrayList<E> list = new ArrayList<>();
        HTSReader<E> reader = getReader();
        E item;
        int i = 0;
        while ((item = reader.ReadRecord()) != null && ++i <= num) {
            list.add(item);
        }
        reader.close();
        return list;
    }

    public abstract HTSReader<E> getReader();

    public abstract HTSWriter<E> getWriter();

    public abstract HTSWriter<E> getWriter(boolean append);

    public void Append(AbstractFile<E> file) throws IOException {
        HTSWriter<E> writer = getWriter(true);
        HTSReader<E> reader = file.getReader();
        E item;
        while ((item = reader.ReadRecord()) != null) {
            writer.WriterRecordln(item);
            ItemNum++;
        }
        reader.close();
        writer.close();
    }

    public void Append(ArrayList<E> List) throws IOException {
        HTSWriter<E> writer = getWriter(true);
        for (E item : List) {
            writer.WriterRecordln(item);
            ItemNum++;
        }
        writer.close();
    }

//    public void SortFile(AbstractFile<?> OutFile, Comparator<E> comparator) throws IOException {
//        System.out.println(new Date() + "\tSort file: " + getName());
//        BufferedWriter outfile = OutFile.WriteOpen();
//        ItemNum = 0;
//        ReadOpen();
//        E Item;
//        ArrayList<E> SortList = new ArrayList<>();
//        while ((Item = ReadItem()) != null) {
//            SortList.add(Item);
//            ItemNum++;
//        }
//        SortList.sort(comparator);
//        for (int i = 0; i < SortList.size(); i++) {
//            outfile.write(SortList.get(i).toString());
//            outfile.write("\n");
//            SortList.set(i, null);// 及时去除，减少内存占用
//        }
//        outfile.close();
//        ReadClose();
//        sorted = true;
//        System.out.println(new Date() + "\tEnd sort file: " + getName());
//    }

//    public synchronized void MergeSortFile(AbstractFile<E>[] InFile, Comparator<E> comparator) throws IOException {
//        ItemNum = 0;
//        System.out.print(new Date() + "\tMerge ");
//        for (File s : InFile) {
//            System.out.print(s.getName() + " ");
//        }
//        System.out.print("to " + getName() + "\n");
//        // =========================================================================================
//        LinkedList<E> SortList = new LinkedList<>();
//        BufferedWriter writer = WriteOpen();
//        if (InFile.length == 0) {
//            return;
//        }
//        for (int i = 0; i < InFile.length; i++) {
//            InFile[i].ReadOpen();
//            E item = InFile[i].ReadItem();
//            if (item != null) {
//                item.serial = i;
//                SortList.add(item);
//            } else {
//                InFile[i].ReadClose();
//            }
//        }
//        SortList.sort(comparator);
//        while (SortList.size() > 0) {
//            E item = SortList.remove(0);
//            int serial = item.serial;
//            writer.write(item.toString());
//            writer.write("\n");
//            ItemNum++;
//            item = InFile[serial].ReadItem();
//            if (item == null) {
//                continue;
//            }
//            item.serial = serial;
//            Iterator<E> iterator = SortList.iterator();
//            boolean flage = false;
//            int i = 0;
//            while (iterator.hasNext()) {
//                E item1 = iterator.next();
//                if (comparator.compare(item, item1) <= 0) {
//                    SortList.add(i, item);
//                    flage = true;
//                    break;
//                }
//                i++;
//            }
//            if (!flage) {
//                SortList.add(item);
//            }
//        }
//        WriteClose();
//        // ============================================================================================
//        System.out.print(new Date() + "\tEnd merge ");
//        for (File s : InFile) {
//            System.out.print(s.getName() + " ");
//        }
//        System.out.print("to " + getName() + "\n");
//    }

    public synchronized void Merge(AbstractFile<E>[] files) throws IOException {
        HTSWriter<E> writer = getWriter();
        ItemNum = 0;
        E lines;
        for (AbstractFile<E> x : files) {
            System.out.println(new Date() + "\tMerge " + x.getName() + " to " + getName());
            HTSReader<E>  reader = x.getReader();
            while ((lines = reader.ReadRecord()) != null) {
                writer.WriterRecordln(lines);
                ItemNum++;
            }
            reader.close();
        }
        writer.close();
        System.out.println(new Date() + "\tDone merge");
    }

    public ArrayList<CommonFile> SplitFile(String Prefix, long itemNum) throws IOException {
        int filecount = 0;
        int count = 0;
        CommonFile TempFile;
        E item;
        ArrayList<CommonFile> Outfile = new ArrayList<>();
        HTSReader<E> reader = getReader();
        Outfile.add(TempFile = new CommonFile(Prefix + ".Split" + filecount));
        HTSWriter<String> outfile = TempFile.getWriter();
        while ((item = reader.ReadRecord()) != null) {
            count++;
            if (count > itemNum) {
                TempFile.ItemNum = itemNum;
                outfile.close();
                filecount++;
                Outfile.add(TempFile = new CommonFile(Prefix + ".Split" + filecount));
                outfile = TempFile.getWriter();
                count = 1;
            }
            outfile.WriterRecordln(item.toString());
        }
        TempFile.ItemNum = count;
        outfile.close();
        reader.close();
        return Outfile;
    }

    public boolean clean() {
        return clean(this);
    }

    public static boolean clean(File f) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.close();
        } catch (IOException e) {
            System.err.println("Warning! can't clean " + f.getPath());
            return false;
        }
        return true;
    }

    public static void delete(File f) {
        if (f.exists() && !f.delete()) {
            System.err.println("Warning! can't delete " + f.getPath());
        }
    }

    // public SortItem<E> ReadSortItem() throws IOException {
    // return ExtractSortItem(ReadItemLine());
    // }
    //
    // protected abstract SortItem<E> ExtractSortItem(String[] s);

    public long getItemNum() {
        if (ItemNum <= 0) {
            try {
                CalculateItemNumber();
            } catch (IOException e) {
                System.err.println("Warning! can't get accurate item number, current item number: " + getName() + " " + ItemNum);
            }
        }
        return ItemNum;
    }

    public boolean isSorted() {
        return sorted;
    }
}
