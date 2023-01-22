package com.example.hitcalc.utility.items_selector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;

import java.util.ArrayList;

public class ItemSelector extends LinearLayout {

    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ArrayList<String> mItems = null;
    private String mSelectedValue;
    private int mSelectedIndex = 0;
    private boolean mIsChanged = false; //Check whether the settings was changed

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnClickItemSelectorListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onItemSelectorClick(String item); //provide item selected to the listener
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private OnClickItemSelectorListener mListener;

    // Assign the listener implementing events interface that will receive the events
    public void setOnClickItemSelectorListener(ItemSelector.OnClickItemSelectorListener listener) {
        mListener = listener;
    }

    public ItemSelector(Context context) {
        super(context);
        initializeViews(context);
    }

    public ItemSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ItemSelector(Context context,
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
    private void initializeViews(Context context) {
        mListener = null;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_selector, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Sets the images for the previous and next buttons. Uses
        // built-in images so you don't need to add images, but in
        // a real application your images should be in the
        // application package so they are always available.
        mPreviousButton = (ImageButton) this
                .findViewById(R.id.sidespinner_view_previous);
        mPreviousButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (mSelectedIndex > 0) {
                    int newSelectedIndex = mSelectedIndex - 1;
                    setSelectedIndex(newSelectedIndex);

                    //Change state to show the value was changed
                    mIsChanged = true;

                    //Notify listeners about new value chosen by user
                    if (mListener != null)
                        mListener.onItemSelectorClick(getSelectedValue()); // <---- fire listener here
                }
            }
        });

        mNextButton = (ImageButton)this
                .findViewById(R.id.sidespinner_view_next);
        mNextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (mItems != null
                        && mSelectedIndex < mItems.size() - 1) {
                    int newSelectedIndex = mSelectedIndex + 1;
                    setSelectedIndex(newSelectedIndex);

                    //Change state to show the value was changed
                    mIsChanged = true;

                    //Notify listeners about new value chosen by user
                    if (mListener != null)
                        mListener.onItemSelectorClick(getSelectedValue()); // <---- fire listener here
                }
            }
        });

        // Select the first value by default.
        setSelectedIndex(0);
    }

    /**
     * Sets the list of value in the spinner, selecting the first value
     * by default.
     *
     * @param values
     *           the values to set in the spinner.
     */
    public void setValues(ArrayList<String> values) {
        mItems = values;

        // Select the first item of the string array by default since
        // the list of value has changed.
        setSelectedIndex(0);
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

    /**
     * Sets the selected index of the spinner.
     *
     * @param index
     *           the index of the value to select.
     */
    public void setSelectedIndex(int index) {
        // If no values are set for the spinner, do nothing.
        if (mItems == null || mItems.size() == 0)
            return;



        // If the index value is invalid, do nothing.
        if (index < 0 || index >= mItems.size())
            return;

        // Set the current index and display the value.
        mSelectedIndex = index;
        TextView currentValue;
        currentValue = (TextView)this
                .findViewById(R.id.value);
        //Get correct value of the index if array value starts not from 0 value


        currentValue.setText(mItems.get(index));

        //Do not allow to go index under 0
        if (mSelectedIndex < 0) mSelectedIndex = 0;

        //Do not allow to go index above 0 the higher value
        if (mSelectedIndex == mItems.size()) mSelectedIndex = mItems.size() - 1;

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
        return mSelectedIndex;
    }

    //Return the current value change state
    public boolean isChanged(){
        return mIsChanged;
    }
}
