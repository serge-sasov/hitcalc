package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.completion;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.WarriorInCombatView;

import java.util.ArrayList;

public class ArrangedWarriorPerCompletionCriteriaView extends LinearLayout {

    public ArrangedWarriorPerCompletionCriteriaView(Context context) {
        super(context);
        initializeView(context);
    }

    public ArrangedWarriorPerCompletionCriteriaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public ArrangedWarriorPerCompletionCriteriaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.formation_warriors_per_criteria, this);
    }

    /*
     * activationValue - represent a filter criteria to select the warriors which current weakness value
     *  matches to the given activation value
     * input:
     * - formationActivated flag shows current formation state
     * */
    public void setWarriors(ArrayList<WarriorInCombat> warriors){
        
        updateViewOutput(warriors);
    }

    //Display warriors of the formation
    protected void updateViewOutput(ArrayList<WarriorInCombat> warriors){
        TextView criteria = (TextView) findViewById(R.id.criteria);
        criteria.setText("Criteria"); //Root or Retreat or Check for Root & Retreat

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.warriorsList);

        for (WarriorInCombat warrior : warriors) {
            if (warrior.simple() == true) {
                //show the content only in case the worrier's activation value matches to its weakness state
                int threshold = warrior.getActivationThreshold();

                if(threshold == 1){
                    //Cast from Formation to FormationInCombat
                    WarriorInCombatView warriorView = new WarriorInCombatView(getContext());
                    warriorView.setUnit(warrior);

                    //assign a view id value to the new element
                    int id = View.generateViewId();
                    warriorView.setId(id);

                    linearLayout.addView(warriorView);
                }
            }
        }
    }
}

