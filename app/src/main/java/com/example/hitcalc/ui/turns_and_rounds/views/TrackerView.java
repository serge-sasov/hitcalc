package com.example.hitcalc.ui.turns_and_rounds.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.ActionPoint;

import java.util.ArrayList;

public class TrackerView extends ConstraintLayout {
    private Game mGame;

    public TrackerView(Context context) {
        super(context);
        initializeView(context);
    }

    public TrackerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public TrackerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.tracker_view, this);
    }

    public void config(Game game){
        mGame = game;

        updateView();
    }

    private void updateView(){
        String message = null;
        //display current round number
        TextView roundNumber = (TextView) findViewById(R.id.roundNumber);
        roundNumber.setText("" + mGame.currentRound().id());

        //display current turn number
        TextView turnNumber = (TextView) findViewById(R.id.turnNumber);
        turnNumber.setText("" + mGame.currentRound().currentTurn().id());

        //Calculate available points
        ArrayList<ActionPoint> availableActionPoints = mGame.currentRound().getAvailableActionPoints(mGame.activePlayer().title());
        if(availableActionPoints != null){
            if(availableActionPoints.size() > 0){
                message = availableActionPoints.size() + " " + getResources().getString(R.string.available_action_points);;
            }else{
                message = getResources().getString(R.string.no_action_points);;

            }
        }
        TextView actionPointsValue = (TextView) findViewById(R.id.actionPointsValue);
        actionPointsValue.setText(message);


        //derive current tern stage (main turn or seizure attempt)
        String currentTurnPlayer = mGame.currentRound().currentTurn().activePlayer();
        String prevTurnPlayer = null;
        if( mGame.currentRound().getPreviousTurn() != null){
            prevTurnPlayer = mGame.currentRound().getPreviousTurn().activePlayer();
        }

        //Build up the massege
        message = getResources().getString(R.string.main_turn);
        if(prevTurnPlayer != null) {
            if (currentTurnPlayer.equals(prevTurnPlayer)) {
                message = getResources().getString(R.string.seizure_attempt);
            }
        }

        //display active players title
        TextView turnState = (TextView) findViewById(R.id.turnState);
        turnState.setText(message);
    }
}
