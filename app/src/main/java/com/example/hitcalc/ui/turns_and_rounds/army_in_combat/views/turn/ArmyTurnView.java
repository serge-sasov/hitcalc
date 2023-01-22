package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.Turn;
import com.example.hitcalc.utility.LoadTable;

import java.util.HashMap;

public class ArmyTurnView extends LinearLayout{
    private ArmyInCombat mArmy;
    private HashMap<String, Integer> mMappingFormationToViewIds; //mapping between formation & view Ids

    public ArmyTurnView(Context context) {
        super(context);
        initializeView(context);
    }

    public ArmyTurnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public ArmyTurnView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.army_turn_view, this);
    }

    public void setArmy(Turn currentTurn, ArmyInCombat army, LoadTable unitActionTable){
        mArmy = army;
        updateViewOutput(unitActionTable);
    }

    //Display warriors of the formation
    protected void updateViewOutput(LoadTable unitActionTable){
        if(mArmy != null){
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.armyInCombatLayout);
            mMappingFormationToViewIds = new HashMap<String, Integer>();

            for(Formation formation :  mArmy.getFormations()){
                //Cast from Formation to FormationInCombat
                FormationInCombat formationInCombat = (FormationInCombat) formation;

                FormationTurnView formationView = new FormationTurnView(getContext());
                //By default not to show the formation members to speed up the layout load
                formationView.setFormation(formationInCombat, unitActionTable, false);

                int id = View.generateViewId();
                mMappingFormationToViewIds.put(formation.getLeader(), id);

                formationView.setId(id);
                linearLayout.addView(formationView);
            }
        }
    }

    //rollback any changes made on turn action setup
    public void rollBackActions(){
        for(Integer id: mMappingFormationToViewIds.values()){
            //for each given formation call a rollback action
            FormationTurnView formationView = (FormationTurnView) findViewById(id);
            formationView.rollBackActions();
        }
    }
}

