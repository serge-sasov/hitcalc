package com.example.hitcalc.ui.combat_scenes.army.views;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.combat_scenes.map.Map;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FormationView extends LinearLayout {
    private Formation mFormation;
    private ArrayList<WarriorItem> mWarriors;
    private String mLeader;
    private Map mMap; //reference to the battle map the warriors are placed on.
    private ArrayList<WarriorItem> mProcessedWarriors; //list of warriors that has been participated in the battle scenes

    //attributes for handling of unit selection state
    private WarriorItem mSeletedWarrior; //is used to pass the data to the calling method
    private String mFormationTitle;

    public FormationView(Context context, String leader) {
        super(context);
        initializeFormationList(context);

        //set an initial data derived from formation provided
        mLeader = leader;

        //display warriors of the formation
        updateViewOutput();
    }

    public FormationView(Context context) {
        super(context);
        initializeFormation(context);

    }

    public FormationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeFormation(context);
    }

    public FormationView(Context context,
                    AttributeSet attrs,
                    int defStyle) {
        super(context, attrs, defStyle);
        initializeFormation(context);
    }

    private void initializeFormation(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.formation_view, this);
    }

    private void initializeFormationList(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.formation_view_list, this);
    }

    //Display warriors of the formation
    protected void updateViewOutput(){
        LinearLayout layout;

        TextView formationTitle = (TextView)findViewById(R.id.formationTitle);

        formationTitle.setText(mFormationTitle);

        if(mWarriors != null){
            //Add a warrior list of the formation
            for(WarriorItem warriorItem : mWarriors ){

                Boolean condition = true;

                if(warriorItem.type().equals("Harizma") != true && mMap.formations() != null &&
                        mMap.formations().get(mFormation) != null &&
                        mMap.formations().get(mFormation).contains(warriorItem) == true) {

                    //allow to display a warrior if it is missing in the activated warrior set
                    condition = false;
                }

                if(condition == true){
                    //Need to convert objects from WarriorItem to WarriorInShock
                    WarriorInShock warrior = (WarriorInShock) warriorItem;

                    UnitView unitView;
                    int id;

                    layout = (LinearLayout) findViewById(R.id.formationLayout);

                    unitView = new UnitView(getContext(), warrior.type());
                    id = View.generateViewId();
                    unitView.setId(id);

                    unitView.setPadding(5, 5, 5, 5);
                    //Create child object out of WorrierItem
                    unitView.setUnit(warrior);
                    layout.addView(unitView);

                    //Allow units to be dragged by user and moved around
                    unitView.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            JSONObject warriorJSON = null;
                            try {
                                //Convert warrior into JSON Object
                                warriorJSON = warrior.convertToJSON();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //send to the map setup the formation/warrior mapping
                            mMap.placeWarrior((FormationActivated) mFormation, warrior);


                            JSONObject jo = new JSONObject();
                            // --------- build up json data --------------
                            try {
                                jo.put("formation_hashcode", mFormation.hashCode());
                                jo.put("warrior_hashcode", warrior.hashCode());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // --------- build up json data --------------
                            ClipData data = ClipData.newPlainText("hashcodes", jo.toString());
                            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                            view.startDrag(data, //data to be dragged
                                    shadowBuilder, //drag shadow
                                    view, //local data about the drag and drop operation
                                    0   //no needed flags
                            );
                            if (!(warrior.type().equals("Dummy") || warrior.type().equals("Harizma"))) {
                                //Hide any normal units from display
                                view.setVisibility(View.GONE);
                            }
                            return true;
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //Set user specific data if data were provided
        if(mWarriors != null){
            //display warriors of the formation
            updateViewOutput();
        }
    }

    /*
    * Configure the formation and update their outlook view
    * */
    public void setFormation(Map map, Formation formation) {
        //set an initial data derived from formation provided
        mMap = map;
        mFormation = formation;
        mWarriors = formation.warriors();
        mLeader = formation.getLeader();
        mFormationTitle = formation.getTitle();

        updateViewOutput(); //display warriors of the formation
    }

    public WarriorItem getWarrior() {
        return mSeletedWarrior;
    }
}
