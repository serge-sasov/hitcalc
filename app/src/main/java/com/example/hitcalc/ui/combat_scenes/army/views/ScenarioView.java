package com.example.hitcalc.ui.combat_scenes.army.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;

import java.util.ArrayList;

public class ScenarioView extends LinearLayout {
    private Scenario mScenario;
    private Integer [] mArmyViewIds = {R.id.armyView01, R.id.armyView02};

    //Handle clicks and other staff on display activities
    private Army mSeletedArmy;
    private ArmyView mSelectedArmyView;

    public ScenarioView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ScenarioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ScenarioView(Context context,
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
        inflater.inflate(R.layout.scenario_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    //get a scenario
    public void setScenario(Scenario scenario){
        mScenario = scenario;

        updateViewOutput();
    }

    //Display the used armies
    protected void updateViewOutput(){

        if(mScenario != null){
            //get first all children of the view
            Integer [] armyViewIds = {R.id.armyView01, R.id.armyView02};

            //get a whole list of both army formations
            ArrayList<String> civils =  mScenario.getCivilizations();

            for(int i = 0; i < civils.size(); i++) {

                Army army = (Army) mScenario.getArmy(civils.get(i));
                ArmyView armyView = (ArmyView) findViewById(armyViewIds[i]);
                armyView.setArmy(army);

                armyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }
        }
    }
}
