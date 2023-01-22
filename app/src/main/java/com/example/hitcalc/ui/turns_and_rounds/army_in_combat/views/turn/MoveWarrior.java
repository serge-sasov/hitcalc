package com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.turn;

import android.content.ClipData;
import android.view.View;

import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.views.WarriorInCombatView;

public class MoveWarrior {
    /*
     * Step 1 - This interface defines the type of messages I want to communicate to my owner.
     * Provide the info about the available action view along with the action made recently
     * */
    public interface OnWarriorMoveListener {
        /*
        input attributes:
        - this view
        - warrior in combat view
        - Warrior in Combat
         */
        public void OnWarriorMove(View sourceView,
                                  WarriorInCombatView warriorView,
                                  WarriorInCombat warrior);
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private OnWarriorMoveListener mWarriorMoveListener = null;

    // Step 3: Assign the listener implementing events interface that will receive the events
     public void setOnMoveListener(OnWarriorMoveListener listener) {
        mWarriorMoveListener = listener;
    }

    //allow the player manually moving a warrior killed during the turn into rooted warriors set
    public void enableWarriorMove(View sourceView,
                                  WarriorInCombatView warriorView, WarriorInCombat warrior){
        //Allow action marker to be dragged by user and moved around
        warriorView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //Provide the selected action type to the formation action control box
                ClipData data = ClipData.newPlainText("warrior", warrior.title());
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                view.startDrag( data, //data to be dragged
                        shadowBuilder, //drag shadow
                        view, //local data about the drag and drop operation
                        0   //no needed flags
                );

                //fire event for all listeners
                if(mWarriorMoveListener != null) {
                    mWarriorMoveListener.OnWarriorMove(sourceView, warriorView, warrior);
                }

                return true;
            }
        });
    }
}
