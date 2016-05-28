package cn.edu.bit.cs.moecleaner;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.cs.moecleaner.systemmonitor.MemoryMonitor;
import cn.edu.bit.cs.moecleaner.systemmonitor.customview.MemoryBoostListItem;
import cn.edu.bit.cs.moecleaner.util.TextUtil;

public class MemoryCleanActivity extends AppCompatActivity {

    TextView textSizeNumber, textSizeUnit, textMemOccupied;
    ListView listView;
    Button btn;
    View progressBar;

    ArrayList<ProcessMemoryInfo> processMemoryInfoList = new ArrayList<>();
    long memoryToClean = 0;

    boolean[] flag;

    static final int SYSTEM_APP_FLAG = ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM;

    ActivityManager.MemoryInfo memoryInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_clean);
        textSizeNumber = (TextView) findViewById(R.id.textView13);
        textSizeUnit = (TextView) findViewById(R.id.textView14);
        textMemOccupied = (TextView) findViewById(R.id.textView25);
        memoryInfo = MemoryMonitor.getBriefMemoryInfo(MemoryCleanActivity.this);
        listView = (ListView) findViewById(R.id.listView);
        btn = (Button) findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBarIndeterminate);

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return processMemoryInfoList.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View v, ViewGroup parent) {
                View convertView = new MemoryBoostListItem(MemoryCleanActivity.this, null);
                ((MemoryBoostListItem) convertView).setProcessMemoryInfo(processMemoryInfoList.get(position));
                if (flag != null)
                    ((MemoryBoostListItem) convertView).getCheckBox().setChecked(flag[position]);
                else
                    ((MemoryBoostListItem) convertView).getCheckBox().setChecked(true);
                ((MemoryBoostListItem) convertView).getCheckBox().setTag(position);
                ((MemoryBoostListItem) convertView).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int position = (int) buttonView.getTag();
                        if(flag == null)
                            return;
                        if (position < 0 || position > flag.length)
                            return;
                        flag[position] = isChecked;
                        if (isChecked) {
                            memoryToClean += processMemoryInfoList.get(position).totalMemory;
                        } else {
                            memoryToClean -= processMemoryInfoList.get(position).totalMemory;
                        }
                        updateMemoryNum();
                    }
                });
                return convertView;
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RunningAppCleanTask().execute();
            }
        });

        new RunningAppScanTask().execute();
    }

    void updateMemoryNum() {
        String[] strs = TextUtil.formatStorageSizeStrArr(memoryToClean);
        textSizeNumber.setText(strs[0]);
        textSizeUnit.setText(strs[1]);
        textMemOccupied.setText(TextUtil.formatStorageSizeStr(memoryToClean)
                + "/"
                + TextUtil.formatStorageSizeStr(memoryInfo.totalMem));
    }

    public class RunningAppScanTask extends AsyncTask<Void, Long, Long> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btn.setClickable(false);
            btn.setText(getText(R.string.btn_text_scanning));
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            updateMemoryNum();
        }

        @Override
        protected Long doInBackground(Void... params) {
            processMemoryInfoList.clear();
            PackageManager pm = MemoryCleanActivity.this.getPackageManager();
            ActivityManager am = (ActivityManager) MemoryCleanActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningAppProcessInfos = am.getRunningServices(100);
            for(ActivityManager.RunningServiceInfo info : runningAppProcessInfos) {
                try {
                    System.out.println(info.process + " " + info.pid);
                    ProcessMemoryInfo pmInfo = new ProcessMemoryInfo();
                    pmInfo.packageInfo = pm.getPackageInfo(info.process, PackageManager.GET_SERVICES);
                    if(info.process.equals(MemoryCleanActivity.this.getPackageName())){
                        continue;
                    }
                    //是系统进程，忽略
                    if((pmInfo.packageInfo.applicationInfo.flags & SYSTEM_APP_FLAG) != 0) {
                        continue;
                    }
                    pmInfo.pid = info.pid;
                    Debug.MemoryInfo memoryInfo = am.getProcessMemoryInfo(new int[]{info.pid})[0];
                    pmInfo.totalMemory = memoryInfo.getTotalPrivateDirty() * 1024;
                    processMemoryInfoList.add(pmInfo);
                    memoryToClean += pmInfo.totalMemory;
                    publishProgress();
                } catch (Exception e) {
                    continue;
                }

            }
            return memoryToClean;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            btn.setText(getString(R.string.btn_text_click_to_clean_mem));
            btn.setClickable(true);
            flag = new boolean[processMemoryInfoList.size()];
            for(int i = 0; i < flag.length; i++){
                flag[i] = true;
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    public class RunningAppCleanTask extends AsyncTask<Void, Long, Long>{
        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
            updateMemoryNum();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btn.setClickable(false);
            btn.setText(getString(R.string.btn_text_cleaning));
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            memoryToClean = 0;
            updateMemoryNum();
            listView.setVisibility(View.INVISIBLE);
            btn.setClickable(false);
            btn.setText(getString(R.string.btn_text_clean_finished));
        }

        @Override
        protected Long doInBackground(Void... params) {
            ActivityManager am = (ActivityManager) MemoryCleanActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
            java.lang.Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (IOException e) {

            }
            for(int i = 0; i < flag.length; i++){
                if(!flag[i]) continue;

                try {
                    //第一种方法 android.os.Process类中的killProcess方法
                    Process.killProcess(processMemoryInfoList.get(i).pid);
                    //第二种方法 ActivityManager的killBackgroundProcesses
                    am.killBackgroundProcesses(processMemoryInfoList.get(i).packageInfo.packageName);
                    //第三种方法 反射调用forceStopPackage方法
                    Method forceStopPackage = am.getClass()
                            .getDeclaredMethod("forceStopPackage", String.class);
                    forceStopPackage.setAccessible(true);
                    forceStopPackage.invoke(am, processMemoryInfoList.get(i).packageInfo.packageName);
                } catch (Exception e) {

                }

                try {
                    //第四种方法 获取root权限之后直接执行kill命令
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("kill " + processMemoryInfoList.get(i).pid + "\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    process.waitFor();
                    os.close();
                } catch (Exception e) {

                }

                memoryToClean -= processMemoryInfoList.get(i).totalMemory;
                publishProgress();
            }
            return null;
        }
    }

    public class ProcessMemoryInfo {
        public PackageInfo packageInfo;
        public int pid;
        public long totalMemory;
    }
}
