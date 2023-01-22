package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.WarriorInCombatView;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.UnitAbstractAction;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.views.UnitWeaknessSetupTurnView;

import java.util.ArrayList;

//public class ArrangedWarriorsPerActivation extends LinearLayout implements KillWarrior {
public class ArrangedWarriorsPerActivation extends LinearLayout implements MoveWarrior.OnWarriorMoveListener {
    private Army mArmy;
    private String mEffect; //Stores the view title
    private Boolean mActivated = false;
    private ArrayList<WarriorInCombat> mWarriors;

    // ----------- recovering warrior listener -----------
    private WarriorInCombat mRecoveringWarrior;
    private Boolean mRecovering = false; //flag to show weather the listener shall be appointed to the warrior to enable manual recovering
    private FormationInCombat mRecoveringFormation;

    //----------- rooting warrior listener -----------
    private MoveWarrior mRootWarriorInterface = new MoveWarrior();

    // provide a reference to root warrior layout as a listener for manual action
    public void setOnRootListener(MoveWarrior.OnWarriorMoveListener listener) {
        if(mRootWarriorInterface == null){
            mRootWarriorInterface = new MoveWarrior();
        }
        mRootWarriorInterface.setOnMoveListener(listener);
    }

    public ArrangedWarriorsPerActivation(Context context) {
        super(context);
        initializeView(context);
    }

    public ArrangedWarriorsPerActivation(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public ArrangedWarriorsPerActivation(Context context, AttributeSet attrs, int defStyle) {
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
    public void warriors(ArrayList<WarriorInCombat> warriors, Integer activationValue, Boolean formationActivated){
        mWarriors = warriors;

        //remove any child views
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.warriorsList);
        linearLayout.removeAllViews();

        updateViewOutput(warriors, activationValue, formationActivated);
    }

    /*
     * activationValue - represent a filter criteria to select the warriors which current weakness value
     *  matches to the given activation value
     * input:
     * - formationActivated flag shows current formation state
     * */
    public void warriors(FormationInCombat formation, String effect, ArrayList<WarriorInCombat> warriors, Boolean recovering){
        mRecoveringFormation = formation;
        mRecovering = recovering; //flag to show weather the listener shall be appointed to the warrior to enable manual recovering
        warriors(effect, warriors);
    }

    public void warriors(Army army, String effect, ArrayList<WarriorInCombat> warriors){
        mArmy = army;
        warriors(effect, warriors);
    }

    public void warriors(String effect, ArrayList<WarriorInCombat> warriors){
        mWarriors = warriors;
        mEffect = effect;

        updateViewOutput(warriors, mEffect);
    }

    public void actualizeWarriors(ArrayList<WarriorInCombat> warriors){
        mWarriors = warriors;

        //remove any child views
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.warriorsList);
        linearLayout.removeAllViews();

        updateViewOutput(warriors, mEffect);
    }

    public void addWarrior(WarriorInCombat warrior){
        ArrayList<WarriorInCombat> warriors = new ArrayList<WarriorInCombat>();
        warriors.add(warrior);

        updateViewOutput(warriors, mEffect);
    }

    //Display warriors of the formation
    protected void updateViewOutput(ArrayList<WarriorInCombat> warriors, String effect){
        ArrangedWarriorsPerActivation arrangedWarriorsView = this;

        TextView actionPointValue = (TextView) findViewById(R.id.criteria);
        actionPointValue.setText(effect);

        //listen to the manual recover for the warriors
        LinearLayout rootedWarriorLayout = (LinearLayout) findViewById(R.id.warriorsList);
        rootedWarriorLayout.setOnDragListener(new ManuallyWarriorRecoverHandler());

        if(warriors != null && warriors.size() > 0) {
            for (WarriorInCombat warrior : warriors) {
                if (warrior.simple() == true) {
                    deactivateMessage();
                    View warriorView = null;

                    FormationInCombat formation = null;
                    if(mArmy != null) {
                        formation = (FormationInCombat) mArmy.deriveFormationByWarrior(warrior);
                    }

                    //Cast from Formation to FormationInCombat
                    if(effect.equals("Retreat") && formation != null){
                        //put additionally root check to the warrior view
                        warriorView = (UnitWeaknessSetupTurnView ) RootViewOutlook(formation, warrior);
                    }else {
                        warriorView = new WarriorInCombatView(getContext());
                        ((WarriorInCombatView ) warriorView).setUnit(warrior);
                    }

                    //add listeners to the recovering effect
                    if(mRecovering == true && warriorView instanceof WarriorInCombatView){
                        ((WarriorInCombatView) warriorView).enableWarriorMove(warrior);
                        ((WarriorInCombatView) warriorView).setOnRecoverListener(arrangedWarriorsView);
                    }

                    rootedWarriorLayout.addView(warriorView);
                }
            }
        }else{
            //Show default message "no warrior rooted yet"
            activateMessage();
        }
    }

    // Add root action to remove the warrior directly in the round completion view
    private UnitWeaknessSetupTurnView RootViewOutlook(FormationInCombat formation, WarriorInCombat warrior){
        //get a root action for given warrior
        ArrayList<UnitAbstractAction> actionList = warrior.rootAction(formation);

        //define available action list for each warrior
        UnitWeaknessSetupTurnView unitView = new UnitWeaknessSetupTurnView(getContext());
        unitView.setAvailableActions((WarriorInCombat) warrior, actionList);

        return unitView;
    }

    //Display warriors of the formation
    protected void updateViewOutput(ArrayList<WarriorInCombat> warriors, Integer activationValue, Boolean formationActivated){
        ArrangedWarriorsPerActivation arrangedWarriorsPerActivation = this;

        TextView actionPointValue = (TextView) findViewById(R.id.criteria);
        //get localized "Action Points"
        String available_action_pointsStr = getResources().getString(R.string.available_action_points);
        String requiredStr = getResources().getString(R.string.required);
        actionPointValue.setText(requiredStr + " " + activationValue + " " + available_action_pointsStr);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.warriorsList);

        if(warriors != null) {
            for (WarriorInCombat warrior : warriors) {
                if (warrior.simple() == true) {
                    deactivateMessage();

                    //show the content only in case the worrier's activation value matches to its weakness state
                    int threshold = warrior.getActivationThreshold();
                    if (formationActivated == true) {
                        threshold = threshold + 1;
                    }

                    if (threshold == activationValue) {
                        //Cast from Formation to FormationInCombat
                        WarriorInCombatView warriorView = new WarriorInCombatView(getContext());
                        warriorView.setUnit(warrior);

                        //allow the player manually moving a warrior killed during the turn into rooted warriors set
                        mRootWarriorInterface.enableWarriorMove(arrangedWarriorsPerActivation, warriorView, warrior);

                        //assign a view id value to the new element
                        int id = View.generateViewId();
                        warriorView.setId(id);

                        linearLayout.addView(warriorView);
                    }
                }
            }
        }
    }

    //remove killed warrior from the arranged list
    public void removeWarrior(WarriorInCombat warrior){
        if(mWarriors != null && mWarriors.size() > 0) {
            mWarriors.remove(warrior);
        }
    }

    //hide default message that there is still no died warriors.
    private void deactivateMessage(){
        TextView message = (TextView) findViewById(R.id.message);
        message.setVisibility(View.GONE);
    }

    //hide default message that there is still no died warriors.
    private void activateMessage(){
        TextView message = (TextView) findViewById(R.id.message);
        message.setVisibility(View.VISIBLE);
    }

    //Highlight the warriors activated via user action.
    public void activate(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.warriorsPerActivationLayout);
        linearLayout.setBackgroundColor(Color.parseColor("#558B2F")); //green
        mActivated = true;
    }

    //Shadow the warriors activated via user action
    public void deactivate(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.warriorsPerActivationLayout);
        linearLayout.setBackgroundColor(Color.parseColor("#7E8081")); //gray
        mActivated = false;
    }

    //List of provided warriors
    public ArrayList<WarriorInCombat> warriors(){
        return mWarriors;
    }

    //Show status of the warriors array, i.e. activated or disabled. In case the warriors
    //are activated, they shall be transferred as a filter criteria to the turn layout.
    public Boolean state(){
        return mActivated;
    }

    //Give with a list of activated warriors
    public ArrayList<WarriorInCombat> activatedWarriors(){
        if(mActivated == true) {
            return mWarriors;
        }

        return null;
    }

    @Override
    //Catch long click event on a warrior item and store it reference to use afterwards in the OnDrag Event Handler
    public void OnWarriorMove(View sourceView, WarriorInCombatView warriorView, WarriorInCombat warrior) {
        mRecoveringWarrior = warrior;
    }

    /*
     * Process the manually killed warrior workflow
     * Stage 2: Treated as a confirmation, that the warrior has successfully reached the target rooted warrior set.
     * */
    class ManuallyWarriorRecoverHandler implements View.OnDragListener {
        private Boolean mOwnWarrior = true; //a flag to check the warrior membership

        @Override
        public boolean onDrag(View view, DragEvent event) {
            int action = event.getAction();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    // Derive warrior formation first

                    if(mRecoveringWarrior != null) {
                        FormationInCombat formation = null;
                        if(mRecoveringFormation == null) {
                            formation = (FormationInCombat) mArmy.deriveFormationByWarrior(mRecoveringWarrior);
                        }else{
                            formation = mRecoveringFormation;
                        }
                        //Recover warrior
                        formation.recoverWarrior(mRecoveringWarrior);

                        Log.i("Warrior recovered:", mRecoveringWarrior.title());

                        //clean up the attribute
                        mRecoveringWarrior = null;
                    }


                    break;
                case DragEvent.ACTION_DROP:
                    //no action available

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //to define in future
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}
