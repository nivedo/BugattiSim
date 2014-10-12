package tyrantlib.controller;

import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.concurrent.Task;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

import tyrantlib.model.*;
import tyrantlib.view.*;

import org.controlsfx.dialog.Dialogs;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jay on 9/9/2014.
 */
public class BugattiController {

    public static final String VERSION = "6.0.1";

    public Gauntlet gauntlet = new Gauntlet();
    public Gauntlet gwGauntlet = new Gauntlet();

    public ArrayList<String> eventNames = new ArrayList<String>();
    public ArrayList<Gauntlet> eventGauntlets = new ArrayList<Gauntlet>();

    public ArrayList<Card> benchCards = new ArrayList<Card>();

    private boolean useMultiOptions = false;
    private ArrayList<BGOptions> optionsList = new ArrayList<BGOptions>();
    private ArrayList<Double> weights = new ArrayList<Double>();

    @FXML
    private TextArea deckEntryArea;
    @FXML
    private TextArea benchArea;

    @FXML
    private TableView<Simulator> deckTable;
    @FXML
    private TableColumn<Simulator, String> deckColumn;
    @FXML
    private TableColumn<Simulator, Number> avgColumn;
    @FXML
    private TableColumn<Simulator, Number> atkColumn;
    @FXML
    private TableColumn<Simulator, Number> defColumn;

    @FXML
    private VBox deckButtonPane;
    @FXML
    private VBox deckEntryPane;
    @FXML
    private VBox simulationPane;
    @FXML
    private VBox optimizePane;

    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button runButton;
    @FXML
    private Button optimizeButton;

    @FXML
    private ComboBox modeBox;
    @FXML
    private ComboBox bgEffectBox;

    @FXML
    private CheckBox enableFortCheck;
    @FXML
    private AnchorPane fortressPane;
    @FXML
    private ComboBox defenseBox0;
    @FXML
    private ComboBox defenseBox1;
    @FXML
    private ComboBox siegeBox0;
    @FXML
    private ComboBox siegeBox1;
    @FXML
    private ComboBox enemyDefenseBox0;
    @FXML
    private ComboBox enemyDefenseBox1;
    @FXML
    private ComboBox enemySiegeBox0;
    @FXML
    private ComboBox enemySiegeBox1;

    @FXML
    private RadioButton optOrderRadio;
    @FXML
    private RadioButton optDefenseRadio;

    private Main mainApp;

    public BugattiController() {}

    @FXML
    private void initialize() {
        loadData();
        initializeTable();
        initializeButtons();
        initializeComboBoxes();
        initializeRadio();
    }

    private void showError(String masthead, String message) {
        Dialogs.create().owner(mainApp.getPrimaryStage()).title("Error!").masthead(masthead).message(message).lightweight().showError();
    }

    private void setControlDisable(boolean disabled) {
        deckEntryPane.setDisable(disabled);
        deckButtonPane.setDisable(disabled);
        simulationPane.setDisable(disabled);
        optimizePane.setDisable(disabled);
    }

    private void initializeTable() {
        // Set Table Widths
        deckColumn.prefWidthProperty().bind(deckTable.widthProperty().subtract(272)); // HACK: 1 px to prevent horz scroll
        avgColumn.prefWidthProperty().bind(new SimpleDoubleProperty(90));
        atkColumn.prefWidthProperty().bind(new SimpleDoubleProperty(90));
        defColumn.prefWidthProperty().bind(new SimpleDoubleProperty(90));

        deckColumn.setCellValueFactory(new Callback<CellDataFeatures<Simulator, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Simulator, String> p) {
                return p.getValue().getDeckStringProperty();
            }
        });
        avgColumn.setCellValueFactory(new Callback<CellDataFeatures<Simulator, Number>, ObservableValue<Number>>() {
            public ObservableValue<Number> call(CellDataFeatures<Simulator, Number> p) {
                return p.getValue().getAvgScoreProperty();
            }
        });
        atkColumn.setCellValueFactory(new Callback<CellDataFeatures<Simulator, Number>, ObservableValue<Number>>() {
            public ObservableValue<Number> call(CellDataFeatures<Simulator, Number> p) {
                return p.getValue().getAtkScoreProperty();
            }
        });
        defColumn.setCellValueFactory(new Callback<CellDataFeatures<Simulator, Number>, ObservableValue<Number>>() {
            public ObservableValue<Number> call(CellDataFeatures<Simulator, Number> p) {
                return p.getValue().getDefScoreProperty();
            }
        });

        deckTable.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                int index = deckTable.getSelectionModel().getSelectedIndex();
                if(index >= 0) {
                    Simulator selected = deckTable.getSelectionModel().getSelectedItem();
                    deckEntryArea.setText(selected.getPlayerDeck().toCopyString());
                }
            }
        });
    }

    private void initializeButtons() {
        addButton.setOnAction((event) -> {
            String deckString = deckEntryArea.getText();
            //String[] split = deckString.split("\\|");
            String[] split = deckString.split("\n",2);

            if(split.length == 2) {
                String commander = split[0].trim();
                String deckCards = split[1].replace('\n',',').trim();
                try {
                    Deck newDeck = new Deck(commander, deckCards);
                    mainApp.getObsDeckList().add(new Simulator(newDeck,gauntlet.getDeckList(),new BGOptions()));
                    deckEntryArea.setText("");
                }
                catch (RuntimeException e) {
                    showError("Deck Construction Failed",e.getMessage());
                }
            } else {
                showError("Not a valid deck!", "Check your deck entry for errors.");
            }
        });

        removeButton.setOnAction((event) -> {
            int index = deckTable.getSelectionModel().getSelectedIndex();
            if(index >= 0) {
                mainApp.getObsDeckList().remove(index);
            }
        });

        loadButton.setOnAction((event) -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fc.getExtensionFilters().add(extFilter);

            File file = fc.showOpenDialog(mainApp.getPrimaryStage());
            if (file != null) {
                Gauntlet dl = new Gauntlet();
                dl.load(file.getAbsolutePath());
                mainApp.getObsDeckList().clear();
                for(Deck deck : dl.getDeckList()) {
                    mainApp.getObsDeckList().add(new Simulator(deck, gauntlet.getDeckList(), new BGOptions()));
                }
            }
        });

        saveButton.setOnAction((event) -> {
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fc.getExtensionFilters().add(extFilter);

            File file = fc.showSaveDialog(mainApp.getPrimaryStage());
            if (file != null) {
                try {
                    PrintWriter writer = new PrintWriter(file);
                    for (Simulator sim : mainApp.getObsDeckList()) {
                        Deck deck = sim.getPlayerDeck();
                        String deckString = deck.getCommander().toString();
                        for(Card card : deck.getCards()) {
                            if(card != null) deckString += ("," + card.toString());
                        }
                        deckString += "\n";
                        writer.println(deckString);
                    }
                    writer.close();
                } catch (Exception e) {
                    showError("Save Failed",e.getMessage());
                }
            }
        });

        clearButton.setOnAction((event) -> {
            mainApp.getObsDeckList().clear();
        });

        runButton.setOnAction((event) -> {
            Gauntlet activeGauntlet = getActiveGauntlet();
            BGOptions options = getBGOptions();
            setMultiOptions();

            setControlDisable(true);

            Task task = new Task() {
                @Override public Void call() {
                    int numRuns = getNumRuns(activeGauntlet.getDeckList().size());

                    for (Simulator sim : mainApp.getObsDeckList()) {
                        sim.setGauntlet(activeGauntlet.getDeckList());
                        sim.setOptions(options);
                        if(useMultiOptions) {
                            sim.setMultiOptions(optionsList, weights);
                        } else {
                            sim.clearMultiOptions();
                        }
                        sim.runSimulation(numRuns);
                    }

                    return null;
                }
            };

            task.setOnSucceeded((taskEvent) -> {
                deckTable.getColumns().get(0).setVisible(false);
                deckTable.getColumns().get(0).setVisible(true);
                setControlDisable(false);
            });

            task.setOnCancelled((taskEvent) -> {
                setControlDisable(false);
            });

            task.setOnFailed((taskEvent) -> {
                setControlDisable(false);
            });

            new Thread(task).start();
        });

        optimizeButton.setOnAction((event) -> {
            int index = deckTable.getSelectionModel().getSelectedIndex();
            if(index >= 0) {
                Deck selectedDeck = mainApp.getObsDeckList().get(index).getPlayerDeck();
                Gauntlet activeGauntlet = getActiveGauntlet();
                BGOptions options = getBGOptions();
                BGOptions simOptions = getBGOptions();
                setMultiOptions();

                boolean isOptOrder = optOrderRadio.isSelected();
                String modeStr = (String) modeBox.getSelectionModel().getSelectedItem();

                if(!benchArea.isDisabled()) {
                    if(!checkBench()) { return; }
                }

                setControlDisable(true);

                Task task = new Task<Simulator>() {
                    @Override public Simulator call() {
                        DeckOptimizer optimizer = new DeckOptimizer(selectedDeck, activeGauntlet.getDeckList(), options);
                        if(useMultiOptions) optimizer.setMultiOptions(optionsList, weights);

                        Deck optimizedDeck;
                        setBench();
                        for (Card card : benchCards) {
                            optimizer.addCardToBench(card);
                        }
                        optimizedDeck = optimizer.optimizeClimb(isOptOrder, getNumRuns(activeGauntlet.getDeckList().size()));

                        /*
                        if(isOptOrder) {
                            if(modeStr.toLowerCase().contains("event")) {
                                optimizedDeck = optimizer.optimizeEvent();
                            } else {
                                optimizedDeck = optimizer.optimizeClimb(true, getNumRuns(activeGauntlet.getDeckList().size()));
                            }
                        }
                        else {
                            optimizedDeck = optimizer.optimizeClimb(false, getNumRuns(activeGauntlet.getDeckList().size()));
                        }
                        */

                        // Add new deck to deck list
                        Simulator sim = new Simulator(optimizedDeck,activeGauntlet.getDeckList(),simOptions);
                        if(useMultiOptions) {
                            sim.setMultiOptions(optionsList, weights);
                        } else {
                            sim.clearMultiOptions();
                        }

                        int numRuns = getNumRuns(activeGauntlet.getDeckList().size());
                        sim.runSimulation(numRuns);

                        return sim;
                    }
                };

                task.setOnSucceeded((taskEvent) -> {
                    WorkerStateEvent t = (WorkerStateEvent)taskEvent;
                    mainApp.getObsDeckList().add((Simulator)t.getSource().getValue());
                    setControlDisable(false);
                });

                task.setOnCancelled((taskEvent) -> {
                    setControlDisable(false);
                });

                task.setOnFailed((taskEvent) -> {
                    setControlDisable(false);
                });

                new Thread(task).start();
            }
        });
    }

    private void initializeComboBoxes() {
        // Initialize Mode Box
        modeBox.getItems().addAll("CCS 6.0","Brawl Mode","Guild War");
        modeBox.getItems().addAll(eventNames);
        modeBox.getSelectionModel().select(0);

        // Initialize BG Effects
        ArrayList<String> skillTypes = new ArrayList<String>();
        String skillName = "";
        for(SkillType stype : SkillType.values()) {
            if(SkillType.isEnhancableSkill(stype)) {
                skillName = stype.name();
                for(int i = 1; i <= 3; i++) {
                    skillTypes.add("Enhance All " + skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase() + " " + i);
                }
            }
            if(stype == SkillType.PROGENITOR || stype == SkillType.REAPING || SkillType.isActiveSkill(stype) && stype != SkillType.ENHANCE) {
                switch(stype) {
                    case PROGENITOR:
                        skillName = stype.name();
                        skillTypes.add(skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase() + " 1");
                        break;
                    case OVERLOAD:
                        skillName = stype.name();
                        for (int i = 1; i <= 3; i++) {
                            skillTypes.add(skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase() + " " + i);
                        }
                        break;
                    case REAPING:
                        skillName = stype.name();
                        for (int i = 1; i <= 3; i++) {
                            skillTypes.add(skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase() + " " + i);
                        }
                        break;
                    default:
                        skillName = stype.name();
                        for (int i = 1; i <= 3; i++) {
                            skillTypes.add(skillName.substring(0, 1).toUpperCase() + skillName.substring(1).toLowerCase() + " All " + i);
                        }
                        break;
                }
            }
        }
        Collections.sort(skillTypes);
        skillTypes.add(0, "None");
        bgEffectBox.getItems().addAll(skillTypes);
        bgEffectBox.getSelectionModel().select(0);
        bgEffectBox.setDisable(true);

        modeBox.setOnAction((event) -> {
            String modeString = (String) modeBox.getSelectionModel().getSelectedItem();
            if(modeString.toLowerCase().contains("event")) {
                optOrderRadio.setSelected(true);
                optDefenseRadio.setDisable(true);
                //benchArea.setDisable(false);
                enableFortCheck.setDisable(false);
            } else {
                if(!modeString.toLowerCase().contains("guild war")) {
                    enableFortCheck.setDisable(true);
                } else {
                    enableFortCheck.setDisable(false);
                }
                optDefenseRadio.setDisable(false);
                //if(optOrderRadio.isSelected()) benchArea.setDisable(true);
            }
            if(modeString.toLowerCase().contains("ccs")) {
                bgEffectBox.setDisable(true);
            } else {
                bgEffectBox.setDisable(false);
            }
        });

        ArrayList<String> defFortStrings = new ArrayList<String>();
        defFortStrings.add("None");
        for(Card c : CardHandler.getInstance().getDefenseForts()) {
            defFortStrings.add(c.getName());
        }
        defenseBox0.getItems().addAll(defFortStrings);
        defenseBox1.getItems().addAll(defFortStrings);
        enemyDefenseBox0.getItems().addAll(defFortStrings);
        enemyDefenseBox1.getItems().addAll(defFortStrings);
        defenseBox0.getSelectionModel().select(0);
        defenseBox1.getSelectionModel().select(0);
        enemyDefenseBox0.getSelectionModel().select(0);
        enemyDefenseBox1.getSelectionModel().select(0);

        ArrayList<String> siegeFortStrings = new ArrayList<String>();
        siegeFortStrings.add("None");
        for(Card c : CardHandler.getInstance().getSiegeForts()) {
            siegeFortStrings.add(c.getName());
        }
        siegeBox0.getItems().addAll(siegeFortStrings);
        siegeBox1.getItems().addAll(siegeFortStrings);
        enemySiegeBox0.getItems().addAll(siegeFortStrings);
        enemySiegeBox1.getItems().addAll(siegeFortStrings);
        siegeBox0.getSelectionModel().select(0);
        siegeBox1.getSelectionModel().select(0);
        enemySiegeBox0.getSelectionModel().select(0);
        enemySiegeBox1.getSelectionModel().select(0);
    }

    public void initializeRadio() {
        optOrderRadio.setSelected(true);
        optOrderRadio.setOnAction((event) -> {
            String modeString = (String) modeBox.getSelectionModel().getSelectedItem();
            //if(!modeString.toLowerCase().contains("event")) { benchArea.setDisable(true); }
        });
        /*
        optDefenseRadio.setOnAction((event) -> {
            benchArea.setDisable(false);
        });
        */

        enableFortCheck.setSelected(false);
        enableFortCheck.setDisable(true);
        setEnableFort();
        enableFortCheck.setOnAction((event) -> {
            setEnableFort();
        });
    }

    public void loadData() {
        CardHandler handler = CardHandler.getInstance();

        try {
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
                    gauntlet.getDeckList().add(deck);
                }
            }

            FileInputStream input2 = new FileInputStream(new File("ccs6.des"));
            desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
            cipheris = new CipherInputStream(input2, desCipher);

            br = new BufferedReader(new InputStreamReader(cipheris));
            while ((line = br.readLine()) != null) {
                String arr[] = line.trim().split(",", 2);
                if (arr.length == 2) {
                    Deck deck = new Deck(arr[0], arr[1]);
                    gwGauntlet.getDeckList().add(deck);
                }
            }

            BufferedReader br2 = new BufferedReader(new FileReader("events.decklist"));
            line = "";
            while ((line = br2.readLine()) != null) {
                String split[] = line.split("\\|");
                if(split.length == 2) {
                    eventNames.add("Event - " + split[0]);
                    String arr[] = split[1].split(",", 2);
                    if (arr.length == 2) {
                        Deck deck = new Deck(arr[0], arr[1], true);
                        Gauntlet g = new Gauntlet();
                        g.getDeckList().add(deck);
                        eventGauntlets.add(g);
                    }
                }
            }
            br.close();
        }
        catch(Exception e) {
            showError("Data Load Failed",e.getMessage());
        }
    }

    public Gauntlet getActiveGauntlet() {
        String selectedStr = (String) modeBox.getSelectionModel().getSelectedItem();

        if (selectedStr.toLowerCase().contains("ccs") || selectedStr.toLowerCase().contains("guild war")) {
            return gwGauntlet;
        }
        else if (modeBox.getSelectionModel().getSelectedIndex() >= 3) {
            return eventGauntlets.get(modeBox.getSelectionModel().getSelectedIndex() - 3);
        }

        return gauntlet;
    }

    // Set BG Options based on simulation parameters
    public BGOptions getBGOptions() {
        BGOptions options = new BGOptions();

        if(!bgEffectBox.isDisabled()) {
            String bgEffectString = (String) bgEffectBox.getSelectionModel().getSelectedItem();
            if (!bgEffectString.isEmpty() && !bgEffectString.equals("None")) {
                if(bgEffectString.toLowerCase().contains("enhance")) {
                    options.isEnhance = true;
                }
                bgEffectString = bgEffectString.replace("Enhance", "");
                bgEffectString = bgEffectString.replace("All ", "");
                String[] bgSplit = bgEffectString.trim().split(" ");

                if (bgSplit.length == 2) {
                    options.bgEffect = SkillType.stringToSkillType(bgSplit[0].trim());
                    options.bgX = Integer.parseInt(bgSplit[1].trim());
                }

                if (options.bgEffect == SkillType.UNKNOWN) {
                    System.err.println( bgEffectString + " is not a valid BGE!" );
                }
            }
        }

        if(!enableFortCheck.isDisabled() && enableFortCheck.isSelected()) {
            CardHandler handler = CardHandler.getInstance();

            if (defenseBox0.getSelectionModel().getSelectedIndex() != 0) {
                options.playerDefense[0] = handler.getCard((String) defenseBox0.getSelectionModel().getSelectedItem());
            }
            if (defenseBox1.getSelectionModel().getSelectedIndex() != 0) {
                options.playerDefense[1] = handler.getCard((String) defenseBox1.getSelectionModel().getSelectedItem());
            }

            if (siegeBox0.getSelectionModel().getSelectedIndex() != 0) {
                options.playerSiege[0] = handler.getCard((String) siegeBox0.getSelectionModel().getSelectedItem());
            }
            if (siegeBox1.getSelectionModel().getSelectedIndex() != 0) {
                options.playerSiege[1] = handler.getCard((String) siegeBox1.getSelectionModel().getSelectedItem());
            }

            if (enemyDefenseBox0.getSelectionModel().getSelectedIndex() != 0) {
                options.enemyDefense[0] = handler.getCard((String) enemyDefenseBox0.getSelectionModel().getSelectedItem());
            }
            if (enemyDefenseBox1.getSelectionModel().getSelectedIndex() != 0) {
                options.enemyDefense[1] = handler.getCard((String) enemyDefenseBox1.getSelectionModel().getSelectedItem());
            }

            if (enemySiegeBox0.getSelectionModel().getSelectedIndex() != 0) {
                options.enemySiege[0] = handler.getCard((String) enemySiegeBox0.getSelectionModel().getSelectedItem());
            }
            if (enemySiegeBox1.getSelectionModel().getSelectedIndex() != 0) {
                options.enemySiege[1] = handler.getCard((String) enemySiegeBox1.getSelectionModel().getSelectedItem());
            }
        }

        String selectedStr = (String) modeBox.getSelectionModel().getSelectedItem();
        if(selectedStr.toLowerCase().contains("event")) {
            options.surge = false;
        }
        if(selectedStr.toLowerCase().contains("ccsd")) {
            options.isAttack = false;
        }
        if(selectedStr.toLowerCase().contains("brawl")) {
            options.isBrawlMode = true;
        }

        return options;
    }

    public int getNumRuns(int gSize) {
        int numRuns = 30000 / gSize / (useMultiOptions ? weights.size() : 1);
        if(numRuns > 1000) numRuns = 1000;

        return numRuns;
    }

    public void setMultiOptions() {
        String selectedStr = (String) modeBox.getSelectionModel().getSelectedItem();
        optionsList.clear();
        weights.clear();
        useMultiOptions = false;

        if(selectedStr.toLowerCase().contains("ccs")) {
            CardHandler handler = CardHandler.getInstance();
            useMultiOptions = true;

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
        }
    }

    public void setBench() {
        benchCards.clear();
        String benchString = benchArea.getText();
        String[] split = benchString.split("\n");

        for (String cardName : split) {
            if (!cardName.isEmpty()) {
                cardName = cardName.trim().toLowerCase();
                int cardNum = 1;
                int cardLevel = 0;

                if (cardName.contains("*")) {
                    String[] cardSplit = cardName.split("\\*");
                    cardNum = Integer.parseInt(cardSplit[0].trim());
                    cardName = cardSplit[1].trim();
                }

                if(cardName.contains("lv.")) {
                    String[] cardSplit = cardName.split("lv\\.");
                    cardName = cardSplit[0].trim();
                    cardLevel = Integer.parseInt(cardSplit[1].trim());
                }

                for (int i = 0; i < cardNum; i++) {
                    if(cardLevel > 0) {
                        benchCards.add(CardHandler.getInstance().getCard(cardName,cardLevel));
                    } else {
                        benchCards.add(CardHandler.getInstance().getCard(cardName));
                    }
                }
            }
        }
    }

    public boolean checkBench() {
        String benchString = benchArea.getText();
        String[] split = benchString.split("\n");

        boolean cardNotFound = false;
        for(String cardName : split) {
            if (!cardName.isEmpty()) {
                int cardNum = 1;
                cardName = cardName.trim().toLowerCase();

                if (cardName.contains("*")) {
                    String[] cardSplit = cardName.split("\\*");
                    cardNum = Integer.parseInt(cardSplit[0].trim());
                    cardName = cardSplit[1].trim();
                }

                if(cardName.contains("lv.")) {
                    String[] cardSplit = cardName.split("lv\\.");
                    cardName = cardSplit[0].trim();
                }

                try {
                    Card card = CardHandler.getInstance().getCard(cardName);
                    if(!card.isValidInDeck()) {
                        showError("Bench Invalid", card.getName() + " is not a valid bench card!");
                        return false;
                    }
                } catch (RuntimeException e) {
                    showError("Bench Invalid", e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public void setEnableFort() {
        if(enableFortCheck.isSelected()) {
            fortressPane.minHeightProperty().bind(new SimpleDoubleProperty(150));
            fortressPane.maxHeightProperty().bind(new SimpleDoubleProperty(150));
            fortressPane.setDisable(false);
        } else {
            fortressPane.minHeightProperty().bind(new SimpleDoubleProperty(0));
            fortressPane.maxHeightProperty().bind(new SimpleDoubleProperty(0));
            fortressPane.setDisable(true);
        }
    }

    public void setMain(Main mainApp) {
        this.mainApp = mainApp;
        deckTable.setItems(mainApp.getObsDeckList());
    }
}
