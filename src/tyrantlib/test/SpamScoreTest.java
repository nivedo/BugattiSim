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

public class SpamScoreTest {

    public static void main(String args[]) {

        //ArrayList<Deck> gauntlet = new ArrayList<Deck>();
        Gauntlet attackGauntlet = new Gauntlet();
        Gauntlet defenseGauntlet = new Gauntlet();
        ArrayList<Simulator> regression1 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression2 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression3 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression4 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression5 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression6 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression7 = new ArrayList<Simulator>();
        ArrayList<Simulator> regression8 = new ArrayList<Simulator>();

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

        try {
            /*
            FileInputStream input = new FileInputStream(new File("ccs6.des"));

            String myEncryptionKey = "CrazyAwesome";
            DESKeySpec dks = new DESKeySpec(myEncryptionKey.getBytes());
            SecretKey myDesKey = SecretKeyFactory.getInstance("DES").generateSecret(dks);
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
            CipherInputStream cipheris = new CipherInputStream(input, desCipher);

            BufferedReader br = new BufferedReader(new InputStreamReader(cipheris));
            String line;
            while ((line = br.readLine()) != null) {
                String arr[] = line.trim().split(",", 2);
                if (arr.length == 2) {
                    Deck deck = new Deck(arr[0], arr[1]);
                    gauntlet.add(deck);
                }
            }
            */

            attackGauntlet.loadEncrypted("ccs7_attack.des");
            defenseGauntlet.loadEncrypted("ccs7_defense.des");

            BGOptions options = new BGOptions();
            options.bgEffect = SkillType.RALLY;
            options.isEnhance = false;
            options.isBrawlMode = true;
            options.surge = true;
            options.bgX = 2;

            Map<String, CardWrapper> cardMap = CardHandler.getInstance().getEpicMap();
            for(Entry<String, CardWrapper> entry : cardMap.entrySet()) {
                CardWrapper wrapper = entry.getValue();
                if(wrapper.getName().equals("Trench Legion") || wrapper.getName().equals("Trench Hurler")) System.out.println(wrapper.getLevel(6).toFullString());
                Simulator sim1 = new Simulator(new Deck("Barracus","10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim1.setMultiOptions(optionsList, weights);
                regression1.add(sim1);
                Simulator sim2 = new Simulator(new Deck("Constantine", "10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim2.setMultiOptions(optionsList, weights);
                regression2.add(sim2);
                Simulator sim3 = new Simulator(new Deck("Nexor","10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim3.setMultiOptions(optionsList, weights);
                regression3.add(sim3);
                Simulator sim4 = new Simulator(new Deck("Halcyon", "10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim4.setMultiOptions(optionsList, weights);
                regression4.add(sim4);
                Simulator sim5 = new Simulator(new Deck("Brood Mother","10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim5.setMultiOptions(optionsList, weights);
                regression5.add(sim5);
                Simulator sim6 = new Simulator(new Deck("Dracorex", "10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim6.setMultiOptions(optionsList, weights);
                regression6.add(sim6);
                Simulator sim7 = new Simulator(new Deck("Typhon Vex", "10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim7.setMultiOptions(optionsList, weights);
                regression7.add(sim7);
                Simulator sim8 = new Simulator(new Deck("Krellus", "10 * " + wrapper.getName()), attackGauntlet, defenseGauntlet, options);
                if(useMultiOptions) sim8.setMultiOptions(optionsList, weights);
                regression8.add(sim8);
            }


        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Testing RSA Encryption


        long startTime = System.currentTimeMillis();

        try {
            PrintWriter writer = new PrintWriter("ccs7rally2.csv", "UTF-8");

            String rowString = ",Barracus,,Constantine,,Nexor,,Halcyon,,Brood Mother,,Dracorex,,Typhon Vex,,Krellus,,";
            writer.println(rowString);

            for(int i = 0; i < regression1.size(); i++) {
                regression1.get(i).runSimulation(100);
                regression2.get(i).runSimulation(100);
                regression3.get(i).runSimulation(100);
                regression4.get(i).runSimulation(100);
                regression5.get(i).runSimulation(100);
                regression6.get(i).runSimulation(100);
                regression7.get(i).runSimulation(100);
                regression8.get(i).runSimulation(100);

                rowString = "";
                rowString += regression1.get(i).getPlayerDeck().getCards()[0] + ",";

                rowString += regression1.get(i).getAttackCCS() + ",";
                rowString += regression1.get(i).getAttackCCS() + " / ";
                rowString += regression1.get(i).getDefenseCCS() + ",";

                rowString += regression2.get(i).getAttackCCS() + ",";
                rowString += regression2.get(i).getAttackCCS() + " / ";
                rowString += regression2.get(i).getDefenseCCS() + ",";

                rowString += regression3.get(i).getAttackCCS() + ",";
                rowString += regression3.get(i).getAttackCCS() + " / ";
                rowString += regression3.get(i).getDefenseCCS() + ",";

                rowString += regression4.get(i).getAttackCCS() + ",";
                rowString += regression4.get(i).getAttackCCS() + " / ";
                rowString += regression4.get(i).getDefenseCCS() + ",";

                rowString += regression5.get(i).getAttackCCS() + ",";
                rowString += regression5.get(i).getAttackCCS() + " / ";
                rowString += regression5.get(i).getDefenseCCS() + ",";

                rowString += regression6.get(i).getAttackCCS() + ",";
                rowString += regression6.get(i).getAttackCCS() + " / ";
                rowString += regression6.get(i).getDefenseCCS() + ",";

                rowString += regression7.get(i).getAttackCCS() + ",";
                rowString += regression7.get(i).getAttackCCS() + " / ";
                rowString += regression7.get(i).getDefenseCCS() + ",";

                rowString += regression8.get(i).getAttackCCS() + ",";
                rowString += regression8.get(i).getAttackCCS() + " / ";
                rowString += regression8.get(i).getDefenseCCS() + ",";

                System.out.println(rowString);

                writer.println(rowString);
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