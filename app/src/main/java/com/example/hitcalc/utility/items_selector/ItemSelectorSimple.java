package com.example.hitcalc.utility.items_selector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;


import java.util.ArrayList;
import java.util.HashMap;

public abstract class ItemSelectorSimple extends LinearLayout {
    private ArrayList<String> mItems = null;
    private HashMap<Integer, Integer> mItemViewIndex; //list of unique item_id and view_id of used ItemView
    private String mSelectedValue;
    private Integer mSelectedIndex; // -1 - nothing is selected
    private boolean mIsChanged = false; //Check whether the settings was changed
    private String mTitle; //title of the view

    //Specify width and font size of item block
    private Integer mMinEms;
    private  Integer mFontSize;
    private Integer mPaddingLeftValue = 0;
    private Integer mPaddingTopValue = 0;
    private Integer mPaddingRightValue = 0;
    private Integer mPaddingBottomValue = 0;

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnClickItemSelectorListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onItemSelectorClick(String item); //provide item selected to the listener
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    protected OnClickItemSelectorListener mListener;

    // Assign the listener implementing events interface that will receive the events
    public void setOnClickItemSelectorListener(ItemSelectorSimple.OnClickItemSelectorListener listener) {
        mListener = listener;
    }

    public ItemSelectorSimple(Context context) {
        super(context);
        initializeViews(context);
    }

    public ItemSelectorSimple(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ItemSelectorSimple(Context context,
                        AttributeSet attrs,
                        int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *           the current context for the view.
     */
    protected abstract void initializeViews(Context context);

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * Sets the list of value in the spinner, selecting the first value
     * by default.
     *
     * @param values
     *           the values to set in the spinner.
     */
    public void setValues(ArrayList<String> values, String title) {
        mTitle = title;
        setValues(values);
    }

    public void setValues(ArrayList<String> values) {
        mItems = values;
        // Select the first item of the string array by default since
        // the list of value has changed.
        setItemsView();
        showTitle();
    }

    //protected abstract void showTitle();
    private void showTitle(){
        //handle the view title
        TextView title = (TextView) findViewById(R.id.title);
        if(mTitle != null){
            title.setText(mTitle);
        }else{
            title.setVisibility(View.GONE);
        }
    }

    /*
     *
     * */
    private void setItemsView(){
        //prepare a list of items
        if(mItems != null && mItems.size() > 0){
            LinearLayout layout = (LinearLayout) findViewById(R.id.itemList);
            //clear any items leaft from the previous data population
            layout.removeAllViews();
            mSelectedIndex = null;
            mSelectedValue = null;

            mItemViewIndex = new HashMap<Integer, Integer>();

            for(Integer index = 0; mItems.size() > index; index++) {
                ItemView itemView;
                int id;

                itemView = new ItemView(getContext());
                id = View.generateViewId();

                //populate a list of view index to get access to the object by yse of method findViewById()
                mItemViewIndex.put(index, id);

                itemView.setId(id);
                itemView.setPadding(5, 5, 5, 5);
                //Create list of
                itemView.setItem(mItems.get(index), index);

                if(mFontSize != null) {
                    itemView.setTextSize(mFontSize);
                }

                if(mMinEms != null){
                    itemView.setMinEms(mMinEms);
                }

                //if any padding dimension given configure the item
                if(mPaddingLeftValue > 0 || mPaddingTopValue > 0 || mPaddingRightValue > 0 || mPaddingBottomValue > 0){
                    itemView.setPadding(mPaddingLeftValue, mPaddingTopValue, mPaddingRightValue, mPaddingBottomValue);
                }

                layout.addView(itemView);

                //Add a listener to each unit to get control over his state
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //update outlook of the view
                        UpdateViewOutlook(itemView.getIndex());

                        //Update values
                        mSelectedIndex = itemView.getIndex();
                        mSelectedValue = itemView.getItem();

                        //if any listener is used notify it about the event
                        if (mListener != null)
                            mListener.onItemSelectorClick(getSelectedValue()); // <---- fire listener here
                    }
                });
            }
        }
    }

    /*
     * Derive the correct index of the value and assign it to the view
     * */
    public void setSelectedValue(String val){
        //Derive right index value out of the received input
        for(int i=0; i < mItems.size(); i++){
            if(val.equals(mItems.get(i))){
                setSelectedIndex(i);
            }
        }
    }

    /*
    * Perform outlook configuration for minEms and fontSize
    * */
    public void setOutlook(Integer minEms, Integer fontSize, Integer leftValue, Integer topValue, Integer rightValue, Integer bottomValue){
        mMinEms = minEms;
        mFontSize = fontSize;
        //Set padding sizes
        mPaddingLeftValue = leftValue;
        mPaddingTopValue = topValue;
        mPaddingLeftValue = rightValue;
        mPaddingBottomValue = bottomValue;

        setItemsView();
    }

    /**
     * Sets the selected index of the spinner.
     *
     * @param index
     *           the index of the value to select.
     */
    private void setSelectedIndex(Integer index) {
        // If no values are set for the spinner, do nothing.
        UpdateViewOutlook(index);

        // Set the current index and display the value.
        mSelectedIndex = index;
    }


    private  void UpdateViewOutlook(Integer index) {
        // If no values are set for the spinner, do nothing.
        if (index == mSelectedIndex || mItems == null || mItems.size() == 0)
            return;

        if(mSelectedIndex != null) {
            //change state of previously selected item
            ItemView itemView = (ItemView) findViewById(mItemViewIndex.get(mSelectedIndex));
            itemView.changeState();
        }

        //change state of currently selected item
        ItemView itemView = (ItemView) findViewById(mItemViewIndex.get(index));
        itemView.changeState();
    }


    /**
     * Gets the selected value of the spinner, or null if no valid
     * selected index is set yet.
     *
     * @return the selected value of the spinner.
     */
    public String getSelectedValue() {
        // If no values are set for the spinner, return an empty string.
        if (mItems == null || mItems.size() == 0)
            return "";

        // If the current index is invalid, return an empty string.
        if (mSelectedIndex < 0 || mSelectedIndex >= mItems.size())
            return "";

        mSelectedValue = mItems.get(mSelectedIndex);

        return mSelectedValue;
    }

    /*
     * Return selected value converted to Integer
     * */
    public Integer getSelectedIntegerValue(){
        CharSequence ch = getSelectedValue();
        return Integer.parseInt(ch.toString());
    }

    /**
     * Gets the selected index of the spinner.
     *
     * @return the selected index of the spinner.
     */
    public int getSelectedIndex() {
        if(mSelectedIndex != null){
            return mSelectedIndex;
        }
        //return -1 if nothing is selected
        return -1;
    }

    //Return the current value change state
    public boolean isChanged(){
        return mIsChanged;
    }
}
