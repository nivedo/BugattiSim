/*
 * Constructs a TU Deck from Commander and Deck List
 *
 * USAGE EXAMPLES:
 *
 * Deck("Nexor Lv.6", "4 * demi constrictor lv.6, 3* xeno suzerain Lv.6, 2* tempest citadel lv.6")
 * Deck("Halcyon Lv.6", "6 * Sculpted Aegis Lv.6")
 *
 * This method is very generous with
 * shorthand and is case / white space insensitive
 */

package tyrantlib.model;

import java.util.Arrays;
import java.util.HashMap;

public class Deck {

    private Card commander = null;
    private Card[] cards = new Card[10];
    private int numCards = 0;

    // Unique cards in deck
    private HashMap<Integer, Card> uniqueCards = new HashMap<Integer, Card>();

    // Special boolean to allow for event bosses decks
    private boolean eventDeck = false;

    // Check validity of deck on instantiation
    private void validateDeck() {
        if(!eventDeck) {
            if (!commander.isCommander()) {
                throw new RuntimeException(commander.getName() + " is not a valid commander!");
            }

            for(int i = 0; i < numCards; i++) {
                if (!cards[i].isValidInDeck()) {
                    throw new RuntimeException(cards[i].getName() + " is not valid in deck!");
                }
            }
        }
    }

    public Deck(String commanderName, String deckList) {
        this(commanderName, deckList, false);
    }

    // Special constructor for event decks
    public Deck(String commanderName, String deckList, boolean eventDeck) {
        this.eventDeck = eventDeck;

        commanderName = commanderName.toLowerCase();
        deckList = deckList.toLowerCase();

        CardHandler handler = CardHandler.getInstance();

        String[] cSplit;
        String cName;
        int cLevel;

        // Retrieve commander first
        if(commanderName.contains("lv.")) {
            cSplit = commanderName.split("lv.");
            cName = cSplit[0].trim();
            cLevel = Integer.parseInt(cSplit[1].trim());
            this.commander = handler.getCard(cName, cLevel);
        } else {
            cName = commanderName.trim();
            this.commander = handler.getCard(cName);
        }


        String[] cardStrings = deckList.split(",");

        for(String cs : cardStrings) {
            String cardName = cs.trim();
            int cardNum = 1;

            if(cardName.contains("*")) {
                String[] cardSplit = cardName.split("\\*");
                cardNum = Integer.parseInt(cardSplit[0].trim());
                cardName = cardSplit[1];
            }

            Card deckCard;
            if(cardName.contains("lv.")) {
                cSplit = cardName.split("lv\\.");
                cName = cSplit[0].trim();
                cLevel = Integer.parseInt(cSplit[1].trim());
                deckCard = handler.getCard(cName, cLevel);
            } else {
                cName = cardName.trim();
                deckCard = handler.getCard(cName);
            }

            if(!uniqueCards.containsKey(deckCard.getId())) {
                uniqueCards.put(deckCard.getId(), deckCard);
            }

            for(int i = 0; i < cardNum; i++) {
                numCards++;

                if(numCards > 10) {
                    throw new RuntimeException("Cannot have more than 10 cards!");
                }

                cards[numCards-1] = deckCard;
            }
        }

        validateDeck();
    }

    public Card getCommander() {
        return commander;
    }

    public Card[] getCards() {
        return cards;
    }

    public void setCard(Card c, int index) { cards[index] = c; }

    public int getNumCards() {
        return numCards;
    }

    public void setNumCards(int numCards) { this.numCards = numCards; }

    public boolean isEventDeck() {
        return eventDeck;
    }

    public HashMap<Integer, Card> getUniqueCards() { return uniqueCards; }

/*
    @Override
    public String toString() {
        String commanderString = commander.getName() + " | ";

        String deckString = "";

        for(int i = 0; i < numCards; i++) {
            if(!deckString.isEmpty()) deckString += ", ";
            deckString += (cards[i].getName() + " Lv." + cards[i].getLevel());
        }

        return commanderString + deckString;
    }
*/

    @Override
    public String toString() {
        String commanderString = commander.getName().replaceAll("\\s+","").substring(0,5).toUpperCase() + " | ";

        String deckString = "";

        for(int i = 0; i < numCards; i++) {
            if(!deckString.isEmpty()) deckString += ", ";
            deckString += (cards[i].getName().replaceAll("\\s+","").substring(0,5).toUpperCase());
        }

        for(int i = numCards; i < 10; i++) {
            deckString += "       ";
        }

        return commanderString + deckString;
    }


    public String toCopyString() {
        String deckString = commander.toString();

        for(int i = 0; i < numCards; i++) {
            deckString += ("\n" + cards[i]);
        }

        return deckString;
    }

    public String toFullString() {
        return "Deck{" +
                "\n" + "commander=" + commander +
                ",\n numCards=" + numCards +
                ",\n cards=" + Arrays.toString(cards) +
                "\n" +
                '}';
    }
}
