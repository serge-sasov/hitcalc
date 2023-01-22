package com.example.hitcalc.utility.items_selector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.hitcalc.R;

public class ItemSelectorSimpleHorizontal extends ItemSelectorSimple {

    public ItemSelectorSimpleHorizontal(Context context) {
        super(context);
    }

    public ItemSelectorSimpleHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemSelectorSimpleHorizontal(Context context,
                              AttributeSet attrs,
                              int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *           the current context for the view.
     */
    protected void initializeViews(Context context) {
        mListener = null;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_selector_simple_horizontal, this);
    }
}
