/*
 * Handles all XML parsing with the most recent TU XML File.
 * XML should only be parsed once and CardHandler should
 * always be called through getInstance().
 */

package tyrantlib.model;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.lang.RuntimeException;
import java.net.UnknownHostException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CardHandler extends DefaultHandler{

    // Set type for CD custom cards
    static final int CD_CUSTOM = 5555;

    // Map for retrieving cards by skill type
    private ArrayList<ArrayList<CardSkillPair>> cardsBySkill =
            new ArrayList<ArrayList<CardSkillPair>>();

    // Fortresses
    private ArrayList<Card> defenseForts = new ArrayList<Card>();
    private ArrayList<Card> siegeForts = new ArrayList<Card>();

    public ArrayList<Card> getDefenseForts() { return defenseForts; }
    public ArrayList<Card> getSiegeForts() { return siegeForts; }

    // Get instance to search for cards
    public static CardHandler instance = null;
    public static CardHandler getInstance() {
        if(instance == null) {
            instance = new CardHandler();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);

            try {
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse("http://mobile-dev.tyrantonline.com/assets/cards.xml", instance);
            }
            catch(Exception e) {
                System.out.println("Internet connection timed out.  Defaulting to local...");
                e.printStackTrace();
            }

            // Default to local file
            if(instance.cardMap.isEmpty()) {
                try {
                    SAXParser saxParser = factory.newSAXParser();
                    saxParser.parse("cards.xml", instance);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                SAXParser saxParser2 = factory.newSAXParser();
                saxParser2.parse("extras.xml", instance);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public CardHandler() {
        for(SkillType s : SkillType.values()) { cardsBySkill.add(new ArrayList<CardSkillPair>()); }
    }

    // Map of Card Name to Card
    private Map<String, CardWrapper> cardMap = new HashMap<String, CardWrapper>();
    private Map<String, CardWrapper> epicMap = new HashMap<String, CardWrapper>();
    private Card card = null;
    private CardWrapper cardWrapper = null;

    public Map<String, CardWrapper> getCardMap() {
        return cardMap;
    }
    public Map<String, CardWrapper> getEpicMap() {
        return epicMap;
    }

    public CardWrapper getWrapper(String name) {
        CardWrapper wrapper = cardMap.get(name.toLowerCase());
        if(wrapper== null) {
            throw new RuntimeException("Cannot find card: " + name);
        }
        return wrapper;
    }

    // Get Specific Level
    public Card getCard(String name, int level) {
        CardWrapper wrapper = getWrapper(name);
        return wrapper.getLevel(level);
    }

    // Get Max Level
    public Card getCard(String name) {
        CardWrapper wrapper = getWrapper(name);
        return wrapper.getLevel(wrapper.getNumLevels());
    }

    // Get all cards with corresponding skill
    public ArrayList<CardSkillPair> getCardsBySkill(SkillType type) {
        return cardsBySkill.get(type.ordinal());
    }

    String activeName;
    int stackLevel = 0;
    int skillNum = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        activeName = "";

        if (qName.equalsIgnoreCase("unit")) {
            cardWrapper = new CardWrapper();
            card = new Card();
            skillNum = 0;
            cardWrapper.setLevel(1, card);
            stackLevel++;
        }
        else if (qName.equalsIgnoreCase("upgrade")) {
            card = new Card(cardWrapper.getLevel(cardWrapper.getNumLevels()));
            skillNum = 0;
        }
        else if (qName.equalsIgnoreCase("skill")) {
            if(skillNum < 3) {
                Skill skill = new Skill();
                skill.setId(SkillType.stringToSkillType(attributes.getValue("id")));

                if (attributes.getValue("x") != null) skill.x = Integer.parseInt(attributes.getValue("x"));
                if (attributes.getValue("y") != null)
                    skill.y = Faction.values()[Integer.parseInt(attributes.getValue("y")) - 1];
                if (attributes.getValue("s") != null) skill.s = SkillType.stringToSkillType(attributes.getValue("s"));
                if (attributes.getValue("c") != null) skill.c = Integer.parseInt(attributes.getValue("c"));
                if (attributes.getValue("n") != null) skill.n = Integer.parseInt(attributes.getValue("n"));
                if (attributes.getValue("all") != null) skill.all = Integer.parseInt(attributes.getValue("all")) != 0;

                CardSkillPair pair = new CardSkillPair(card, skill);
                cardsBySkill.get(skill.id.ordinal()).add(pair);

                card.setSkill(skill, skillNum++);
            } else {
                // this isn't supported yet
            }
        }
        else { activeName = qName; }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("unit")) {
            if(cardWrapper.getSet() != 9999 && cardWrapper.getSet() != 0 && !cardWrapper.getFail()) {
                cardMap.put(cardWrapper.getName().toLowerCase(), cardWrapper);
                if(cardWrapper.isValidInDeck() && cardWrapper.getFusion() == 2 && cardWrapper.getRarity().ordinal() >= Rarity.EPIC.ordinal()) {
                    epicMap.put(cardWrapper.getName().toLowerCase(), cardWrapper);
                }
                if(cardWrapper.getFortress()==1 && cardWrapper.getSet() != CD_CUSTOM) {
                    defenseForts.add(cardWrapper.getLevel(cardWrapper.getNumLevels()));
                }
                if(cardWrapper.getFortress()==2 && cardWrapper.getSet() != CD_CUSTOM) {
                    siegeForts.add(cardWrapper.getLevel(cardWrapper.getNumLevels()));
                }
                if(cardWrapper.getFortress()==3 && cardWrapper.getSet() != CD_CUSTOM) {
                    siegeForts.add(cardWrapper.getLevel(cardWrapper.getNumLevels()));
                    defenseForts.add(cardWrapper.getLevel(cardWrapper.getNumLevels()));
                }
            }
            stackLevel--;
        }
        if (qName.equalsIgnoreCase("upgrade")) {
            cardWrapper.setLevel(card.getLevel(), card);
            cardWrapper.setNumLevels(card.getLevel());
            //if(card.getLevel() == 6 && cardWrapper.getFusion() == 2 && card.getSkill(0) == null && card.getSkill(1) == null && card.getSkill(2) == null) { System.out.println(card.getName()); }
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if(stackLevel == 1) {

            String strval = new String(ch, start, length);
            strval = strval.trim();

            try {
                if (activeName.equals("id")) {
                    card.setId(Integer.parseInt(strval));
                } else if (activeName.equals("card_id")) {
                    card.setId(Integer.parseInt(strval));
                } else if (activeName.equals("name")) {
                    cardWrapper.setName(strval);
                    card.setName(strval);
                    //System.out.println(strval);
                } else if (activeName.equals("level")) {
                    card.setLevel(Integer.parseInt(strval));
                } else if (activeName.equals("attack")) {
                    card.setAttack(Integer.parseInt(strval));
                } else if (activeName.equals("health")) {
                    card.setHealth(Integer.parseInt(strval));
                } else if (activeName.equals("cost")) {
                    card.setWait(Integer.parseInt(strval));
                } else if (activeName.equals("rarity")) {
                    Rarity r = Rarity.values()[Integer.parseInt(strval) - 1];
                    cardWrapper.setRarity(r);
                    card.setRarity(r);
                } else if (activeName.equals("type")) {
                    Faction f = Faction.values()[Integer.parseInt(strval) - 1];
                    cardWrapper.setType(f);
                    card.setType(f);
                } else if (activeName.equals("set")) {
                    if (strval.isEmpty()) {
                        strval = "2000";
                    }
                    cardWrapper.setSet(Integer.parseInt(strval));
                    card.setSet(Integer.parseInt(strval));
                } else if (activeName.equals("fusion_level")) {
                    cardWrapper.setFusion(Integer.parseInt(strval));
                    card.setFusion(Integer.parseInt(strval));
                } else if (activeName.equals("fortress_type")) {
                    cardWrapper.setFortress(Integer.parseInt(strval));
                    card.setFortress(Integer.parseInt(strval));
                } else if (activeName.equals("asset_bundle")) {
                    int assetBundle = Integer.parseInt(strval);
                    if(assetBundle == 2501) {
                        cardWrapper.setFortress(3);
                        card.setFortress(3);
                    }
                }


                activeName = "";
            } catch (Exception e) {
                card.setFail(true);
            }
        }
    }
}