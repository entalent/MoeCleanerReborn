package cn.edu.bit.cs.moecleaner;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import cn.edu.bit.cs.moecleaner.cleaner.ApkCacheInfoProvider;
import cn.edu.bit.cs.moecleaner.cleaner.TypedFileInfoProvider;
import cn.edu.bit.cs.moecleaner.cleaner.UnusedApkInfoProvider;
import cn.edu.bit.cs.moecleaner.ui.customview.ApkCacheItemView;
import cn.edu.bit.cs.moecleaner.ui.customview.FileItemView;
import cn.edu.bit.cs.moecleaner.ui.customview.JunkFileGroupView;
import cn.edu.bit.cs.moecleaner.util.FileUtil;
import cn.edu.bit.cs.moecleaner.util.TextUtil;

public class JunkCleanActivity extends AppCompatActivity {

    ExpandableListView listView;
    Button btn;
    TextView textSizeNumber, textSizeUnit;
    TextView textCurrentFile;
    View progressBar;

    ArrayList<ApkCacheInfoProvider.PackageCacheItem> cacheFileList = new ArrayList<>();
    ArrayList<File> typedFileList = new ArrayList<>();
    ArrayList<File> unusedApkList = new ArrayList<>();

    boolean[][] flags = null;

    Integer threadsLeft = 0;

    LinkedList<File> filesToDelete = new LinkedList<>();

    long cacheFileSize = 0, typedFileSize = 0, unusedApkSize = 0;
    long totalSize = 0;

    final int threadPoolSize = 4;
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
    Semaphore sem = new Semaphore(threadPoolSize);

    boolean scanFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_junk_clean);

        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        btn = (Button) findViewById(R.id.button);
        textSizeNumber = (TextView) findViewById(R.id.textView13);
        textSizeUnit = (TextView) findViewById(R.id.textView14);
        textCurrentFile = (TextView) findViewById(R.id.textView25);
        progressBar = findViewById(R.id.progressBarIndeterminate);
        /*
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        */
        setTitle(getText(R.string.title_junk_clean_activity));
        getSupportActionBar().setTitle(getText(R.string.title_junk_clean_activity));
        listView.setAdapter(new MyExpandableListAdapter(JunkCleanActivity.this, listView));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始清理
                btn.setText(getText(R.string.btn_text_cleaning));
                btn.setClickable(false);
                progressBar.setVisibility(View.VISIBLE);
                for(int i = 0; i < cacheFileList.size() && i < flags[0].length; i++){
                    if(!flags[0][i]) continue;
                    filesToDelete.addAll(cacheFileList.get(i).cacheFiles);
                }
                for(int i = 0; i < typedFileList.size() && i < flags[1].length; i++){
                    if(!flags[1][i]) continue;
                    filesToDelete.add(typedFileList.get(i));
                }
                for(int i = 0; i < unusedApkList.size() && i < flags[2].length; i++){
                    if(!flags[2][i]) continue;
                    filesToDelete.add(unusedApkList.get(i));
                }
                sem.drainPermits();
                for(int i = 0; i < threadPoolSize; i++){
                    FileCleanTask task = new FileCleanTask();
                    task.executeOnExecutor(executorService);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sem.acquire(threadPoolSize);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //清理完成
                        totalSize = 0;
                        cacheFileList.clear();
                        typedFileList.clear();
                        unusedApkList.clear();
                        cacheFileSize = 0;
                        typedFileSize = 0;
                        unusedApkSize = 0;
                        scanFinished = true;
                        updateSizeToShow(totalSize);
                        updateListView(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn.setText(getText(R.string.btn_text_clean_finished));
                                btn.setClickable(false);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }).start();
            }
        });
        btn.setText(getText(R.string.btn_text_scanning));
        btn.setClickable(false);

        threadsLeft = 3;

        //扫描位置固定的垃圾文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                ApkCacheInfoProvider provider = new ApkCacheInfoProvider(JunkCleanActivity.this);
                provider.setPackageInfoList(pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES));
                provider.scanPackageCacheFile();
                cacheFileList = provider.getCacheInfoList();
                cacheFileSize = provider.getTotalCacheSize();
                updateListView(1);
            }
        }).start();

        //扫描几种常见后缀的垃圾文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String[] postfix = {
                        "weico", "log", "temp", "tmp", "cache"
                };

                FileFilter fileFilter = new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isDirectory()) return true;
                        String name = pathname.getName();
                        for(String i : postfix) {
                            if(name.endsWith(i))
                                return true;
                        }
                        return false;
                    }
                };

                TypedFileInfoProvider provider = new TypedFileInfoProvider(Environment.getExternalStorageDirectory());
                provider.setFileFilter(fileFilter);
                provider.scanFilteredFile();
                ArrayList<File> files = provider.getScannedFiles();
                Iterator<File> it = files.iterator();
                while (it.hasNext()) {
                    System.out.println(it.next().getAbsolutePath());
                }
                typedFileList = provider.getScannedFiles();
                typedFileSize = provider.getTotalFileSize();
                updateListView(2);
            }
        }).start();

        //扫描无用安装包
        new Thread(new Runnable() {
            @Override
            public void run() {
                UnusedApkInfoProvider provider = new UnusedApkInfoProvider(Environment.getExternalStorageDirectory(), JunkCleanActivity.this);
                provider.scanFilteredFile();
                ArrayList<File> files = provider.getScannedFiles();
                Iterator<File> it = files.iterator();
                while (it.hasNext()) {
                    System.out.println(it.next().getAbsolutePath());
                }
                unusedApkList = provider.getScannedFiles();
                unusedApkSize = provider.getTotalFileSize();
                updateListView(3);
            }
        }).start();
    }

    CompoundButton.OnCheckedChangeListener onChildCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CheckBoxTag tag = (CheckBoxTag) buttonView.getTag();
            System.out.println(tag.group + " " + tag.index);
            System.out.println(flags.length);
            System.out.println(flags[0].length);
            flags[tag.group][tag.index] = isChecked;
            if(isChecked) {
                 switch (tag.group){
                     case 0:
                         cacheFileSize += cacheFileList.get(tag.index).cacheSize;
                         break;
                     case 1:
                         typedFileSize += FileUtil.getFileOrDirectorySize(typedFileList.get(tag.index));
                         break;
                     case 2:
                         unusedApkSize += FileUtil.getFileOrDirectorySize(unusedApkList.get(tag.index));
                         break;
                 }
            } else {
                switch (tag.group){
                    case 0:
                        cacheFileSize -= cacheFileList.get(tag.index).cacheSize;
                        break;
                    case 1:
                        typedFileSize -= FileUtil.getFileOrDirectorySize(typedFileList.get(tag.index));
                        break;
                    case 2:
                        unusedApkSize -= FileUtil.getFileOrDirectorySize(unusedApkList.get(tag.index));
                        break;
                }
            }
            totalSize = cacheFileSize + typedFileSize + unusedApkSize;
            updateSizeToShow(totalSize);
        }
    };
/*
    CompoundButton.OnCheckedChangeListener onGroupCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CheckBoxTag tag = (CheckBoxTag) buttonView.getTag();
            System.out.println(tag.group);

        }
    };
*/

    private void updateSizeToShow(final long totalSize) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] strs = TextUtil.formatStorageSizeStrArr(totalSize);
                textSizeNumber.setText("" + strs[0]);
                textSizeUnit.setText("" + strs[1]);
            }
        });
    }

    private void updateListView(final int i) {
        System.out.println("called by " + i);
        //不能用View.post
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                totalSize = cacheFileSize + unusedApkSize + typedFileSize;
                updateSizeToShow(totalSize);
                synchronized (threadsLeft) {
                    threadsLeft--;
                    if(threadsLeft == 0) {
                        flags = new boolean[3][0];
                        flags[0] = new boolean[cacheFileList.size()];
                        flags[1] = new boolean[typedFileList.size()];
                        flags[2] = new boolean[unusedApkList.size()];
                        for(int i = 0; i < flags.length; i++){
                            for(int j = 0; j < flags[i].length; j++){
                                flags[i][j] = true;
                            }
                        }
                        //扫描完成
                        btn.setText(getText(R.string.btn_text_click_to_clean));
                        btn.setClickable(true);
                        progressBar.setVisibility(View.INVISIBLE);
                        scanFinished = true;
                    }
                }
            }
        });
    }



    class MyExpandableListAdapter extends BaseExpandableListAdapter {
        public MyExpandableListAdapter(Context context, ExpandableListView listView) {
            this.context = context;
            this.listView = listView;
        }

        ExpandableListView listView;
        Context context;

        @Override
        public int getGroupCount() {
            //缓存文件 无用apk 自定义缓存文件
            return 3;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case 0:
                    return cacheFileList.size();
                case 1:
                    return typedFileList.size();
                case 2:
                    return unusedApkList.size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupPosition;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            JunkFileGroupView groupView = new JunkFileGroupView(context, null);
            switch (groupPosition) {
                case 0:
                    groupView.setTitleText(context.getString(R.string.group_title_cache_file));
                    groupView.setSizeText(TextUtil.formatStorageSizeStr(cacheFileSize));
                    //groupView.setChecked(true);
                    //groupView.setCheckBoxTag(new CheckBoxTag(groupPosition, -1));
                    break;
                case 1:
                    groupView.setTitleText(context.getString(R.string.group_title_custom_junk_file));
                    groupView.setSizeText(TextUtil.formatStorageSizeStr(typedFileSize));
                    //groupView.setChecked(true);
                    //groupView.setCheckBoxTag(new CheckBoxTag(groupPosition, -1));
                    break;
                case 2:
                    groupView.setTitleText(context.getString(R.string.group_title_unused_apk_file));
                    groupView.setSizeText(TextUtil.formatStorageSizeStr(unusedApkSize));
                    //groupView.setChecked(true);
                    //groupView.setCheckBoxTag(new CheckBoxTag(groupPosition, -1));
                    break;
            }
            groupView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!scanFinished)
                        return;
                    if (!listView.isGroupExpanded(groupPosition))
                        listView.expandGroup(groupPosition);
                    else
                        listView.collapseGroup(groupPosition);
                }
            });
            return groupView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            switch (groupPosition) {
                case 0:
                    if (cacheFileList.size() == 0) {
                        TextView tv = new TextView(context);
                        tv.setPadding(0, 16, 0, 16);
                        tv.setText("empty");
                        return tv;
                    } else {
                        ApkCacheItemView v = new ApkCacheItemView(context, null);
                        v.setPackageCacheItem(cacheFileList.get(childPosition));
                        v.setChecked(flags == null ? true : flags[groupPosition][childPosition]);
                        v.setOnCheckedChangeListener(onChildCheckedChangeListener);
                        v.setCheckBoxTag(new CheckBoxTag(groupPosition, childPosition));
                        return v;
                    }
                case 1:
                    if (typedFileList.size() == 0) {
                        TextView tv = new TextView(JunkCleanActivity.this);
                        tv.setPadding(0, 16, 0, 16);
                        tv.setText("empty");
                        return tv;
                    } else {
                        int index = childPosition;
                        FileItemView v = new FileItemView(context, null);
                        v.setFile(typedFileList.get(index));
                        v.setChecked(flags == null ? true : flags[groupPosition][childPosition]);
                        v.setOnCheckedChangeListener(onChildCheckedChangeListener);
                        v.setCheckBoxTag(new CheckBoxTag(groupPosition, childPosition));

                        return v;
                    }
                    //break;
                case 2:
                    if (unusedApkList.size() == 0) {
                        TextView tv = new TextView(JunkCleanActivity.this);
                        tv.setPadding(0, 16, 0, 16);
                        tv.setText("empty");
                        return tv;
                    } else {
                        int index = childPosition;
                        FileItemView v = new FileItemView(context, null);
                        v.setFile(unusedApkList.get(index));
                        v.setChecked(flags == null ? true : flags[groupPosition][childPosition]);
                        v.setOnCheckedChangeListener(onChildCheckedChangeListener);
                        v.setCheckBoxTag(new CheckBoxTag(groupPosition, childPosition));

                        return v;
                    }
                    //break;
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    class CheckBoxTag {
        public CheckBoxTag(int group, int index) {
            this.group = group;
            this.index = index;
        }

        public int group, index;
    }

    class FileCleanTask extends AsyncTask<Object, Object, Long> {

        @Override
        protected Long doInBackground(Object... params) {
            System.out.println("execute");
            File file;
            while (true) {
                if (filesToDelete.isEmpty())
                    break;
                synchronized (filesToDelete) {
                    file = filesToDelete.poll();
                }
                long size = FileUtil.getFileOrDirectorySize(file);
                //delete here!!
                FileUtil.deleteFileOrDirectory(file);
                publishProgress(file, size);
            }
            sem.release();
            System.out.println("thread exit");
            return 0l;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            File file = (File) values[0];
            Long size = (Long) values[1];
            System.out.println(file.getAbsolutePath() + " " + size);
            totalSize -= size;
            updateSizeToShow(totalSize);
            textCurrentFile.setText(getString(R.string.text_delete, file.getAbsolutePath()));
        }
    }
}
