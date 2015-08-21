package tyrantlib.test;

import tyrantlib.model.*;

public class OptimalDefense {
    public static void main(String args[]) {
        CardHandler cardHandler = CardHandler.getInstance();
        Gauntlet testGauntlet = new Gauntlet();
        Gauntlet attackGauntlet = new Gauntlet();
        Gauntlet defenseGauntlet = new Gauntlet();

        attackGauntlet.loadEncrypted("ccs9_attack.des");
        defenseGauntlet.loadEncrypted("ccs9_defense.des");

        // Set up defensive decks
        //testGauntlet.add(new Deck("Typhon Vex","3 * Guillotine Grinder, Dreamhaunter, Obsidian, Trench Hurler"));
        //testGauntlet.add(new Deck("Malort","Razarp Shank, Dreamhaunter, Obsidian, 2 * Abhorrent Recluse, Serraco Sire, Shapesavant, Ravenous Terrorsaur"));
        //testGauntlet.add(new Deck("Daedalus","Cockatrice Baxis, Dreamhaunter, Gehenna Cursed, Savant Ascendant, 2 * Scorched Hellwing, Styxasis, Erebus City Sector"));
        //testGauntlet.add(new Deck("Daedalus","2 * Justice Victorious, Dune Runner, Dreamhaunter, Obsidian, Serraco Sire, Hylas Adroit, Highstorm Bastille, Octane's Bulwark"));
        // Set up defensive decks
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-1));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-2));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-3));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-4));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-5));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-6));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-7));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-8));
        testGauntlet.add(attackGauntlet.getDeckList().get(attackGauntlet.size()-9));

        BGOptions options = new BGOptions();
        //options.bgEffect = SkillType.TURNINGTIDE;
        //options.bgX = 2;
        //options.isEnhance = false;
        options.isAttack = false;

        String bestStructures = "Corrosive Spore,Death Factory";
        double bestRate = 999;
        String secondBest = "";
        String thirdBest = "";
    /*
        // Find Optimial Enemy Attack
        for(Card siegeCard1 : cardHandler.getSiegeForts()) {
            for(Card siegeCard2 : cardHandler.getSiegeForts()) {
                options.enemySiege[0] = siegeCard1;
                options.enemySiege[1] = siegeCard2;
                double averageRate = 0;
                for(Deck testDeck : testGauntlet.getDeckList()) {
                    SimulatorV2 sim = new SimulatorV2(testDeck, defenseGauntlet, options);
                    sim.runSimulation(1000);
                    //System.out.println("Sim Deck: " + testDeck.toCopyString());
                    //System.out.println("Sim Win Rate: " + sim.getScore());
                    averageRate += sim.getScore();
                }
                averageRate /= testGauntlet.size();
                if(averageRate < bestRate) {
                    thirdBest = secondBest;
                    secondBest = bestStructures;
                    bestStructures = siegeCard1.getName() + "," + siegeCard2.getName();
                    bestRate = averageRate;
                    System.out.println(bestStructures + " : " + bestRate);
                }
            }
        }
*/
        System.out.println("FIRST: " + bestStructures + " SECOND: " + secondBest + " THIRD: " + thirdBest);

        Card bestDef = null;
        double bestScore = 0;

        // Find optimal defense for best 3 open siege
        String[] split = bestStructures.split(",");
        options.enemySiege[0] = cardHandler.getCard(split[0]);
        options.enemySiege[1] = cardHandler.getCard(split[1]);

        for(Card defCard1 : cardHandler.getDefenseForts()) {
            options.playerDefense[0] = defCard1;
            double averageRate = 0;
            for(Deck testDeck : testGauntlet.getDeckList()) {
                SimulatorV2 sim = new SimulatorV2(testDeck, defenseGauntlet, options);
                sim.runSimulation(1000);
                averageRate += sim.getScore();
            }
            averageRate /= testGauntlet.size();
            if(averageRate > bestScore) {
                bestScore = averageRate;
                bestDef = defCard1;
                System.out.println("Best Defense: " + bestDef + " (" + bestScore + ")");
            }
        }
    }
}
