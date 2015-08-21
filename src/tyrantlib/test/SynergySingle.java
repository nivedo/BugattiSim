/*
 * This will test the basic CCS 3.0 regression for validation purposes.
 * CD Internal use only.
 */

package tyrantlib.test;

import tyrantlib.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;

public class SynergySingle {

    public static void main(String args[]) {

        String card2 = "Regal Wharf";
        String cmd = "Daedalus";

        //ArrayList<Deck> gauntlet = new ArrayList<Deck>();
        Gauntlet attackGauntlet = new Gauntlet();
        Gauntlet defenseGauntlet = new Gauntlet();
        ArrayList<Simulator> regressionSingle = new ArrayList<Simulator>();

        HashMap<Card, ArrayList<Deck>> deckMap = new HashMap<Card, ArrayList<Deck>>();

        CardHandler handler = CardHandler.getInstance();
        ArrayList<BGOptions> optionsList = new ArrayList<BGOptions>();
        ArrayList<Double> weights = new ArrayList<Double>();

        BGOptions op0 = new BGOptions();
        Card siegeCard = handler.getCard("CDSiege0");
        op0.playerSiege[0] = siegeCard;
        op0.playerSiege[1] = siegeCard;
        op0.enemySiege[0] = siegeCard;
        op0.enemySiege[1] = siegeCard;

        optionsList.add(op0);
        weights.add(0.333);

        BGOptions op1 = new BGOptions();
        siegeCard = handler.getCard("CDSiege1");
        op1.playerSiege[0] = siegeCard;
        op1.playerSiege[1] = siegeCard;
        op1.enemySiege[0] = siegeCard;
        op1.enemySiege[1] = siegeCard;

        optionsList.add(op1);
        weights.add(0.333);

        BGOptions op2 = new BGOptions();
        siegeCard = handler.getCard("CDSiege2");
        op2.playerSiege[0] = siegeCard;
        op2.playerSiege[1] = siegeCard;
        op2.enemySiege[0] = siegeCard;
        op2.enemySiege[1] = siegeCard;

        optionsList.add(op2);
        weights.add(0.333);

        boolean useMultiOptions = false;
        Simulator singleSim = new Simulator(new Deck(cmd, "10 * " + card2), attackGauntlet, defenseGauntlet, new BGOptions());
        if (useMultiOptions) singleSim.setMultiOptions(optionsList, weights);

        try {
            attackGauntlet.loadEncrypted("ccs9_attack.des");
            defenseGauntlet.loadEncrypted("ccs9_defense.des");

            BGOptions options = new BGOptions();

            Map<String, CardWrapper> cardMap = CardHandler.getInstance().getEpicMap();
            for(Entry<String, CardWrapper> entry : cardMap.entrySet()) {
                CardWrapper wrapper = entry.getValue();
                Simulator sim1 = new Simulator(new Deck(cmd, "10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if (useMultiOptions) sim1.setMultiOptions(optionsList, weights);
                regressionSingle.add(sim1);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Testing RSA Encryption


        long startTime = System.currentTimeMillis();

        try {
            PrintWriter writer = new PrintWriter("ccs9-single.csv", "UTF-8");

            String rowString = ",,Card1,,Card2,,ABAB,,AABB,,BBAA,,";
            writer.println(rowString);

            for(int i = 0; i < regressionSingle.size(); i++) {
                regressionSingle.get(i).runSimulation(100);
                System.out.println("Processing Card: " + regressionSingle.get(i).getPlayerDeck().getCards()[0] + " " + regressionSingle.get(i).getAttackCCS());
            }
            singleSim.runSimulation(100);
            for(int i = 0; i < regressionSingle.size(); i++) {
                if(regressionSingle.get(i).getAttackCCS() > 100 || singleSim.getAttackCCS() > 100) {
                    String card1 = regressionSingle.get(i).getPlayerDeck().getCards()[0].getName();

                    Simulator sim2 = new Simulator(new Deck("Arkadios Ultra", card1 + "," + card2 + "," +
                            card1 + "," + card2 + "," +
                            card1 + "," + card2 + "," +
                            card1 + "," + card2 + "," +
                            card1 + "," + card2 + ","), attackGauntlet, defenseGauntlet, new BGOptions());
                    if (useMultiOptions) sim2.setMultiOptions(optionsList, weights);
                    sim2.runSimulation(100);

                    Simulator sim3 = new Simulator(new Deck("Arkadios Ultra", "5 * " + card1 + ", 5 * " + card2), attackGauntlet, defenseGauntlet, new BGOptions());
                    if (useMultiOptions) sim3.setMultiOptions(optionsList, weights);
                    sim3.runSimulation(100);

                    Simulator sim4 = new Simulator(new Deck("Arkadios Ultra", "5 * " + card2 + ", 5 * " + card1), attackGauntlet, defenseGauntlet, new BGOptions());
                    if (useMultiOptions) sim4.setMultiOptions(optionsList, weights);
                    sim4.runSimulation(100);

                    if((sim2.getAttackCCS() > (regressionSingle.get(i).getAttackCCS() + 50) && sim2.getAttackCCS() > (singleSim.getAttackCCS() + 50)) ||
                            (sim3.getAttackCCS() > (regressionSingle.get(i).getAttackCCS() + 50) && sim3.getAttackCCS() > (singleSim.getAttackCCS() + 50)) ||
                            (sim4.getAttackCCS() > (regressionSingle.get(i).getAttackCCS() + 50) && sim4.getAttackCCS() > (singleSim.getAttackCCS() + 50))) {
                        System.out.println("Card(" + i + ") of " + regressionSingle.size());

                        rowString = "";
                        rowString += regressionSingle.get(i).getPlayerDeck().getCards()[0] + ",";
                        rowString += singleSim.getPlayerDeck().getCards()[0] + ",";

                        rowString += regressionSingle.get(i).getAttackCCS() + ",";
                        rowString += regressionSingle.get(i).getAttackCCS() + " / ";
                        rowString += regressionSingle.get(i).getDefenseCCS() + ",";

                        rowString += singleSim.getAttackCCS() + ",";
                        rowString += singleSim.getAttackCCS() + " / ";
                        rowString += singleSim.getDefenseCCS() + ",";

                        rowString += sim2.getAttackCCS() + ",";
                        rowString += sim2.getAttackCCS() + " / ";
                        rowString += sim2.getDefenseCCS() + ",";

                        rowString += sim3.getAttackCCS() + ",";
                        rowString += sim3.getAttackCCS() + " / ";
                        rowString += sim3.getDefenseCCS() + ",";

                        rowString += sim4.getAttackCCS() + ",";
                        rowString += sim4.getAttackCCS() + " / ";
                        rowString += sim4.getDefenseCCS() + ",";

                        System.out.println(rowString);
                        writer.println(rowString);
                    }
                }
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