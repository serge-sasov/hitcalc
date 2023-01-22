package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.round;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.Round;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.view.AvailableActionsView;

import java.util.HashMap;

public class ArmyRoundView extends LinearLayout implements FormationRoundView.OnActivationListener {
    private ArmyInCombat mArmy;
    private Round mRound; //derive current data about the available actions
    private HashMap<String, Integer> mMappingFormationToViewIds; //mapping between formation & view Ids

    public ArmyRoundView(Context context) {
        super(context);
        initializeView(context);
    }

    public ArmyRoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public ArmyRoundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.army_round_view, this);
    }

    //use for active player
    public void setArmy(ArmyInCombat army, Round round, FormationRoundView.OnClickListener listener){
        mArmy = army;
        mRound = round;
        updateViewOutput(round, listener);
    }

    //use for passive player
    public void setArmy(ArmyInCombat army, Round round){
        mArmy = army;
        mRound = round;
        updateViewOutput(round, null);
    }

    //Display formation warriors
    protected void updateViewOutput(Round round, FormationRoundView.OnClickListener listener){
        //shows which player is currently displayed
        Boolean isActivePlayer = true;
        if(listener == null){
            isActivePlayer = false;
        }

        if(mArmy != null){
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.armyInCombatLayout);
            mMappingFormationToViewIds = new HashMap<String, Integer>();

            for(Formation  formation :  mArmy.getFormations()){
                //Cast from Formation to FormationInCombat
                FormationInCombat formationInCombat = (FormationInCombat) formation;

                FormationRoundView formationView = new FormationRoundView(getContext());
                formationView.setFormation(round, mArmy.getCivilizationTitle(), formationInCombat, isActivePlayer);

                //Set listener first
                if(isActivePlayer == true) {
                    formationView.setOnClickListener(listener);
                    formationView.setOnActivationListener(this);
                }

                int id = View.generateViewId();
                mMappingFormationToViewIds.put(formation.getLeaderOrFormationTitle(), id);

                formationView.setId(id);
                linearLayout.addView(formationView);
            }
        }
    }

    @Override
    //trigger update of the action panel and deactivate formation which have been previously activated
    public void onActivation(String formationTitle) {
        FormationRoundView formationView;
        AvailableActionsView availableActionsView;
        //Deselect any previously activated formations
        for(Formation formation: mArmy.getFormations()){
            availableActionsView = (AvailableActionsView) findViewById(R.id.availableActionsPanel);

            if(formation.getLeaderOrFormationTitle().equals(formationTitle)){
                //Populate the activation panel with available actions
                availableActionsView.setActions(mRound.getAvailableActionsForTheTurn(formationTitle));

                //get related formation view to assign to it the action panel drag listener
                formationView = (FormationRoundView) findViewById(mMappingFormationToViewIds.get(formationTitle));
                //availableActionsView.setOnDragListener(formationView);

            }else{
                //deactivate previously activated formations
                formationView = (FormationRoundView) findViewById(mMappingFormationToViewIds.get(formation.getLeaderOrFormationTitle()));
                formationView.deactivate();
            }

            //Notify all formation listeners about actions to be applied
            availableActionsView.setOnDragListeners(formationView);
        }
    }
}
