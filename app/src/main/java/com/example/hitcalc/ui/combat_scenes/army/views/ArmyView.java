package com.example.hitcalc.ui.combat_scenes.army.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.combat_scenes.army.Formation;

import java.util.ArrayList;
import java.util.HashMap;

public class ArmyView extends LinearLayout {
    private Army mArmy;
    private ArrayList<Formation> mFormations;
    private Formation mChosenFormation;
    private HashMap<String, Integer> mFormationToViewIds;

    public ArmyView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ArmyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ArmyView(Context context,
                    AttributeSet attrs,
                    int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *           the current context for the view.
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.army_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    //get a list of formations
    public void setArmy(Army army){
        mArmy = army;
        mFormations = army.getFormations();

        updateViewOutput();
    }

    //Display warriors of the formation
    protected void updateViewOutput(){

        if(mFormations.isEmpty() == false){
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.armyLayout);
            mFormationToViewIds = new HashMap<String, Integer>();

            for(Formation formation : mFormations){
                // FormationView formationView = new FormationView(getContext(), formation);
                FormationView formationView = new FormationView(getContext(), formation.getLeader());
                //ViewIdGenerator.generateViewId();
                int id = View.generateViewId();
                mFormationToViewIds.put(formation.getLeader(), id);

                formationView.setId(id);
                linearLayout.addView(formationView);

                formationView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //highlight selected formation and gray out the other ones
                        mChosenFormation = formation;
                        highlightChosenFormation(id);

                        //Call parent view element
                        View parent = (View) v.getParent();
                        parent.performClick();
                    }
                });
            }
        }
    }

    private void highlightChosenFormation(Integer formationViewId){
        for(Integer viewId : mFormationToViewIds.values()){
            FormationView formationView = (FormationView) findViewById(viewId);
            TextView formationTitle = formationView.findViewById(R.id.formationTitle);

            if(viewId.equals(formationViewId)){
                //If selected formation is found highlight it
                formationTitle.setBackgroundResource(R.color.colorRed);
            }
            else{
                //Simply gray out everything else
                formationTitle.setBackgroundResource(R.color.colorGray);
            }

        }
    }

    //Get Selected formation from the activity
    public Formation getChosenFormation() {
        return mChosenFormation;
    }

    public void setChosenFormation(Formation formation) {
        mChosenFormation = formation;

        //Highlight given formation row
        highlightChosenFormation(mFormationToViewIds.get(formation.getLeader()));
    }
}
