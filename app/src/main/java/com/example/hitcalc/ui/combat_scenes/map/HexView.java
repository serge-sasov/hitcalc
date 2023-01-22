package com.example.hitcalc.ui.combat_scenes.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.views.UnitView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;

public class HexView extends LinearLayout {
    private String mHexTitle;
    private WarriorInShock mWarrior;

    //Coordinates of used hex
    private Integer mCoordinateX;
    private Integer mCoordinateY;

    private Boolean mToBeMoved = false; //Shows whether the action was started from the given hex or that some moving view was just moved over it.

    private boolean mIsSelected = false; //defines the selection state of the hex

    public WarriorInShock warrior() {
        return mWarrior;
    }

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnClickHexListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onHexClick(Integer X, Integer Y); //coordinates X & Y
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private OnClickHexListener mListener;

    public HexView(Context context) {
        super(context);
        initializeViews(context);

    }

    public HexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public HexView(Context context,
                   AttributeSet attrs,
                   int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        mListener = null;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.hex_view, this);
    }

    // Assign the listener implementing events interface that will receive the events
    public void setOnClickHexListener(OnClickHexListener listener) {
        mListener = listener;
    }

    //provide the hex settings for text
    public void setHex(String hexTitle){
        mHexTitle = hexTitle;

        updateViewOutput();
    }

    //provide the hex settings for warrior placed on the selected hex
    public void setHex(WarriorInShock item){
        mWarrior = item;
        updateViewOutput();
    }

    //Update the content of the hex according to the data provided
    protected void updateViewOutput(){

        TextView textView = (TextView) findViewById(R.id.hexTitle);
        UnitView unitView = (UnitView) findViewById(R.id.smallUnit);
        ViewGroup.LayoutParams layoutParamsSUV = unitView.getLayoutParams();

        //Show/Hide the not relevant view - either hint message or warrior view
        if(mWarrior != null){
            //Show the warrior placed on the hex
            textView.setVisibility(View.GONE);

            unitView.setVisibility(View.VISIBLE);
            unitView.setUnit(mWarrior);

        } else if(mHexTitle != null){
            //Show the hex title giving the hint how the hex is interpreted by
            textView.setVisibility(View.VISIBLE);
            //Show translated hex titles
            switch (mHexTitle){
                case "Front":
                    textView.setText(getResources().getString(R.string.FrontHex));
                    break;
                case "Flank":
                    textView.setText(getResources().getString(R.string.FlankHex));
                    break;
                case "Rear":
                    textView.setText(getResources().getString(R.string.RearHex));
                    break;
                case "Defender":
                    textView.setText(getResources().getString(R.string.DefenderHex));
                    break;

                    default:
                        textView.setText(mHexTitle);
            }

            unitView.setVisibility(View.GONE);
        }
    };

    public void setCoordinateX(Integer x) {
        mCoordinateX = x;
    }

    public void setCoordinateY(Integer y) {
        mCoordinateY = y;
    }

    public Integer getCoordinateX() {
        return mCoordinateX;
    }

    public Integer getCoordinateY() {
        return mCoordinateY;
    }

    //Use the operation to change state of hex selection and return the actual state of the hex
    public boolean selectHex(){
        LinearLayout layout = layout = (LinearLayout) findViewById(R.id.hexLayout);;

        //inverse the state of the hex
        mIsSelected = !mIsSelected;
        if(mIsSelected){
            //If user selected the hex, highlight the click with bold red border
            layout.setBackgroundResource(R.drawable.rounded_border_map_selected);
        }
        else{
            layout.setBackgroundResource(R.drawable.rounded_border_map);
        }

        return mIsSelected;
    }

    @Override
    /*
    *
    * */
    protected void onFinishInflate() {
        super.onFinishInflate();

        //set listener for each hex
        LinearLayout layout = (LinearLayout) findViewById(R.id.hexLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get coordinates of selected hex
                if (mListener != null)
                    mListener.onHexClick(mCoordinateX, mCoordinateY); // <---- fire listener here
            }
        });
    }

    public Boolean toBeMoved(){
        return mToBeMoved;
    }

    public void toBeMoved(Boolean toBeMoved){
        mToBeMoved = toBeMoved;
    }

    //Clear out any units placed on the hex
    protected void clearHex(){
        mWarrior = null;
        mToBeMoved = false; //set to default state

        updateViewOutput();
    }

}
