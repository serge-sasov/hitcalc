package com.example.hitcalc.ui.turns_and_rounds.game.turn_actions;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.game.Player;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PassTurn extends TurnAbstractAction {
    public PassTurn(Player player) {
        super(player);
        setup();
    }

    //Instantiate JSON constructor in the parent abstract class
    public PassTurn(JSONObject jo, HashMap<Integer, Player> players) throws JSONException {
        super(jo, players);
        setup();
    }

    private void setup(){
        mColor = "default";
        mTitle = "Pass Turn";
        mTitleId = R.string.pass_turn;
    }

    @Override
    public void apply() {

    }

    @Override
    public void rollback() {

    }


}
