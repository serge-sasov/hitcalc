package com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.ActionPoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;

import java.util.ArrayList;

public class TurnActionMarkerView extends ConstraintLayout {
    private ArrayList<TurnAbstractAction> mActions;
    private Boolean mActionChosen = false; //flag to indicate a current state of given action

    public TurnActionMarkerView(Context context) {
        super(context);
        initializeView(context);
    }

    public TurnActionMarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public TurnActionMarkerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        mActions = new ArrayList<TurnAbstractAction>();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.turn_action_marker, this);
    }

    //Populate applicable actions
    public void setAction(TurnAbstractAction action){
        if(action.getClass() == ActionPoint.class) {
            //if there is any action that differs from ActionPoint, then clear out them
            if(mActions.size() > 0 && mActions.get(mActions.size()-1).getClass() != ActionPoint.class){
                mActions.clear();
            }
        }else{
            //if any different from action point item just clear out the previous one
            mActions.clear();
        }

        mActions.add(action);
        updateViewOutput();
    }
    // get all applied actions
    public ArrayList<TurnAbstractAction> getActions() {
        return mActions;
    }

    //get the last action
    public TurnAbstractAction getLastAction() {
        if(mActions.size() > 0) {
            return mActions.get(mActions.size() - 1);
        }
        return null;
    }

    //Display warriors of the formation
    protected void updateViewOutput(){
        //Set color schema to the unit
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.counter_box);

        Integer color = WarriorItem.mUnitColors.get(getLastAction().color());
        layout.setBackgroundResource(color);

        //Set action title
        TextView actionTitle = (TextView) findViewById(R.id.actionTitle);
        if(mActions != null && mActions.size() == 1){
            //Localize action title
            int id = getLastAction().titleId();
            String title = getResources().getString(id);

            actionTitle.setText(title);
        }else if(mActions.size() > 1){
            //localize "APs"
            String APsStr = getResources().getString(R.string.APs);
            actionTitle.setText(mActions.size() + " " + APsStr);
        }

        if(getLastAction().color().equals("yellow")){
            actionTitle.setTextColor(Color.BLACK);
        }
    }

    //invalidate all data stored
    public void clear(){
        if(mActions.size() > 0){
            mActions.clear();
        }
    }

    public void setToDefaultOutlook(){
        //Set to default color
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.counter_box);
        Integer color = WarriorItem.mUnitColors.get("silver");
        layout.setBackgroundResource(color);

        //Set to default text
        TextView actionTitle = (TextView) findViewById(R.id.actionTitle);
        actionTitle.setText("Free");
    }
}
