package com.example.hitcalc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.ui.combat_scenes.army.parser.ArmyParser;
import com.example.hitcalc.utility.LoadResourceTables;
import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.utility.items_selector.ItemSelectorSimpleVertical;
import com.google.android.material.navigation.NavigationView;
import com.opencsv.exceptions.CsvException;
import com.rollbar.android.Rollbar;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class SelectGameActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SelectGameActivity activity = this;

        setContentView(R.layout.select_game_activity);

        String scenarioTitle = "battle_gaugamela";

        ArrayList<String> scenarioList = new ArrayList<String>();
        scenarioList.add("battle_issue");
        scenarioList.add("battle_granicus");
        scenarioList.add("battle_raphia");
        scenarioList.add("battle_great_plains");
        scenarioList.add("battle_gaugamela");
        scenarioList.add("battle_hidaspes");
        scenarioList.add("battle_paraetacene");
        scenarioList.add("battle_zama");


        ItemSelectorSimpleVertical gamesView = (ItemSelectorSimpleVertical) findViewById(R.id.gameSelector);
        gamesView.setValues(scenarioList);
        gamesView.setSelectedValue(scenarioTitle);


        //Handle Back click
        Button startGameBtn = (Button) findViewById(R.id.startGame);
        startGameBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Navigate to the MainActivity
                if (gamesView.getSelectedIndex() != -1) {
                    //Integer index = (stringFiles.size() - 1) - filesView.getSelectedIndex();

                    Integer index = gamesView.getSelectedIndex();
                    //index of the selected game
                    //scenarioList.get(index);

                    //trigger the main activity intent
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.putExtra("scenario", scenarioList.get(index));

                    startActivity(intent);
                }
            }
        });
    }
}
