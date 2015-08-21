package tyrantlib.test;

import tyrantlib.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetaTest {
    public static void main(String args[]) {

        Map<String, CardWrapper> cardMap = CardHandler.getInstance().getEpicMap();
        //Deck seed = new Deck("Daedalus","Tabitha's Trusted, Ultimata Ragefueled, Sleek Beamshot, Cycle Blaster, Sleek Beamshot, Bio Equalizer, Cycle Blaster, Bio Equalizer, Sleek Beamshot");
        Deck seed = new Deck("Empress","Ultimata Ragefueled, Sleek Beamshot, Cycle Blaster, Lockon Sight, Lockon Sight, Cycle Blaster, Lockon Sight, Lockon Sight, Sleek Beamshot, Sleek Beamshot");
        for(Map.Entry<String, CardWrapper> entry : cardMap.entrySet()) {
            CardWrapper wrapper = entry.getValue();
            Deck test = new Deck("Arkadios","10 * " + wrapper.getName());
            runBattle(test, seed, null, null);
            test = new Deck("Daedalus","10 * " + wrapper.getName());
            runBattle(test, seed, null, null);
        }

        //runBattle(deck1, deck2, corrosiveSpore, lightningCannon);
        //runBattle(deck1, deck2, corrosiveSpore, corrosiveSpore);
        //runBattle(deck1, deck2, lightningCannon, lightningCannon);
        //runBattle(deck1, deck2, inspiringAltar, inspiringAltar);
        //runBattle(deck1, deck2, corrosiveSpore, mortarTower);
        //runBattle(deck1, deck2, mortarTower, mortarTower);
    }

    private static void runBattle(Deck deck1, Deck deck2, Card siege1, Card siege2) {
        Field f = new Field(deck1, deck2, true, 100);
        BGOptions bgOptions = new BGOptions();
        //bgOptions.bgEffect = SkillType.REAPING;
        //bgOptions.bgX = 3;
        //bgOptions.playerSiege = new Card[] { siege1, siege2 };
        f.setBGOptions(bgOptions, true);
        f.run();
        double winRate = 100.0 * f.getNumWins() / f.getNumRuns();
        if(winRate > 60) {
            System.out.println(deck1.toFullString());
            System.out.println(f.getNumWins() + " wins (" + winRate + "%)");
        }
    }
}
