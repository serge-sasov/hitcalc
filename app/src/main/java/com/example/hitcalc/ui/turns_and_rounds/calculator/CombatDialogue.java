package com.example.hitcalc.ui.turns_and_rounds.calculator;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.hitcalc.R;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.combat_pager.tables.ShockResultTable;
import com.example.hitcalc.ui.combat_scenes.map.MapView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.ForcedRetreat;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.ShockCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.UnitAbstractAction;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.views.UnitWeaknessSetupTurnView;
import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.ui.combat_scenes.army.views.ShockResultView;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CombatDialogue extends DialogFragment {
    private ShockCombatCalculator mShockCombatCalculator;
    private Integer mHits; //Calculated hits result
    private Integer mShockHits; //
    private Integer mDiceRandom; //Random Dice Roll Result
    private LoadTable mAttackerTable;
    private LoadTable mDeffenderTable;
    private GameStorage mGameStorage;
    private MapView mMapView;


    public CombatDialogue(ShockCombatCalculator shockCombatCalculator,
                          GameStorage gameStorage, int rand){
        mShockCombatCalculator = shockCombatCalculator;
        mShockHits = shockCombatCalculator.getShockCombatCalculationResult();
        mAttackerTable = gameStorage.getShockResultAttackerTable();;
        mDeffenderTable = gameStorage.getShockResultDefenderTable();
        mDiceRandom = rand;

        mGameStorage = gameStorage;
        mMapView = gameStorage.game().mapView();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        HashMap<String, ShockResultTable> combatOutcomes = new HashMap<String, ShockResultTable>();
        HashMap<String, ArrayList<WarriorInShock>> warriors = new HashMap<String, ArrayList<WarriorInShock>>();

        //Calculate total hit number
        mHits = mShockHits + mDiceRandom;

        // All the rest of the code goes here
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());

        LayoutInflater inflater =
                getActivity().getLayoutInflater();

        View dialogView =
                inflater.inflate(R.layout.shock_result_dialogue, null);

        //Load applicable scenario
        TextView shockCombatTableResult = (TextView) dialogView.findViewById(R.id.shockCombatTableResult);
        shockCombatTableResult.setText("" + mShockHits);

        TextView rundomDiceRoll = (TextView) dialogView.findViewById(R.id.randomDiceRollValue);
        rundomDiceRoll.setText("" + mDiceRandom);

        ShockResultView attackerShockResultView = (ShockResultView) dialogView.findViewById(R.id.attackerResultView);
        ShockResultView defenderShockResultView = (ShockResultView) dialogView.findViewById(R.id.defenderResultView);

        try {
            //combat results of both parties
            combatOutcomes.put("Attacker", new ShockResultTable(mAttackerTable, mHits));
            combatOutcomes.put("Defender", new ShockResultTable(mDeffenderTable, mHits));

            //list of warriors participating in the combat scene from both sides
            warriors.put("Attacker", mShockCombatCalculator.attackers());
            warriors.put("Defender", mShockCombatCalculator.defenders());

            attackerShockResultView.displayShockResult(combatOutcomes.get("Attacker"), mShockCombatCalculator.getBestAttackerUnit(), "Attacker");
            defenderShockResultView.displayShockResult(combatOutcomes.get("Defender"), mShockCombatCalculator.getDefenderUnit(), "Defender");

            //handle checks to be carried out manually (root or retreat)
            handleRootCheckAndWeaknessConfig(dialogView, combatOutcomes, warriors);

        } catch (CsvException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Add buttons to control
        Button btnOK = (Button) dialogView.findViewById(R.id.btnOK);
        btnOK.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //processing of weakness, root & retreat events
                handleOutcome(combatOutcomes, warriors);

                //remove all warriors participated in the combat scene
                mMapView.cleanUp();

                dismiss();
            }
        });

        builder.setView(dialogView).setMessage("Add a new note");

        // Handle the cancel button
        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(dialogView).setMessage("Combat result");

        return builder.create();
    }

    protected void handleOutcome(HashMap<String, ShockResultTable> combatOutcomes,
                                 HashMap<String, ArrayList<WarriorInShock>> warriors){
        LoadTable actionsTable = mGameStorage.getUnitActionsTable();

        for(String initiative : combatOutcomes.keySet()) {
            ShockResultTable civilCombatOutcome = combatOutcomes.get(initiative);
            try {

                for(WarriorInShock warrior : warriors.get(initiative)){
                        //add combat weakness after battle scene resolution
                        ShockCombat shockCombat = new ShockCombat(actionsTable, (WarriorInCombat) warrior);
                        shockCombat.apply();
                }

                //if any warriors roots
                if (civilCombatOutcome.rout()) {
                    root(warriors.get(initiative), actionsTable);
                }

                //if any defending warriors retreats
                if (civilCombatOutcome.retreat()) {
                    retreat(warriors.get(initiative), actionsTable);
                }

            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
        }
    }

    protected void handleRootCheckAndWeaknessConfig(View dialogView, HashMap<String, ShockResultTable> combatOutcomes,
                                                    HashMap<String, ArrayList<WarriorInShock>> warriors){
        LoadTable actionsTable = mGameStorage.getUnitActionsTable();

        for(String initiative : combatOutcomes.keySet()) {
            ShockResultTable civilCombatOutcome = combatOutcomes.get(initiative);

            //handle two size units weakness
            phalanxWeaknessSetup(dialogView, warriors.get(initiative));

            //if any defending warriors retreats
            if (civilCombatOutcome.routCheck()) {
                rootCheck(dialogView, warriors.get(initiative));
            }
        }
    }

    //handle root event
    protected void root(ArrayList<WarriorInShock> warriors, LoadTable actionsTable){
        //get warrior formation activated
        HashMap <FormationActivated, ArrayList<WarriorInShock>> formations = mMapView.map().formations();
        for(WarriorInShock warrior: warriors){
            for(FormationActivated formationActivated : formations.keySet()){
                if(formations.get(formationActivated).contains(warrior)){
                    //kill warrior in the parent formation
                    FormationInCombat formationInCombat = formationActivated.source();
                    formationInCombat.killWarrior((WarriorInCombat) warrior);
                }
            }
        }
    }

    //handle retreat event
    protected void retreat(ArrayList<WarriorInShock> warriors, LoadTable actionsTable) throws IOException, CsvException {
        for(WarriorInShock warrior: warriors){
            ForcedRetreat forcedRetreat = new ForcedRetreat(actionsTable, (WarriorInCombat) warrior);
            forcedRetreat.apply();
        }
    }

    //handle root check case
    protected void rootCheck(View dialogView, ArrayList<WarriorInShock> warriors){
        //get warrior formation activated
        HashMap <FormationActivated, ArrayList<WarriorInShock>> formations = mMapView.map().formations();
        LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.outcomeSetup);

        for(WarriorInShock warrior: warriors){
            for(FormationActivated formationActivated : formations.keySet()){
                if(warrior.simple() && formations.get(formationActivated).contains(warrior)){
                    //search for the source formation the warrior belongs to
                    FormationInCombat sourceFormation = formationActivated.source();

                    //get a root action for given warrior
                    ArrayList<UnitAbstractAction> actionList = ((WarriorInCombat) warrior).rootAction(sourceFormation);

                    //define available action list for each warrior
                    UnitWeaknessSetupTurnView unitView = new UnitWeaknessSetupTurnView(getContext());
                    unitView.setAvailableActions((WarriorInCombat) warrior, actionList);

                    int id = View.generateViewId();

                    unitView.setId(id);
                    linearLayout.addView(unitView);
                }
            }
        }
    }

    protected void phalanxWeaknessSetup(View dialogView, ArrayList<WarriorInShock> warriors){
        //get warrior formation activated
        HashMap <FormationActivated, ArrayList<WarriorInShock>> formations = mMapView.map().formations();
        LinearLayout linearLayout = (LinearLayout) dialogView.findViewById(R.id.phalanxSetup);

        LoadTable unitActionsTable = mGameStorage.getUnitActionsTable();

        for(WarriorInShock warrior: warriors){
            for(FormationActivated formationActivated : formations.keySet()){
                if(warrior.simple() && formations.get(formationActivated).contains(warrior) && warrior.twoHexSize()){
                    //search for the source formation the warrior belongs to
                    FormationInCombat sourceFormation = formationActivated.source();

                    //get a root action for given warrior
                    ArrayList<UnitAbstractAction> actionList = ((WarriorInCombat) warrior).shockAction(unitActionsTable);

                    //define available action list for each warrior
                    UnitWeaknessSetupTurnView unitView = new UnitWeaknessSetupTurnView(getContext());
                    unitView.setAvailableActions((WarriorInCombat) warrior, actionList);

                    int id = View.generateViewId();
                    unitView.setId(id);

                    linearLayout.addView(unitView);
                }
            }
        }
    }
}
