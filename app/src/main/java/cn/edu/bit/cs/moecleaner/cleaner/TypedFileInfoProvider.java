package cn.edu.bit.cs.moecleaner.cleaner;

import android.app.PendingIntent;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import cn.edu.bit.cs.moecleaner.util.FileUtil;

/**
 * Created by entalent on 2016/5/24.
 */
public class TypedFileInfoProvider {

    ArrayList<File> fileList;
    FileFilter fileFilter;
    File rootDirectory;

    long totalSize = 0;

    public TypedFileInfoProvider(File rootDirectory) {
        if(rootDirectory == null || (!rootDirectory.canRead()) || (!rootDirectory.isDirectory()) || (!rootDirectory.canRead())) {
            throw new IllegalArgumentException("illegal root directory");
        }
        this.rootDirectory = rootDirectory;
        fileList = new ArrayList<File>();
    }

    public void scanFilteredFile() {
        Stack<File> fileStack = new Stack<>();
        fileStack.push(rootDirectory);
        while(!fileStack.isEmpty()) {
            File currentFile = fileStack.peek();
            fileStack.pop();

            // vis
            if(isFilteredFile(currentFile)) {
                synchronized (this) {
                    totalSize += FileUtil.getFileOrDirectorySize(currentFile);
                    fileList.add(currentFile);
                }
            }

            if(currentFile.isDirectory()) {
                File[] files;
                if(fileFilter != null)
                    files = currentFile.listFiles(fileFilter);
                else
                    files = currentFile.listFiles();
                for(File i : files) {
                    fileStack.add(i);
                }
            }
        }
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public boolean isFilteredFile(File file) {
        if(file.exists() && file.canRead() && file.isFile()) {
            return true;
        } else {
            return false;
        }
    }

    public  long getTotalFileSize() {
        synchronized (this) {
            return totalSize;
        }
    }

    public ArrayList<File> getScannedFiles() {
        return this.fileList;
    }

}
