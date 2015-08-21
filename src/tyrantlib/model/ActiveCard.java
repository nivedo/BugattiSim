/*
 * Represents and Active Card on the game board.
 * Contains miscellaneous stats and utility functions
 * for simulation purposes.
 */

package tyrantlib.model;

public class ActiveCard {

    // Underlying TU Card
    private final Card card;

    private final ActiveDeck deck;

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

    // Current card index in ActiveDeck
    protected int index = -1;

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
    protected boolean[] evolved = new boolean[SkillType.values().length];

    // Passive skills and modifiers
    protected int evade;
    protected int evadedNum = 0; // Skills evaded this turn
    protected int payback;
    protected int paybackNum = 0;
    protected int armored;
    protected int swipe;
    protected int fortify = 0;
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
    protected int legion = 0;
    protected int legionNum = 0;
    protected boolean bloodlust = false;
    protected int bloodlustNum = 0;
    protected int berserk;
    protected int berserkNum = 0; // Total berserk benefit
    protected int avenge;
    protected int avengeNum = 0; // Total avenge benefit
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
        for(int i = 0; i < evolved.length; i++) {
            evolved[i] = false;
        }

        // Passive skills and modifiers
        evade = evadedNum = payback = paybackNum = armored = fortify = counter = corrosive = corroded = corrodedNum = flurry = flurryCD = 0;
        wall = hasFlurried = false;

        // Attack skills and modifiers
        pierce = valor = valorNum = legion = legionNum = bloodlustNum = berserk = berserkNum = avenge = avengeNum =
                leech = poison = poisoned = inhibit = inhibited = inhibitedNum = 0;
        valorActive = false;
        bloodlust = false;

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

    public int getArmor() {
        if(armored > fortify) return armored;
        return fortify;
    }

    // SIMULATION FUNCTIONS

    // Effective attack
    public int getEffectiveAttack() {
        int attackBeforeRally = attack + berserkNum + avengeNum +  valorNum - corrodedNum - weaken;
        if(attackBeforeRally < 0) attackBeforeRally = 0;
        return (attackBeforeRally + rally);
    }

    public int getWeaken() {
        int currentAttack = attack + berserkNum + avengeNum +  valorNum - corrodedNum;
        if (currentAttack < weaken) {
            return currentAttack;
        }
        return weaken;
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
        if( health <= 0 ) {
            health = 0;
            onDeath();
        }
    }

    public void setIndex(int i) { index = i; }

    public void onDeath() {
        // Special BGE callback
        deck.onUnitDeath(index);
    }

    public void addHealth(int heal) {
        assert health > 0;
        health += heal;
        if(health > (card.getHealth() + avengeNum)) health = card.getHealth() + avengeNum;
    }

    public boolean matchesType(Faction type) {
        return (getType() == type || getType() == Faction.PROGENITOR || type == Faction.NOTYPE || deck.isMetamorphosis());
    }

    public boolean canEnfeeble() { return !isDead(); }
    public void doEnfeeble(int x, boolean overload, ActiveCard assault, boolean isPayback) {
        if(overload || !evaded()) {
            enfeeble += x;
            if(assault != null && !isPayback && paybackNum++ < payback) {
                assault.doEnfeeble(x, true, this, true);
            }
        }
    }

    public boolean canHeal() { return !isDead() && (health < (card.getHealth() + avengeNum)); }
    public void doHeal(int x, boolean overload) { if(overload || !inhibited()) addHealth(x); }

    public boolean canProtect() { return !isDead(); }
    public void doProtect(int x, boolean overload) { if(overload || !inhibited()) protect += x; }

    public boolean canRally() { return canAct() && !acted; }
    public void doRally(int x, boolean overload) { if(overload || !inhibited()) rally += x; }

    public boolean canSiege() { return !isDead(); }
    public void doSiege(int x, boolean overload) { if(overload || !evaded()) { removeHealth(x); } }

    public boolean canStrike() { return !isDead(); }
    public void doStrike(int x, boolean overload, ActiveCard assault, boolean isPayback) {
        if(overload || !evaded()) {
            int strikeDamage = x + enfeeble - ((overload && assault != null) ? 0 : protect);
            if(strikeDamage > 0) { removeHealth(strikeDamage); }
            if(assault != null && !isPayback && paybackNum++ < payback) {
                assault.doStrike(x, true, this, true);
            }
        }
    }

    public void doSwipe(int x) {
        int swipeDamage = x + enfeeble - protect;
        if(swipeDamage > 0) { removeHealth(swipeDamage); }
    }

    public boolean canWeaken() { return canAct() && (getEffectiveAttack() > 0); }
    public void doWeaken(int x, boolean overload, ActiveCard assault, boolean isPayback) {
        if(overload || !evaded()) {
            weaken += x;
            if(assault.deck.isTurningTide()) {
                assault.deck.onWeaken(getWeaken());
            }
            if(assault != null && !isPayback && paybackNum++ < payback) {
                assault.doWeaken(x, true, this, true);
            }
        }
    }

    // Jam returns TRUE if jam was successful
    public boolean canJam() { return canAct(); }
    public boolean doJam(boolean overload, ActiveCard assault, boolean isPayback) {
        jammed = (overload || !evaded());
        if(assault != null && !isPayback && jammed && paybackNum++ < payback) {
            assault.doJam(true, this, true);
        }
        return jammed;
    }

    public boolean canEnhance(SkillType skillId) {
        Skill s = card.getSkillByIndex(skillId);
        return (s != null) && (s.isPassiveSkill() || (canAct() && !acted));
    }
    public void doEnhance(int x, SkillType skillId) {
        if(!inhibited()) enhanceX[skillId.ordinal()] += x;
    }

    public boolean canEvolve(SkillType skillId) {
        Skill s = card.getSkillByIndex(skillId);
        return (s != null) && !evolved[skillId.ordinal()];
    }
    public void doEvolve(SkillType skillId) {
        if(!inhibited()) evolved[skillId.ordinal()] = true;
    }

    // Proposed new skill
    public boolean canOverload(boolean hasInhibit) { return canAct() && !acted && !overloaded &&
            (overloadTarget || jamActive() || (overloadInhibit && hasInhibit)); }
    public void doOverload() { if(!inhibited()) overloaded = true; }

    public void doAvenge() {
        if(avenge > 0) {
            avengeNum += avenge;
            addHealth(avengeNum);
        }
    }

    // PHASE CALLBACKS

    // Run after removing dead from field
    public void startPhase() {
        for(Skill s : getSkills()) {
            if(s != null) {
                enhanceX[s.id.ordinal()] = 0;
                evolved[s.id.ordinal()] = false;
            }
        }
        enfeeble = protect = 0;
        evadedNum = 0;
        paybackNum = 0;
        overloaded = false;
    }

    public void endPhase() {
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

        // Heal refresh
        if (leech > 0 && health > 0 && evolved[SkillType.LEECH.ordinal()]) {
            addHealth(leech);
        }

        // Take poison damage
        if(poisoned > 0) {
            int poisonDamage = poisoned - protect + enfeeble;
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
                int corrodedMax = attack + berserkNum + avengeNum;
                if(corrodedNum > corrodedMax) {
                    corrodedNum = corrodedMax;
                }
            }
        }

        // Reset vars
        rally = weaken = inhibited = inhibitedNum = legionNum = bloodlustNum = 0;
        jammed = false;
        bloodlust = false;
        fortify = 0;

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

    public void checkValor(ActiveCard opposingCard) {
        // Apply Valor on first activation
        if(opposingCard != null && !valorActive && opposingCard.isAssault() && getEffectiveAttack() < opposingCard.getEffectiveAttack()) {
            valorNum += valor;
        }
        valorActive = true;
    }

    public void applyLegion() { legionNum += legion; }
    public void applyBloodlust(int n) { bloodlustNum += n; }

    // Attacks an enemy unit (assault > wall > commander)
    public void attack(ActiveCard targetCard) {
        assert health > 0 && isAssault() && canAct();
        assert (!isStructure() || wall);

        int effectiveAttack = getEffectiveAttack();

        if(effectiveAttack > 0) {
            attacked = true;

            // Add enfeebled to damage because pierce != enfeeble
            int totalDamage = effectiveAttack;
            int totalProtect = targetCard.protect + targetCard.getArmor() - pierce;
            if (totalProtect < 0) totalProtect = 0;

            totalDamage = totalDamage - totalProtect + targetCard.enfeeble + legionNum + bloodlustNum;

            // Apply Venom if Poison evolved, target is poisoned
            if(targetCard.poisoned > 0 && poison > 0 && evolved[SkillType.POISON.ordinal()]) {
                totalDamage += poison;
            }

            if (totalDamage > 0 && targetCard.health > 0) {
                targetCard.removeHealth(totalDamage);
                dealtDamage = true;

                deck.onAttackDamage(this);

                // Apply counter damage first
                if (targetCard.counter > 0) {
                    // Trigger counterflux, round up
                    if(targetCard.health > 0 && targetCard.isAssault() && deck.isCounterflux()) {
                        int fluxBonus = (targetCard.counter + 3) / 4;
                        if(fluxBonus > 0) {
                            targetCard.berserkNum += fluxBonus;
                            targetCard.addHealth(fluxBonus);
                        }
                    }
                    int counterDamage = targetCard.counter - protect + enfeeble;
                    if(counterDamage > 0 && health > 0) {
                        removeHealth(counterDamage);
                    }
                }

                // Apply offensive poison, inhibit, leech (assaults only)
                if(targetCard.isAssault()) {
                    if (poison > targetCard.poisoned) targetCard.poisoned = poison;
                    if (inhibit > 0) targetCard.inhibited = inhibit;
                    if (leech > 0 && health > 0 && !evolved[SkillType.LEECH.ordinal()]) addHealth(totalDamage < leech ? totalDamage : leech);
                }

                if(health > 0) {
                    // Apply berserk
                    if (berserk > 0) {
                        berserkNum += berserk;
                        if(deck.isEnduringRage()) {
                            int rageBonus = (berserk + 1) / 2;
                            doProtect(rageBonus, true);
                            doHeal(rageBonus, true);
                        }
                    }
                    // Apply defensive corrosive
                    if (targetCard.corrosive > corroded) corroded = targetCard.corrosive;
                }
            }
        }
    }

    public ActiveCard( Card card, ActiveDeck deck ) {
        this.card = card;
        this.deck = deck;
        reset();
    }

    public void populateSkill(Skill s) {
        if(s != null) {
            int enhanceVal = enhanceX[s.id.ordinal()];
            if (s.id == SkillType.EVADE) {
                evade = s.x + enhanceVal;
            } else if (s.id == SkillType.PAYBACK) {
                payback = s.x + enhanceVal;
            } else if (s.id == SkillType.ARMORED) {
                armored = s.x + enhanceVal;
            } else if (s.id == SkillType.SWIPE) {
                swipe = s.x + enhanceVal;
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
            } else if (s.id == SkillType.LEGION) {
                legion = s.x + enhanceVal;
            } else if (s.id == SkillType.BERSERK) {
                berserk = s.x + enhanceVal;
            } else if (s.id == SkillType.AVENGE) {
                avenge = s.x + enhanceVal;
            } else if (s.id == SkillType.LEECH) {
                leech = s.x + enhanceVal;
            } else if (s.id == SkillType.REFRESH) {
                leech = s.x + enhanceVal;
                evolved[SkillType.LEECH.ordinal()] = true;
            } else if (s.id == SkillType.POISON) {
                poison = s.x + enhanceVal;
            } else if (s.id == SkillType.INHIBIT) {
                inhibit = s.x + enhanceVal;
            } else if (s.id == SkillType.JAM) {
                jam = s.c - enhanceVal;
            } else if (s.id == SkillType.BESIEGE) {
                overloadTarget = true;
                evolved[SkillType.SIEGE.ordinal()] = true;
            } else if (s.id == SkillType.SIEGE || s.id == SkillType.ENFEEBLE ||
                    s.id == SkillType.STRIKE || s.id == SkillType.WEAKEN) {
                overloadTarget = true;
            } else if (s.id == SkillType.HEAL || s.id == SkillType.MEND || s.id == SkillType.PROTECT || s.id == SkillType.RALLY) {
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
