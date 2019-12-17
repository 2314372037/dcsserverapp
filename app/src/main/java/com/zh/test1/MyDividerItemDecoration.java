package com.zh.test1;

import android.content.Context;
import androidx.recyclerview.widget.DividerItemDecoration;

public class MyDividerItemDecoration extends DividerItemDecoration {

    public MyDividerItemDecoration(Context context, int orientation) {
        super(context, orientation);
        setDrawable(context.getDrawable(R.color.line));
    }

}
