package cn.edu.bit.cs.moecleaner.ui.customview;

import android.widget.CheckBox;

/**
 * Created by entalent on 2016/5/24.
 */
public interface ICheckableView {
    public void setOnCheckedChangeListener(CheckBox.OnCheckedChangeListener listener);

    public void setChecked(boolean checked);

    public void setCheckBoxTag(Object tag);
}
