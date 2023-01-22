package com.example.hitcalc.utility;

import android.content.res.Resources;

import com.example.hitcalc.R;

import java.io.InputStream;
import java.util.HashMap;

public class LoadResourceTables {
    HashMap<String,InputStream> mRowResourceTables; //set of basic tables

    public LoadResourceTables(Resources resources){
        mRowResourceTables = new HashMap<String, InputStream>();

        //Load tables into hash map array
        mRowResourceTables.put("attacker_shock_results",resources.openRawResource(R.raw.attacker_shock_results));
        mRowResourceTables.put("defender_shock_results",resources.openRawResource(R.raw.defender_shock_results));
        mRowResourceTables.put("front_attacks",resources.openRawResource(R.raw.front_attacks));
        mRowResourceTables.put("flank_rear_attacks",resources.openRawResource(R.raw.flank_rear_attacks));

        //Weakness & Activations
        mRowResourceTables.put("unit_actions",resources.openRawResource(R.raw.unit_actions));
        mRowResourceTables.put("unit_weakness",resources.openRawResource(R.raw.unit_weakness));

        //Load units
        mRowResourceTables.put("units_new",resources.openRawResource(R.raw.units_new));

        //Load Scenarios
        mRowResourceTables.put("battle_hidaspes",resources.openRawResource(R.raw.battle_hidaspes));
        mRowResourceTables.put("battle_gaugamela",resources.openRawResource(R.raw.battle_gaugamela));
        mRowResourceTables.put("battle_great_plains",resources.openRawResource(R.raw.battle_great_plains));
        mRowResourceTables.put("battle_issue",resources.openRawResource(R.raw.battle_issue));
        mRowResourceTables.put("battle_granicus",resources.openRawResource(R.raw.battle_granicus));
        mRowResourceTables.put("battle_raphia",resources.openRawResource(R.raw.battle_raphia));
        mRowResourceTables.put("battle_paraetacene",resources.openRawResource(R.raw.battle_paraetacene));
        mRowResourceTables.put("battle_zama",resources.openRawResource(R.raw.battle_zama_spqr));
    }

    /*
     * Load a config files into tables and save it to global variable
     * */
    public HashMap<String,InputStream> getRowResourceTable(){
        return mRowResourceTables;
    }
}
