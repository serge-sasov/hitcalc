package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.round;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.WarriorInCombatView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.ArrangedWarriorsPerActivation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.MoveWarrior;
import com.example.hitcalc.ui.turns_and_rounds.game.Round;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.ActionPoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.PassTurn;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizureFailure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizurePoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SkipSeizure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.view.AvailableActionsView;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.view.TurnActionMarkerView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
/*
* Show a formation configuration for a given round and turn along with the unit weakness value and actions available
* */
public class FormationRoundView extends LinearLayout implements AvailableActionsView.OnDragListener,
        MoveWarrior.OnWarriorMoveListener {
    private ArrayList<TurnActionMarkerView> mTurnActionViews; //turn actions to be applied
    private Boolean mIsSelected = false; //true if formation was selected by user click. Then shows/hides control elements and action panels for chosen formation
    private String mArmyTitle; // army title a given formation belongs to
    private FormationInCombat mFormation; //current formation instance
    private AvailableActionsView mAvailableActionsPanelView; //keep direct access to the panel to remove/recover the turn actions
    private Boolean mIsSubFormation = false; //A flag to show whether it is a sub-formation view
    private HashMap<Integer, ArrayList<WarriorInCombat>> mArrangedWarriorsPerActivation; //mapping between activation value and warriors arranged to it
    private HashMap<Integer, Integer> mActivationValuePerArrangedWarriorsViewId; //mapping between activation value and arranged warrior view id
    private HashMap<String, Integer> mSubFormationPerViewId; //mapping between sub-formation and view id

    // -------- Necessary data to support warrior killing in manual mode ---------
    private ArrangedWarriorsPerActivation mRootedWarriorsView;
    private ArrangedWarriorsPerActivation mSourceView; //current warrior set the unit moving from
    private WarriorInCombatView mWarriorView;
    private WarriorInCombat mWarriorInCombat;
    // -------- Necessary data to support warrior killing in manual mode ---------
    private Boolean mWarriorsViewAdded = false;

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnClickListener {
        // These methods are the different events and need to pass relevant arguments related to the event triggered
        /* to be provided arguments:
        * view of the button
        * title of the root formation
        * list of activated sub-formations
        * list of applied actions, that will be used as a filter criteria for warrior display
         */
        public void onClick(View view, FormationActivated formation, ArrayList<TurnAbstractAction> appliedActions) throws IOException, JSONException; //current view & formation title
    }

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnActivationListener {

        //Listener 1. Notify the parent army layout view that the other previously selected formation is to be deselected, i.e. change color, hide buttons and etc....
        //Listener 2. Show the panel of available actions
        public void onActivation(String formation);
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private FormationRoundView.OnClickListener mOnClickListener = null;
    private FormationRoundView.OnActivationListener mOnActivationListener = null;

    // Step 3: Assign the listener implementing events interface that will receive the events
    public void setOnClickListener(FormationRoundView.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnActivationListener(FormationRoundView.OnActivationListener listener) {
        mOnActivationListener = listener;
    }


    public FormationRoundView(Context context) {
        super(context);
        initializeView(context);
    }

    //set the sub-formation flag directly
    public FormationRoundView(Context context, Boolean isSubFormation) {
        super(context);
        mIsSubFormation = isSubFormation;
        initializeView(context);
    }

    public FormationRoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public FormationRoundView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.formation_round_view, this);
    }

    //configure the input data
    public void setFormation(Round round, String armyTitle, FormationInCombat formation, Boolean isActive){
        mFormation = formation;
        mArmyTitle = armyTitle;
        updateView(isActive, round);
    }

    public void updateView(Boolean isActive, Round round) {
        TextView formationTitle = (TextView) findViewById(R.id.formationTitle);
        formationTitle.setText(mFormation.getLeaderOrFormationTitle());

        //show formation activation state
        TextView state = (TextView) findViewById(R.id.state);
        if(mFormation.state() == true) {
            state.setText(getResources().getString(R.string.spent));
            state.setBackgroundColor(Color.parseColor("#C41313"));
            state.setTextColor(Color.parseColor("#ffffff"));
        }else{
            state.setText(getResources().getString(R.string.fresh));
        }

        //Show available seizure points
        ArrayList<TurnAbstractAction> actions = round.getAvailableActionsForRound(mArmyTitle, mFormation.getLeaderOrFormationTitle());
        int NumberOfAvailableSeizurePoints= 0;
        for(TurnAbstractAction action : actions){
            if(action.getClass() == SeizurePoint.class){
                NumberOfAvailableSeizurePoints++;
            }
        }


        //Show available seizure points
        TextView seizurePoints = (TextView) findViewById(R.id.seizurePoints);
        if(NumberOfAvailableSeizurePoints > 0) {
            seizurePoints.setText(NumberOfAvailableSeizurePoints + " " + getResources().getString(R.string.available_seizure_points));
            seizurePoints.setBackgroundColor(Color.parseColor("#0009ff"));
            seizurePoints.setTextColor(Color.parseColor("#ffffff"));
        }else{
            seizurePoints.setText("");
        }

        //Set listener on a click event to navigate to the unit weakness turn setup fragment
        Button configureBtn = (Button) findViewById(R.id.configureBtn);
        configureBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //send to the listener the info about warriors activated in the turn
                if(mOnClickListener != null){
                    /* need to transfer: button view, turn action(s) & activated formation:
                    *  title of the formation - mFormation
                    *  title of activated sub-formations if any - activatedSubFormations
                    *  list of turn action applied - mTurnActionViews
                     */
                    ArrayList<TurnAbstractAction> appliedActions = new ArrayList<TurnAbstractAction>();
                    //Derive a list of turn actions to be applied
                    if(mTurnActionViews != null){
                        for(TurnActionMarkerView markerView : mTurnActionViews){
                            appliedActions.add(markerView.getLastAction());
                        }
                    }

                    //make an activated formation with a list of activated warriors
                    FormationActivated formation = makeActivatedFormation();

                    try {
                        mOnClickListener.onClick(view, formation, appliedActions);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //Set listener for on Drag event to catch the unit moved to the cell
        TurnActionMarkerView actionMarker = (TurnActionMarkerView) findViewById(R.id.actionMarker);
        actionMarker.setOnDragListener(new MyDragListener());

        //Update layout outlook - show/hide activation control elements
        updateFormationActivationOutlook();

        //for active player allow to select formation by clicking on target formation
        LinearLayout formationRoundLayout = (LinearLayout) findViewById(R.id.formationCaptureLayout);
        formationRoundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Notify all listeners for active player if user clicks on formation
                if (mOnActivationListener != null) {
                    mOnActivationListener.onActivation(mFormation.getLeaderOrFormationTitle());
                }

                //Update layout outlook
                mIsSelected = !mIsSelected;
                updateFormationActivationOutlook();

                LinearLayout innerStructure = (LinearLayout) findViewById(R.id.innerStructure);

                //append warriors to the view
                if(mIsSelected == true && mWarriorsViewAdded == false){
                    appendWarriorsToView(isActive, round);
                }else if(mIsSelected == true){
                    innerStructure.setVisibility(View.VISIBLE);
                }
                else{
                    innerStructure.setVisibility(View.GONE);
                }
            }
        });
    }

    //return a list of activated root-layer warriors
    public ArrayList<WarriorInCombat> activatedWarriors(){
        //Derive a list of activated warriors for root formation
        if(mActivationValuePerArrangedWarriorsViewId != null){
            ArrangedWarriorsPerActivation arrangedWarriorsView;
            ArrayList<WarriorInCombat> activatedWarriors = new ArrayList<WarriorInCombat>();
            ArrayList<WarriorInCombat> warriors;

            for(int id : mActivationValuePerArrangedWarriorsViewId.values()){
                arrangedWarriorsView = findViewById(id);
                warriors = arrangedWarriorsView.activatedWarriors();
                if(warriors != null) {
                    activatedWarriors.addAll(warriors);
                }
            }

            return activatedWarriors;
        }

        return null;
    }

    //prepare a list of activated warriors, leader & harizma
    private FormationActivated makeActivatedFormation(){
        //get a list of activated root layer warriors
        FormationActivated formationActivated = new FormationActivated(mFormation);
        ArrayList<WarriorInCombat> activatedWarriors;

        if(mFormation.warriors() != null && mFormation.warriors().size() > 0){
            activatedWarriors = activatedWarriors();
            formationActivated.setMainWarriors(activatedWarriors);
        }

        if(mFormation.subFormations() != null && mFormation.subFormations().size() > 0){
            HashMap<String, ArrayList<WarriorInCombat>> activatedSubFormationWarriors = new HashMap<String, ArrayList<WarriorInCombat>>();
            for(String title: mSubFormationPerViewId.keySet()){
                FormationRoundView subFormationView = (FormationRoundView) findViewById(mSubFormationPerViewId.get(title));
                activatedWarriors = subFormationView.activatedWarriors();

                if(activatedWarriors != null && activatedWarriors.size() > 0){
                    activatedSubFormationWarriors.put(title, activatedWarriors);
                }
            }

            formationActivated.setSubFormationWarriors(activatedSubFormationWarriors);
        }

        return formationActivated;
    }

    /*
    * Append warrior set to the given view.
    * @Input
    * Boolean isActive -
    * Round round -
     */

    private void appendWarriorsToView(Boolean isActive, Round round){
        //------------------ Populate the primary formation list first -----------------------------
        //get a layout the further view structures (arranged warrior lists or sub formations) to be attached to
        LinearLayout innerStructure = (LinearLayout) findViewById(R.id.innerStructure);

        //Attach the arranged warrior lists first
        ArrayList<WarriorItem> warriors = mFormation.warriors();
        if(warriors != null && warriors.size() > 0){
            mArrangedWarriorsPerActivation = mFormation.arrangeWarriorsPerActivationValue();
            mActivationValuePerArrangedWarriorsViewId = new HashMap<Integer, Integer>();

            for(Integer actionPointValue: mArrangedWarriorsPerActivation.keySet()){
                ArrayList<WarriorInCombat> warriorList = mArrangedWarriorsPerActivation.get(actionPointValue);
                if(warriorList != null && warriorList.size() > 0) {
                    //Add a warrior list arranged per activation value
                    ArrangedWarriorsPerActivation arrangedWarriorsView =
                            new ArrangedWarriorsPerActivation(getContext());
                    arrangedWarriorsView.warriors(mArrangedWarriorsPerActivation.get(actionPointValue), actionPointValue, mFormation.state());

                    int id = View.generateViewId();
                    arrangedWarriorsView.setId(id);

                    //listen if any warriors is killed and need to be moved to the sub-array side of rooted warriors
                    arrangedWarriorsView.setOnRootListener(this);

                    //populate a mapping between activation value and view identity
                    mActivationValuePerArrangedWarriorsViewId.put(actionPointValue, id);

                    innerStructure.addView(arrangedWarriorsView);
                }
            }
            //------------------------- Add rooted warriors view  -------------------------------------

            //Add killed or rooted warriors to the list
            ArrayList<WarriorInCombat> rootedWarriorList = mFormation.getRootedWarriors();
            //Add a warrior list arranged per activation value
            mRootedWarriorsView =
                    new ArrangedWarriorsPerActivation(getContext());

            //localize rooted warriors -> "Rooted warriors"
            String effect = getResources().getString(R.string.rooted_warriors);
            if(rootedWarriorList != null && rootedWarriorList.size() > 0) {
                mRootedWarriorsView.warriors(effect, rootedWarriorList);
            }else{
                mRootedWarriorsView.warriors(effect, null);
            }

            int id = View.generateViewId();
            mRootedWarriorsView.setId(id);

            //Implements the manual warrior killing workflow (receiver part)
            mRootedWarriorsView.setOnDragListener(new ManuallyRootedWarriorListener());

            innerStructure.addView(mRootedWarriorsView);

            //-------------------------------------------------------------------------------

        }else{
            Collection<Formation> subFormations = mFormation.subFormations().values();
            mSubFormationPerViewId = new HashMap<String, Integer>();

            for(Formation formation: subFormations){
                FormationRoundView formationRoundView = new FormationRoundView(getContext(), true);
                formationRoundView.setFormation(round, mArmyTitle, (FormationInCombat) formation, isActive );

                int id = View.generateViewId();
                formationRoundView.setId(id);

                //Store view id values to be able to access them later during the activation phase
                mSubFormationPerViewId.put(formation.getLeaderOrFormationTitle(), id);

                innerStructure.addView(formationRoundView);
            }
        }

        //set the flag to true not to generate the views once again
        mWarriorsViewAdded = true;
    }



    // return current formation selection state
    public Boolean getSelectionState(){
        return mIsSelected;
    }

    //deactivate the given formation if player choices another one
    protected void deactivate(){
        if(mIsSelected == true){
            //delete any selected turn actions
            if(mTurnActionViews != null) {
                //clear out any applied actions
                mTurnActionViews.clear();
            }

            //restore default outlook and remove any configuration defined
            TurnActionMarkerView actionMarker = (TurnActionMarkerView) findViewById(R.id.actionMarker);
            actionMarker.clear();

            //Change the layout outlook
            mIsSelected = !mIsSelected;
            updateFormationActivationOutlook();
        }
    }

    /*
    * Catch the drag event:
    * 1. check for action applied
    * 2. activate warriors according to the action applied (AP(s) or Seizure)
    * 3. show config button
    * */
    class MyDragListener implements OnDragListener {
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
                    String actionTitle = clip.getText().toString();

                    //---------------------------- Validation check  -------------------------------
                    //Evaluate the action title to protect from the wrong object type, f.e. a warrior
                    String[] validTitles = TurnAbstractAction.supportableActions();
                    Boolean check = false;

                    for(String title:validTitles){
                        if(actionTitle.equals(title)){
                            check = true;
                        }
                    }

                    //if formation is not selected by user forbid any further processing
                    /*  ------------> need to place extra validations <-----------
                    if(mIsSelected != true ){
                        check = false;
                    }
                    */

                    //---------------------------- Validation check  -------------------------------

                    if(check == true) {
                        //Stage 1: Evaluate user turn actions applied and change their outlook in the panel (hide or leave unchanged)
                        arrangeAppliedActions();

                        /*
                         * Stage 2: Highlight a set of warriors arranged to the action points value which was assigned by user
                         * Apply the further steps to main formations only in case there is any warriors otherwise call the same
                         * function for all sub-formations.
                         */
                        if (mIsSubFormation == false && mArrangedWarriorsPerActivation != null) {
                            //there is any number of warriors available to display
                            highlightActivatedWarriorsAndControls();
                        } else if (mSubFormationPerViewId != null) {
                            //if there are any subformations available handle their visualization
                            Boolean highlightedFlag = false;
                            for (String formation : mSubFormationPerViewId.keySet()) {
                                //derive each sub-formation view and provide the data
                                int viewId = mSubFormationPerViewId.get(formation);
                                FormationRoundView formationRoundView = (FormationRoundView) findViewById(viewId);
                                if (formationRoundView.highlightSubFormationWarriorsAndControls(mTurnActionViews) == true) {
                                    highlightedFlag = true;
                                }
                                // show control button
                                manageControlButtonVisibility(highlightedFlag);
                            }
                        }
                    }

                    Log.i("Action Dropped ", actionTitle);

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

    //Evaluate user turn actions applied and change their outlook in the panel (hide or leave unchanged)
    private void arrangeAppliedActions(){
        if(mAvailableActionsPanelView != null){
            TurnActionMarkerView actionView;

            /*Check if there is a switch to the previous action(s). If any, remove any previous actions and use the new one.
             * Otherwise either add a new action point or just ignore a additional seizure attempt
             */
            if(mTurnActionViews.size() > 1){
                //there shall be at least two actions
                int size = mTurnActionViews.size();
                TurnAbstractAction lastAction = mTurnActionViews.get(mTurnActionViews.size() -1).getLastAction();
                TurnAbstractAction prevAction = mTurnActionViews.get(mTurnActionViews.size() -2).getLastAction();

                //Check for action change
                if(prevAction.getClass() != lastAction.getClass()){
                    //if actions are differ, restore the previous one(s) in the available actions panel for further use
                    for(int i = 0; i < size - 1; i ++){
                        //there can be more than just one to be applied action to be recovered
                        actionView = mTurnActionViews.get(i);
                        mAvailableActionsPanelView.showAction(actionView);
                    }
                    //memorize the last action view to restore it later on
                    TurnActionMarkerView lastActionView = mTurnActionViews.get(mTurnActionViews.size() -1);

                    //remove all previous action views out of the local to be applied actions array
                    mTurnActionViews.clear();

                    //store the last action
                    mTurnActionViews.add(lastActionView);
                }

                //update the outlook of newly applied action as in the panel as well in the formation view
                if(prevAction.getClass() == lastAction.getClass() && lastAction.getClass() == SeizurePoint.class ){
                    //ignore the second attempt to add new seizure point
                    mTurnActionViews.remove(mTurnActionViews.size() - 1);
                }else {
                    // move action from panel to formation view
                    changeAppliedActionOutlook();
                }
            }else{
                // move action from panel to formation view
                changeAppliedActionOutlook();
            }
        }
    }

    //Highlight a set of warriors arranged to the action points value which was assigned by user and activate control button
    private void highlightActivatedWarriorsAndControls(){
        //If there is no warriors view then nothing may be highlighted.
        if(mWarriorsViewAdded == false){
            return;
        }

        //get a reference to the configure button
        Button configureBtn = (Button) findViewById(R.id.configureBtn);

        //get last action to determine further actions
        TurnAbstractAction lastAction = mTurnActionViews.get(mTurnActionViews.size() -1).getLastAction();

        //derive number of actions applied
        int actionQuantity = mTurnActionViews.size();

        //Case 1. Spend Action Point(s):
        if (lastAction.getClass() == ActionPoint.class) {
            //hide config button as the available activation points may be not enough to enable any warrior set
            configureBtn.setVisibility(View.INVISIBLE);

            for(Integer activationValue: mActivationValuePerArrangedWarriorsViewId.keySet()) {
                Integer viewId = mActivationValuePerArrangedWarriorsViewId.get(activationValue);
                if (viewId != null) {
                    ArrangedWarriorsPerActivation arrangedWarriorsView = (ArrangedWarriorsPerActivation) findViewById(viewId);
                    if(activationValue <= actionQuantity) {
                        //Activate found warrior sets
                        arrangedWarriorsView.activate();

                        //Apply the further steps to main formations only
                        if(mIsSubFormation == false) {
                            //show config button on reaching the activation threshold
                            configureBtn.setVisibility(View.VISIBLE);
                            //Set text - "Configure Weakness"
                            configureBtn.setText(getResources().getString(R.string.config_weakness_btn));
                        }
                    }else{
                        //deactivate the rest of left warrior sets
                        arrangedWarriorsView.deactivate();
                    }
                }
            }
        }
        //Case 2. Spend Seizure Point:
        if (lastAction.getClass() == SeizurePoint.class) {
            //activate all available warriors independent of their weakness state
            for (Integer viewId : mActivationValuePerArrangedWarriorsViewId.values()) {
                //Activate all found warrior sets
                ArrangedWarriorsPerActivation arrangedWarriorsView = (ArrangedWarriorsPerActivation) findViewById(viewId);
                arrangedWarriorsView.activate();
            }

            //show config button
            if(mIsSubFormation == false) {
                //Apply the further steps to main formations only
                configureBtn.setVisibility(View.VISIBLE);
                configureBtn.setText("Config Weakness");
            }
        }

        //Case 3. Seizure Failure, Skip Seizure or Pass Turn:
        if (lastAction.getClass() == SeizureFailure.class ||
                lastAction.getClass() == PassTurn.class ||
                lastAction.getClass() == SkipSeizure.class) {
            //deactivate all warriors in the formation
            for (Integer viewId : mActivationValuePerArrangedWarriorsViewId.values()) {
                //Deactivate all found warrior sets
                ArrangedWarriorsPerActivation arrangedWarriorsView = (ArrangedWarriorsPerActivation) findViewById(viewId);
                arrangedWarriorsView.deactivate();
            }
            //show turn finish button
            //show config button
            if(mIsSubFormation == false) {
                //Apply the further steps to main formations only
                //show config button on reaching the activation threshold
                configureBtn.setVisibility(View.VISIBLE);
                //Set Text "End Turn"
                configureBtn.setText(getResources().getString(R.string.end_turn_btn));
            }
        }
    }

    /*
    * manage the visibility of control button via a flag isActivated = false
    * */
    private void manageControlButtonVisibility(Boolean isActivated){
        //get a reference to the configure button
        Button configureBtn = (Button) findViewById(R.id.configureBtn);

        //get last action to determine further actions
        TurnAbstractAction lastAction = mTurnActionViews.get(mTurnActionViews.size() -1).getLastAction();

        //Case 1 & 2. Spend Action Point(s) or Spend Seizure Point:
        //if ((lastAction.getClass() == ActionPoint.class && isActivated == true) ||
        if (lastAction.getClass() == ActionPoint.class ||
                lastAction.getClass() == SeizurePoint.class) {
            if(mIsSubFormation == false) {
                configureBtn.setVisibility(View.VISIBLE);
                configureBtn.setText("Config Weakness");
            }
        }

        //Case 3 & 4. Spend Seizure Failure or Pass Turn:
        if (lastAction.getClass() == SeizureFailure.class ||
                lastAction.getClass() == PassTurn.class ||
                lastAction.getClass() == SkipSeizure.class) {
            //show config button
            if(mIsSubFormation == false) {
                //Apply the further steps to main formations only
                configureBtn.setVisibility(View.VISIBLE);
                configureBtn.setText("End Turn");
            }
        }
    }

    /*
    * 1. Provide current turn action config data to the sub-formations
    * 2. Highlight activated warrior set if possible.
    * 3. If any one is highlighted return true (to use for showing the control button in the root formation view), otherwise false.
    */
    public Boolean highlightSubFormationWarriorsAndControls(ArrayList<TurnActionMarkerView> turnActionViews){
        //If there is no warriors view then nothing may be highlighted.
        if(mWarriorsViewAdded == false){
            return false;
        }

        //get current config of parent root formation
        mTurnActionViews = turnActionViews;

        //highlight/shadow the warriors
        highlightActivatedWarriorsAndControls();

        //get last action to determine further actions
        TurnAbstractAction lastAction = mTurnActionViews.get(mTurnActionViews.size() -1).getLastAction();

        //derive number of actions applied
        int actionQuantity = mTurnActionViews.size();

        if(lastAction.getClass() == SeizurePoint.class){
            //return true if the amount of applied actions exceeds or
            // equal to activation value at least of one available warrior set
            return true;
        }else if(lastAction.getClass() == ActionPoint.class){
            for(Integer activationValue: mActivationValuePerArrangedWarriorsViewId.keySet()) {
                if(activationValue <= actionQuantity){
                    return true;
                }
            }
        }

        return false;
    }

    //show to be applied action in the formation view and hide it in the control panel
    private void changeAppliedActionOutlook(){
        // Hide applied action out of the available actions panel
        TurnActionMarkerView actionView = mTurnActionViews.get(mTurnActionViews.size() - 1);
        mAvailableActionsPanelView.hideAction(actionView);

        //update formation marker outlook according to the applied action
        TurnActionMarkerView actionMarker = (TurnActionMarkerView) findViewById(R.id.actionMarker);
        actionMarker.setAction(actionView.getLastAction());
    }

    /********** Implements listener of available actions panel ***********/
    @Override
    /*
    * Receive to be applied action from the panel
    * */
    public void onDrag(AvailableActionsView panel, TurnActionMarkerView actionView) {
        //populate a list of actions to be applied for the given turn and formation
        if(mTurnActionViews == null){
            //instantiate the turn actions view array
            mTurnActionViews = new ArrayList<TurnActionMarkerView>();
        }

        //Store actions and reference to the action control panel
        mTurnActionViews.add(actionView);
        mAvailableActionsPanelView = panel;
    }
    /********** Implements listener of the available action view ***********/

    //show/hide activation control & shadow previously activated arranged formations
    private void updateFormationActivationOutlook(){
        LinearLayout formationRoundLayout = (LinearLayout) findViewById(R.id.formationCaptureLayout);
        Button configureBtn = (Button) findViewById(R.id.configureBtn);
        TurnActionMarkerView actionMarker = (TurnActionMarkerView) findViewById(R.id.actionMarker);

        if(mIsSubFormation == false){
            //handle only for main formation
            if(mIsSelected == false){
                configureBtn.setVisibility(View.GONE);
                //actionMarker.setVisibility(View.GONE);
                //set the outlook of the action marker to default view
                actionMarker.setToDefaultOutlook();
                //Set listener for on Drag event to catch the unit moved to the cell
            }else{
                //actionMarker.setVisibility(View.VISIBLE);
                //Set listener for on Drag event to catch the unit moved to the cell
            }
        }else if(mIsSubFormation == true){
            //Hide unconditionally the control elements for sub-formations
            configureBtn.setVisibility(View.GONE);
            actionMarker.setVisibility(View.GONE);
        }

        if(mIsSelected == false){
            formationRoundLayout.setBackgroundColor(Color.parseColor("#EDBB23"));
        }else{
            formationRoundLayout.setBackgroundColor(Color.parseColor("#558B2F"));
        }
    }

    @Override
    /*
    * Support the process of manually killing warrior
    * Stage 1: - persist the intent data of a warrior going to be killed
    * */
    public void OnWarriorMove(View sourceView, WarriorInCombatView warriorView, WarriorInCombat warrior) {
        mSourceView = (ArrangedWarriorsPerActivation) sourceView;
        mWarriorView = warriorView;
        mWarriorInCombat = warrior;
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
                    if(mWarriorInCombat == null || !mWarriorInCombat.title().equals(warriorTitle)){
                        //this is a warrior out of the other formation, therefore stop processing
                        mOwnWarrior = false;
                    }

                    //Stage 1: Remove the warrior from its previous position
                    if(mOwnWarrior == true) {
                        mWarriorView.setVisibility(View.GONE);

                        //remove warrior from the own formation
                        mFormation.killWarrior(mWarriorInCombat);


                        //remove warrior from the formation view local storage
                        mSourceView.removeWarrior(mWarriorInCombat);
                    }

                    //Stage 2: add the warrior to the rooted warriors set.
                    //...
                    if(mOwnWarrior == true){
                        //add new warrior to the rooted warriors
                        mRootedWarriorsView.addWarrior(mWarriorInCombat);
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