package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.MoveWarrior;

public class WarriorInCombatView extends ConstraintLayout implements WarriorInCombat.OnDataChangeListener{
    private WarriorInCombat mWarrior;
    private MoveWarrior mRecoverWarriorListener = new MoveWarrior();

    // provide multiple references to recovering warrior items as listeners for manual action
    public void setOnRecoverListener(MoveWarrior.OnWarriorMoveListener listener) {
        mRecoverWarriorListener.setOnMoveListener(listener);
    }

    //allow to listen to the warrior moving event
    public void enableWarriorMove(WarriorInCombat warrior){
        mRecoverWarriorListener.enableWarriorMove(null, this, warrior);
    }

    public WarriorInCombatView(Context context) {
        super(context);
        initializeViews(context);
    }

    public WarriorInCombatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public WarriorInCombatView(Context context,
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
        inflater.inflate(R.layout.warrior_in_combat, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //Set user specific data if data were provided
        if(mWarrior != null){
            updateViewOutput();
        }
    }

    public void setUnit(WarriorInCombat warrior){
        mWarrior = warrior;
        mWarrior.setOnDataChangedListener((WarriorInCombatView) this);

        updateViewOutput();
    }


    private void updateViewOutput(){
        TextView unitTitleView, unitTypeView, unitWeaknessView, unitWeaknessLimitView;
        //Set color schema to the unit
        Integer color = WarriorItem.mUnitColors.get(mWarrior.color());

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.counter_box);

        //set width for double sized units
        if(mWarrior.twoHexSize() == true){
            LayoutParams layoutParams = (LayoutParams) layout.getLayoutParams();
            layoutParams.width = 2 * layoutParams.width - 100;
            layout.setLayoutParams(layoutParams);
        }

        layout.setBackgroundResource(color);

        //Define unit type and handle following fields depending on the type "commander" or any other simple warrior unit
        unitTypeView = (TextView) this.findViewById(R.id.unitType);
        unitTypeView.setText(mWarrior.type());

        //Set unit title
        unitTitleView = (TextView) this.findViewById(R.id.counterTitle);
        unitTitleView.setText(mWarrior.title());

        //Set weakness limit
        unitWeaknessLimitView = (TextView) this.findViewById(R.id.weaknessLimit);
        unitWeaknessLimitView.setText(mWarrior.getWeaknessLimit().toString());

        //Set current weakness value
        unitWeaknessView = (TextView) this.findViewById(R.id.weakness);
        unitWeaknessView.setText(mWarrior.getWeakness().toString());

        if (mWarrior.color().equals("white")) {
            unitTitleView.setTextColor(Color.BLACK);
        }
        //Applicable for all units type
        if (mWarrior.color().equals("yellow") || mWarrior.color().equals("turquoise")) {
            unitTitleView.setTextColor(Color.BLACK);
            unitTypeView.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onWeaknessChanged(Integer weakness) {
        TextView unitTitleView, unitTypeView, unitWeaknessView, unitWeaknessLimitView;

        //Set current weakness value
        unitWeaknessView = (TextView) this.findViewById(R.id.weakness);
        unitWeaknessView.setText(weakness.toString());
    }
}

