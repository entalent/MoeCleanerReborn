package cn.edu.bit.cs.moecleaner.ui.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.edu.bit.cs.moecleaner.R;

/**
 * Created by entalent on 2016/5/24.
 */
public class JunkFileGroupView extends RelativeLayout implements ICheckableView {
    TextView titleText, sizeText;
    CheckBox checkBox;

    public JunkFileGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_junkfilegroup, this);
        titleText = (TextView) findViewById(R.id.textView23);
        sizeText = (TextView) findViewById(R.id.textView24);
        checkBox = (CheckBox) findViewById(R.id.checkBox2);
    }

    public void setTitleText(String text) {
        titleText.setText(text);
    }

    public void setSizeText(String text) {
        sizeText.setText(text);
    }

    @Override
    public void setOnCheckedChangeListener(CheckBox.OnCheckedChangeListener listener) {
        checkBox.setOnCheckedChangeListener(listener);
    }

    @Override
    public void setChecked(boolean checked) {
        this.checkBox.setChecked(checked);
    }

    @Override
    public void setCheckBoxTag(Object tag) {
        this.checkBox.setTag(tag);
    }
}
