/*
 * This will test the basic CCS 3.0 regression for validation purposes.
 * CD Internal use only.
 */

package tyrantlib.test;

import tyrantlib.model.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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
import javax.xml.bind.DatatypeConverter;

import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SecurityTest {

    private static void copy(InputStream is, OutputStream os) throws IOException {
        int i;
        byte[] b = new byte[1024];
        while((i=is.read(b))!=-1) {
            os.write(b, 0, i);
        }
    }

    public static void main(String args[]) {

        Gauntlet gauntlet = new Gauntlet();

        //ArrayList<Deck> gauntlet = new ArrayList<Deck>();
        ArrayList<Deck> regression = new ArrayList<Deck>();
        HashMap<Card, ArrayList<Deck>> deckMap = new HashMap<Card, ArrayList<Deck>>();

        try {
            FileInputStream input = new FileInputStream(new File("dt_attack.csv"));
            FileOutputStream output = new FileOutputStream(new File("dt_attack.des"));

            String myEncryptionKey = "SpicyBanana";
            DESKeySpec dks = new DESKeySpec(myEncryptionKey.getBytes());
            SecretKey myDesKey = SecretKeyFactory.getInstance("DES").generateSecret(dks);
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
            CipherOutputStream cipheros = new CipherOutputStream(output, desCipher);

            copy(input, cipheros);
            cipheros.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileInputStream input = new FileInputStream(new File("dt_attack.des"));
            FileOutputStream output = new FileOutputStream(new File("decrypted.csv"));

            String myEncryptionKey = "SpicyBanana";
            DESKeySpec dks = new DESKeySpec(myEncryptionKey.getBytes());
            SecretKey myDesKey = SecretKeyFactory.getInstance("DES").generateSecret(dks);
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
            CipherInputStream cipheris = new CipherInputStream(input, desCipher);

            BufferedReader br = new BufferedReader(new InputStreamReader(cipheris));
            String line;
            while ((line = br.readLine()) != null) {
                String arr[] = line.trim().split(",", 2);
                if(arr.length == 2) {
                    System.out.println(line);
                    Deck deck = new Deck(arr[0], arr[1]);
                    gauntlet.add(deck);
                }
            }

            copy(cipheris, output);
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader("regression.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",", 2);
                Deck deck = new Deck(arr[0],arr[1]);
                regression.add(deck);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(Deck regdeck : regression) {
            Simulator simulator = new Simulator(regdeck, gauntlet, gauntlet, new BGOptions());
            simulator.runSimulation(200);
            if(simulator.getCCS() > 750) {
                System.out.println(regdeck);
                System.out.println(simulator.getAttackCCS() + "/" + simulator.getDefenseCCS() + " > " + simulator.getCCS());
            }
            System.out.println(simulator.getAttackCCS() + "/" + simulator.getDefenseCCS() + " > " + simulator.getCCS());
        }

        // Testing RSA Encryption
        /*
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            String modString = "2C8Q09vAQ8dWR9syQJryc0OFEFfmNKfI82JaLXP9asPp2J+qCR6FNTUuZwC6ocapf/pqnknoLw9m/OO2Or3w1s4rDfOskrm6T88jCUGeTHYP6BFewaj/wmoCedx+PStUX/caTjDkrLbse5oHsaUe7vRaClN2UtS9tbX/O8EAZzc=";
            String expString = "XIzhvmFw0VOQi5i6zc/IBjKcz99hrZ87N38eriDfGAshnNzV9at8Scgnwm8cd0/OlvyFEpj/bs5AP/nYtRNF/FZV2zpV6fZ6eNbZkO10WktlaN55BGD6w38WjdsfGkK8CcG6ITMzBqXqjax45NPZfql5ryrpVhj5WlNuVQbAXxE=";
            BigInteger modulus = new BigInteger(1, DatatypeConverter.parseBase64Binary(modString));
            BigInteger pExp = new BigInteger(1, DatatypeConverter.parseBase64Binary(expString));
            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(modulus, pExp);
            RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);

            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, key);

            System.out.println(modulus);
            System.out.println(pExp);

            byte[] encoded = DatatypeConverter.parseBase64Binary("ENABzJB1xqoC5OAbmXgTENg+YhyCOGWYXauU7HGFhIz8UUR/fVrKmCWyMOXK1vSJ/Ud/hREUsJuT+uAVyCNtoMpGps28+1eYmbdQLgrGWrjgdZSqCQX1W6G3tduP0nR8VZGrBtIq9e4F9grXXGcgmkWcSdVvAzszsqwlsl8tOhs=");
            byte[] utf8 = rsa.doFinal(encoded);

            String decrypted = new String(utf8, "utf8");
            System.out.println(decrypted);
            String[] split = decrypted.split("_");
            int expireTime = Integer.parseInt(split[1].trim());
            int secondsToExpiry = expireTime - (int)(System.currentTimeMillis()/1e3);
            System.out.println(secondsToExpiry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        LicenseValidator lv = new LicenseValidator();
        lv.validate();
    }
}