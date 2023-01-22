package com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect;


import java.util.Random;

public abstract class AbstractWeaknessEffect {
    protected Integer mExtraValue = 0;
    protected String mEffect;

    public AbstractWeaknessEffect(Integer value){
        mExtraValue = value;
    }

    public AbstractWeaknessEffect(){
    }

    //randomD10 - reference to the common random object
    public String getEffect(Random randomD10, int troopQuality) {
        int diceValue = randomD10.nextInt(10);
        if(troopQuality < diceValue + mExtraValue){
            return mEffect;
        }

        return null;
    }

    public Integer getExtraValue() {
        return mExtraValue;
    }
}
