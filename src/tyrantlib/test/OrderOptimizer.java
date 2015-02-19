/*
 * This will test the basic CCS 3.0 regression for validation purposes.
 * CD Internal use only.
 */

package tyrantlib.test;

import tyrantlib.model.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.PrintWriter;

public class OrderOptimizer {

    public static void main(String args[]) {

        Gauntlet gauntlet = new Gauntlet();
        Gauntlet atkGauntlet = new Gauntlet();
        ArrayList<Deck> optimizedDecks = new ArrayList<Deck>();
        HashMap<Card, ArrayList<Deck>> deckMap = new HashMap<Card, ArrayList<Deck>>();

        try {
            atkGauntlet.loadEncrypted("ccs7.des");
            BufferedReader br = new BufferedReader(new FileReader("ccs7-defense.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",", 2);
                Deck deck = new Deck(arr[0],arr[1]);
                System.out.println(deck);
                gauntlet.add(deck);
            }
            br.close();

            // Regression Test

            /*
            br = new BufferedReader(new FileReader("regression.csv"));
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",", 2);
                Deck deck = new Deck(arr[0],arr[1]);
                regression.add(deck);
            }
            br.close();
            */

            // Find best cards
            /*
            Map<String, CardWrapper> cardMap = CardHandler.getInstance().getEpicMap();
            for(Entry<String, CardWrapper> entry : cardMap.entrySet()) {
                CardWrapper wrapper = entry.getValue();
                Deck deck = new Deck("Barracus","Tempest Citadel, 9 * " + wrapper.getName());
                regression.add(deck);
                if(wrapper.getType() == Faction.RIGHTEOUS|| wrapper.getType() == Faction.PROGENITOR) {
                    Deck deck2 = new Deck("Constantine", "Tempest Citadel, 9 * " + wrapper.getName());
                    regression.add(deck2);
                }
                if(wrapper.getType() == Faction.IMPERIAL|| wrapper.getType() == Faction.PROGENITOR) {
                    Deck deck3 = new Deck("Halcyon", "Tempest Citadel, 9 * " + wrapper.getName());
                    regression.add(deck3);
                }
            }

            */
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Testing RSA Encryption


        long startTime = System.currentTimeMillis();
        boolean isAttack = false;

        for(Deck regdeck : gauntlet.getDeckList()) {
            BGOptions bgopt = new BGOptions();
            bgopt.isAttack = isAttack;
            SimulatorV2 simulator = new SimulatorV2(regdeck, atkGauntlet, bgopt);
            simulator.runSimulation(200);

            int bestScore = simulator.getScore();
            Deck bestDeck = regdeck;

            System.out.println(bestDeck);
            System.out.println("Unoptimized CCS: " + bestScore);

            for(int i = 0; i < 2; i++) {
                System.out.println(i);
                BGOptions bgopt2 = new BGOptions();
                bgopt2.isAttack = isAttack;
                DeckOptimizer optimizer = new DeckOptimizer(regdeck, atkGauntlet, bgopt2);
                Deck optimizedDeck = optimizer.optimizeClimb(isAttack, 200);
                simulator = new SimulatorV2(optimizedDeck, atkGauntlet, bgopt2);
                simulator.runSimulation(200);

                if(simulator.getScore() > bestScore) {
                    bestScore = simulator.getScore();
                    bestDeck = optimizedDeck;
                }
            }

            System.out.println(bestDeck);
            System.out.println("Optimized CCS: " + bestScore);

            optimizedDecks.add(bestDeck);

            /*
            SimulatorV2 simulator = new SimulatorV2(regdeck, gauntlet);
            //simulator.setBGEffect(SkillType.OVERLOAD, 3);
            simulator.runSimulation(200);
            if(simulator.getCCS() > 750) {
                System.out.println(regdeck);
                System.out.println(simulator.getAttackCCS() + "/" + simulator.getDefenseCCS() + " > " + simulator.getCCS());
            }
            System.out.println(simulator.getAttackCCS() + "/" + simulator.getDefenseCCS() + " > " + simulator.getCCS());
            */
        }

        try {
            PrintWriter writer = new PrintWriter("ccs7-defoptimized.csv", "UTF-8");
            for (Deck deck : optimizedDecks) {
                String deckString = deck.getCommander().toString();
                for(Card card : deck.getCards()) {
                    if(card != null) deckString += ("," + card.toString());
                }
                deckString += "\n";
                writer.println(deckString);
            }
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Runtime: " + totalTime);
    }
}