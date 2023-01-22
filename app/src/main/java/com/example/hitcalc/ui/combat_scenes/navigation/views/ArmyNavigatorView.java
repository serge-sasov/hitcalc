package com.example.hitcalc.ui.combat_scenes.navigation.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.utility.items_selector.ItemSelectorSimpleHorizontal;

import java.util.ArrayList;

public class ArmyNavigatorView extends LinearLayout{
private Scenario mScenario;
private ArrayList<String> mCivilizations;
private String mCivil; //chosen civilization
private String mFormationTitle; //last chosen formation


    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnChangeListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onDataChanged(String army, String formation); //provide item selected to the listener
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private OnChangeListener mListener;

    // Assign the listener implementing events interface that will receive the events
    public void setOnChangeListener(ArmyNavigatorView.OnChangeListener listener) {
        mListener = listener;
    }

    public ArmyNavigatorView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ArmyNavigatorView(Context context, Scenario scenario) {
        super(context);
        mScenario = scenario;

        initializeViews(context);
    }

    public ArmyNavigatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ArmyNavigatorView(Context context,
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
        inflater.inflate(R.layout.army_navigator, this);

        configArmyNavigator();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    //Set default view configuration
    protected void configArmyNavigator(){
        //Specify the input value for available armies and formations
        if(mScenario != null){
            mCivilizations = mScenario.getCivilizations();
            String civil = mCivilizations.get(0);
            String formationTitle = mScenario.getArmy(civil).getFormations().get(0).getLeader();

            configArmyNavigator(civil, formationTitle);
        }
    }

    //perform configuration with exact data provided (civil and formation) for initial setup
    protected void configArmyNavigator(String civil, String formationTitle){
        mCivil = civil;
        mFormationTitle = formationTitle;

        //Specify the input value for available armies and formations
        if(mScenario != null){
            mCivilizations = mScenario.getCivilizations();

            //Configure army civilization view
            ItemSelectorSimpleHorizontal armySelector = (ItemSelectorSimpleHorizontal) findViewById(R.id.armySelector);
            //Catch army data change event and update list of formation for new civilization chosen
            armySelector.setOnClickItemSelectorListener(new ItemSelectorSimpleHorizontal.OnClickItemSelectorListener() {
                @Override
                public void onItemSelectorClick(String civil) {
                    mCivil = civil;
                    updateFormationSelector(civil);

                    //If civil changes, then the formation list has to be changed as well
                    if (mListener != null)
                        mListener.onDataChanged(mCivil, mFormationTitle); // <---- fire listener here
                }
            });
            armySelector.setValues(mCivilizations);
            armySelector.setSelectedValue(civil);

            //Configure formation view
            ItemSelectorSimpleHorizontal formationSelector = (ItemSelectorSimpleHorizontal) findViewById(R.id.formationSelectorSimple);
            //Add list of available formations for the given civilization
            formationSelector.setValues(mScenario.getArmy(civil).getFormationNames());
            //Set selected formation pointer
            formationSelector.setSelectedValue(mFormationTitle);

            formationSelector.setOnClickItemSelectorListener(new ItemSelectorSimpleHorizontal.OnClickItemSelectorListener() {
                @Override
                public void onItemSelectorClick(String formationTitle) {
                    mFormationTitle = formationTitle;
                    if (mListener != null)
                        mListener.onDataChanged(mCivil, mFormationTitle); // <---- fire listener here
                }
            });
            /* ********************************************************** */
        }
    }

    //update army view for given civil with first formation as default value from the available list
    private void updateFormationSelector(String civil){
        String formationTitle = mScenario.getArmy(civil).getFormations().get(0).getLeader();
        mFormationTitle = formationTitle;

        ItemSelectorSimpleHorizontal formationSelector = (ItemSelectorSimpleHorizontal) findViewById(R.id.formationSelectorSimple);
        //Show default formation of the given army
        //formationSelector.setValues(mScenario.getArmy(civil).getFormationTitles());
        formationSelector.setValues(mScenario.getArmy(civil).getFormationLeaders());
    }

    //Set used formation
    public void setFormationSelector(String formation){
        ItemSelectorSimpleHorizontal formationSelector = (ItemSelectorSimpleHorizontal) findViewById(R.id.formationSelectorSimple);
        //Show default formation of the given army
        formationSelector.setSelectedValue(formation);
    }

    //provide basic data for initial setup of scenario navigation map
    public void configForScenarioNavigation(Scenario scenario, String civil, String formationTitle){
        mScenario = scenario;
        configArmyNavigator(civil, formationTitle);
    }

    /*
    * provide basic data for initial setup of given formation navigation map
    * scenario data
    * formationTitle - formation to be shown by default
    * civil - given civilization which formation shall be shown as selection options
    */
    public void configForFormationNavigation(Scenario scenario, String civil, String formationTitle){
        configForScenarioNavigation(scenario, civil, formationTitle);

        //hide civilization selection to prohibit any switch to enemy formation list
        ItemSelectorSimpleHorizontal armySelector = (ItemSelectorSimpleHorizontal) findViewById(R.id.armySelector);
        armySelector.setVisibility(View.GONE);
    }
}
