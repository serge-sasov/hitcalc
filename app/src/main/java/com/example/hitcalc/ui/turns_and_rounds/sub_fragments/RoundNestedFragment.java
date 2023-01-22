package com.example.hitcalc.ui.turns_and_rounds.sub_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.round.ArmyRoundView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.round.FormationRoundView;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.PassTurn;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizureFailure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SkipSeizure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class RoundNestedFragment extends Fragment implements FormationRoundView.OnClickListener{
    //keep the backup of the configuration in the file
    private GameStorage mGameStorage;

    //current game instance
    private Game mGame;

    //Selected formation to be activated in the turn
    private String mFormationTitle;

    //player's initiative being displayed
    private Boolean mIsActivePlayerFragment = true;

    public RoundNestedFragment(GameStorage gameStorage, int position){

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

        View root = inflater.inflate(R.layout.nested_fragment_round, container, false);

        //Get game storage from main activity and parse it out
        try {
            MainActivity activity = (MainActivity) getActivity();
            mGameStorage = activity.gameStorageData();
            mGame = mGameStorage.game();

            //Instantiate a new game
            if (mGame == null) {
                //initialise the input attributes
                ArrayList<String> players = mGameStorage.scenario().getCivilizations();
                String firstPlayer = players.get(0);

                mGame = new Game(mGameStorage, firstPlayer);
                mGameStorage.setGame(mGame);
            }
        }catch(Error|Exception e){
            e.printStackTrace();
        }

        /*
         * Display active's player army
         * */
        ArmyRoundView armyInCombatTurnView = (ArmyRoundView) root.findViewById(R.id.armyInCombatView);
        //Set active army along with round data and a listener
        if(mIsActivePlayerFragment == true) {
            armyInCombatTurnView.setArmy(mGame.getActivePlayersArmy(), mGame.currentRound(), this);
        }else{
            armyInCombatTurnView.setArmy(mGame.getPassivePlayersArmy(), mGame.currentRound());
        }

        return root;
    }

    @Override
    //Store the chosen formation title in the GameStorage and navigate to the TurnFragment
    public void onClick(View view, FormationActivated formation, ArrayList<TurnAbstractAction> appliedActions) throws IOException, JSONException {
        //is applicable only for the active player of current turn
        if (mIsActivePlayerFragment == true) {
            //store current turn actions and user choices made
            mGame.configCurrentTurn(formation, appliedActions);

            if (appliedActions.size() == 1 &&
                    (appliedActions.get(0).getClass() == PassTurn.class ||
                            appliedActions.get(0).getClass() == SeizureFailure.class ||
                            appliedActions.get(0).getClass() == SkipSeizure.class)) {

                //finish current turn & navigate to the same page
                mGame.finishTurn();

                //Save changes made
                mGameStorage.save(getContext());

                if (mGame.checkRoundCompleted() == true) {
                    //Check if any completion actions are necessary

                    //Navigate to round completion fragment if any actions need to be done
                    Navigation.findNavController(view).navigate(R.id.action_fragmentRound_to_roundCompletionFragment);
                } else {
                    Navigation.findNavController(view).navigate(R.id.action_fragmentRound_self);
                }
            } else {
                //Navigate to the turn fragment configuration page
                Navigation.findNavController(view).navigate(R.id.action_fragmentRound_to_rootTurnFragment);
            }
        }
    }
}
