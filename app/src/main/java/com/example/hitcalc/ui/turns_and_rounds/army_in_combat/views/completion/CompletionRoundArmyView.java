package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.completion;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.AffectedFormationWarriors;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.FormationTurnView;
import com.example.hitcalc.utility.LoadTable;

import java.util.HashMap;
import java.util.Random;

/*
* Show a list of formation which warriors are reached a given threshold and
* need to undertake a given actions as root and retreat if is needed.
* */
public class CompletionRoundArmyView extends LinearLayout {
    private ArmyInCombat mArmy;
    private HashMap<String, Integer> mMappingFormationToViewIds; //mapping between formation & view Ids

    public CompletionRoundArmyView(Context context) {
        super(context);
        initializeView(context);
    }

    public CompletionRoundArmyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public CompletionRoundArmyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.army_turn_view, this);
    }

    public void setArmy(ArmyInCombat army){
        mArmy = army;
        //Random Value
        Random mRandomDiceRoll = new Random();

        HashMap<String, AffectedFormationWarriors> armyEffect = mArmy.calculateRoundCompletionEffect(mRandomDiceRoll);
        updateViewOutput(armyEffect);
    }

    //Show up affected army formations
    protected void updateViewOutput(HashMap<String, AffectedFormationWarriors> armyEffect){

        if(armyEffect != null){
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.armyInCombatLayout);
            mMappingFormationToViewIds = new HashMap<String, Integer>();

            for(String title :  armyEffect.keySet()){

                CompletionRoundFormationView formationView = new CompletionRoundFormationView(getContext());
                //By default not to show the formation members to speed up the layout load
                formationView.setFormation(mArmy, title, armyEffect.get(title));

                int id = View.generateViewId();
                mMappingFormationToViewIds.put(title, id);

                formationView.setId(id);
                linearLayout.addView(formationView);
            }
        }
    }
}
