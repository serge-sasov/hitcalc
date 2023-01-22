package com.example.hitcalc.ui.combat_scenes.army.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;

public class UnitView extends ConstraintLayout {
    private WarriorInShock mWarrior;

    public UnitView(Context context, String type) {
        super(context);
        initializeViews(context, type);
    }

    public UnitView(Context context) {
        super(context);
        initializeViews(context);
    }

    public UnitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public UnitView(Context context,
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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.unit_small, this);
    }

    //If a unit type is provided, choose whether leader or normal unit is to be displayed
    private void initializeViews(Context context, String type) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(type.equals("Leader") || type.equals("Harizma")) {
            inflater.inflate(R.layout.unit_leader, this);
        }else{
            inflater.inflate(R.layout.unit_small, this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //Set user specific data if data were provided
        if(mWarrior != null){
            updateViewOutput();
        }
    }

    //Allow to use drag and drop event
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    //Provide the input data for given unit and update it outlook view
    public void setUnit(WarriorInShock item){
        mWarrior = item;

        //Refresh the output values on the unit view
        updateViewOutput();
    }
    //
    protected void updateViewOutput(){
        TextView unitTitleView, unitAddPropertyView, unitSizeView, unitTypeView, unitTQView, unitMoveView, unitProperty;
        //Set color schema to the unit
        Integer color = WarriorItem.mUnitColors.get(mWarrior.color());

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.counter_box);
        layout.setBackgroundResource(color);

        //Define unit type and handle following fields depending on the type "commander" or any other simple warrior unit
        unitTypeView = (TextView) this.findViewById(R.id.unitType);
        String unitType = mWarrior.type();

        //Replace Dummy throw D
        if(unitType.equals("Dummy")){
            unitTypeView.setText("D");
        }else{
            unitTypeView.setText(unitType);
        }

        //Set unit title
        unitTitleView = (TextView) this.findViewById(R.id.counterTitle);
        unitTitleView.setText(mWarrior.title());

        if(!(unitType.equals("Leader") || unitType.equals("Harizma"))){
            //Hide commander status as it is unused for this view
            TextView comanderField = (TextView) this.findViewById(R.id.unitComander);
            comanderField.setVisibility(View.INVISIBLE);

            //For all units except leaders populate following fields
            unitSizeView = (TextView) this.findViewById(R.id.unitSize);
            unitTQView = (TextView) this.findViewById(R.id.unitTQ);
            unitMoveView = (TextView) this.findViewById(R.id.unitMove);

            unitSizeView.setText(mWarrior.size().toString());
            unitTQView.setText(mWarrior.troopQuality().toString());
            unitMoveView.setText(mWarrior.movement().toString());

            //For all units except leaders populate following fields
            TextView unitComander = (TextView) this.findViewById(R.id.unitComander);
            if (mWarrior.leader() != null || mWarrior.harizma() != null) {
                //to define what to display
                unitComander.setVisibility(View.VISIBLE);

                if(mWarrior.leader().type().equals("Harizma")){
                    unitComander.setText("H");
                }
                else{
                    unitComander.setText("C");
                }
            } else {
                unitComander.setVisibility(View.INVISIBLE);
            }
            unitAddPropertyView = (TextView) this.findViewById(R.id.unitAddProp);
            if(mWarrior.addProperty() != null){
                unitAddPropertyView.setText(mWarrior.addProperty());

            }else {
                unitAddPropertyView.setVisibility(View.GONE);
            }
        }
        else{
            //set H or L for Harisma and Leader respectively
            unitProperty = (TextView) findViewById(R.id.unitProperty);

            if(unitType.equals("Harizma")){
                unitTitleView.setTextColor(Color.BLACK);
                unitProperty.setText("H");
            }
            else{
                unitProperty.setText("L");
            }

            if (mWarrior.color().equals("white")) {
                unitTitleView.setTextColor(Color.BLACK);
            }
        }
        //Applicable for all units type
        if (mWarrior.color().equals("yellow") || mWarrior.color().equals("turquoise")) {
            unitTitleView.setTextColor(Color.BLACK);
            unitTypeView.setTextColor(Color.BLACK);
        }
    }

    public WarriorInShock warrior(){
        return mWarrior;
    }
}
