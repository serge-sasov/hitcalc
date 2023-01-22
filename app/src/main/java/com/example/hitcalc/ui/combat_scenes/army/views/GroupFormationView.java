package com.example.hitcalc.ui.combat_scenes.army.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.ui.combat_scenes.map.Map;
import com.example.hitcalc.ui.combat_scenes.navigation.views.ArmyNavigatorView;

import java.util.ArrayList;
import java.util.HashMap;

/*
* Class is used to show the available units for given formation along
* with any number of applicable sub-formations
* */
public class GroupFormationView extends LinearLayout {
    private Scenario mScenario;
    private Map mMap;
    private String mAttakerCivil, mDefenderCivil;
    private String mAttackerFormationTitle;
    private boolean mIsAttackerHex = false;
    private ArrayList<Integer> mFormationViewIds; //Ids for available formationViews
    private ArmyNavigatorView mArmyNavigatorView;
    private String mDefenderFormationTitle; //Store this value to be able to recover it once returns

    public GroupFormationView(Context context) {
        super(context);
        initialize(context);
    }

    public GroupFormationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public GroupFormationView(Context context,
                         AttributeSet attrs,
                         int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.group_army_formation, this);
    }

    /*
    * Initial setup for display of available navigation menu for defending side along with all available formations and
    * selected attacker formation including any number of sub-formations
    *
    * scenario - main scenario object
    * attackerCivil - a name of civilization chosen as attacker for given battle scene
    * attackerFormationTitle - a name of formation of selected civilization chosen as attacker for the battle scene
    * defenderCivil - an opponent to defender civilization, may select any warrior of any available formation
    */

    public void configForFormationNavigation(Map map, Scenario scenario, String attackerCivil, String attackerFormationTitle, String defenderCivil){
        mScenario = scenario;
        mAttakerCivil = attackerCivil;
        mDefenderCivil = defenderCivil;
        mAttackerFormationTitle = attackerFormationTitle;
        mMap = map;

        //Get first element of the available list of defending formation as a default formation title
        mDefenderFormationTitle = mScenario.getArmy(defenderCivil).getFormations().get(0).getTitle();

        //Navigation menu for defending formations
        mArmyNavigatorView = (ArmyNavigatorView) findViewById(R.id.groupFormationNavigation);

        //Configure this just one time and enable listener here to catch change event to choice new formation selected
        mArmyNavigatorView.configForFormationNavigation(scenario, defenderCivil, mDefenderFormationTitle);
        displaySelectedFormation(defenderCivil, mDefenderFormationTitle);

        mArmyNavigatorView.setOnChangeListener(new ArmyNavigatorView.OnChangeListener() {
            public void onDataChanged(String civil, String formation) {
                //Store last selected defender formation
                mDefenderFormationTitle = formation;
                displaySelectedFormation(civil, formation);
            }
        });
    }

    /*
    * Update menu item according to the selected formation
    *
    * Input:
    * formationTitle - title of selected formation
    * */
    public void updateMenuItem(String formationTitle){
        //Update the selected defender formation title, the display will be changed by setNavigationVisibility method
        mDefenderFormationTitle = formationTitle;
        mArmyNavigatorView.setFormationSelector(formationTitle);
    }

    /*
    *   Navigate menu to the selected formation of the given army
    *   civil - selected civil to display
    *   formationTitle - selected formation title out of that a unit going to be chosen
    *
    * */
    public void displaySelectedFormation(String civil, String formationTitle){
        LinearLayout layout = (LinearLayout) findViewById(R.id.groupFormationLayout);
        Formation baseFormation = mScenario.getArmy(civil).getFormationByTitle(formationTitle);

        //Update menu item according to the selected formation
        if(baseFormation.warriors().size() > 0){
            //There is just one single formation
            addSingleFormationView(layout, baseFormation, 0);

        }else if(baseFormation.subFormations() != null && baseFormation.subFormations().size() > 0){
            //There are many sub-formations
            HashMap<String, Formation> subFormations = baseFormation.subFormations();
            Integer index = 0;
            for(String title : subFormations.keySet()){
                addSingleFormationView(layout, subFormations.get(title), index);
                index++;
            }
        }

    }

    private void addSingleFormationView(LinearLayout layout, Formation formation, Integer id){
        FormationView formationView;
        Integer formationViewId;

        //for the first iteration (id = 0) remove all previous formation views
        if(mFormationViewIds != null && id == 0 && mFormationViewIds.size() > 0) {
            //clear out all previous formation views
            for(Integer index: mFormationViewIds) {
                formationView = (FormationView) findViewById(index);
                layout.removeView(formationView);
            }
        }

        formationView = new FormationView((getContext()));
        //Assign unique Id to the formation
        if(mFormationViewIds == null) {
            mFormationViewIds = new ArrayList<Integer>();
        }

        //Assign unique Id to the formation
        if(mFormationViewIds.size() == id) {
            formationViewId = View.generateViewId();
            //Store the view id value into the array for later reuse
            mFormationViewIds.add(formationViewId);
        }
        else{
            formationViewId = mFormationViewIds.get(id);
        }

        formationView.setId(formationViewId);
        formationView.setFormation(mMap, formation);
        //formationView.setPadding(5,5,5,5);
        layout.addView(formationView);
    }

    /*
    * Show/Hide the navigation menu depending on the chosen hex type, i.e. "attacker" or "defender"
    */
    public void setNavigationVisibility(boolean isAttackerHex){
        mIsAttackerHex = isAttackerHex;

        if(isAttackerHex){
            mArmyNavigatorView.setVisibility(View.GONE);
            displaySelectedFormation(mAttakerCivil, mAttackerFormationTitle);
        }else{
            mArmyNavigatorView.setVisibility(View.VISIBLE);
            displaySelectedFormation(mDefenderCivil, mDefenderFormationTitle);
        }
    }
}
