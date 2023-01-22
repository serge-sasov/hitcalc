package com.example.hitcalc.utility.items_selector;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
/*
* Holds item and its current state - selected or not selected
* */
public class ItemView extends LinearLayout {
    private String mValue;
    private Integer mIndex;
    private  boolean mState = false; //by default unselected

    public ItemView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ItemView(Context context,
                    AttributeSet attrs,
                    int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context the current context for the view.
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    //Change item state on the opposite value - true or false
    public void changeState() {
        mState = !mState;

        //Update view outlook
        updateView();
    }

    private void updateView(){
        TextView textView = (TextView) findViewById(R.id.value);
        textView.setText(mValue);

        if(mState == true){
            textView.setBackgroundResource(R.drawable.rounded_border_red);
        }else{
            textView.setBackgroundResource(R.drawable.rounded_border_blue);
        }
    }

    /**
     * Set the given item value
     *
     * @param value the item to be hold by the view.
     */
    public void setItem(String value, Integer index) {
        mValue = value;
        mIndex = index;

        //Update view outlook
        updateView();
    }

    public void setMinEms(Integer minEms){
        TextView textView = (TextView) this.findViewById(R.id.value);
        textView.setMinEms(minEms);
    }

    public void setPadding(Integer leftValue, Integer topValue, Integer rightValue, Integer bottomValue){
        TextView textView = (TextView) this.findViewById(R.id.value);
        textView.setPadding(leftValue, topValue, rightValue, bottomValue);
    }

    public void setTextSize(Integer size){
        TextView textView = (TextView) this.findViewById(R.id.value);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public String getItem() {
        return mValue;
    }

    public Integer getIndex() {
        return mIndex;
    }
}