package com.example.hitcalc.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.utility.items_selector.ItemSelectorSimpleVertical;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        //String mPath;
        ArrayList<String> stringFiles = new ArrayList<String>();
        File directory;
        File[] files;

        //Get game storage from main activity and parse it out
        MainActivity activity = (MainActivity) getActivity();
        GameStorage gameStorage = activity.gameStorageData();
        //get available files
        files = gameStorage.getFiles(activity.getApplicationContext());

        Log.d("Files", "Size: " + files.length);
        for (int i = files.length - 1; i >= 0; i--) {
            //Use it later on as filter for given scenario
            /*
            if(files[i].getName().contains("game_instance_backup")) {

            }
             */
            Log.d("Files", "FileName: " + files[i].getName());
            //reverse the output of the files to show the most recent first
            stringFiles.add(files[i].getName());
        }

        ItemSelectorSimpleVertical filesView = (ItemSelectorSimpleVertical) root.findViewById(R.id.fileSelector);
        //Populate the input values
        filesView.setValues(stringFiles);

        //Handle Back click
        Button buttonReturnBack = (Button) root.findViewById(R.id.buttonBack);
        if (buttonReturnBack != null) {
            buttonReturnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //Navigation.findNavController(v).navigate(R.id.action_settings_to_armyBattleScenesFragment);
                    } catch (Error | Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        //Handle Delete files click
        Button buttonDeleteFiles = (Button) root.findViewById(R.id.buttonDeleteFiles);
        if (buttonDeleteFiles != null) {
            buttonDeleteFiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].getName().equals(gameStorage.getFileName())) {
                            //Remove all old persisted game items except the current one
                            Log.d("Removed Files", "FileName: " + files[i].getName());
                            //reverse the output of the files to show the most recent first
                            files[i].delete();
                        }
                    }

                    try {
                        if (filesView.getSelectedIndex() != -1) {
                            Integer index = (stringFiles.size() - 1) - filesView.getSelectedIndex();
                            gameStorage.load(activity.getApplicationContext(), stringFiles.get(index));
                        }
                        Navigation.findNavController(v).navigate(R.id.action_settings_self);
                    } catch (IOException | JSONException | Error e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        //Handle Back click
        Button loadGameBtn = (Button) root.findViewById(R.id.buttonLoadGame);
        if (loadGameBtn != null) {
            loadGameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //recalculate a file index
                        if (filesView.getSelectedIndex() != -1) {
                            //Integer index = (stringFiles.size() - 1) - filesView.getSelectedIndex();

                            Integer index = filesView.getSelectedIndex();
                            gameStorage.load(activity.getApplicationContext(), stringFiles.get(index));
                        }
                        Navigation.findNavController(v).navigate(R.id.action_fragmentSettings_to_fragmentRound);
                    } catch (IOException | JSONException | Error e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        return root;
    }
}