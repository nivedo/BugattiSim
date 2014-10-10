/*
 * Represents and Active Card on the game board.
 * Contains miscellaneous stats and utility functions
 * for simulation purposes.
 */

package tyrantlib.model;

public class ActiveCard {

    // Underlying TU Card
    private final Card card;

    // Current base stats
    protected int attack;
    protected int health;
    protected int wait;

    // Whether card has already acted
    protected boolean acted = false;

    // Whether card attacked
    protected boolean attacked = false;

    // Whether card successfully dealt damage
    protected boolean dealtDamage = false;

    // Activated skill modifiers
    protected int enfeeble;
    protected int protect;
    protected int rally;
    protected int weaken;
    protected int jam;
    protected int jamCD = 0; // Num turns until jam activates
    protected boolean jammed;
    protected boolean hasJammed = false;
    protected boolean overloaded = false;
    protected boolean overloadTarget = false;
    protected boolean overloadInhibit = false;
    protected int[] enhanceX = new int[SkillType.values().length];

    // Passive skills and modifiers
    protected int evade;
    protected int evadedNum = 0; // Skills evaded this turn
    protected int armored;
    protected int counter;
    protected int corrosive;
    protected int corroded = 0; // Corroded value
    protected int corrodedNum = 0; // Total corrosive penalty
    protected boolean wall;
    protected int flurry;
    protected int flurryCD = 0; // Num turns until flurry activates
    protected boolean hasFlurried = false;

    // Attack skills and modifiers
    protected int pierce;
    protected int valor;
    protected int valorNum = 0;
    protected boolean valorActive = false;
    protected int berserk;
    protected int berserkNum = 0; // Total berserk benefit
    protected int leech;
    protected int poison;
    protected int poisoned = 0; // Poisoned value
    protected int inhibit;
    protected int inhibited = 0; // Inhibited value;
    protected int inhibitedNum = 0; // Num skills inhibited this turn

    public void reset() {
        acted = false;
        attacked = false;
        dealtDamage = false;

        // Activated skill modifiers
        enfeeble = protect = rally = weaken = jam = jamCD = 0;
        jammed = hasJammed = overloaded = overloadTarget = overloadInhibit = false;
        for(int i = 0; i < enhanceX.length; i++) {
            enhanceX[i] = 0;
        }

        // Passive skills and modifiers
        evade = evadedNum = armored = counter = corrosive = corroded = corrodedNum = flurry = flurryCD = 0;
        wall = hasFlurried = false;

        // Attack skills and modifiers
        pierce = valor = valorNum = berserk = berserkNum =
                leech = poison = poisoned = inhibit = inhibited = inhibitedNum = 0;
        valorActive = false;

        attack = card.getAttack();
        health = card.getHealth();
        wait   = card.getWait();

        for(Skill s : card.getSkills()) {
            populateSkill(s);
        }
    }

    // Utility Functions
    public Skill[] getSkills() { return card.getSkills(); }
    public Skill getSkill(int priority) { return card.getSkill(priority); }
    public Faction getType() { return card.getType(); }
    public String getName() { return card.getName(); }
    public boolean isAssault() { return card.isAssault(); }
    public boolean isCommander() { return card.isCommander(); }
    public boolean isStructure() { return card.isStructure(); }

    // SIMULATION FUNCTIONS

    // Effective attack
    public int getEffectiveAttack() {
        int attackBeforeRally = attack + berserkNum + valorNum - corrodedNum - weaken;
        if(attackBeforeRally < 0) attackBeforeRally = 0;
        return (attackBeforeRally + rally);
    }

    public boolean isDead() { return (health <= 0); }

    // Evade - returns false if cannot be evaded
    public boolean evaded() {
        if(evadedNum == evade) { return false; }
        evadedNum++;
        return true;
    }

    // Inhibit - returns true if skill is inhibited
    public boolean inhibited() {
        if(inhibitedNum == inhibited) { return false; }
        inhibitedNum++;
        return true;
    }

    public boolean canAct() { return wait == 0 && health > 0 && !jammed; }
    public boolean isActive() { return wait == 0; }
    public boolean jamActive() { return jam > 0 && jamCD == 0; }
    public boolean flurryActive() { return flurry > 0 && flurryCD == 0; }

    public void removeHealth(int damage) {
        assert health > 0;
        health -= damage;
        if( health < 0 ) health = 0;
    }

    public void addHealth(int heal) {
        assert health > 0;
        health += heal;
        if(health > card.getHealth()) health = card.getHealth();
    }

    public boolean matchesType(Faction type) {
        return (getType() == type || getType() == Faction.PROGENITOR || type == Faction.NOTYPE );
    }

    public boolean canEnfeeble() { return !isDead(); }
    public void doEnfeeble(int x, boolean overload) { if(overload || !evaded()) enfeeble += x; }

    public boolean canHeal() { return !isDead() && (health < card.getHealth()); }
    public void doHeal(int x, boolean overload) { if(overload || !inhibited()) addHealth(x); }

    public boolean canProtect() { return !isDead(); }
    public void doProtect(int x, boolean overload) { if(overload || !inhibited()) protect += x; }

    public boolean canRally() { return canAct() && !acted; }
    public void doRally(int x, boolean overload) { if(overload || !inhibited()) rally += x; }

    public boolean canSiege() { return !isDead(); }
    public void doSiege(int x, boolean overload) { if(overload || !evaded()) { removeHealth(x); } }

    public boolean canStrike() { return !isDead(); }
    public void doStrike(int x, boolean overload) {
        if(overload || !evaded()) {
            int strikeDamage = x + enfeeble - (overload ? 0 : protect);
            if(strikeDamage > 0) { removeHealth(strikeDamage); }
        }
    }

    public boolean canWeaken() { return canAct() && (getEffectiveAttack() > 0); }
    public void doWeaken(int x, boolean overload) { if(overload || !evaded()) weaken += x; }

    // Jam returns TRUE if jam was successful
    public boolean canJam() { return canAct(); }
    public boolean doJam(boolean overload) { return (jammed = (overload || !evaded())); }

    public boolean canEnhance(SkillType skillId) {
        Skill s = card.getSkillByIndex(skillId);
        return (s != null) && (s.isPassiveSkill() || (canAct() && !acted));
    }
    public void doEnhance(int x, SkillType skillId) {
        if(!inhibited()) enhanceX[skillId.ordinal()] += x;
    }

    // Proposed new skill
    public boolean canOverload(boolean hasInhibit) { return canAct() && !acted && !overloaded &&
            (overloadTarget || (overloadInhibit && hasInhibit)); }
    public void doOverload() { if(!inhibited()) overloaded = true; }

    // PHASE CALLBACKS

    // Run after removing dead from field
    public void startPhase() {
        for(Skill s : getSkills()) {
            if(s != null) { enhanceX[s.id.ordinal()] = 0; }
        }
        enfeeble = protect = 0;
        evadedNum = 0;
        overloaded = false;
    }

    public void endPhase() {
        rally = weaken = inhibited = inhibitedNum = 0;
        jammed = false;

        // Set Jam cooldowns
        if(hasJammed) {
            jamCD = jam;
            hasJammed = false;
        }

        // Set Flurry cooldowns
        if(hasFlurried) {
            flurryCD = flurry;
            hasFlurried = false;
        }

        // Take poison damage
        if(poisoned > 0) {
            int poisonDamage = poisoned - protect;
            if(poisonDamage > 0 && health > 0) {
                removeHealth(poisonDamage);
            }
        }

        // Corrosive Penalty
        if(corroded > 0) {
            // Clear corroded
            if(!attacked) {
                corroded = 0;
                corrodedNum = 0;
            } else {
                corrodedNum += corroded;
                int corrodedMax = attack + berserkNum;
                if(corrodedNum > corrodedMax) {
                    corrodedNum = corrodedMax;
                }
            }
        }

        // Reset flags
        attacked = false;
        acted = false;
        dealtDamage = false;

        // Decrement counters - this is early, but card wait is required for valid targeting.
        if(wait > 0) wait--;
        if(jamCD > 0) jamCD--;
        if(flurryCD > 0) flurryCD--;
    }

    /* NOT NEEDED FOR NOW
    public void enemyStartPhase() {
    }

    public void enemyEndPhase() {

    }
    */

    // Attacks an enemy unit (assault > wall > commander)
    public void attack(ActiveCard targetCard) {
        assert health > 0 && isAssault() && canAct();
        assert (!isStructure() || wall);

        int effectiveAttack = getEffectiveAttack();

        // Apply Valor on first activation
        if(!valorActive && targetCard.isAssault() && effectiveAttack < targetCard.getEffectiveAttack()) {
            valorNum += valor;
            effectiveAttack += valor;
        }
        valorActive = true;

        if(effectiveAttack > 0) {
            attacked = true;

            // Add enfeebled to damage because pierce != enfeeble
            int totalDamage = effectiveAttack;
            int totalProtect = targetCard.protect + targetCard.armored - pierce;
            if (totalProtect < 0) totalProtect = 0;

            totalDamage = totalDamage - totalProtect + targetCard.enfeeble;

            if (totalDamage > 0 && targetCard.health > 0) {
                targetCard.removeHealth(totalDamage);
                dealtDamage = true;

                // Apply counter damage first
                if (targetCard.counter > 0) {
                    int counterDamage = targetCard.counter - protect;
                    if(counterDamage > 0 && health > 0) {
                        removeHealth(counterDamage);
                    }
                }

                // Apply offensive poison, inhibit, leech (assaults only)
                if(targetCard.isAssault()) {
                    if (poison > targetCard.poisoned) targetCard.poisoned = poison;
                    if (inhibit > 0) targetCard.inhibited = inhibit;
                    if (leech > 0 && health > 0) addHealth(totalDamage < leech ? totalDamage : leech);
                }

                if(health > 0) {
                    // Apply berserk
                    if (berserk > 0) berserkNum += berserk;
                    // Apply defensive corrosive
                    if (targetCard.corrosive > corroded) corroded = targetCard.corrosive;
                }
            }
        }
    }

    public ActiveCard( Card card ) {
        this.card = card;
        reset();
    }

    public void populateSkill(Skill s) {
        if(s != null) {
            int enhanceVal = enhanceX[s.id.ordinal()];
            if (s.id == SkillType.EVADE) {
                evade = s.x + enhanceVal;
            } else if (s.id == SkillType.ARMORED) {
                armored = s.x + enhanceVal;
            } else if (s.id == SkillType.COUNTER) {
                counter = s.x + enhanceVal;
            } else if (s.id == SkillType.CORROSIVE) {
                corrosive = s.x + enhanceVal;
            } else if (s.id == SkillType.WALL) {
                wall = true;
            } else if (s.id == SkillType.FLURRY) {
                flurry = s.c - enhanceVal;
            } else if (s.id == SkillType.PIERCE) {
                pierce = s.x + enhanceVal;
            } else if (s.id == SkillType.VALOR) {
                valor = s.x + enhanceVal;
            } else if (s.id == SkillType.BERSERK) {
                berserk = s.x + enhanceVal;
            } else if (s.id == SkillType.LEECH) {
                leech = s.x + enhanceVal;
            } else if (s.id == SkillType.POISON) {
                poison = s.x + enhanceVal;
            } else if (s.id == SkillType.INHIBIT) {
                inhibit = s.x + enhanceVal;
            } else if (s.id == SkillType.JAM) {
                jam = s.c - enhanceVal;
                overloadTarget = true;
            } else if (s.id == SkillType.SIEGE || s.id == SkillType.ENFEEBLE ||
                    s.id == SkillType.STRIKE || s.id == SkillType.WEAKEN) {
                overloadTarget = true;
            } else if (s.id == SkillType.HEAL || s.id == SkillType.PROTECT || s.id == SkillType.RALLY) {
                overloadInhibit = true;
            }
        }
    }


    public String toFullString() {
        return getName() + "[" + attack + "," + health + "," + wait + "]\n" + card.toString();
    }

    @Override
    public String toString() {
        return getName() + "[" + getEffectiveAttack() + "," + health + "," + wait + "]";
    }
}
