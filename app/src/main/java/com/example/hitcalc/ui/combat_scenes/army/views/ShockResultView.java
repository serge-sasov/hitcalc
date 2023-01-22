package com.example.hitcalc.ui.combat_scenes.army.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.combat_scenes.combat_pager.tables.ShockResultTable;

public class ShockResultView extends LinearLayout {

    public ShockResultView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ShockResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ShockResultView(Context context,
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
        inflater.inflate(R.layout.shock_result_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void displayShockResult(ShockResultTable shockResult, WarriorItem warrior, String title){
        LinearLayout hitLayout, isRoutCheckLayout, extraRoutDiceLayout, isRetreatLayout, isRoutLayout;
        TextView hitView, categoryTitle, isRootCheckState, extraRoutDiceValue, isRetreatState, isRoutState;

        //Set the view category title
        categoryTitle = (TextView) findViewById(R.id.categoryTitle);

        //Translate it into used language
        if(title.equals("Attacker")){
            categoryTitle.setText(getResources().getString(R.string.Attacker));
        }else{
            categoryTitle.setText(getResources().getString(R.string.Defender));
        }


        UnitView unitView = (UnitView) findViewById(R.id.unitView);
        unitView.setUnit((WarriorInShock) warrior);

        //Show hit result
        if(shockResult.hits() != 0) {
            hitView = (TextView) findViewById(R.id.hitValue);
            hitView.setText("" + shockResult.hits());
        }
        else{
            //If nothing to display just hide it
            hitLayout = (LinearLayout) findViewById(R.id.hitLayout);
            hitLayout.setVisibility(View.GONE);
        }

        //If check for root is needed
        if(shockResult.routCheck()) {
            if(shockResult.extraPoints() > 0){
                extraRoutDiceValue = (TextView) findViewById(R.id.extraRoutDiceValue);
                extraRoutDiceValue.setText("" + shockResult.extraPoints());
            }
            else{
                //If nothing to display just hide it
                extraRoutDiceLayout = (LinearLayout) findViewById(R.id.extraRoutDiceLayout);
                extraRoutDiceLayout.setVisibility(View.GONE);
            }
        }
        else{
            //If nothing to display just hide it
            isRoutCheckLayout = (LinearLayout) findViewById(R.id.isRoutCheckLayout);
            isRoutCheckLayout.setVisibility(View.GONE);

            extraRoutDiceLayout = (LinearLayout) findViewById(R.id.extraRoutDiceLayout);
            extraRoutDiceLayout.setVisibility(View.GONE);
        }
        //If nothing to display just hide it
        if(!shockResult.retreat()) {
            isRetreatLayout = (LinearLayout) findViewById(R.id.isRetreatLayout);
            isRetreatLayout.setVisibility(View.GONE);
        }

        //If nothing to display just hide it
        if(!shockResult.rout()) {
            isRoutLayout = (LinearLayout) findViewById(R.id.isRoutLayout);
            isRoutLayout.setVisibility(View.GONE);
        }
    }
}