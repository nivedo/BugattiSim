package tyrantlib.model;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.util.ArrayList;
public class Gauntlet {

    ArrayList<Deck> deckList = new ArrayList<Deck>();

    public void setDeckList(ArrayList<Deck> deckList) { this.deckList = deckList; }
    public ArrayList<Deck> getDeckList() { return deckList; }
    public void add(Deck deck) { deckList.add(deck); }

    public void loadEncrypted(String file) {
        deckList.clear();
        try {
            FileInputStream input = new FileInputStream(new File(file));

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
                    deckList.add(deck);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void load(String file) {
        deckList.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",", 2);
                if (arr.length == 2) {
                    Deck deck = new Deck(arr[0], arr[1]);
                    deckList.add(deck);
                }
            }
            br.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String file) {
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for (Deck deck : deckList) {
                String deckString = deck.getCommander().toString();
                for (Card card : deck.getCards()) {
                    if (card != null) deckString += ("," + card.toString());
                }
                deckString += "\n";
                writer.println(deckString);
            }
            writer.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() { deckList.clear(); }
    public int size() { return deckList.size(); }
}
