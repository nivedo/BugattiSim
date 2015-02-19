package tyrantlib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

public class Simulator {

    private Gauntlet atkGauntlet;
    private Gauntlet defGauntlet;
    private final Deck playerDeck;
    private int numRuns = 150;
    private int numSims = 0;

    private int attackCCS;
    private int defenseCCS;
    private int averageCCS;

    public int getCCS() { return averageCCS; }
    public int getAttackCCS() { return attackCCS; }
    public int getDefenseCCS() { return defenseCCS; }

    private BGOptions options;

    // Weighted options proposed by uDecor + Axis
    private boolean useMultiOpt = false;
    private ArrayList<BGOptions> optionsList;
    private ArrayList<Double> weights;

    public void setMultiOptions(ArrayList<BGOptions> opts, ArrayList<Double> weights) {
        this.optionsList = opts;
        this.weights = weights;
        useMultiOpt = true;
    }

    public void clearMultiOptions() {
        this.optionsList = null;
        this.weights = null;
        useMultiOpt = false;
    }

    public Simulator(Deck playerDeck, Gauntlet atkGauntlet, Gauntlet defGauntlet, BGOptions options) {
        this.playerDeck = playerDeck;
        this.atkGauntlet = atkGauntlet;
        this.defGauntlet = defGauntlet;
        this.options = options;
    }

    public Deck getPlayerDeck() { return playerDeck; }

    // JavaFX Wrappers
    public StringProperty getDeckStringProperty() {
        return new SimpleStringProperty(playerDeck.toString());
    }
    public IntegerProperty getAvgScoreProperty() {
        return new SimpleIntegerProperty(getCCS());
    }
    public IntegerProperty getAtkScoreProperty() {
        return new SimpleIntegerProperty(getAttackCCS());
    }
    public IntegerProperty getDefScoreProperty() {
        return new SimpleIntegerProperty(getDefenseCCS());
    }

    public void setAttackGauntlet(Gauntlet gauntlet) { this.atkGauntlet = gauntlet; }
    public void setDefenseGauntlet(Gauntlet gauntlet) { this.defGauntlet = gauntlet; }
    public void setOptions(BGOptions options) { this.options = options; }

    public void runSimulation(int n) {
        List<Field> attackFields = new ArrayList<Field>();
        List<Field> defenseFields = new ArrayList<Field>();
        List<Callable<Object>> taskList = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(16);
        numRuns = n;

        if(!useMultiOpt) {
            if(atkGauntlet != null) {
                for (Deck gauntletDeck : atkGauntlet.getDeckList()) {
                    Field fieldAttack = new Field(playerDeck, gauntletDeck, options.surge, numRuns);
                    fieldAttack.setBGOptions(options, true);
                    taskList.add(Executors.callable(fieldAttack));
                    attackFields.add(fieldAttack);
                }
            }
            if(defGauntlet != null) {
                for (Deck gauntletDeck : defGauntlet.getDeckList()) {
                    Field fieldDefense = new Field(gauntletDeck, playerDeck, options.surge, numRuns);
                    fieldDefense.setBGOptions(options, false);
                    taskList.add(Executors.callable(fieldDefense));
                    defenseFields.add(fieldDefense);
                }
            }
        } else {
            for(int i = 0; i < optionsList.size(); i++) {
                BGOptions bgopt = optionsList.get(i);

                // Override BG effects
                bgopt.bgEffect = options.bgEffect;
                bgopt.bgX = options.bgX;

                double weight = weights.get(i);
                if(atkGauntlet != null) {
                    for (Deck gauntletDeck : atkGauntlet.getDeckList()) {
                        Field fieldAttack = new Field(playerDeck, gauntletDeck, options.surge, numRuns);
                        fieldAttack.setBGOptions(bgopt, true);
                        fieldAttack.setWeight(weight);
                        taskList.add(Executors.callable(fieldAttack));
                        attackFields.add(fieldAttack);
                    }
                }
                if(defGauntlet != null) {
                    for (Deck gauntletDeck : defGauntlet.getDeckList()) {
                        Field fieldDefense = new Field(gauntletDeck, playerDeck, options.surge, numRuns);
                        fieldDefense.setBGOptions(bgopt, false);
                        fieldDefense.setWeight(weight);
                        taskList.add(Executors.callable(fieldDefense));
                        defenseFields.add(fieldDefense);
                    }
                }
            }
        }

        try {
            executor.invokeAll(taskList);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        attackCCS = calcAttackCCS(attackFields);
        defenseCCS = calcDefenseCCS(defenseFields);
        averageCCS = (attackCCS + defenseCCS) / 2;

        numSims++;
    }

    private int calcAttackCCS(List<Field> attackFields) {
        if(attackFields.size() == 0) return 0;

        int numTotal = 0;
        int numWins = 0;
        int turnCount = 0;
        int unitsKilled = 0;
        for(Field f : attackFields) {
            numTotal += (f.getNumRuns() * f.getWeight());
            numWins += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
            unitsKilled += (f.getUnitsKilled() * f.getWeight());
        }
        /*
        if(options.isBrawlMode) {
            double expectedScore = 1.0 * numWins / numTotal * (50.0 + (2.0 * unitsKilled - 0.5 * turnCount) / numWins) + 5.0 * (numTotal - numWins) / numTotal;
            return (int) (1000 * expectedScore / 60.0);
        }
        */
        return (int)(1000.0 * numWins / numTotal);
    }

    private int calcDefenseCCS(List<Field> defenseFields) {
        if(defenseFields.size() == 0) return 0;

        int numTotal = 0;
        int numWins = 0;
        int turnCount = 0;
        int unitsKilled = 0;
        for(Field f : defenseFields) {
            numTotal += (f.getNumRuns() * f.getWeight());
            numWins += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
            unitsKilled += (f.getUnitsKilled() * f.getWeight());
        }
        /*
        if(options.isBrawlMode) {
            double expectedScore = 1.0 * numWins / numTotal * (50.0 + (2.0 * unitsKilled - 0.5 * turnCount) / numWins) + 5.0 * (numTotal - numWins) / numTotal;
            return (int) (1000 * (1 - expectedScore / 60.0));
        }
        */
        /*
        double expectedScore = 1.0 * numWins / numTotal * (80.0 - 1.0 * turnCount / numWins) + 5.0 * (numTotal - numWins) / numTotal;

        if(options.isAttack) {
            return (int) (1000 * (expectedScore - 5.0) / 75.0);
        } else {
            return (int) (1000 * (1 - (expectedScore - 5.0) / 75.0));
        }
        */
        return (int)(1000.0 - 1000.0 * numWins / numTotal);
    }

    private int calcAttackTurns(List<Field> attackFields) {
        int numTotal = 0;
        int turnCount = 0;
        for(Field f : attackFields) {
            numTotal += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
        }

        return (int)(turnCount / numTotal);
    }

    private int calcDefenseTurns(List<Field> defenseFields) {
        int numTotal = 0;
        int turnCount = 0;
        for(Field f : defenseFields) {
            numTotal += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
        }

        return (int)(turnCount / numTotal);
    }

    /*
    public void printDetailedCCS() {
        for(int i = 0 ; i < attackFields.size(); i++) {
            Field attackField = attackFields.get(i);
            Field defenseField = defenseFields.get(i);
            System.out.println("ATTACK[" + i + "]: " + 1000.0 * attackField.getNumWins() / attackField.getNumRuns());
            System.out.println("DEFENSE[" + i + "]: " + (1000.0 - 1000.0 * defenseField.getNumWins() / defenseField.getNumRuns()));
        }

    }
    */

}
