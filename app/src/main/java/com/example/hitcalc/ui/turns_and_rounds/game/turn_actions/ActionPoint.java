package com.example.hitcalc.ui.turns_and_rounds.game.turn_actions;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.game.Player;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/*
* The class is used to spent available AP if any
* */
public class ActionPoint extends TurnAbstractAction {

    //Instantiate constructor in the parent abstract class
    public ActionPoint(Player player){
        super(player);
        setup();
    }

    //Instantiate JSON constructor in the parent abstract class
    public ActionPoint(JSONObject jo, HashMap<Integer, Player> players) throws JSONException {
        super(jo, players);
        setup();
    }

    private void setup(){
        mColor = "green";
        mTitle = "Action Point";
        mTitleId = R.string.action_point;
    }
}
