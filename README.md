# ExternalSdcardDemo
  获取外置SD卡路径与操作外置SD卡(全版本)

## 说明
***
  此demo只提供方法,不讨论原理,相关原理请点击链接.</br></br>
  由于Android的版本各有差异,加上国内的ROM各种定制,参差不一,再加上Android的官方版本都到7.0了,而国内很多制作商都还停留在4.4-5.1时代.</br></br>
  因此在获取外置SD卡路径和如何在外置SD卡上操作文件变成了一个问题,百度和GOOGLE了很多的方法,找到了两个比较获取路径的方法,但是这两个方法都不适用于全版本.而在外置SD卡上的操作是Google在5.0之后修改了API,有新的方法代替.下面先看获取路径的方法<br></br>
  测试机器(nexus 5x[6.0.1],HTC M8[6.0],长虹C1[5.1.1],学习平板1[5.0],学习平板2[4.4.4],学习平板3[4.4.2],学习平板4[4.2.2],学习平板5[4.2]),其中除了nexus5x无卡槽之外,其他机器都有卡槽.</br></br>
## 获取外置SD卡路径与内存使用情况
***
    先不要急着copy代码,可以先看完测试结果再选择方法也不迟.
  1,获取外置SD卡路径方法一:http://blog.csdn.net/com314159/article/details/22859059</br>
  再贴出自己修改过的代码:</br>
  ```Java
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
  ```
  2,获取外置SD卡路径方法二:http://blog.fidroid.com/post/android/ru-he-zheng-que-huo-de-androidnei-wai-sdqia-lu-jing</br>
  再贴出自己修改过的代码(其实就是删除了方法里面的布尔参数233333):</br>
  ```Java
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
  ```
  3,获取外置SD卡的内存状况</br>
  具体请查看[QuerySpace.java](https://github.com/wenwenwen888/ExternalSdcardDemo/blob/master/app/src/main/java/com/wyt/externalsdcarddemo/QuerySpace.java)</br>
  (1)获取传入路径剩余存储空间</br>
  ```java
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
  ```
  (2)获取传入路径总的存储空间</br>
  ```java
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
  ```
## 测试结果
***
    nexus5x[6.0.1]无SD卡槽
  方法一:返回null</br>
  方法二:返回null</br>

    HTC M8[6.0]插卡状态
  方法一:返回null</br>
  方法二:返回正确路径</br>
  方法二路径查询内存状态正常</br>
  
    HTC M8[6.0]无插卡状态
  方法一:返回null</br>
  方法二:返回正确路径,这里没看错,没卡也能返回路径</br>
  但是路径查询内存状态会报错,报错原因为"路径无效"</br>
  
    长虹 C1[5.1.1],平板1[5.0]插卡状态(测试结果一样)
  方法一:返回正确路径</br>
  方法二:返回正确路径</br>
  两个路径查询内存状态正常</br>
  
    长虹 C1[5.1.1],平板1[5.0]无插卡状态(测试结果一样)
  方法一:返回null</br>
  方法二:返回正确路径,这里没看错,没卡也能返回路径</br>
  但是路径查询内存状态为0,并没有报错</br>
  
    平板2[4.4.4]插卡状态,PS:此台机器内部存储有分区,多了一个NAND FLASH存储区(具体百度)
  方法一:返回正确路径</br>
  方法二:返回了NAND FLASH存储区的路径</br>
  方法一路径查询外置内存状态正常,方法二路径查询到的内存状态为NAND FLASH存储区内存状态</br>
  
    平板2[4.4.4]无插卡状态,PS:此台机器内部存储有分区,多了一个NAND FLASH存储区(具体百度)
  方法一:返回null</br>
  方法二:返回了NAND FLASH存储区的路径</br>
  方法二路径查询到的内存状态为NAND FLASH存储区内存状态</br>
  
    平板3[4.4.2]插卡状态
  方法一:返回正确路径</br>
  方法二:返回正确路径</br>
  两个方法路径查询外置内存状态正常</br>
  
    平板3[4.4.2]无插卡状态
  方法一:返回null</br>
  方法二:依旧返回了外置SD卡的路径</br>
  方法二路径查询到的内存状态居然不为0,也不报错,查询到的内存为300+MB,不清楚什么原因,用自带的文件管理器也可以看得到此路径,但是用第三方的文件管理器就看不到</br>
  
    平板4[4.2.2]插卡状态,PS:此台机器内部存储有分区;平板5[4.2]插卡状态测试结果同
  方法一:返回null</br>
  方法二:返回正确路径</br>
  方法二路径查询外置内存状态正常</br>
  
    平板4[4.2.2]无插卡状态,PS:此台机器内部存储有分区;平板5[4.2]插卡状态测试结果同
  方法一:返回null</br>
  方法二:依旧返回了外置SD卡的路径</br>
  方法二路径查询到的内存状态为0,不报错</br>
  
## 综上所述的结论[仅在我测试过的机器中]:
  比较特殊的就是4.4系统,方法2要么会失效,要么出现BUG(也不算BUG,因为有分区,分区路径被当成外置路径了)</br>
  但在这个时候,方法一居然就管用了.其他时候,方法2基本管用,但是在无插卡状态的时候也能返回路径,实在是神奇.</br>
  因此可以在判断有无外置SD卡的时候可以加上判断有无内存,内存为0或报错即无外置卡.</br>
  暂时只测过这么几个机器,没其他机器了.</br>
  然后自己做了一个判断方法,如下:[MainActivity.java](https://github.com/wenwenwen888/ExternalSdcardDemo/blob/master/app/src/main/java/com/wyt/externalsdcarddemo/MainActivity.java)
```Java
private void queryspace() {
        //获取本机SDK版本
        VersionSdk = Build.VERSION.SDK_INT;
        //SDK为19即Android4.4
        if (VersionSdk == 19) {
            TFCardPath = querySpace.getTFCardPath1();
            if (TFCardPath == null) {
                Toast.makeText(this, "您的手机没有外置的SD卡哦~", Toast.LENGTH_SHORT).show();
            } else {
                calculateSpace();
            }
        } else {
            TFCardPath = querySpace.getTFCardPath2();
            if (TFCardPath == null) {
                Toast.makeText(this, "您的手机没有外置的SD卡哦~", Toast.LENGTH_SHORT).show();
            } else {
                long size = 0;
                //这里使用try-catch是因为在Android6.0中虽然有返回路径,但查询内存会报"路径无效"错误
                //而Android6.0以下查询内存为O
                try {
                    size = querySpace.getTotalPathMemorySize(TFCardPath);
                } catch (Exception e) {
                    size = 0;
                }
                if (size < 1) {
                    Toast.makeText(this, "您的手机没有外置的SD卡哦~", Toast.LENGTH_SHORT).show();
                } else {
                    calculateSpace();
                }
            }
        }

    }

    /**
     * 计算外置SD卡的内存使用情况
     */
    private void calculateSpace() {

        //查询可用与总内存
        long available = querySpace.getAvailablePathMemorySize(TFCardPath);
        long total = querySpace.getTotalPathMemorySize(TFCardPath);
        //已用内存
        long used = total - available;

        //计算已使用的百分比
        float usePercent = (float) available / (float) total;
        int progress = 100 - (int) (usePercent * 100);

        //已使用的空间转换为String类型
        String freeSize = querySpace.formatFileSize(available, false);
        String usedSize = querySpace.formatFileSize(used, false);
        String spaceInfo = "(已用:" + usedSize + "/可用:" + freeSize + ")";

        //显示信息
        linearlayout.setVisibility(View.VISIBLE);
        progressbar.setProgress(progress);
        spaceinfo.setText(spaceInfo);
    }
```

## 操作外置SD卡(新建文件，删除文件，重命名等)

  这个就没什么好说的了,4.4版本及以下版本API没变,就是一般的操作</br>
  5.0以上版本请参考此文:http://zhixinliu.com/2015/02/24/2015-02-24-SAF-and-client-code/</br>
  附上自己的测试代码片段(详细:[MainActivity.java](https://github.com/wenwenwen888/ExternalSdcardDemo/blob/master/app/src/main/java/com/wyt/externalsdcarddemo/MainActivity.java):

```Java
 /**
     * 在外置SD卡创建文件
     */
    private void creatfile() {
        //获取本机SDK版本
        if (VersionSdk <= 19) {
            File file = null;
            try {
                file = new File(TFCardPath + "/WENWENWEN");
                if (!file.exists()) {
                    file.mkdirs();
                    Toast.makeText(this, "创建成功~", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "已经有此文件啦~", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "创建失败~", Toast.LENGTH_SHORT).show();
            }
        } else {
            /**
             * 当存在外置SD卡的时候
             * 检查是否存在外置SD卡路径的uri
             * 没有就跳转获取,有就直接创建文件夹
             */
            uri_string = mSharedPreferences.getString("uri", null);
            if (uri_string == null || uri_string.equals("")) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                Toast.makeText(this, "请选择外置SD卡的根目录~", Toast.LENGTH_SHORT).show();
            } else {
                mkdirsOnTFCard(Uri.parse(uri_string));
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == DIRECTORY_CHOOSE_REQ_CODE) {
            //获取返回的Uri
            Uri uri = data.getData();
            //保存uri避免每次都调用Intent
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            mEditor.putString("uri", uri.toString());
            mEditor.apply();
            mkdirsOnTFCard(uri);
        } else {
            Log.e("onActivityResult", "没有返回的resultCode");
        }
    }

    /**
     * DocumentFile外置SD卡创建文件夹
     */
    public void mkdirsOnTFCard(Uri uri) {
        //创建DocumentFile
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        /**
         * 如果没有该文件夹,则创建一个新的文件并写入内容
         * 查询文件是否存在时,假如文件存在,则返回true;不存在时不会返回false,而是返回null
         * 所以这里应该用try-catch来判断,出现异常则不存在此文件
         */
        boolean ishasDirectory;
        try {
            ishasDirectory = pickedDir.findFile("WENWENWEN").exists();
        } catch (Exception e) {
            ishasDirectory = false;
        }
        if (!ishasDirectory) {
            try {
                //创建新的一个文件夹
                pickedDir.createDirectory("WENWENWEN");
                //找到新文件夹的路径
                pickedDir = pickedDir.findFile("WENWENWEN");
                //创建新的文件
                DocumentFile newFile = pickedDir.createFile("text/plain", "new_file");
                //写入内容到新建文件
                OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
                if (out != null) {
                    out.write("测试".getBytes());
                    out.close();
                }
                Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "创建失败", Toast.LENGTH_SHORT).show();
                Log.e("Exception", "DocumentFile创建失败:" + e);
            }
        }
    }
```

## 惊喜
    Android 6.0用此方法操作文件居然不用检查询问权限!!![其实在选择路径的时候就相当于允许权限了,但是不用弹窗]

### PS:还有其他好的方法麻烦通知一声我~
