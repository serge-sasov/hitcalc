package com.example.hitcalc;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.ui.combat_scenes.army.parser.ArmyParser;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.TurnNestedFragment;
import com.example.hitcalc.utility.ErrorMessageTracker;
import com.example.hitcalc.utility.LoadResourceTables;
import com.example.hitcalc.utility.LoadTable;
import com.google.android.material.navigation.NavigationView;
import com.opencsv.exceptions.CsvException;
import com.rollbar.android.Rollbar;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    //error tracking
    private Rollbar rollbar;

    private AppBarConfiguration mAppBarConfiguration;

    //keep the backup of the configuration in the file
    private String mFileName;
    private GameStorage mGameStorage;
    private HashMap<String, InputStream> mRowResourceTables;
    private Scenario mScenario;

    private ErrorMessageTracker mErrorTracker = new ErrorMessageTracker();

    //catch the back button click and forward it to the fragment the call triggered from
    public interface FragmentOnBackClickInterface {
        void onBackClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle uncaught exception and write into log all configuration data
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                e.printStackTrace();
                try {
                    mGameStorage.save(getBaseContext(), "exception");
                    System.exit(1);

                } catch (IOException | JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        //Initiate error tracking
        rollbar = Rollbar.init(this);
        //rollbar.debug("Start MainActivity onCreate");

        LoadTable scenario;

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragmentRound, R.id.fragmentSettings)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // <------------------------------- Old code starts here --------------------------------->
        LoadResourceTables loadResTables = new LoadResourceTables(getResources());
        mRowResourceTables = loadResTables.getRowResourceTable();

        //Instantiate a new or get from received intent previously saved data
        try {
            //Store the local copy of the current game instance
            if(mGameStorage != null){
                mGameStorage.save(getApplicationContext());
            }

            //Load scenario configuration for both armies into battle map
            if(mScenario == null) {
                Intent intent = getIntent();
                String scenarioTitle = intent.getStringExtra("scenario");

                //Load basic data from the raw csv tables
                scenario = new LoadTable(mRowResourceTables.get(scenarioTitle));
                LoadTable units = new LoadTable(mRowResourceTables.get("units_new"));

                //parse out loaded data
                ArmyParser armyParser = new ArmyParser(scenario, units);
                mScenario = new Scenario(armyParser);

                if(mFileName == null) {
                    //Set up file name
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy_HHmmss");
                    formatter.setTimeZone(TimeZone.getDefault());
                    Date date = new Date();
                    mFileName = scenarioTitle + "_" + formatter.format(date) + ".json";
                }

                //Store global variables in the Game Instance data structure
                mGameStorage = new GameStorage(mScenario, mFileName);

                //set combat tables via common data exchange object
                mGameStorage.setCombatTables(mRowResourceTables);
            }
        } catch (JSONException | IOException | CsvException e) {
            e.printStackTrace();
            //Append log
            //mErrorTracker.appendLog(e.toString());
            e.printStackTrace();
            rollbar.error(e,"MainActivity Exception");
        }

        // <------------------------------- Old code ends here --------------------------------->
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_settings) {
            moveToEditWeakness();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveToEditWeakness(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        int id = navController.getCurrentDestination().getId();

        switch(id) {
            case R.id.fragmentRound:
                navController.navigate(R.id.action_fragmentRound_to_editFragment);
                break;

            case R.id.fragmentSettings:
                navController.navigate(R.id.action_fragmentSettings_to_editFragment);
                break;

            default:
                break;
        }
    }

    /*
    * detect navigation toolbar button click & handle it
    * */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //--------------------
        int id = navController.getCurrentDestination().getId();

        //Looking for pushing on back button in case of user decides to change his choice and select another formation or config on round config page
        if(id == R.id.rootTurnFragment) {
            checkRollback();
            return false;
        }else if(id == R.id.roundCompletionFragment) {
            //forbid unconditionally to return back on the previous page
            return false;
        }
        //----------------------------------------------------------------------

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mGameStorage.save(getBaseContext(), "app-pause");
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkRollback(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        int id = navController.getCurrentDestination().getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Fragment navHostFragment;
                Fragment parentFragment, parentTurnFragment;
                List<Fragment> turnNestedFragments = null;
                // Do nothing but close the dialog
                navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
                parentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0); // parent - TurnFragment
                parentTurnFragment = parentFragment.getChildFragmentManager().getFragments().get(0); //ParentTurnNestedFragment
                turnNestedFragments = parentTurnFragment.getChildFragmentManager().getFragments(); // childes - TurnNestedFragments

                for (Fragment fragment : turnNestedFragments) {
                    //notify each TurnNestedFragment to handle necessary actions to rollback any changes done
                    if (fragment != null && fragment instanceof FragmentOnBackClickInterface) {
                        ((FragmentOnBackClickInterface) fragment).onBackClick();
                    }
                }

                //Navigate to the turn fragment configuration page
                navController.navigate(R.id.action_rootTurnFragment_to_fragmentRound);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public GameStorage gameStorageData(){
        return mGameStorage;
    }

    public void setGameStorage(GameStorage gameStorage){
        mGameStorage = gameStorage;
    }

    public HashMap<String, InputStream> getRowResourceTables(){
        if(mRowResourceTables != null){
            return mRowResourceTables;
        }
        return null;
    }

    public Rollbar getRollbar(){
        if(rollbar != null) {
            return rollbar;
        }
        return null;
    }
}