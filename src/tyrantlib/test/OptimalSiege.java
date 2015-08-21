package tyrantlib.test;

import tyrantlib.model.*;

public class OptimalSiege {
    public static void main(String args[]) {
        CardHandler cardHandler = CardHandler.getInstance();
        Gauntlet testGauntlet = new Gauntlet();
        Gauntlet attackGauntlet = new Gauntlet();
        Gauntlet defenseGauntlet = new Gauntlet();

        attackGauntlet.loadEncrypted("ccs9_attack.des");
        defenseGauntlet.loadEncrypted("ccs9_defense.des");

        // Set up defensive decks
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-1));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-2));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-3));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-4));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-5));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-6));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-7));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-8));
        testGauntlet.add(defenseGauntlet.getDeckList().get(defenseGauntlet.size()-9));

        BGOptions options = new BGOptions();
        options.bgEffect = SkillType.ENDURINGRAGE;
        //options.bgX = 2;
        //options.isEnhance = false;
        options.isAttack = true;

        String bestStructures = "";
        double bestRate = 0;
        String secondBest = "";
        String thirdBest = "";

        // Find Optimial Self Attack
        for(Card siegeCard1 : cardHandler.getSiegeForts()) {
            for(Card siegeCard2 : cardHandler.getSiegeForts()) {
                //if(!siegeCard1.getName().equals("Inspiring Altar") || !siegeCard2.getName().equals("Inspiring Altar")) { continue; }
                options.playerSiege[0] = siegeCard1;
                options.playerSiege[1] = siegeCard2;
                double averageRate = 0;
                for(Deck testDeck : testGauntlet.getDeckList()) {
                    SimulatorV2 sim = new SimulatorV2(testDeck, attackGauntlet, options);
                    sim.runSimulation(1000);
                    averageRate += sim.getScore();
                }
                averageRate /= testGauntlet.size();
                if(averageRate > bestRate) {
                    thirdBest = secondBest;
                    secondBest = bestStructures;
                    bestStructures = siegeCard1.getName() + "," + siegeCard2.getName();
                    bestRate = averageRate;
                    System.out.println(bestStructures + " : " + bestRate);
                }
            }
        }

        System.out.println("FIRST: " + bestStructures + " SECOND: " + secondBest + " THIRD: " + thirdBest);
    }
}
