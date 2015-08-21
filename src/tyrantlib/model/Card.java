/*
 * Simple container for Tyrant Unleashed Card.
 * This card is only used for deck construction.
 * See ActiveCard for cards used in actual simulation.
 */

package tyrantlib.model;

public class Card {

    private int id;
    private String name;
    private int level = 1;
    private int attack = -1; // Hack to check for structure
    private int health;
    private int wait;
    private Rarity rarity;
    private Faction type;
    private int set;
    private int fusion = 0;
    private int fortress = 0;
    private boolean fail = false;

    // For fast lookups
    final private Skill[] skillByIndex = new Skill[SkillType.values().length];
    final private Skill[] skills = new Skill[3];

    public Card() {}

    public Card(Card card) {
        this.id = card.id;
        this.name = card.name;
        this.level = card.level;
        this.attack = card.attack;
        this.health = card.health;
        this.wait = card.wait;
        this.rarity = card.rarity;
        this.type = card.type;
        this.set = card.set;
        this.fortress = card.fortress;

        for(int i = 0; i < SkillType.values().length; i++) {
            this.skillByIndex[i] = card.skillByIndex[i];
        }
        for(int i = 0; i < 3; i++) {
            this.skills[i] = card.skills[i];
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public Rarity getRarity() { return rarity; }

    public void setRarity(Rarity rarity) { this.rarity = rarity; }

    public Faction getType() { return type; }

    public void setType(Faction type) { this.type = type; }

    public int getSet() { return set; }

    public void setSet(int set) { this.set = set; }

    public int getFusion() { return fusion; }

    public void setFusion(int fusion) { this.fusion = fusion; }

    public int getFortress() { return fortress; }

    public void setFortress(int fortress) { this.fortress = fortress; }

    public boolean getFail() { return fail; }

    public void setFail(boolean b) { fail = b; }

    public void setSkill(Skill skill, int priority)  {
        skillByIndex[skill.id.ordinal()] = skill;
        skills[priority] = skill;
    }
    public Skill getSkillByIndex(SkillType type) { return skillByIndex[type.ordinal()]; }
    public Skill[] getSkills() { return skills; }
    public Skill getSkill(int priority) { return skills[priority]; }

    // Utilities
    public boolean isValidInDeck() { return (set <= 5000); }
    public boolean isCommander()   { return (set == 7000); }
    public boolean isAssault()     { return (set != 7000 && attack >= 0); }
    public boolean isStructure()   { return (set != 7000 && attack < 0); }

    // Card Type by ID
    /*
    if (c.ID >= 1000 && c.ID < 2000) || (c.ID >= 25000 && c.ID < 30000) {
        c.CardType = CT_COMMANDER
    }
    if (c.ID >= 2000 && c.ID < 3000 || c.ID >= 8000 && c.ID < 10000) || (c.ID >= 17000 && c.ID < 25000) {
        c.CardType = CT_STRUCTURE
    }
    if !(c.ID >= 1000 && (c.ID < 4000 || c.ID >= 8000) && (c.ID < 10000 || c.ID >= 17000)) || c.ID >= 30000 {
        c.CardType = CT_ASSAULT
    }
    */

    @Override
    public String toString() {
        return name + " Lv." + level;
    }

    public String toShortString() {
        return name + "[" + attack + "," + health + "," + wait + "]";
    }

    public String toFullString() {
        String ret = "Card{" + "\n" + "\t" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", attack=" + attack +
                ", health=" + health +
                ", wait=" + wait +
                ", rarity=" + rarity +
                ", type=" + type +
                ", set=" + set +
                "\n";

        for(Skill s : skills) {
            if (s != null) ret = ret + "\t" + s + "\n";
        }

        ret += '}';

        return ret;
    }

}
