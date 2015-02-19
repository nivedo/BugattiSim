package tyrantlib.model;

import tyrantlib.util.ArrayIndexComparator;

import java.util.*;

public class DeckOptimizer {

    private Gauntlet gauntlet;
    private ArrayList<Card> benchCards = new ArrayList<Card>();
    private HashMap<Card, ArrayList<Deck>> deckMap = new HashMap<Card, ArrayList<Deck>>();
    private Deck deck;

    private BGOptions options;

    // Weighted options proposed by uDecor + Axis
    private boolean useMultiOpt = false;
    private ArrayList<BGOptions> optionsList;
    private ArrayList<Double> weights;

    public void setMultiOptions(ArrayList<BGOptions> opts, ArrayList<Double> weights) {
        this.optionsList = opts;
        this.weights = weights;
        useMultiOpt = true;
    }

    private SkillType bgSkill = SkillType.UNKNOWN;
    private int bgEnhance;

    public void setBGOptions(BGOptions options) {
        this.options = options;
    }

    public int getNumRuns() {
        return 200 / (useMultiOpt ? weights.size() : 1);
    }

    public DeckOptimizer(Deck deck, Gauntlet gauntlet, BGOptions options) {
        this.deck = deck;
        this.gauntlet = gauntlet;
        this.options = options;
    }

    public void addCardToBench(Card c) {
        benchCards.add(c);
    }

    public Deck optimizeClimb(boolean isAttack, int numRuns) {
        Deck optimizedDeck = deck;
        options.isAttack = isAttack;

        SimulatorV2 simulator = new SimulatorV2(deck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        //simulator.runSimulation(1000);

        ArrayList<Integer> spamScores = new ArrayList<Integer>();
        ArrayList<Boolean> inDeck = new ArrayList<>();

        for(Card card : deck.getCards()) {
            if(card != null) benchCards.add(card);
        }

        for(Card bcard : benchCards) {
            SimulatorV2 sim = new SimulatorV2(new Deck(deck.getCommander().getName(),"10 * " + bcard.getName()), gauntlet, options);
            if(useMultiOpt) sim.setMultiOptions(optionsList, weights);
            sim.runSimulation(numRuns);
            spamScores.add(sim.getScore());
            //System.out.println(bcard.getName() + " : " + sim.getScore());
        }

        // JN_HACK: sort indices ... has to be an easier way?!
        ArrayIndexComparator comparator = new ArrayIndexComparator(spamScores.toArray(new Integer[spamScores.size()]));
        Integer[] indices = comparator.createIndexArray();
        Arrays.sort(indices, comparator);

        Deck bestDeck = new Deck(deck.getCommander().getName(),benchCards.get(indices[0]).getName());
        simulator = new SimulatorV2(bestDeck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(numRuns);

        int bestScore = simulator.getScore();

        int curIndex = 0;
        // This step does "insertion sort" like optimization given spam score rankings
        for(int i = 1; i < benchCards.size() && bestDeck.getNumCards() < 10; i++) {
            curIndex = i;
            String deckString = "";
            Card bcard = benchCards.get(indices[i]);
            int tempBestScore = -1;
            int tempBestIndex = -1;
            Deck tempDeck;

            if(isAttack) {
                // Iterate through all possible positions and pick best
                for (int j = 0; j <= bestDeck.getNumCards(); j++) {
                    deckString = "";
                    // Add all cards up to j
                    for (int k = 0; k < j; k++) {
                        if (k > 0) deckString += ",";
                        deckString += bestDeck.getCards()[k].toString();
                    }
                    if (j > 0) deckString += ",";
                    deckString += bcard;
                    for (int k = j; k < bestDeck.getNumCards(); k++) {
                        deckString += ("," + bestDeck.getCards()[k].toString());
                    }

                    // Extension of CD "Spam Score", spam combo
                    int numRep = 10 / (bestDeck.getNumCards() + 1);
                    String newDeckString = "";
                    for (int k = 0; k < numRep; k++) {
                        if (k > 0) newDeckString += ",";
                        newDeckString += deckString;
                    }

                    //System.out.println(newDeckString);

                    tempDeck = new Deck(deck.getCommander().getName(), newDeckString);
                    simulator = new SimulatorV2(tempDeck, gauntlet, options);
                    if (useMultiOpt) simulator.setMultiOptions(optionsList, weights);
                    simulator.runSimulation(numRuns);

                    if (simulator.getScore() > tempBestScore) {
                        tempBestScore = simulator.getScore();
                        tempBestIndex = j;
                        //System.out.println("BEST SCORE: " + tempBestScore);
                    }
                }
            } else {
                // Add single card at the end
                for (int k = 0; k < bestDeck.getNumCards(); k++) {
                    if (k > 0) deckString += ",";
                    deckString += bestDeck.getCards()[k].toString();
                }
                deckString += ",";
                deckString += bcard;

                // Extension of CD "Spam Score", spam combo
                int numRep = 10 / (bestDeck.getNumCards() + 1);
                String newDeckString = "";
                for (int k = 0; k < numRep; k++) {
                    if (k > 0) newDeckString += ",";
                    newDeckString += deckString;
                }

                tempDeck = new Deck(deck.getCommander().getName(), newDeckString);
                simulator = new SimulatorV2(tempDeck, gauntlet, options);
                if (useMultiOpt) simulator.setMultiOptions(optionsList, weights);
                simulator.runSimulation(numRuns);

                if (simulator.getScore() > tempBestScore) {
                    tempBestIndex = bestDeck.getNumCards();
                }
            }

            deckString = "";
            // Add all cards up to bestIndex
            for(int k = 0; k < tempBestIndex; k++) {
                if(k > 0) deckString += ",";
                deckString += bestDeck.getCards()[k].toString();
            }
            if(tempBestIndex > 0) deckString += ",";
            deckString += bcard;
            for(int k = tempBestIndex; k < bestDeck.getNumCards(); k++) {
                deckString += ("," + bestDeck.getCards()[k].toString());
            }

            tempDeck = new Deck(deck.getCommander().getName(), deckString);
            simulator = new SimulatorV2(tempDeck, gauntlet, options);
            if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
            simulator.runSimulation(numRuns);

            if(simulator.getScore() >= bestScore) {
                bestScore = simulator.getScore();
                bestDeck = tempDeck;
            }

            bestDeck = trimClimb(bestDeck, numRuns);
        }

        if(bestDeck.getNumCards() == 10 && ++curIndex < benchCards.size()) {
            for(int i = curIndex; i < benchCards.size(); i++) {
                Card bcard = benchCards.get(indices[i]);
                bestDeck = subClimb(bestDeck, bcard, numRuns);
            }
        }

        return trimClimb(bestDeck, numRuns);
    }

    // Trim helper function for climbing algo
    public Deck subClimb(Deck origDeck, Card subCard, int numRuns) {
        Deck bestDeck = origDeck;
        SimulatorV2 simulator = new SimulatorV2(bestDeck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(numRuns);
        int bestScore = simulator.getScore();

        Deck tempBestDeck = bestDeck;
        int tempBestScore = bestScore;
        boolean repeat = true;

        for (int i = 0; i <= bestDeck.getNumCards(); i++) {
            String commanderString = origDeck.getCommander().toString();
            String deckString = "";

            for (int k = 0; k < bestDeck.getNumCards(); k++) {
                if (k != i) {
                    if (!deckString.isEmpty()) deckString += ",";
                    deckString += bestDeck.getCards()[k];
                } else {
                    if (!deckString.isEmpty()) deckString += ",";
                    deckString += subCard;
                }
            }

            Deck tempDeck = new Deck(commanderString, deckString);
            simulator = new SimulatorV2(tempDeck, gauntlet, options);
            if (useMultiOpt) simulator.setMultiOptions(optionsList, weights);
            simulator.runSimulation(numRuns);

            if (simulator.getScore() >= tempBestScore) {
                tempBestScore = simulator.getScore();
                tempBestDeck = tempDeck;
            }
        }

        if(tempBestScore > bestScore + 3) {
            bestDeck = tempBestDeck;
            bestScore = tempBestScore;
        }

        return bestDeck;
    }


    // Trim helper function for climbing algo
    public Deck trimClimb(Deck origDeck, int numRuns) {
        Deck bestDeck = origDeck;
        SimulatorV2 simulator = new SimulatorV2(bestDeck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(numRuns);
        int bestScore = simulator.getScore();

        Deck tempBestDeck = bestDeck;
        int tempBestScore = bestScore;
        boolean repeat = true;

        while(repeat) {
            for (int i = 0; i <= bestDeck.getNumCards(); i++) {
                String commanderString = origDeck.getCommander().toString();
                String deckString = "";

                for (int k = 0; k < bestDeck.getNumCards(); k++) {
                    if (k != i) {
                        if (!deckString.isEmpty()) deckString += ",";
                        deckString += bestDeck.getCards()[k];
                    }
                }

                Deck tempDeck = new Deck(commanderString, deckString);
                simulator = new SimulatorV2(tempDeck, gauntlet, options);
                if (useMultiOpt) simulator.setMultiOptions(optionsList, weights);
                simulator.runSimulation(numRuns);

                if (simulator.getScore() >= tempBestScore) {
                    tempBestScore = simulator.getScore();
                    tempBestDeck = tempDeck;
                }
            }

            if(tempBestScore > bestScore + 3) {
                bestDeck = tempBestDeck;
                bestScore = tempBestScore;
                repeat = true;
            } else {
                repeat = false;
            }
        }

        return bestDeck;
    }

    // NOTE: To be only used with Event type optimizations!!
    public Deck optimizeEvent() {
        Deck optimizedDeck = deck;
        options.isAttack = true;
        options.surge = false;

        SimulatorV2 simulator = new SimulatorV2(deck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        //simulator.runSimulation(1000);

        ArrayList<Integer> spamScores = new ArrayList<Integer>();

        for(Card card : deck.getCards()) {
            if(card != null) benchCards.add(card);
        }

        for(Card bcard : benchCards) {
            SimulatorV2 sim = new SimulatorV2(new Deck(deck.getCommander().getName(),"10 * " + bcard.getName()), gauntlet, options);
            if(useMultiOpt) sim.setMultiOptions(optionsList, weights);
            sim.runSimulation(1000);
            spamScores.add(sim.getScore());

            //System.out.println(bcard.getName() + " : " + sim.getScore());
        }

        // JN_HACK: sort indices ... has to be an easier way?!
        ArrayIndexComparator comparator = new ArrayIndexComparator(spamScores.toArray(new Integer[spamScores.size()]));
        Integer[] indices = comparator.createIndexArray();
        Arrays.sort(indices, comparator);

        Deck bestDeck = new Deck(deck.getCommander().getName(),benchCards.get(indices[0]).getName());
        simulator = new SimulatorV2(bestDeck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(1000);

        int bestScore = simulator.getScore();

        // This step does "insertion sort" like optimization given spam score rankings
        for(int i = 1; i < benchCards.size() && bestDeck.getNumCards() < 10; i++) {
            // Iterate through all possible positions and pick best
            String deckString = "";
            Card bcard = benchCards.get(indices[i]);
            int tempBestScore = -1;
            int tempBestIndex = -1;
            Deck tempDeck;

            for(int j = 0; j <= bestDeck.getNumCards(); j++) {
                deckString = "";
                // Add all cards up to j
                for(int k = 0; k < j; k++) {
                    if(k > 0) deckString += ",";
                    deckString += bestDeck.getCards()[k].toString();
                }
                if(j > 0) deckString += ",";
                deckString += bcard;
                for(int k = j; k < bestDeck.getNumCards(); k++) {
                    deckString += ("," + bestDeck.getCards()[k].toString());
                }

                // Extension of CD "Spam Score", spam combo
                int numRep = 10 / (bestDeck.getNumCards() + 1);
                String newDeckString = "";
                for(int k = 0; k < numRep; k++) {
                    if(k > 0) newDeckString += ",";
                    newDeckString += deckString;
                }

                //System.out.println(newDeckString);
                tempDeck = new Deck(deck.getCommander().getName(), newDeckString);
                simulator = new SimulatorV2(tempDeck, gauntlet, options);
                if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
                simulator.runSimulation(1000);

                if(simulator.getScore() > tempBestScore) {
                    tempBestScore = simulator.getScore();
                    tempBestIndex = j;
                }
            }

            deckString = "";
            // Add all cards up to bestIndex
            for(int k = 0; k < tempBestIndex; k++) {
                if(k > 0) deckString += ",";
                deckString += bestDeck.getCards()[k].toString();
            }
            if(tempBestIndex > 0) deckString += ",";
            deckString += bcard;
            for(int k = tempBestIndex; k < bestDeck.getNumCards(); k++) {
                deckString += ("," + bestDeck.getCards()[k].toString());
            }

            tempDeck = new Deck(deck.getCommander().getName(), deckString);
            simulator = new SimulatorV2(tempDeck, gauntlet, options);
            if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
            simulator.runSimulation(1000);

            if(simulator.getScore() >= bestScore) {
                bestScore = simulator.getScore();
                bestDeck = tempDeck;
            }
        }

        // "Trim the fat" - minimalize deck
        Deck tempBestDeck = bestDeck;
        int tempBestScore = bestScore;
        boolean repeat = true;

        while(repeat) {
            for (int i = 0; i <= bestDeck.getNumCards(); i++) {
                String commanderString = deck.getCommander().toString();
                String deckString = "";

                for (int k = 0; k < bestDeck.getNumCards(); k++) {
                    if (k != i) {
                        if (!deckString.isEmpty()) deckString += ",";
                        deckString += bestDeck.getCards()[k];
                    }
                }

                Deck tempDeck = new Deck(commanderString, deckString);
                simulator = new SimulatorV2(tempDeck, gauntlet, options);
                if (useMultiOpt) simulator.setMultiOptions(optionsList, weights);
                simulator.runSimulation(1000);

                if (simulator.getScore() >= tempBestScore) {
                    tempBestScore = simulator.getScore();
                    tempBestDeck = tempDeck;
                }
            }

            if(tempBestScore > bestScore) {
                bestDeck = tempBestDeck;
                bestScore = tempBestScore;
                repeat = true;
            } else {
                repeat = false;
            }
        }

        return bestDeck;
    }

    public Deck optimizeDefense() {
        Deck optimizedDeck = deck;
        boolean repeat = true;
        options.isAttack = false;
        options.surge = true;

        while(repeat) {
            SimulatorV2 simulator = new SimulatorV2(deck, gauntlet, options);
            if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
            simulator.runSimulation(getNumRuns());
            int curScore = simulator.getScore();

            deck = trim();
            if(benchCards.size() > 0) {
                if (deck.getNumCards() == 10 && deck == optimizedDeck) {
                    // Move each card to bench, and try substituting
                    for (int i = 0; i < deck.getNumCards(); i++) {
                        deck = substitute(i);
                    }
                } else {
                    deck = expand();
                }
            }

            simulator = new SimulatorV2(deck, gauntlet, options);
            if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
            simulator.runSimulation(getNumRuns());
            int newScore = simulator.getScore();

            // Threshold cutoff
            if (newScore - curScore > 3) {
                optimizedDeck = deck;
            } else {
                repeat = false;
            }
        }

        return deck;
    }

    // NOTE: trim ALWAYS removes a card to make room for expand substitution
    public Deck trim() {
        options.isAttack = false;
        options.surge = true;

        Card[] cards = deck.getCards();
        boolean[] trimmed = new boolean[deck.getNumCards()];
        SimulatorV2[] trimDecks = new SimulatorV2[deck.getNumCards()];

        // Helper vars
        SimulatorV2 simulator = new SimulatorV2(deck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(getNumRuns());
        int bestScore = simulator.getScore();
        int bestIndex = -1;
        Deck bestDeck = deck;

        boolean repeat = true;

        while(repeat) {
            Arrays.fill(trimDecks, null);
            // Try to trim single card
            for (int i = 0; i < deck.getNumCards(); i++) {
                if (!trimmed[i]) {
                    String commanderString = deck.getCommander().toString();
                    String deckString = "";

                    for (int k = 0; k < deck.getNumCards(); k++) {
                        if (!trimmed[k] && k != i) {
                            if (!deckString.isEmpty()) deckString += ",";
                            deckString += cards[k];
                        }
                    }

                    Deck newDeck = new Deck(commanderString, deckString);
                    trimDecks[i] = new SimulatorV2(newDeck, gauntlet, options);
                    if(useMultiOpt) trimDecks[i].setMultiOptions(optionsList, weights);
                    trimDecks[i].runSimulation(getNumRuns());
                }
            }

            repeat = false;
            for (int i = 0; i < deck.getNumCards(); i++) {
                if (trimDecks[i] != null) {
                    int defenseScore = trimDecks[i].getScore();
                    if (defenseScore > bestScore) {
                        bestIndex = i;
                        bestScore = defenseScore;
                        bestDeck = trimDecks[i].getPlayerDeck();
                        repeat = true;
                    }
                }
            }

            if(repeat) {
                trimmed[bestIndex] = true;
                benchCards.add(deck.getCards()[bestIndex]);
            }
        }

        return bestDeck;
    }

    public Deck expand() {
        options.isAttack = false;
        options.surge = true;

        Card[] cards = deck.getCards();
        boolean[] inserted = new boolean[benchCards.size()];
        SimulatorV2[] expandDecks = new SimulatorV2[benchCards.size()];
        int numExpanded = 0;

        // Helper vars
        SimulatorV2 simulator = new SimulatorV2(deck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(getNumRuns());
        int bestScore = simulator.getScore();
        int bestIndex = -1;
        Deck bestDeck = deck;

        boolean repeat = true;

        while(repeat && (deck.getNumCards() + numExpanded < 10)) {
            Arrays.fill(expandDecks, null);
            for (int i = 0; i < benchCards.size(); i++) {
                if (!inserted[i]) {
                    String commanderString = deck.getCommander().toString();
                    String deckString = "";

                    for (int k = 0; k < deck.getNumCards(); k++) {
                        if (!deckString.isEmpty()) deckString += ",";
                        deckString += cards[k];
                    }

                    for (int k = 0; k < benchCards.size(); k++) {
                        if(inserted[k]) {
                            deckString += ",";
                            deckString += benchCards.get(k);
                        }
                    }

                    deckString += ",";
                    deckString += benchCards.get(i);

                    Deck newDeck = new Deck(commanderString, deckString);
                    expandDecks[i] = new SimulatorV2(newDeck, gauntlet, options);
                    if(useMultiOpt) expandDecks[i].setMultiOptions(optionsList, weights);
                    expandDecks[i].runSimulation(getNumRuns());
                }
            }

            repeat = false;
            for (int i = 0; i < benchCards.size(); i++) {
                if (expandDecks[i] != null) {
                    int defenseScore = expandDecks[i].getScore();
                    if (defenseScore > bestScore) {
                        bestIndex = i;
                        bestScore = defenseScore;
                        bestDeck = expandDecks[i].getPlayerDeck();
                        repeat = true;
                    }
                }
            }

            if(repeat) {
                inserted[bestIndex] = true;
                numExpanded++;
            }
        }

        for(int i = 0; i < benchCards.size(); i++) {
            if(inserted[i]) benchCards.remove(i--);
        }

        return bestDeck;
    }

    public Deck substitute(int index) {
        options.isAttack = false;
        options.surge = true;

        Card[] cards = deck.getCards();

        // Move single card to the bench
        for (int k = 0; k < deck.getNumCards(); k++) {
            if(k == index) {
                benchCards.add(cards[k]);
                break;
            }
        }

        SimulatorV2[] expandDecks = new SimulatorV2[benchCards.size()];

        // Helper vars
        int bestScore = 0;
        int bestIndex = 0;
        Deck bestDeck = deck;

        for (int i = 0; i < benchCards.size(); i++) {
            String commanderString = deck.getCommander().toString();
            String deckString = "";

            for (int k = 0; k < deck.getNumCards(); k++) {
                if (k != index) {
                    if (!deckString.isEmpty()) deckString += ",";
                    deckString += cards[k];
                }
            }

            deckString += ",";
            deckString += benchCards.get(i);

            Deck newDeck = new Deck(commanderString, deckString);
            expandDecks[i] = new SimulatorV2(newDeck, gauntlet, options);
            if(useMultiOpt) expandDecks[i].setMultiOptions(optionsList, weights);
            expandDecks[i].runSimulation(getNumRuns());
        }

        for (int i = 0; i < benchCards.size(); i++) {
            if (expandDecks[i] != null) {
                int defenseScore = expandDecks[i].getScore();
                if (defenseScore > bestScore) {
                    bestIndex = i;
                    bestScore = defenseScore;
                    bestDeck = expandDecks[i].getPlayerDeck();
                }
            }
        }

        benchCards.remove(bestIndex);

        return bestDeck;
    }

    public Deck optimize(int numRuns) {
        options.isAttack = true;
        options.surge = true;

        Card[] cards = deck.getCards();
        boolean[] fixed = new boolean[deck.getNumCards()];

        SimulatorV2 simulator = new SimulatorV2(deck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(getNumRuns());
        int initialScore = simulator.getScore();

        // Helper vars
        int dropNum = 0;
        String deckPrefix = "";

        // Shuffle list
        List<Integer> seedList = new ArrayList<Integer>();
        for (int i = 0; i < deck.getNumCards(); i++) {
            seedList.add(i);
        }

        while(dropNum < deck.getNumCards()) {
            deckMap.clear();
            for (int i = 0; i < deck.getNumCards(); i++) {
                if(!fixed[i]) {
                    Card card = cards[i];
                    if (!deckMap.containsKey(card)) {
                        ArrayList<Deck> deckList = new ArrayList<Deck>();

                        for (int j = 0; j < numRuns; j++) {
                            Collections.shuffle(seedList);
                            String commanderString = deck.getCommander().toString();
                            String deckString = deckPrefix;
                            if (!deckString.isEmpty()) deckString += ",";
                            deckString += card;

                            for (int k = 0; k < deck.getNumCards(); k++) {
                                int index = seedList.get(k);
                                if (index != i && !fixed[index]) {
                                    deckString += ("," + cards[index]);
                                }
                            }

                            Deck randomDeck = new Deck(commanderString, deckString);
                            deckList.add(randomDeck);
                        }

                        deckMap.put(card, deckList);
                    }
                }
            }

            Card bestDrop = null;
            int bestScore = -1;

            for (Map.Entry<Card, ArrayList<Deck>> entry : deckMap.entrySet()) {
                //System.out.println("Testing DROP (" + dropNum + "): " + entry.getKey());
                int ccsSum = 0;
                int count = 0;

                ArrayList<Deck> deckList = entry.getValue();
                for (Deck regdeck : deckList) {
                    simulator = new SimulatorV2(regdeck, gauntlet, options);
                    if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
                    simulator.runSimulation(getNumRuns());
                    ccsSum += simulator.getScore();
                    count++;
                    //System.out.println(simulator.getAttackCCS() + "/" + simulator.getDefenseCCS() + " > " + simulator.getCCS());
                }

                int score = (ccsSum / count);
                if (score > bestScore) {
                    bestDrop = entry.getKey();
                    bestScore = score;
                }
                //System.out.println("Average SURGE score: " + (ccsSum / count));
            }

            //assert bestDrop != null;

            //System.out.println("BEST DROP(" + dropNum + "): " + bestDrop);

            dropNum++;
            if (!deckPrefix.isEmpty()) deckPrefix += ",";
            deckPrefix += bestDrop;

            for(int i = 0; i < deck.getNumCards(); i++) {
                if(!fixed[i] && cards[i].getId() == bestDrop.getId()) {
                    fixed[i] = true;
                    break;
                }
            }

            //System.out.println("\n>>>> DROP ORDER: " + deckPrefix + "\n");
        }

        Deck newDeck = new Deck(deck.getCommander().toString(), deckPrefix);
        simulator = new SimulatorV2(newDeck, gauntlet, options);
        if(useMultiOpt) simulator.setMultiOptions(optionsList, weights);
        simulator.runSimulation(getNumRuns());
        int newScore = simulator.getScore();

        if(initialScore > newScore) {
            return deck;
        }

        return newDeck;
    }

}
