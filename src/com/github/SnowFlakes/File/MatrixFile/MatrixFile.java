package com.github.SnowFlakes.File.MatrixFile;

import com.github.SnowFlakes.File.AbstractFile;
import com.github.SnowFlakes.IO.HTSReader;
import com.github.SnowFlakes.IO.HTSWriter;
import com.github.SnowFlakes.IO.MatrixReaderExtension;
import com.github.SnowFlakes.unit.*;
import htsjdk.samtools.util.IOUtil;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

/**
 * Created by 浩 on 2019/2/1.
 */
public class MatrixFile extends AbstractFile<MatrixItem> {
    private Format format = Format.SparseMatrix;

    public enum Format {
        DenseMatrix, SparseMatrix, EmptyFile, ErrorFormat
    }

    public Format CheckFormat() {
        BufferedReader reader = IOUtil.openFileForBufferedReading(this);
        HashSet<Integer> col_count = new HashSet<>();
        try {
            String line = reader.readLine();
            if (line == null) {
                format = Format.EmptyFile;
            } else {
                col_count.add(line.split("\\s+").length);
                while ((line = reader.readLine()) != null) {
                    int len = line.split("\\s+").length;
                    if (!col_count.contains(len)) {
                        col_count.add(len);
                        format = Format.ErrorFormat;
                        break;
                    }
                }
                if (col_count.size() <= 1 && col_count.iterator().next() == 3) {
                    format = Format.SparseMatrix;
                } else if (col_count.size() <= 1) {
                    format = Format.DenseMatrix;
                }
            }
        } catch (IOException e) {
            format = Format.ErrorFormat;
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return format;
    }

    public MatrixFile(String pathname) {
        super(pathname);
    }

    @Override
    public MatrixReaderExtension getReader() {
        return null;
    }

    @Override
    public HTSWriter<MatrixItem> getWriter() {
        return null;
    }

    @Override
    public HTSWriter<MatrixItem> getWriter(boolean append) {
        return null;
    }

    public void PlotHeatMap(ArrayList<ChrRegion> bin_size, int resolution, float threshold, File outFile) throws IOException {
        MatrixReaderExtension reader = getReader();
        MatrixItem item = reader.ReadRecord();
        reader.close();
        BufferedImage image = item.DrawHeatMap(bin_size, resolution, threshold);
        ImageIO.write(image, outFile.getName().substring(outFile.getName().lastIndexOf('.') + 1), outFile);
    }

    public void PlotHeatMap(ChrRegion chr1, ChrRegion chr2, int resolution, float threshold, File outFile) throws IOException {
        MatrixReaderExtension reader = getReader();
        MatrixItem item = reader.ReadRecord();
        reader.close();
        item.Chr1 = chr1;
        item.Chr2 = chr2;
        ImageIO.write(item.DrawHeatMap(resolution, threshold, true),
                outFile.getName().substring(outFile.getName().lastIndexOf('.') + 1), outFile);
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Format getFormat() {
        return format;
    }
}
