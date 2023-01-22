package com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect;

public class CheckForRetreat extends AbstractWeaknessEffect{
    public CheckForRetreat(Integer value) {
        super(value);
        mEffect = "Retreat";
    }

    public CheckForRetreat() {
        super();
        mEffect = "Retreat";
    }
}
