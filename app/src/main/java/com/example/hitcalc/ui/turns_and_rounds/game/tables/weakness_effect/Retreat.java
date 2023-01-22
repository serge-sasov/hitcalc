package com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect;

import java.util.Random;

public class Retreat extends CheckForRetreat {
    public Retreat() {
        super();
    }

    @Override
    public String getEffect(Random randomD10, int troopQuality) {
        return mEffect;
    }
}
