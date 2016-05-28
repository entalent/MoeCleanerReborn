package cn.edu.bit.cs.moecleaner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by entalent on 2016/4/27.
 */
public class FileUtil {
    /**
     * @param fileToDelete 删除一个文件或一个目录
     * @return 被删除的文件数量
     */
    public static int deleteFileOrDirectory(File fileToDelete) {
        //文件不存在或不可写
        if(!(fileToDelete.exists() && fileToDelete.canWrite())) {
            return 0;
        }
        if(fileToDelete.isFile()) {
            return fileToDelete.delete() ? 1 : 0;
        } else if(fileToDelete.isDirectory()) {
            int deleteCnt = 0;
            File[] files = fileToDelete.listFiles();
            for(File i : files) {
                deleteCnt += deleteFileOrDirectory(i);
            }
            deleteCnt += fileToDelete.delete() ? 1 : 0;
            return deleteCnt;
        } else {
            return 0;
        }
    }

    /**
     *
     * @param file 要统计大小的文件
     * @return 文件或目录的大小
     */
    public static long getFileOrDirectorySize(File file) {
        long fileSize = 0;
        if(!(file.exists() && file.canRead())) {
            return fileSize;
        }
        if(file.isFile()) {
            fileSize = file.length();
        } else if(file.isDirectory()) {
            File[] files = file.listFiles();
            for(File i : files) {
                fileSize += getFileOrDirectorySize(i);
            }
        }
        return fileSize;
    }

    public static boolean copyFile(File source, File target) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            target.getParentFile().mkdirs();
            target.createNewFile();
            fi = new FileInputStream(source);
            fo = new FileOutputStream(target);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if(fi != null) fi.close();
                if(fo != null) fo.close();
                if(in != null) in.close();
                if(out != null) out.close();
            } catch (Exception e) {

            }
        }
        return true;
    }
}
