package com.example.hitcalc.ui.turns_and_rounds.sub_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.hitcalc.R;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.ArmyTurnView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.FormationTurnView;
import com.example.hitcalc.ui.turns_and_rounds.game.Turn;

public class EditNestedFragment extends TurnNestedFragment {

    public EditNestedFragment(GameStorage gameStorage, int position) {
        super(gameStorage, position);
        //Set intentionally the active player attribute to false to show the whole army's warriors range
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ArmyInCombat army = null;

        View root = inflater.inflate(R.layout.nested_fragment_turn, container, false);

        ArmyTurnView armyTurnView = (ArmyTurnView) root.findViewById(R.id.armyTurnView);
        FormationTurnView formationTurnView = (FormationTurnView) root.findViewById(R.id.formationTurnView);


        if(mIsActivePlayerFragment == true){
            //Set up army configuration of active player
            army = mGame.getActivePlayersArmy();
        }else{
            //Set up army configuration of passive player
            army = mGame.getPassivePlayersArmy();
        }
        Turn currentTurn = mGame.currentRound().currentTurn();
        armyTurnView.setArmy(currentTurn, army, mGameStorage.getUnitActionsTable());

        //hide formation view
        formationTurnView.setVisibility(View.GONE);

        return root;
    }
}
