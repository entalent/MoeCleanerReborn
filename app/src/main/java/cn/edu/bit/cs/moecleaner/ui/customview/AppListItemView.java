package cn.edu.bit.cs.moecleaner.ui.customview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.edu.bit.cs.moecleaner.AppManageActivity;
import cn.edu.bit.cs.moecleaner.R;
import cn.edu.bit.cs.moecleaner.util.TextUtil;

/**
 * Created by entalent on 2016/5/25.
 */
public class AppListItemView extends RelativeLayout implements ICheckableView {
    ImageView icon;
    TextView textAppName, textTotalSize, textSubText;
    CheckBox checkBox;

    public AppListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_applistitem, this);
        icon = (ImageView) findViewById(R.id.imageView);
        textAppName = (TextView) findViewById(R.id.text_app_name);
        textTotalSize = (TextView) findViewById(R.id.text_total_size);
        textSubText = (TextView) findViewById(R.id.text_sub_text);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
    }

    @Override
    public void setOnCheckedChangeListener(CheckBox.OnCheckedChangeListener listener) {
        checkBox.setOnCheckedChangeListener(listener);
    }

    @Override
    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }

    @Override
    public void setCheckBoxTag(Object tag) {
        checkBox.setTag(tag);
    }

    public void setAppListItem(AppManageActivity.AppListItem item) {
        PackageManager pm = getContext().getPackageManager();
        textAppName.setText(pm.getApplicationLabel(item.packageInfo.applicationInfo));
        textTotalSize.setText(TextUtil.formatStorageSizeStr(item.totalSize));
        textSubText.setText(item.packageInfo.versionName);
        icon.setImageDrawable(pm.getApplicationIcon(item.packageInfo.applicationInfo));
    }
}
