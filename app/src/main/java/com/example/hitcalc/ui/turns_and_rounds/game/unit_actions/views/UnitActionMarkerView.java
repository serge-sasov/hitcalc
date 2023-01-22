package com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.UnitAbstractAction;

public class UnitActionMarkerView extends ConstraintLayout {
    private UnitAbstractAction mAction;
    private Boolean mActionChosen = false; //flag to indicate a current state of given action

    public UnitActionMarkerView(Context context) {
        super(context);
        initializeView(context);
    }

    public UnitActionMarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public UnitActionMarkerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.unit_action_marker, this);
    }

    public void setAction(UnitAbstractAction action){
        mAction = action;
        updateViewOutput();
    }

    //Display warriors of the formation
    protected void updateViewOutput(){

        //Set color schema to the unit
        Integer color = WarriorItem.mUnitColors.get("default");

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.counter_box);

        //set width for double sized units
        if(mAction.isDoubleSize()){
            LayoutParams layoutParams = (LayoutParams) layout.getLayoutParams();
            layoutParams.width = 2 * layoutParams.width + 5;
            layout.setLayoutParams(layoutParams);
        }

        layout.setBackgroundResource(color);

        if(mAction != null){
            TextView actionTitle = (TextView) findViewById(R.id.actionTitle);
            //localizing the title
            String title = mAction.getTitle();
            if(mAction.getTitleId() != null){
                title = getResources().getString(mAction.getTitleId());
            }

            actionTitle.setText(title);
        }
    }

    //Get a current state of the given action
    public Boolean isActionChosen(){
        return mActionChosen;
    }

    public void changeActionState(){
        if(mActionChosen == false){
            mActionChosen = true;
            setActionStateOutlook("red");
            mAction.apply();

        }else{
            mActionChosen = false;
            setActionStateOutlook("default");
            mAction.rollback();
        }
    }

    protected void setActionStateOutlook(String colourProfile) {

        //Set color schema to the unit
        Integer color = WarriorItem.mUnitColors.get(colourProfile);

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.counter_box);
        layout.setBackgroundResource(color);
    }

    //rollback action made
    public void rollback(){
        if(mActionChosen){
            mAction.rollback();
        }
    }
}
