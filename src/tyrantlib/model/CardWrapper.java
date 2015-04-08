/*
 * Wrapper for each card and it's corresponding levels.
 * Each level is a unique card, but all cards with the same name
 * fall under this wrapper.
 */

package tyrantlib.model;

import java.lang.RuntimeException;

public class CardWrapper {

    private String name;
    private Rarity rarity;
    private Faction type;
    private int set;
    private int fusion = 0;
    private int fortress = 0;

    private Card[] levels = new Card[10];
    private int numLevels = 1;

    public Card getLevel(int i) {
        if(i > numLevels) {
            throw new RuntimeException("Card " + name + " does not have " + i + " levels!");
        }
        return levels[i-1];
    }

    public void setLevel(int i, Card card) {
        levels[i-1] = card;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumLevels() {
        return numLevels;
    }

    public void setNumLevels(int numLevels) {
        this.numLevels = numLevels;
    }

    public boolean getFail() {
        for(int i = 1; i <= numLevels; i++) {
            if(getLevel(i).getFail()) return true;
        }
        return false;
    }

    public boolean isValidInDeck() { return (set <= 5000); }
    public boolean isCommander()   { return (set == 7000); }
}
