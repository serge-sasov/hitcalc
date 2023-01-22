package com.example.hitcalc.ui.turns_and_rounds.army_in_combat;

import com.example.hitcalc.ui.combat_scenes.army.Warrior;

import java.util.ArrayList;
import java.util.HashMap;

//Hold a list of affected warriors at the end of each round
public class AffectedFormationWarriors {
    private HashMap<String, ArrayList<WarriorInCombat>> mEffectToWarriors;
    private HashMap<String, AffectedFormationWarriors> mSubFormations;

    //add a warrior arranged to the effect
    public void addWarrior(String effect, WarriorInCombat warrior){
        ArrayList<WarriorInCombat> warriors;

        if(mEffectToWarriors == null){
            mEffectToWarriors = new HashMap<String, ArrayList<WarriorInCombat>>();
            warriors = new ArrayList<WarriorInCombat>();
        }else{
            warriors = mEffectToWarriors.get(effect);
            if(warriors == null){
                warriors = new ArrayList<WarriorInCombat>();
            }
        }
        warriors.add(warrior);
        mEffectToWarriors.put(effect, warriors);
    }

    //put a warrior into sub-formation
    public void addSubFormationWarrior(String title, String effect, WarriorInCombat warrior){
        if(mSubFormations == null){
            mSubFormations = new HashMap<String, AffectedFormationWarriors>();
        }

        AffectedFormationWarriors subFormation = mSubFormations.get(title);
        if(subFormation == null){
            subFormation = new AffectedFormationWarriors();
        }

        subFormation.addWarrior(effect, warrior);

        mSubFormations.put(title, subFormation);
    }

    public HashMap<String, ArrayList<WarriorInCombat>> getEffectToWarriors() {
        return mEffectToWarriors;
    }

    public HashMap<String, AffectedFormationWarriors> getSubFormations() {
        return mSubFormations;
    }
}
