package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.completion;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.AffectedFormationWarriors;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.ArrangedWarriorsPerActivation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CompletionRoundFormationView extends LinearLayout {
    public CompletionRoundFormationView(Context context) {
        super(context);
        initializeView(context);
    }

    public CompletionRoundFormationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public CompletionRoundFormationView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.formation_round_view, this);
    }

    //configure the input data
    public void setFormation(Army army, String formationTitle, AffectedFormationWarriors formation){
        updateView(army, formationTitle, formation);
    }


    public void updateView(Army army, String formationTitle, AffectedFormationWarriors formation) {
        TextView formationTitleView = (TextView) findViewById(R.id.formationTitle);
        formationTitleView.setText(formationTitle);

        //------------------ Populate the primary formation list first -----------------------------
        //get a layout the further view structures (arranged warrior lists or sub formations) to be attached to
        LinearLayout innerStructure = (LinearLayout) findViewById(R.id.innerStructure);

        //Attach the arranged warrior lists first
        HashMap<String, ArrayList<WarriorInCombat>> warriors = formation.getEffectToWarriors();
        if(warriors != null && warriors.size() > 0) {
            for(String effect: warriors.keySet()){
                ArrayList<WarriorInCombat> warriorList = warriors.get(effect);
                if(warriorList != null && warriorList.size() > 0) {
                    //Add a warrior list arranged per activation value
                    ArrangedWarriorsPerActivation arrangedWarriorsView =
                            new ArrangedWarriorsPerActivation(getContext());
                    arrangedWarriorsView.warriors(army, effect, warriorList);

                    int id = View.generateViewId();
                    arrangedWarriorsView.setId(id);

                    innerStructure.addView(arrangedWarriorsView);
                }
        }
        }else{
            Collection<String> titles = formation.getSubFormations().keySet();

            for(String title: titles){

                CompletionRoundFormationView formationView = new CompletionRoundFormationView(getContext());
                formationView.setFormation(army, title, formation.getSubFormations().get(title));

                int id = View.generateViewId();
                formationView.setId(id);

                innerStructure.addView(formationView);
            }
        }
    }
}