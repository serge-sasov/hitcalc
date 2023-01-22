package com.example.hitcalc.ui.turns_and_rounds.sub_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.ArmyTurnView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn.FormationTurnView;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.ui.turns_and_rounds.game.Turn;

import java.util.ArrayList;

public class TurnNestedFragment extends Fragment implements MainActivity.FragmentOnBackClickInterface{
    //keep the backup of the configuration in the file
    protected GameStorage mGameStorage;

    //current game instance
    protected Game mGame;

    //Selected formation to be activated in the turn
    private String mFormationTitle;

    //player's initiative being displayed
    protected Boolean mIsActivePlayerFragment = true;

    public TurnNestedFragment(GameStorage gameStorage, int position){

        //Get game storage from main activity and parse it out
        try {
            MainActivity activity = (MainActivity) getActivity();
            mGameStorage = gameStorage;
            mGame = mGameStorage.game();

        }catch(Error|Exception e){
            e.printStackTrace();
        }

        if(position == 1){
            //change the initiative for the being displayed passive player
            mIsActivePlayerFragment = false;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.nested_fragment_turn, container, false);

        ArmyTurnView armyTurnView = (ArmyTurnView) root.findViewById(R.id.armyTurnView);
        FormationTurnView formationTurnView = (FormationTurnView) root.findViewById(R.id.formationTurnView);
        Turn currentTurn = mGame.currentRound().currentTurn();
        String civilTitle;

        if(mIsActivePlayerFragment == true){
            //get chosen formation title -> transfer to the TurnSubFragment
            mFormationTitle = currentTurn.formationTitle();
            if(mFormationTitle != null) {
                //Remove army view element
                container.removeView(armyTurnView);

                //get formation data out of activated army from scenario data set
                civilTitle = mGame.activePlayer().title();
                ArrayList<Formation> formations = mGameStorage.scenario().getArmies().get(civilTitle).getFormations();

                formationTurnView.setFormation(currentTurn, currentTurn.getFormation(), mGameStorage.getUnitActionsTable());
            }
        }

        if(mIsActivePlayerFragment == false){
            //hide formation view
            formationTurnView.setVisibility(View.GONE);

            //create a copy of activated army
            civilTitle = mGame.passivePlayer().title();
            ArmyActivated army = (ArmyActivated) mGameStorage.scenario().getArmies().get(civilTitle);

            //Set up army configuration of passive player
            armyTurnView.setArmy(currentTurn, army, mGameStorage.getUnitActionsTable());
        }

        return root;
    }

    //Call the action rollback method to reverse any actions done
    @Override
    public void onBackClick() {
        //Detect what fragment is currently displayed - either for attacker (just one formation shall be accessible) or defender (all available formations)
        if(mIsActivePlayerFragment == true) {
            //Only selected formation shall be available
            FormationTurnView formationTurnView = (FormationTurnView) getView().findViewById(R.id.formationTurnView);
            formationTurnView.rollBackActions();

        }else{
            //a whole army must be accessible
            ArmyTurnView armyTurnView = (ArmyTurnView) getView().findViewById(R.id.armyTurnView);
            armyTurnView.rollBackActions();
        }
    }
}
