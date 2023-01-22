package com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.view;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;

import java.util.ArrayList;

/*
* The view shows the availbale player's action for chosen formation and current turn.
* The list of actions is reloaded according to the selected formation data provided.
* */
public class AvailableActionsView extends LinearLayout {
    private ArrayList<TurnAbstractAction> mActions;
    private ArrayList<Integer> mViewIdArray = new ArrayList<Integer>(); //holds a list of generated action view identities

    /*
    * Step 1 - This interface defines the type of messages I want to communicate to my owner.
    * Provide the info about the available action view along with the action made recently
    * */
    public interface OnDragListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onDrag(AvailableActionsView panel, TurnActionMarkerView actionView); //current view & formation title
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private AvailableActionsView.OnDragListener mOnDragListener = null;

    private ArrayList<AvailableActionsView.OnDragListener> mOnDragListeners = null;

    // Step 3: Assign the listener implementing events interface that will receive the events
    public void setOnDragListener(AvailableActionsView.OnDragListener listener) {
        mOnDragListener = listener;
    }

    //create a list of listeners
    public void setOnDragListeners(AvailableActionsView.OnDragListener listener) {
        if(mOnDragListeners == null){
            mOnDragListeners = new ArrayList<AvailableActionsView.OnDragListener>();
        }
        if(!mOnDragListeners.contains(listener)) {
            mOnDragListeners.add(listener);
        }
    }

    public AvailableActionsView(Context context) {
        super(context);
        initializeView(context);
    }

    public AvailableActionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public AvailableActionsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.available_action_views, this);
    }

    public void setActions(ArrayList<TurnAbstractAction> actions){
        mActions = actions;
        updateViewOutput();
    }

    //Display available turn actions
    protected void updateViewOutput(){
        AvailableActionsView availableActionsView = this;

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.availableActions);

        int itteration = 0;
        for (TurnAbstractAction action : mActions) {

            TurnActionMarkerView markerView;
            int id;

            //Reuse or instantiate the market view
            if(mViewIdArray.size() < itteration + 1) {
                //if no object exists, create a new one
                markerView = new TurnActionMarkerView(getContext());
                id = View.generateViewId();
                mViewIdArray.add(id);

                //remove any previous configuration first
                markerView.clear();

                //configure new settings
                markerView.setAction(action);
                markerView.setId(id);

                //Allow action marker to be dragged by user and moved around
                markerView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        markerView.getLastAction();

                        //Provide the selected action type to the formation action control box
                        ClipData data = ClipData.newPlainText("action", ((TurnActionMarkerView) view).getLastAction().title());
                        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                        view.startDrag( data, //data to be dragged
                                shadowBuilder, //drag shadow
                                view, //local data about the drag and drop operation
                                0   //no needed flags
                        );

                        //fire event for all listeners
                        if(mOnDragListeners != null){
                            for(AvailableActionsView.OnDragListener listener : mOnDragListeners){
                                listener.onDrag(availableActionsView, (TurnActionMarkerView) view);
                            }
                        }

                        return true;
                    }
                });

                //add new view to layout
                linearLayout.addView(markerView);
            }else{
                //reuse the previous one
                id = mViewIdArray.get(itteration);
                markerView = (TurnActionMarkerView) findViewById(id);

                //show up the view unconditionally
                markerView.setVisibility(View.VISIBLE);

                //remove any previous configuration first
                markerView.clear();

                //configure new settings
                markerView.setAction(action);
            }

            //increase the iteration value by ones
            itteration++;
        }

        //hide any unused previously generated market views
        if(mViewIdArray.size() > itteration){
            for(int i = itteration; i < mViewIdArray.size(); i++){
                int id = mViewIdArray.get(i);
                TurnActionMarkerView markerView = (TurnActionMarkerView) findViewById(id);
                //show up the view unconditionally
                markerView.setVisibility(View.GONE);
            }
        }
    }

    //Hide selected action
    public void hideAction(TurnActionMarkerView actionView){
        if(actionView != null){
            actionView.setVisibility(View.GONE);
        }
    }

    //Show selected action
    public void showAction(TurnActionMarkerView actionView){
        if(actionView != null){
            actionView.setVisibility(View.VISIBLE);
        }
    }
}
