package com.zh.dcsservertools.helper;

import android.content.Context;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.zh.dcsservertools.R;

public class MyDividerItemDecoration extends DividerItemDecoration {

    public MyDividerItemDecoration(Context context, int orientation) {
        super(context, orientation);
        setDrawable(context.getDrawable(R.drawable.item_decoration));
    }

}
