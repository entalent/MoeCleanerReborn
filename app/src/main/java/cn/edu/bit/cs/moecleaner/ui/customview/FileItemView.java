package cn.edu.bit.cs.moecleaner.ui.customview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cn.edu.bit.cs.moecleaner.R;
import cn.edu.bit.cs.moecleaner.util.FileUtil;
import cn.edu.bit.cs.moecleaner.util.TextUtil;

/**
 * Created by entalent on 2016/5/24.
 */
public class FileItemView extends RelativeLayout implements ICheckableView {
    PackageManager pm;
    File file;
    TextView appNameText, cacheSizeText;
    ImageView appIconImage;
    CheckBox checkBox;

    public FileItemView (Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_junkcleanlistitem, this);
        appNameText = (TextView) findViewById(R.id.text_app_name);
        cacheSizeText = (TextView) findViewById(R.id.text_total_size);
        appIconImage = (ImageView) findViewById(R.id.imageView);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        pm = getContext().getPackageManager();
    }

    public void setFile(File file) {
        this.file = file;
        appNameText.setText(file.getName());
        cacheSizeText.setText(TextUtil.formatStorageSizeStr(FileUtil.getFileOrDirectorySize(file)));
    }

    public File getFile() {
        return this.file;
    }

    public void setOnCheckedChangeListener(CheckBox.OnCheckedChangeListener listener) {
        this.checkBox.setOnCheckedChangeListener(listener);
    }

    public void setChecked(boolean checked) {
        this.checkBox.setChecked(checked);
    }

    @Override
    public void setCheckBoxTag(Object tag) {
        this.checkBox.setTag(tag);
        this.setTag(tag);
    }
}
