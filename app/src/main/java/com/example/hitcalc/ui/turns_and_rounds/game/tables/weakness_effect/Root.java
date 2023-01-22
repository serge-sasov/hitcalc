package com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect;

import java.util.Random;

public class Root extends CheckForRoot {
    public Root() {
        super();
    }

    @Override
    public String getEffect(Random randomD10, int troopQuality) {
        return mEffect;
    }
}
