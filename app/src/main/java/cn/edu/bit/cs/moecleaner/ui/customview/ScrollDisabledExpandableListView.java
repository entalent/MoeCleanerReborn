package cn.edu.bit.cs.moecleaner.ui.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * Created by entalent on 2016/5/24.
 */
public class ScrollDisabledExpandableListView extends ExpandableListView {

    public ScrollDisabledExpandableListView(Context context) {
        super(context);
    }

    public ScrollDisabledExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
