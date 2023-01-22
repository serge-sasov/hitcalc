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
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.completion.CompletionRoundArmyView;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;

/*
 * At the end of each round has to be shown the final action to be done on the units which reached
 * a given threshold.
 */
public class RoundCompletionNestedFragment extends Fragment {

    private GameStorage mGameStorage;
    private Game mGame;     //current game instance

    //player's initiative being displayed
    private Boolean mIsActivePlayerFragment = true;

    public RoundCompletionNestedFragment(GameStorage gameStorage, int position){

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

        View root = inflater.inflate(R.layout.round_completion_nested_fragment_view, container, false);

        CompletionRoundArmyView armyView = (CompletionRoundArmyView) root.findViewById(R.id.armyTurnView);
        if(mIsActivePlayerFragment == true){
            armyView.setArmy(mGame.getActivePlayersArmy());
        }
        else{
            armyView.setArmy(mGame.getPassivePlayersArmy());
        }

        return root;
    }
}
