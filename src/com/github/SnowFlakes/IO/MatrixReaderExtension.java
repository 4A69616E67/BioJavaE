package com.github.SnowFlakes.IO;

import com.github.SnowFlakes.File.MatrixFile.MatrixFile;
import com.github.SnowFlakes.File.MatrixFile.MatrixItem;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.samtools.util.IOUtil;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.util.ArrayList;

public class MatrixReaderExtension extends BufferedLineReader implements HTSReader<MatrixItem> {
    private final MatrixFile.Format format;

    public MatrixReaderExtension(MatrixFile file) {
        super(IOUtil.openFileForReading(file));
        format = file.getFormat();
    }

    @Override
    public MatrixItem ReadRecord() {
        MatrixItem item;
        switch (format) {
            case SparseMatrix:
                item = new MatrixItem(0, 0);
                item.sparse_item = read_all();
                break;
            case DenseMatrix:
                ArrayList<double[]> list = read_all();
                if (list.size() <= 0) {
                    return new MatrixItem(0, 0);
                }
                item = new MatrixItem(list.size(), list.get(0).length);
                Array2DRowRealMatrix matrix = item.dense_item;
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < list.get(i).length; j++) {
                        matrix.setEntry(i, j, list.get(i)[j]);
                    }
                }
                break;
            case EmptyFile:
                return new MatrixItem(0, 0);
            default:
                return null;
        }
        return item;
    }

    private ArrayList<double[]> read_all() {
        ArrayList<double[]> list = new ArrayList<>();
        String line;
        while ((line = readLine()) != null) {
            String[] strs = line.split("\\s+");
            double[] ds = new double[strs.length];
            for (int i = 0; i < ds.length; i++) {
                ds[i] = Double.parseDouble(strs[i]);
            }
            list.add(ds);
        }
        return list;
    }
}
