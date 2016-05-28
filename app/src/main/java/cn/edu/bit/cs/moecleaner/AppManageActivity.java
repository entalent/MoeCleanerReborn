package cn.edu.bit.cs.moecleaner;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.widgets.Dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.bit.cs.moecleaner.ui.customview.AppListItemView;
import cn.edu.bit.cs.moecleaner.util.FileUtil;
import cn.edu.bit.cs.moecleaner.util.PackageInfoUtil;
import cn.edu.bit.cs.moecleaner.util.TextUtil;

public class AppManageActivity extends AppCompatActivity {

    com.gc.materialdesign.views.Button btnUninstall, btnBackup;
    ListView listView;
    TextView textView;

    ArrayList<AppListItem> appInfoList = new ArrayList<>();
    long appTotalSize;

    View progressBar;

    boolean[] flag;

    static final int SYSTEM_APP_FLAG = ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manage);

        btnBackup = (com.gc.materialdesign.views.Button) findViewById(R.id.btn_backup);
        btnUninstall = (com.gc.materialdesign.views.Button) findViewById(R.id.btn_uninstall);
        textView = (TextView) findViewById(R.id.textView26);
        listView = (ListView) findViewById(R.id.listView2);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);


        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return appInfoList.size();
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
            public View getView(int position, View convertView, ViewGroup parent) {
                AppListItemView view;
                if(convertView == null) {
                    convertView = new AppListItemView(AppManageActivity.this, null);
                }
                view = (AppListItemView)convertView;
                view.setAppListItem(appInfoList.get(position));
                if(flag == null)
                    view.setChecked(false);
                else
                    view.setChecked(flag[position]);
                view.setCheckBoxTag(position);
                view.setOnCheckedChangeListener(onCheckedChangeListener);
                return view;
            }
        });

        btnUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<AppListItem> tmpList = new ArrayList<AppListItem>();
                for(int i = 0; i < flag.length; i++){
                    if(flag[i]) {
                        tmpList.add(appInfoList.get(i));
                    }
                }
                if(tmpList.size() == 0) return;
                final Dialog dialog = new Dialog(AppManageActivity.this,
                        getString(R.string.dialog_title_alert),
                        getString(R.string.dialog_content_delete_confirm, tmpList.size()));
                dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(AppListItem i : tmpList) {
                            Uri packageURI = Uri.parse("package:" + i.packageInfo.packageName);
                            Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
                            startActivity(intent);
                        }
                        new QueryAllAppTask().execute();
                    }
                });
                dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                    }
                });
                dialog.show();
            }
        });

        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<AppListItem> tmpList = new ArrayList<AppListItem>();
                for(int i = 0; i < flag.length; i++){
                    if(flag[i]) {
                        tmpList.add(appInfoList.get(i));
                    }
                }
                if(tmpList.size() == 0){
                    return;
                }
                new BackupAppTask().execute(tmpList);
            }
        });

        new QueryAllAppTask().execute();
    }

    public class QueryAllAppTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            btnUninstall.setClickable(true);
            btnBackup.setClickable(true);
            textView.setText(getString(R.string.text_all_apps, appInfoList.size(),
                    TextUtil.formatStorageSizeStr(appTotalSize)));
            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
            flag = new boolean[appInfoList.size()];
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnUninstall.setClickable(false);
            btnBackup.setClickable(false);
            appInfoList.clear();
            progressBar.setVisibility(View.VISIBLE);
            textView.setText(getText(R.string.text_query_in_progress));
        }

        @Override
        protected Void doInBackground(Void... params) {
            PackageManager pm = getPackageManager();
            List<PackageInfo> list = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            appTotalSize = 0;
            for(PackageInfo i : list) {
                //系统应用
                if((i.applicationInfo.flags & SYSTEM_APP_FLAG) != 0) {
                    continue;
                }
                try {
                    long size = PackageInfoUtil.getPkgTotalSize(AppManageActivity.this, i.packageName);
                    AppListItem item = new AppListItem();
                    item.packageInfo = i;
                    item.totalSize = size;
                    appInfoList.add(item);
                    appTotalSize += size;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public class BackupAppTask extends AsyncTask<ArrayList, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnBackup.setClickable(false);
            btnUninstall.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(ArrayList... params) {
            ArrayList<AppListItem> appList = params[0];
            File backupDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AppBackup");
            if(!backupDir.exists()) {
                if(!backupDir.mkdirs()) {
                    return 0;
                }
            }
            if(!backupDir.canWrite()) {
                return 0;
            }
            int res = 0;
            for(AppListItem i : appList) {
                File apkDir = new File(i.packageInfo.applicationInfo.sourceDir);
                File newApkDir = new File(backupDir.getAbsolutePath() + File.separator +
                        i.packageInfo.packageName + " " + i.packageInfo.versionName + ".apk");
                if(newApkDir.exists()) {
                    newApkDir.delete();
                }
                res += FileUtil.copyFile(apkDir, newApkDir) ? 1 : 0;
            }
            return res;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            btnBackup.setClickable(true);
            btnUninstall.setClickable(true);
            Toast.makeText(AppManageActivity.this, getString(R.string.toast_backup_finish, integer), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int index = (int) buttonView.getTag();
            flag[index] = isChecked;
        }
    };

    public class AppListItem {
        public PackageInfo packageInfo;
        public long totalSize;
    }
}
