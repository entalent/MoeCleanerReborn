package cn.edu.bit.cs.moecleaner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import cn.edu.bit.cs.moecleaner.receiver.AlarmReceiver;
import cn.edu.bit.cs.moecleaner.receiver.AlarmSetter;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CODE_CHECK_PHONE_STATE = 0x1,
            REQUEST_CODE_STORAGE = 0x2;

    View junkCleanItem, memoryCleanItem, appManageItem, systemInfoItem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmSetter.setAlarm(MainActivity.this);
        setContentView(R.layout.activity_main);
        junkCleanItem = findViewById(R.id.item_junk_clean);
        memoryCleanItem = findViewById(R.id.item_memory_clean);
        appManageItem = findViewById(R.id.item_app_manage);
        systemInfoItem = findViewById(R.id.item_system_info);
        junkCleanItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, JunkCleanActivity.class));
            }
        });
        memoryCleanItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MemoryCleanActivity.class));
            }
        });
        appManageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AppManageActivity.class));
            }
        });
        systemInfoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SystemInfoActivity.class));
            }
        });

        if(Build.VERSION.SDK_INT >= 23) {
            if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
                //requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_CHECK_PHONE_STATE);
            }
            if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0 || grantResults.length == 0) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CHECK_PHONE_STATE:
                break;
            case REQUEST_CODE_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MainActivity.this, getString(R.string.toast_need_storage_permission, getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
