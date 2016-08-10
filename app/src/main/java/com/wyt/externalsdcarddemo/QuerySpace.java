package com.wyt.externalsdcarddemo;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Won on 2016/7/22.
 */
public class QuerySpace {

    private static final int ERROR = -1;
    private Context context;

    public QuerySpace(Context context) {
        this.context = context;
    }

    /**
     * 获取手机内部路径
     */
    public String getInternalPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取手机内部剩余存储空间
     * 此方法在内部存储有两个分区的时候获取到的是空间小的分区
     * 所以推荐直接传入内部存储路径获取空间值
     */
    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间
     * 此方法在内部存储有两个分区的时候获取到的是空间小的分区
     * 所以推荐直接传入内部存储路径获取空间值
     */
    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取传入路径剩余存储空间
     */
    public long getAvailablePathMemorySize(String Path) {
        if (Path != null) {
            File path = new File(Path);
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 获取传入路径总的存储空间
     */
    public long getTotalPathMemorySize(String Path) {
        if (Path != null) {
            File path = new File(Path);
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 获取系统总内存
     */
    public long getTotalMemorySize() {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     */
    public long getAvailableMemory() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    private DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

    /**
     * 获取外置SD卡方法一(Android4.4可优先选择此方法)
     * 返回path不为null即为外置SD卡的路径
     * 此方法适用于内部存储有两个分区时,方法二检测到的路径与内部存储路径一样的情况下使用
     */
    public String getTFCardPath1() {
        List<String> paths = getAllExterSdcardPath();
        if (paths.size() == 2) {
            for (String path : paths) {
                if (path != null && !path.equals(Environment.getExternalStorageDirectory().getPath())) {
                    return path;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * 获取全部路径
     */
    public List<String> getAllExterSdcardPath() {
        List<String> SdList = new ArrayList<String>();
        String firstPath = Environment.getExternalStorageDirectory().getPath();
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // 将常见的linux分区过滤掉
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("media"))
                    continue;
                if (line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data")
                        || line.contains("tmpfs") || line.contains("shell")
                        || line.contains("root") || line.contains("acct")
                        || line.contains("proc") || line.contains("misc")
                        || line.contains("obb")) {
                    continue;
                }
                if (line.contains("fat") || line.contains("fuse") || (line
                        .contains("ntfs"))) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        String path = columns[1];
                        if (path != null && !SdList.contains(path) && path.contains("sd"))
                            SdList.add(columns[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!SdList.contains(firstPath)) {
            SdList.add(firstPath);
        }
        return SdList;
    }

    /**
     * 获取外置SD卡方法二
     * 返回path不为null即为路径
     * 一般此方法都有效,只有在内存储存有两个分区时可能会出现外置路径与内置路径一样,此时应该使用方法一
     * 此方法有一个问题就是不插入外置SD卡的情况下都能获取到路径
     * 无外置SD卡卡槽的时候返回null
     */
    public String getTFCardPath2() {

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                if ((Boolean) isRemovable.invoke(storageVolumeElement)) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 单位换算
     *
     * @param size      单位为B
     * @param isInteger 是否返回取整的单位
     * @return 转换后的单位
     */
    public String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "MB";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "GB";
        }
        return fileSizeString;
    }

}
