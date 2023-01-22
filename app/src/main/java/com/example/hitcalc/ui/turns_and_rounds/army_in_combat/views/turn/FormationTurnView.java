package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.WarriorInCombatView;
import com.example.hitcalc.ui.turns_and_rounds.game.Turn;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.views.UnitWeaknessSetupTurnView;
import com.example.hitcalc.utility.LoadTable;

import java.util.ArrayList;
import java.util.HashMap;

public class FormationTurnView extends LinearLayout  implements MoveWarrior.OnWarriorMoveListener, FormationInCombat.OnRootListener{
    private FormationInCombat mFormation;
    private HashMap<WarriorInCombat, Integer> mMappingWarriorsToViewIds; //mapping between formation & view Ids
    private HashMap<String, Integer> mMappingSubFormationToViewIds; //mapping between formation & view Ids
    private Boolean mIsVisible = true; // shows current state of the formation members
    private Boolean mIsWarriorsAddedToLayout = false;
    private LoadTable mUnitActionsTable;

    //Support of manual warrior killing
    private UnitWeaknessSetupTurnView mSourceView;
    private WarriorInCombatView mWarriorView;
    private WarriorInCombat mWarriorInCombat;
    private ArrangedWarriorsPerActivation mRootedWarriorsView;


    public FormationTurnView(Context context) {
        super(context);
        initializeView(context);
    }

    public FormationTurnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public FormationTurnView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.formation_turn_view, this);
    }

    public void setFormation(FormationInCombat formation, LoadTable table) {
        mFormation = formation;
        updateView(null, table);
    }

    public void setFormation(FormationInCombat formation, LoadTable table, Boolean isVisible) {
        mIsVisible = isVisible;

        setFormation(formation, table);
    }

    public void setFormation(Turn currentTurn, FormationInCombat formation, LoadTable table) {
        mFormation = formation;
        updateView(currentTurn, table);
    }

    public void setFormation(Turn currentTurn, FormationInCombat formation, LoadTable table,  Boolean isVisible) {
        mIsVisible = isVisible;

        setFormation(currentTurn, formation, table);
    }

    public void updateView(Turn currentTurn, LoadTable table) {
        mUnitActionsTable = table;
        TextView formationTitle = (TextView) findViewById(R.id.formationTitle);
        formationTitle.setText(mFormation.getLeaderOrFormationTitle());

        if(mFormation.subFormations() == null){
            //Load single root-formation if available
            if(mIsVisible == true) {
                //show the formation members if requested
                addWarriorsViewsToFormation(table, currentTurn);
            }else{
                //allow adding warriors by clicking on the given formation title
                formationTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mIsWarriorsAddedToLayout == false) {
                            //add warriors of selected formation
                            addWarriorsViewsToFormation(table, currentTurn);
                        }
                        //show / hide warriors
                        switchWarriorVisibility();
                    }
                });
            }
        }else {
            //Load any available sub-formations if selected & turn data provided
            showSubFormations(table, currentTurn);

            //as there is no root-warriors remove the view
            mRootedWarriorsView = (ArrangedWarriorsPerActivation) findViewById(R.id.rootedWarriors);
            mRootedWarriorsView.setVisibility(View.GONE);
        }
    }

    private void showSubFormations(LoadTable table, Turn currentTurn){
        //Remove unnecessary HorizontalScrollView element first
        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        horizontalScrollView.removeView(this);

        //Look for the root linear layout view
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.rootLayout);
        mMappingSubFormationToViewIds = new HashMap<String, Integer>();

        //load sub-formations as a single formation-view
        for(Formation formation: mFormation.subFormations().values()){
            //show ony activated sub-formations
            if(currentTurn != null && currentTurn.getSubFormationTitles().contains(formation.getLeaderOrFormationTitle())) {
                //display only activated formations
                showSubFormation(linearLayout, formation, table, currentTurn);
            }else if(currentTurn == null){
                //Show all available sub-formations for defender
                showSubFormation(linearLayout, formation, table, null);
            }
        }
    }

    private void showSubFormation(LinearLayout layout, Formation formation, LoadTable table, Turn currentTurn){
            //display only activated formations
            FormationTurnView formationTurnView = new FormationTurnView(getContext());
            formationTurnView.setFormation(currentTurn, (FormationInCombat) formation, table, mIsVisible);

            int id = View.generateViewId();
            mMappingSubFormationToViewIds.put(formation.getLeaderOrFormationTitle(), id);

            formationTurnView.setId(id);
            layout.addView(formationTurnView);
    }


    private void addWarriorsViewsToFormation(LoadTable table, Turn currentTurn) {
        FormationTurnView formationTurnView = this;

        //check for formation similarity
        Boolean similarity = mFormation.getSimilarity();
        Boolean hasElephants = mFormation.formationHasElephants();

        mMappingWarriorsToViewIds = new HashMap<WarriorInCombat, Integer>();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.unitScrollViewList);

        //derive right warrior set
        ArrayList<WarriorItem> warriors = mFormation.warriors();
        if(mFormation instanceof FormationActivated){
            //provide a full set of available warriors
            warriors = ((FormationActivated) mFormation).allWarriors();
        }

        for (WarriorItem warrior : warriors) {
            //the formation may be displayed for attacker according to turn action applied or every time by defender
            if (warrior.type().equals("Leader")) {
                //display a leader of the given formation

            }else if (warrior.simple()) {
                //define available action list for each warrior
                UnitWeaknessSetupTurnView unitView = new UnitWeaknessSetupTurnView(getContext());
                unitView.setAvailableActions(table, (WarriorInCombat) warrior, similarity, hasElephants);

                //listen to the warrior root event
                ((FormationActivated) mFormation).source().setOnRootListener(formationTurnView, 0);

                //subscribe to drag events
                unitView.setOnDragListener(formationTurnView);

                int id = View.generateViewId();
                mMappingWarriorsToViewIds.put((WarriorInCombat) warrior, id);

                unitView.setId(id);
                linearLayout.addView(unitView);

                mIsWarriorsAddedToLayout = true;
            }
        }

        //-------------------- Add killed or rooted warriors to the list --------------------------------
        //Add a warrior list arranged per activation value
        mRootedWarriorsView = (ArrangedWarriorsPerActivation) findViewById(R.id.rootedWarriors);

        if(mFormation.warriors() != null) {
            //localize rooted warriors -> "Rooted warriors" & show only the currently rooted warriors
            String effect = getResources().getString(R.string.rooted_warriors);
            mRootedWarriorsView.warriors(((FormationActivated) mFormation).source(), effect, ((FormationActivated) mFormation).source().getRootedWarriors(), true);

            //Implements the manual warrior killing workflow (receiver part)
            mRootedWarriorsView.setOnDragListener(new ManuallyRootedWarriorListener());
        }else{
            //if there is no root-warriors remove the view
            mRootedWarriorsView.setVisibility(View.GONE);
        }
    }

    private void switchWarriorVisibility(){
        mIsVisible = !mIsVisible;

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.unitScrollViewList);
        if(mIsVisible == true){
            linearLayout.setVisibility(View.VISIBLE);
        }else{
            linearLayout.setVisibility(View.GONE);
        }

    }

    //rollback any changes made on turn action setup
    public void rollBackActions(){
        //Rollback any changes made for root formation
        if((mFormation != null && mFormation.warriors() != null) && mMappingWarriorsToViewIds != null) {
            //Rollback actions for defender side only in case it was activated by user
            for (Integer id : mMappingWarriorsToViewIds.values()) {
                //for each given formation call a rollback action
                UnitWeaknessSetupTurnView unitView = (UnitWeaknessSetupTurnView) findViewById(id);
                unitView.rollBackActions();
            }

            //Add a warrior list arranged per activation value
            if(mFormation.warriors() != null) {
                //localize rooted warriors -> "Rooted warriors" & show only the currently rooted warriors
                String effect = getResources().getString(R.string.rooted_warriors);
                mRootedWarriorsView.warriors(effect, ((FormationActivated) mFormation).source().getRootedWarriors());
            }
        }

        //Call each sub-formation to rollback any changes made
        if((mFormation != null && mFormation.subFormations() != null) && mMappingSubFormationToViewIds != null){
            for (Integer id : mMappingSubFormationToViewIds.values()) {
                //for each given formation call a rollback action
                FormationTurnView formationTurnView = (FormationTurnView) findViewById(id);
                formationTurnView.rollBackActions();
            }
        }
    }

    @Override
    /*
     * handling of manually killing warrior by dragging action
     * Stage 1: - persist the intent data of a warrior going to be killed
     * */
    public void OnWarriorMove(View sourceView, WarriorInCombatView warriorView, WarriorInCombat warrior) {
        mSourceView = (UnitWeaknessSetupTurnView) sourceView;
        mWarriorView = warriorView;
        mWarriorInCombat = warrior;
    }

    @Override
    public void onRoot(WarriorInCombat warrior, Integer pageId) {
        UnitWeaknessSetupTurnView unitView;
        Integer id = mMappingWarriorsToViewIds.get(warrior);
        if(id != null){
            unitView = (UnitWeaknessSetupTurnView) findViewById(id);
            unitView.setVisibility(View.GONE);

            //localize rooted warriors -> "Rooted warriors" & show only the currently rooted warriors
            String effect = getResources().getString(R.string.rooted_warriors);
        }

        mRootedWarriorsView.actualizeWarriors(((FormationActivated) mFormation).source().getRootedWarriors());
    }

    @Override
    public void onRecover(WarriorInCombat warrior, Integer pageId) {
        UnitWeaknessSetupTurnView unitView;
        Integer id = mMappingWarriorsToViewIds.get(warrior);
        if(id != null){
            unitView = (UnitWeaknessSetupTurnView) findViewById(id);
            unitView.setVisibility(View.VISIBLE);
        }else{
            FormationTurnView formationTurnView = this;
            //check for formation similarity
            Boolean similarity = mFormation.getSimilarity();
            Boolean hasElephants = mFormation.formationHasElephants();

            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.unitScrollViewList);

            unitView = new UnitWeaknessSetupTurnView(getContext());
            unitView.setAvailableActions(mUnitActionsTable, (WarriorInCombat) warrior, similarity, hasElephants);

            //listen to the warrior root event
            ((FormationActivated) mFormation).source().setOnRootListener(formationTurnView, 0);

            //subscribe to drag events
            unitView.setOnDragListener(formationTurnView);

            id = View.generateViewId();
            mMappingWarriorsToViewIds.put((WarriorInCombat) warrior, id);

            unitView.setId(id);
            linearLayout.addView(unitView);

            mIsWarriorsAddedToLayout = true;
        }

        mRootedWarriorsView.actualizeWarriors(((FormationActivated) mFormation).source().getRootedWarriors());
    }

    /*
     * Process the manually killed warrior workflow
     * Stage 2: Treated as a confirmation, that the warrior has successfully reached the target rooted warrior set.
     * */
    class ManuallyRootedWarriorListener implements View.OnDragListener {
        private Boolean mOwnWarrior = true; //a flag to check the warrior membership

        @Override
        public boolean onDrag(View view, DragEvent event) {
            int action = event.getAction();

            Log.d("Drag Event = ", "" + action);
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    // Check first whether the unit was moved from a hex around
                    //clearUnitFromHexAndMap();

                    break;
                case DragEvent.ACTION_DROP:
                    // derive the info about turn action dropped
                    ClipData.Item clip=event.getClipData().getItemAt(0);
                    String warriorTitle = clip.getText().toString();

                    //Stage 0: validation check
                    if(!mWarriorInCombat.title().equals(warriorTitle)){
                        //this is a warrior out of the other formation, therefore stop processing
                        mOwnWarrior = false;
                    }

                    //Stage 1: Remove the warrior from its previous position
                    if(mOwnWarrior == true) {
                        mSourceView.setVisibility(View.GONE);
                        mFormation.killWarrior(mWarriorInCombat);
                    }

                    //Stage 2: add the warrior to the rooted warriors set.
                    //...
                    if(mOwnWarrior == true){
                        //add new warrior to the rooted warriors
                        //mRootedWarriorsView.addWarrior(mWarriorInCombat);
                    }

                    //Remove the warrior out of the available warrior set


                    Log.i("Warrior killed", warriorTitle);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //to define in future
                    break;
                default:
                    break;
            }
            Log.d("DraggedAction:", " " + action);

            return true;
        }
    }
}