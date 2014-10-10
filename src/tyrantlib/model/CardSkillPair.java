/*
 * Utility class for looking up cards by skill level.
 */

package tyrantlib.model;

public class CardSkillPair {

    public Card card;
    public Skill skill; // Index of given skill

    public CardSkillPair(Card card, Skill skill) {
        this.card = card;
        this.skill = skill;
    }
}
