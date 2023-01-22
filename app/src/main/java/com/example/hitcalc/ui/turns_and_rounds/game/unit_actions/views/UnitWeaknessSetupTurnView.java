package com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.WarriorInCombatView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.MoveWarrior;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.UnitAbstractAction;
import com.example.hitcalc.utility.LoadTable;

import java.util.ArrayList;

public class UnitWeaknessSetupTurnView extends ConstraintLayout {
    private WarriorInCombat mWarrior;
    //private FormationInCombat mFormation; // warriors own formation
    private ArrayList<UnitAbstractAction> mActionList;
    private ArrayList<Integer> mTurnActionViewIdList; //list of TurnAction ViewId
    private Boolean mFormationSimilarity; //allows to hide the actions not relevant for the similar warriors

    //root warrior listener
    private MoveWarrior mRootWarriorListener = new MoveWarrior();

    // Step 3: Assign the listener implementing events interface that will receive the events
    public void setOnDragListener(MoveWarrior.OnWarriorMoveListener listener) {
        mRootWarriorListener.setOnMoveListener(listener);
    }

    public UnitWeaknessSetupTurnView(Context context) {
        super(context);
        initializeView(context);
    }

    public UnitWeaknessSetupTurnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public UnitWeaknessSetupTurnView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.unit_weakness_setup_turn_view, this);
    }

    private ArrayList<UnitAbstractAction> addNewAction(UnitAbstractAction action){
        int number = 1;
        if(mWarrior.twoHexSize() == true && action.isDoubleSize() == false){
            number = 2;
        }
        return null;
    }

    //specify own formation of the given warrior
    /*
    public void setAvailableActions(LoadTable table, FormationInCombat formation, WarriorInCombat warrior, Boolean similarity, boolean hasElephants) {
        mFormation = formation;
        setAvailableActions(table, warrior, similarity, hasElephants);
    }
     */

    public void setAvailableActions(LoadTable table, WarriorInCombat warrior, Boolean similarity, boolean hasElephants) {
        mWarrior = warrior;
        ArrayList<UnitAbstractAction> actionList = warrior.actionList(table);
        mActionList = new ArrayList<UnitAbstractAction>();

        if(similarity){
            //Optimize action list and remove all not relevant actions out of it
            for(UnitAbstractAction action : actionList){
                if(action.isActionApplicable()){
                    mActionList.add(action);
                }
            }
        }
        else if(hasElephants) {
            //print out all possible actions
            mActionList = actionList;
        }else{
            for(UnitAbstractAction action : actionList){
                //print out all possible actions except Rampage
                if(!action.getTitle().equals("Rampage")){
                    mActionList.add(action);
                }
            }
        }

        mFormationSimilarity = similarity;
        updateViewOutput();
    }

    public void setAvailableActions(WarriorInCombat warrior, ArrayList<UnitAbstractAction> actionList) {
        mWarrior = warrior;
        mActionList = actionList;

        updateViewOutput();
    }

    public Boolean compare(WarriorInCombat warrior){
        if(warrior.equals(mWarrior)){
            return true;
        }
        return false;
    }

    //Display warriors of the formation
    protected void updateViewOutput(){
        ArrayList<Integer> shockActionIds = new ArrayList<Integer>();
        ArrayList<Integer> distanceAttackDefenceIds = new ArrayList<Integer>();
        ArrayList<Integer> rampageDefenceIds = new ArrayList<Integer>();

        if(mWarrior != null){
            mTurnActionViewIdList = new ArrayList<Integer>();

            WarriorInCombatView warriorInCombatView = (WarriorInCombatView) findViewById(R.id.unit);
            warriorInCombatView.setUnit(mWarrior);

            //Handle manual killing of the given warrior by use of drag action
            mRootWarriorListener.enableWarriorMove(this, warriorInCombatView, mWarrior);

            //-------------- add a list of available actions ---------------------
            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.unitWeaknessConstraintLayout);

            //Retrieve a unit view id of the first element to constrains to
            Integer prevId = warriorInCombatView.getId();

            //Getting ready views of the given layout
            for(UnitAbstractAction action : mActionList){
                UnitActionMarkerView actionView = new UnitActionMarkerView(getContext());
                actionView.setAction(action);

                //Allow user to use only suitable for unit actions
                if (action.isActionApplicable()) {
                    actionView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Change action state of the given action
                            actionView.changeActionState();
                        }
                    });
                }

                int id = View.generateViewId();
                actionView.setId(id);

                //Add view id to the array to use if for constrain resolution afterwards
                mTurnActionViewIdList.add(id);

                //Add new view to the layout
                layout.addView(actionView);

                //For double size units memorize their view id
                if(mWarrior.twoHexSize() == true){
                    if(action.getTitle().equals("Shock Combat")) {
                        shockActionIds.add(id);
                    }
                    if(action.getTitle().equals("Distance Attack Defence")) {
                        distanceAttackDefenceIds.add(id);
                    }
                    if(action.getTitle().equals("Rampage Defence")) {
                        rampageDefenceIds.add(id);
                    }
                }
            }
            /*
             * The constrains can be arranged as the view layout is ready
             * clone constrain set from the generated layout
             */
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(layout);

            //Set constraints to newly created views
            for(Integer id: mTurnActionViewIdList){
                //Handle the single action for two hex sized units in a particular manner
                if(shockActionIds.contains(id) || distanceAttackDefenceIds.contains(id) || rampageDefenceIds.contains(id)){
                    int index = 0;
                    if(shockActionIds.contains(id)) {
                        index = shockActionIds.indexOf(id);
                    }

                    if(distanceAttackDefenceIds.contains(id)) {
                        index = distanceAttackDefenceIds.indexOf(id);
                    }

                    if(rampageDefenceIds.contains(id)) {
                        index = rampageDefenceIds.indexOf(id);
                    }

                    if(index == 0){
                        constraintSet.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
                        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM,10);
                    }

                    if(index == 1){
                        constraintSet.connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
                        constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM,10);
                        //save id value for the further use
                        prevId = id;
                    }
                }else {
                    constraintSet.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
                    constraintSet.connect(id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);
                    constraintSet.connect(id, ConstraintSet.TOP, prevId, ConstraintSet.BOTTOM, 10);

                    //save id value for the further use
                    prevId = id;
                }
            }
            constraintSet.applyTo(layout);
        }
    }

    //rollback any changes made on turn action setup
    public void rollBackActions(){
        for(Integer id: mTurnActionViewIdList){
            UnitActionMarkerView actionView = (UnitActionMarkerView) findViewById(id);

            //call rollback for each given action
            actionView.rollback();
        }
    }
}
