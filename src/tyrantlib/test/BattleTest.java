package tyrantlib.test;

import tyrantlib.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class BattleTest {
    public static void main(String args[]) {

    	/*
        ArrayList<Deck> gauntlet = new ArrayList<Deck>();
        ArrayList<Deck> regression = new ArrayList<Deck>();

        try {
            BufferedReader br = new BufferedReader(new FileReader("gauntlet.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",", 2);
                if(arr.length == 2) {
	                Deck deck = new Deck(arr[0], arr[1]);
	                gauntlet.add(deck);
                }
            }
            br.close();

            br = new BufferedReader(new FileReader("regression.csv"));
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",", 2);
                Deck deck = new Deck(arr[0], arr[1]);
                regression.add(deck);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        Deck deck1 = new Deck("Constantine","5 * Magistrate");
        //Deck deck2 = new Deck(
        //  		"Jotun Max","3 * Jotun's Laser",
        //		true);
        Deck deck2 = new Deck("Barracus","Warp Alchemist Lv.6,Warp Alchemist Lv.6,Crogall Static Lv.6,Narscious the Eerie Lv.6,Sacred Sanctuary Lv.6,Narscious the Eerie Lv.6,Charincinerator Lv.6,Heart Devourer Lv.6,Heart Devourer Lv.6,Lord Hades Lv.6");
        //ActiveDeck deck2 = new ActiveDeck(new Deck("Barracus","Warp Alchemist Lv.6,Warp Alchemist Lv.6,Crogall Static Lv.6,Narscious the Eerie Lv.6,Sacred Sanctuary Lv.6,Narscious the Eerie Lv.6," +
        //        "Charincinerator Lv.6,Heart Devourer Lv.6,Heart Devourer Lv.6,Lord Hades Lv.6"));

        CardHandler cardHandler = CardHandler.getInstance();
        Card corrosiveSpore = cardHandler.getCard("Corrosive Spore");
        Card lightningCannon = cardHandler.getCard("Lightning Cannon");
        Card inspiringAltar = cardHandler.getCard("Inspiring Altar");
        Card mortarTower = cardHandler.getCard("Mortar Tower");

        // System.out.println(cardHandler.getCard("Jilted Baughe",6).toFullString());

        runBattle(deck1, deck2, corrosiveSpore, corrosiveSpore);
        //runBattle(deck1, deck2, corrosiveSpore, lightningCannon);
        //runBattle(deck1, deck2, corrosiveSpore, corrosiveSpore);
        //runBattle(deck1, deck2, lightningCannon, lightningCannon);
        //runBattle(deck1, deck2, inspiringAltar, inspiringAltar);
        //runBattle(deck1, deck2, corrosiveSpore, mortarTower);
        //runBattle(deck1, deck2, mortarTower, mortarTower);
    }

    private static void runBattle(Deck deck1, Deck deck2, Card siege1, Card siege2) {
        Field f = new Field(deck1, deck2, false, 1);
        BGOptions bgOptions = new BGOptions();
        bgOptions.bgEffect = SkillType.REAPING;
        bgOptions.bgX = 3;
        bgOptions.playerSiege = new Card[] { siege1, siege2 };
        f.setBGOptions(bgOptions, true);
        f.run();
        System.out.println(f.getNumWins() + " wins (" + 100.0 * f.getNumWins() / f.getNumRuns() + "%)");
    }
}
