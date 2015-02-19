package tyrantlib.model;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

public class ActiveDeck {
    // Underlying TU Card
    private final Deck deck;
    private ActiveCard[] activeCards = new ActiveCard[10];
    private boolean priorityPlayed[];
    private static Logger logger = Logger.getLogger("ActiveDeck");
    private static boolean DEBUG_MODE = false;

    private ArrayList<Integer> hand = new ArrayList<Integer>();
    private int[] drawOrder;

    private int numTurns = 0;
    private int numDrawn = 0;
    private int unitsKilled = 0;
    private PlayMode mode = PlayMode.AUTO;
    private static Random rand = new Random(System.currentTimeMillis());

    public enum PlayMode {
        PRIORITY, AUTO, MANUAL;
    }

    // Main Deck Components
    private ActiveCard commander;
    private ArrayList<ActiveCard> assaults = new ArrayList<ActiveCard>();
    private ArrayList<ActiveCard> structures = new ArrayList<ActiveCard>();

    // TODO: Optional Deck Components (for Guild War)
    private ActiveCard[] fortress = new ActiveCard[2];

    public void setFortress(Card fort0, Card fort1) {
        if(fort0 != null) fortress[0] = new ActiveCard(fort0, this);
        if(fort1 != null) fortress[1] = new ActiveCard(fort1, this);

        reset();
    }

    public Deck getDeck() { return deck; }

    private Skill bgEffect;
    private boolean progEffect;
    private boolean reapEffect;
    private Skill reapHeal;
    private Skill reapRally;
    private boolean bloodlustEffect;
    private int bloodlustN;
    private boolean metaEffect;

    public ActiveDeck( Deck deck ) {
        this.deck = deck;
        assert deck.getNumCards() > 0;

        for(int i = 0; i < deck.getNumCards(); i++) {
            activeCards[i] = new ActiveCard(deck.getCards()[i], this);
        }

        drawOrder = new int[deck.getNumCards()];
        this.priorityPlayed = new boolean[deck.getNumCards()];

        // Set commander right away
        commander = new ActiveCard( deck.getCommander(), this );

        seedDraws();
    }

    public int getUnitsKilled() { return unitsKilled; }

    public void setBGEffect(BGOptions options) {
        bgEffect = new Skill();
        progEffect = false;
        reapEffect = false;
        bloodlustEffect = false;
        metaEffect = false;

        if(options.bgEffect==SkillType.PROGENITOR) {
            progEffect = true;
        }
        else if(options.bgEffect==SkillType.REAPING) {
            reapEffect = true;
            if(reapHeal == null) {
                reapHeal = new Skill();
                reapHeal.setId(SkillType.HEAL);
                reapHeal.all = true;
                reapHeal.x = options.bgX;
            }
            if(reapRally == null) {
                reapRally = new Skill();
                reapRally.setId(SkillType.RALLY);
                reapRally.all = true;
                reapRally.x = options.bgX;
            }
        }
        else if(options.bgEffect==SkillType.BLOODLUST) {
            bloodlustEffect = true;
            bloodlustN = options.bgX;
        }
        else if(options.bgEffect==SkillType.METAMORPHOSIS) {
            metaEffect = true;
        }
        else if (!options.isEnhance) {
            bgEffect.setId(options.bgEffect);
            if(options.bgEffect==SkillType.OVERLOAD) {
                bgEffect.n = options.bgX;
            } else {
                bgEffect.all = true;
            }
        }
        else {
            bgEffect.setId(SkillType.ENHANCE);
            bgEffect.s = options.bgEffect;
            bgEffect.all = true;
        }
        bgEffect.x = options.bgX;
    }

    public void seedDraws() {
        // Create random draw order
        List<Integer> seedList = new ArrayList<Integer>();
        for (int i = 0; i < deck.getNumCards(); i++) {
            seedList.add(i);
        }
        Collections.shuffle(seedList);
        for (int i = 0; i < deck.getNumCards(); i++) {
            drawOrder[i] = seedList.get(i);
        }
    }

    public void reset() {
        numTurns = 0;
        numDrawn = 0;
        unitsKilled = 0;
        commander.reset();

        Arrays.fill(priorityPlayed, false);

        hand.clear();
        assaults.clear();
        structures.clear();

        if(fortress[0] != null) {
            fortress[0].reset();
            structures.add(fortress[0]);
        }
        if(fortress[1] != null) {
            fortress[1].reset();
            structures.add(fortress[1]);
        }

        // HACK: decrement fortress structures initially since we decrement wait at the end.
        endPhase();

        seedDraws();
    }

    public PlayMode getMode() {
        return mode;
    }

    public void setMode(PlayMode mode) {
        this.mode = mode;
    }

    public boolean isDead() { return commander.isDead(); }
    
    public boolean isEventDeck() { return deck.isEventDeck(); }

    public boolean hasInhibitedUnit() {
        for(ActiveCard card : assaults) {
            if(card.inhibited > 0 && card.health > 0) return true;
        }
        return false;
    }

    public boolean isMetamorphosis() {
        return metaEffect;
    }

    public void onUnitDeath() {
        // Special BGE
        if(reapEffect) {
            applySkill(null, reapHeal, this);
            applySkill(null, reapRally, this);
        }
    }

    public void onAttackDamage(ActiveCard attackCard) {
        // Special BGE
        if(bloodlustEffect && !attackCard.bloodlust) {
            for(ActiveCard card : assaults) {
                if (card.canAct()) {
                    card.applyBloodlust(bloodlustN);
                }
            }
            attackCard.bloodlust = true;
        }
    }

    // Targeting Functions - Single variable for all targets
    // as each simulation runs it's own thread.

    private int numTargets;
    private int randomIndex = 0;
    private ActiveCard[] skillTargets = new ActiveCard[20];

    public int getNumTargets() { return numTargets; }
    public ActiveCard[] getTargets() { return skillTargets; }
    public ActiveCard getRandomTarget() {
        randomIndex = rand.nextInt(numTargets);
        return skillTargets[randomIndex];
    }

    public void setWallTargets() {
        numTargets = 0;
        for(ActiveCard card : structures) {
            if(card.wall) { skillTargets[numTargets++] = card; break; }
        }
    }

    public void setSiegeTargets() {
        numTargets = 0;
        for(ActiveCard card : structures) {
            if(card.canSiege()) { skillTargets[numTargets++] = card; }
        }
    }

    public void setEnfeebleTargets() {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canEnfeeble()) { skillTargets[numTargets++] = card; }
        }
    }

    public void setHealTargets(Faction type) {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canHeal() && card.matchesType(type)) { skillTargets[numTargets++] = card; }
        }
    }

    public void setProtectTargets(Faction type) {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canProtect() && card.matchesType(type)) { skillTargets[numTargets++] = card; }
        }
    }

    public void setRallyTargets(Faction type) {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canRally() && card.matchesType(type)) { skillTargets[numTargets++] = card; }
        }
    }

    public void setStrikeTargets() {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canStrike()) { skillTargets[numTargets++] = card; }
        }
    }

    public void setWeakenTargets() {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canWeaken()) { skillTargets[numTargets++] = card; }
        }
    }

    public void setJamTargets() {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canJam()) { skillTargets[numTargets++] = card; }
        }
    }

    public void setEnhanceTargets(SkillType skillId) {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canEnhance(skillId)) { skillTargets[numTargets++] = card; }
        }
    }

    public void setOverloadTargets(Faction type) {
        numTargets = 0;
        for(ActiveCard card : assaults) {
            if(card.canOverload(hasInhibitedUnit()) && card.matchesType(type)) { skillTargets[numTargets++] = card; }
        }
    }

    // PHASE CALLBACKS
    public void doTurn(ActiveDeck enemyDeck) {
        numTurns++;

        cleanup();
        drawPhase();
        playPhase();
        startPhase();
        precombatPhase(enemyDeck);
        commanderPhase(enemyDeck);
        structurePhase(enemyDeck);
        assaultPhase(enemyDeck);
        endPhase();
        cleanup();

        if(DEBUG_MODE) logger.info("[ACTIVE BOARD] " + this);
        if(DEBUG_MODE) logger.info("[ENEMY BOARD] " + enemyDeck);
    }

    // Draw to 3 cards
    public void drawPhase() {
        if (mode != PlayMode.AUTO) {
            while (hand.size() < 3 && numDrawn < deck.getNumCards()) {
                hand.add(drawOrder[numDrawn++]);
            }
        }
    }

    // Creates an ActiveCard on the board
    public void playCard(int index) {
        ActiveCard activeCard = activeCards[index];
        activeCard.reset();

        if(activeCard.isAssault()) { assaults.add(activeCard); }
        if(activeCard.isStructure()) { structures.add(activeCard); }
    }

    public void playPhase() {
        if (mode == PlayMode.AUTO) {
            if(numDrawn < deck.getNumCards()) playCard(drawOrder[numDrawn++]);
        }
        else if (mode == PlayMode.PRIORITY && hand.size() > 0) {
            Card[] cards = deck.getCards();
            for(int i = 0; i < deck.getNumCards(); i++) {
                Card card = cards[i];
                if(!priorityPlayed[i]) {
                    for (int j = 0; j < hand.size(); j++) {
                        if (card.getId() == cards[hand.get(j)].getId()) {
                            priorityPlayed[i] = true;
                            playCard(hand.get(j));
                            hand.remove(j);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void startPhase() {
        commander.startPhase();

        for(ActiveCard card : assaults) {
            card.startPhase();
        }

        for(ActiveCard card : structures) {
            card.startPhase();
        }
    }

    public void commanderPhase(ActiveDeck enemyDeck) {
        // BG Enhance effects
        if(bgEffect != null) applySkill(null, bgEffect, enemyDeck);

        applyAllSkills(commander, enemyDeck);
        commander.acted = true;
    }

    public void structurePhase(ActiveDeck enemyDeck) {
        for(ActiveCard card : structures) {
            if(card.isActive()) {
                applyAllSkills(card, enemyDeck);
            }

            // Flurry
            if(card.canAct() && card.flurryActive()) {
                applyAllSkills(card, enemyDeck);
                card.hasFlurried = true;
            }

            card.acted = true;
        }
    }

    public void precombatPhase(ActiveDeck enemyDeck) {
        for(int i = 0; i < assaults.size(); i++) {
            ActiveCard card = assaults.get(i);

            if(card.canAct()) {
                ActiveCard opCard = null;
                if(i < enemyDeck.assaults.size()) { opCard = enemyDeck.assaults.get(i); }
                card.checkValor(opCard);

                // Check Legion
                if(i > 0 && assaults.get(i-1).getType() == card.getType()) {
                    card.applyLegion();
                }
                if(i < (assaults.size() - 1) && assaults.get(i+1).getType() == card.getType()) {
                    card.applyLegion();
                }
            }
        }
    }

    public void assaultPhase(ActiveDeck enemyDeck) {
        for(int i = 0; i < assaults.size(); i++) {
            ActiveCard card = assaults.get(i);

            // Recompute passive skills with skill.enhance
            for(Skill s : card.getSkills()) {
                if(s != null) card.populateSkill(s);
            }

            if(card.canAct()) {
                applyAllSkills(card, enemyDeck);
                if(card.canAct()) doAttack(i, enemyDeck);

                // Flurry
                if(card.canAct() && card.flurryActive()) {
                    applyAllSkills(card, enemyDeck);
                    if(card.canAct()) doAttack(i, enemyDeck);
                    card.hasFlurried = true;
                }
            }

            card.acted = true;
        }
    }

    public void endPhase() {
        commander.endPhase();

        for(ActiveCard card : assaults) {
            card.endPhase();
        }

        for(ActiveCard card : structures) {
            card.endPhase();
        }
    }

    public void doAttack(int i, ActiveDeck enemyDeck) {
        ActiveCard card = assaults.get(i);
        ActiveCard target = null;

        if(card.getEffectiveAttack() > 0) {
            if (i < enemyDeck.assaults.size() && !enemyDeck.assaults.get(i).isDead()) {
                target = enemyDeck.assaults.get(i);
            } else {
                enemyDeck.setWallTargets();
                if(enemyDeck.getNumTargets() > 0) {
                    // Closest wall takes damage first
                    target = enemyDeck.getTargets()[0];
                } else {
                    target = enemyDeck.commander;
                }
            }
        }

        if(target != null) {
            if(DEBUG_MODE) logger.info("[ATTACK] " + card + " attacks " + target);
            card.attack(target);
        }
    }

    public void applyAllSkills(ActiveCard card, ActiveDeck enemyDeck) {
        for(Skill s : card.getSkills()) {
            if(card.canAct() && s != null) { applySkill(card, s, enemyDeck); }
        }
    }

    // TODO: apply all skills and maybe use lambdas to shorten code?
    public void applySkill(ActiveCard card, Skill skill, ActiveDeck enemyDeck) {
        ActiveCard[] targets;
        ActiveCard target = null;
        int numTargeted = 0;
        int enhanceVal = 0;
        boolean overloaded = (card != null) ? card.overloaded : false;

        Faction sFaction = progEffect ? Faction.NOTYPE : skill.y;

        if(card != null) enhanceVal = card.enhanceX[skill.id.ordinal()];

        if(skill.isActiveSkill()) {
            if (skill.id == SkillType.ENFEEBLE) {
                enemyDeck.setEnfeebleTargets();
                numTargeted = enemyDeck.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = enemyDeck.getTargets();
                        for (int i = 0; i < enemyDeck.getNumTargets(); i++) {
                            targets[i].doEnfeeble(skill.x + enhanceVal, overloaded, card);
                        }
                    } else {
                        target = enemyDeck.getRandomTarget();
                        target.doEnfeeble(skill.x + enhanceVal, overloaded, card);
                    }
                }

            } else if (skill.id == SkillType.HEAL) {

                this.setHealTargets(sFaction);
                numTargeted = this.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = this.getTargets();
                        for (int i = 0; i < this.getNumTargets(); i++) {
                            targets[i].doHeal(skill.x + enhanceVal, overloaded);
                        }
                    } else {
                        target = this.getRandomTarget();
                        target.doHeal(skill.x + enhanceVal, overloaded);
                    }
                }

            } else if (skill.id == SkillType.PROTECT) {

                this.setProtectTargets(sFaction);
                numTargeted = this.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = this.getTargets();
                        for (int i = 0; i < this.getNumTargets(); i++) {
                            targets[i].doProtect(skill.x + enhanceVal, overloaded);
                        }
                    } else {
                        target = this.getRandomTarget();
                        target.doProtect(skill.x + enhanceVal, overloaded);
                    }
                }

            } else if (skill.id == SkillType.RALLY) {

                this.setRallyTargets(sFaction);
                numTargeted = this.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = this.getTargets();
                        for (int i = 0; i < this.getNumTargets(); i++) {
                            targets[i].doRally(skill.x + enhanceVal, overloaded);
                        }
                    } else {
                        target = this.getRandomTarget();
                        target.doRally(skill.x + enhanceVal, overloaded);
                    }
                }

            } else if (skill.id == SkillType.SIEGE) {

                enemyDeck.setSiegeTargets();
                numTargeted = enemyDeck.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = enemyDeck.getTargets();
                        for (int i = 0; i < enemyDeck.getNumTargets(); i++) {
                            targets[i].doSiege(skill.x + enhanceVal, overloaded);
                        }
                    } else {
                        target = enemyDeck.getRandomTarget();
                        target.doSiege(skill.x + enhanceVal, overloaded);
                    }
                }

            } else if (skill.id == SkillType.STRIKE) {

                enemyDeck.setStrikeTargets();
                numTargeted = enemyDeck.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = enemyDeck.getTargets();
                        for (int i = 0; i < enemyDeck.getNumTargets(); i++) {
                            targets[i].doStrike(skill.x + enhanceVal, overloaded, card);
                        }
                    } else {
                        target = enemyDeck.getRandomTarget();
                        target.doStrike(skill.x + enhanceVal, overloaded, card);
                    }
                }

            } else if (skill.id == SkillType.WEAKEN) {

                enemyDeck.setWeakenTargets();
                numTargeted = enemyDeck.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = enemyDeck.getTargets();
                        for (int i = 0; i < enemyDeck.getNumTargets(); i++) {
                            targets[i].doWeaken(skill.x + enhanceVal, overloaded, card);
                        }
                    } else {
                        target = enemyDeck.getRandomTarget();
                        target.doWeaken(skill.x + enhanceVal, overloaded, card);
                    }
                }

            } else if (skill.id == SkillType.JAM) {

                assert card != null;
                // JAM is special in that it requires card to be passed.
                if(card.jamActive()) {
                    enemyDeck.setJamTargets();
                    numTargeted = enemyDeck.getNumTargets();
                    if (numTargeted > 0) {
                        if (skill.all) {
                            targets = enemyDeck.getTargets();
                            for (int i = 0; i < enemyDeck.getNumTargets(); i++) {
                                if (targets[i].doJam(overloaded, card)) {
                                    card.hasJammed = true;
                                }
                            }
                        } else {
                            target = enemyDeck.getRandomTarget();
                            if (target.doJam(overloaded, card)) {
                                card.hasJammed = true;
                            }
                        }
                    }
                }

            } else if (skill.id == SkillType.ENHANCE) {

                this.setEnhanceTargets(skill.s);
                numTargeted = this.getNumTargets();
                if(numTargeted > 0) {
                    if (skill.all) {
                        targets = this.getTargets();
                        for (int i = 0; i < this.getNumTargets(); i++) {
                            targets[i].doEnhance(skill.x, skill.s);
                        }
                    } else {
                        target = this.getRandomTarget();
                        target.doEnhance(skill.x, skill.s);
                    }
                }

            } else if (skill.id == SkillType.OVERLOAD) {
                this.setOverloadTargets(sFaction);
                numTargeted = this.getNumTargets();

                if(numTargeted > skill.n) {
                    List<Integer> rList = new ArrayList<Integer>();
                    for (int i = 0; i < numTargeted; i++) {
                        rList.add(i);
                    }
                    Collections.shuffle(rList);
                    for(int i = 0; i < skill.n; i++) {
                        target = this.skillTargets[rList.get(i)];
                        target.doOverload();
                    }
                } else {
                    targets = this.getTargets();
                    for(int i = 0; i < numTargeted; i++) {
                        targets[i].doOverload();
                    }
                }
            }

            if(DEBUG_MODE) {
                if (skill.all) {
                    if (card != null && numTargeted > 0)
                        logger.info("[" + skill.id + " ALL] " + card + " USES " + skill);
                } else if (target != null) {
                    if (card != null)
                        logger.info("[" + skill.id + "] " + card + " USES " + skill + " ON " + target + "@" + enemyDeck.randomIndex);
                }
            }
        }
    }

    // Cleanup should be called before draw and after end
    public void cleanup() {
        int i;
        if(!assaults.isEmpty()) {
            i = 0;
            while(i < assaults.size()) {
                if(assaults.get(i).isDead()) {
                    if(DEBUG_MODE) logger.info("[DEATH] " + assaults.get(i));
                    assaults.remove(i);
                    unitsKilled++;
                }
                else { i++; }
            }
        }
        if(!structures.isEmpty()) {
            i = 0;
            while(i < structures.size()) {
                if(structures.get(i).isDead()) {
                    if(DEBUG_MODE) logger.info("[DEATH] " + structures.get(i));
                    structures.remove(i);
                    unitsKilled++;
                }
                else { i++; }
            }
        }
    }

    @Override
    public String toString() {
        String myString = "\n\n";

        myString += "\n============= COMMANDER =============\n";
        myString += commander;

        myString += "\n============= ASSAULTS =============\n";
        for(ActiveCard card : assaults) {
            myString += card + " ";
        }

        myString += "\n============= STRUCTURES =============\n";
        for(ActiveCard card : structures) {
            myString += card + " ";
        }

        myString += "\n============= HAND =============\n";
        for(Integer i : hand) {
            myString += deck.getCards()[i] + " ";
        }

        myString += "\n\n\n";

        return myString;
    }
}
