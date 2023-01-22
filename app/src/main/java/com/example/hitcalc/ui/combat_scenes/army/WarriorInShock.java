package com.example.hitcalc.ui.combat_scenes.army;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class WarriorInShock extends WarriorItem {
    private WarriorItem mLeader; //If leader is placed on the unit
    private WarriorItem mLeaderInfluance; //If heroic leader broads its influence on the unit
    private WarriorItem mStokedWarrior; //If any warrior is stoked with the unit on the same hex

    private static final String SHOCK_LEADER = "shock_leader";
    private static final String SHOCK_LEADER_INFLUANCE = "shock_leader_influance";
    private static final String STOKED_WARRIOR = "stocked_warrior";
    /*
    for case when the unit is located in rear or flank but is lying
    in ZOC another anamy units, so it has to be treated as "front" unit
     */
    private boolean isSurrounded;

    //Constructor to clone the WarriorItem into WarriorInShock
    public WarriorInShock(WarriorItem warrior) {
        super(warrior.title(),
                warrior.type(),
                warrior.troopQuality(),
                warrior.size(),
                warrior.movement(),
                warrior.property(),
                warrior.addProperty(),
                warrior.civilization(),
                warrior.twoHexSize(),
                warrior.color(),
                warrior.legioId());
    }

    //Constructor to clone the WarriorInShock into WarriorInShock
    public WarriorInShock(WarriorInShock warrior) {
        super();
        mTitle = warrior.title();
        mType= warrior.type();
        //to avoid any influence from the dummy objects need to consider only original config TQ & Size value
        mTroopQuality = warrior.getNativeTroopQuality();
        mSize = warrior.getOriginalSize();
        mMovement = warrior.movement();
        mProperty = warrior.property();
        mAddProperty = warrior.addProperty();
        mCivilization = warrior.civilization();
        mIsTwoHex = warrior.twoHexSize();
        mColor = warrior.color();

        if(warrior.leader() != null) {
            mLeader = new WarriorItem(warrior.leader());
        }

        if(warrior.harizma() != null) {
            mLeaderInfluance = new WarriorItem(warrior.harizma());
        }

        if(warrior.stokedWarrior() != null) {
            mStokedWarrior = new WarriorItem(warrior.stokedWarrior());
        }
    }

    public WarriorInShock(JSONObject jo) throws JSONException {
        super(jo);
        if(jo.has(SHOCK_LEADER)) {
            mLeader = new WarriorItem(jo.getJSONObject(SHOCK_LEADER));
        }
        if(jo.has(SHOCK_LEADER_INFLUANCE)) {
            mLeaderInfluance = new WarriorItem(jo.getJSONObject(SHOCK_LEADER_INFLUANCE));
        }

        if(jo.has(STOKED_WARRIOR)) {
            mStokedWarrior = new WarriorItem(jo.getJSONObject(STOKED_WARRIOR));
        }
    }

    /*          Converter to JSON object         */
    public JSONObject convertToJSON() throws JSONException {
        //Need to call all previous methods in consequentially
        JSONObject jo = new JSONObject();
        jo = super.convertToJSON();

        if(mLeader != null) {
            jo.put(SHOCK_LEADER, mLeader.convertToJSON());
        }

        if(mLeaderInfluance != null) {
            jo.put(SHOCK_LEADER_INFLUANCE, mLeaderInfluance.convertToJSON());
        }

        if(mStokedWarrior != null) {
            jo.put(STOKED_WARRIOR, mStokedWarrior.convertToJSON());
        }

        return jo;
    }

    //Getter/Setter methods
    public void leader(WarriorItem leader){
        mLeader = null;

        if(leader != null) {
            mLeader = leader;
        }
    }

    public void harizma(WarriorItem harizma){
        mLeaderInfluance = null;

        if(harizma != null) {
            mLeaderInfluance = harizma;
        }
    }

    public WarriorItem leader() {
        return mLeader;
    }

    public WarriorItem harizma() {
        return mLeaderInfluance;
    }

    /*
    * Stock couple of warriors
    * */
    public void stokedWarrior(WarriorItem warrior){
        mStokedWarrior = null;

        if(warrior != null) {
            mStokedWarrior = warrior;
            Log.d("Stocked Warrior: ", mStokedWarrior.mTitle);
        }
    }

    /*
     * Get a stocked warrior
     * */
    public WarriorItem stokedWarrior(){
        return mStokedWarrior;
    }

    //overwrite the size determination methode to get value counting the stoked warriors too.
    public Integer size() {
        Integer size;
        if(mStokedWarrior != null){
            size = mSize + mStokedWarrior.size();
        }
        else{
            size = mSize;
        }
        return size;
    }

    public Integer getOriginalSize() {
        return mSize;
    }

    /*
    * Recalculate Troop Quality if there is a staked dummy unit
    * */
    public Integer troopQuality(){
        Integer quality;
        if(mStokedWarrior != null && mStokedWarrior.type().equals("Dummy")){
            quality = mTroopQuality + mStokedWarrior.troopQuality();
        }
        else{
            quality = mTroopQuality;
        }
        return quality;
    }

    //gives as result the original TQ value assigned by the input config files
    public Integer getNativeTroopQuality(){
        return mTroopQuality;
    }
}
