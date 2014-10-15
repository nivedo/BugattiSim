package tyrantlib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

public class SimulatorV2 {

    private Gauntlet gauntlet;
    private Deck playerDeck;
    private int numRuns = 150;
    private static int MAX_TURNS = 80;

    public int score;
    public double winRate;
    public int turns;

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

    public SimulatorV2(Deck playerDeck, Gauntlet gauntlet, BGOptions options) {
        this.playerDeck = playerDeck;
        this.gauntlet = gauntlet;
        this.options = options;
    }

    public Deck getPlayerDeck() { return playerDeck; }

    public void runSimulation(int n) {
        List<Field> fieldList = new ArrayList<Field>();
        List<Callable<Object>> taskList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(16);
        numRuns = n;

        if(!useMultiOpt) {
            for (Deck gauntletDeck : gauntlet.getDeckList()) {
                Field field;
                if(options.isAttack) {
                    field = new Field(playerDeck, gauntletDeck, options.surge, numRuns);
                } else {
                    field = new Field(gauntletDeck, playerDeck, options.surge, numRuns);
                }
                field.setBGOptions(options, options.isAttack);
                taskList.add(Executors.callable(field));
                fieldList.add(field);
            }
        } else {
            for(int i = 0; i < optionsList.size(); i++) {
                BGOptions bgopt = optionsList.get(i);

                // Override BG effects
                bgopt.bgEffect = options.bgEffect;
                bgopt.bgX = options.bgX;

                double weight = weights.get(i);
                for (Deck gauntletDeck : gauntlet.getDeckList()) {
                    Field field;
                    if(options.isAttack) {
                        field = new Field(playerDeck, gauntletDeck, bgopt.surge, numRuns);
                    } else {
                        field = new Field(gauntletDeck, playerDeck, bgopt.surge, numRuns);
                    }
                    field.setBGOptions(bgopt, options.isAttack);
                    field.setWeight(weight);
                    taskList.add(Executors.callable(field));
                    fieldList.add(field);
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

        score = calcScore(fieldList);
        winRate = calcWinRate(fieldList);
        turns = calcTurns(fieldList);
    }

    public int getScore() { return score; }
    public double getWinRate() { return winRate; }
    public int getTurns() { return turns; }

    private int calcScore(List<Field> fieldList) {
        int numTotal = 0;
        int numWins = 0;
        int turnCount = 0;
        int unitsKilled = 0;
        for(Field f : fieldList) {
            numTotal += (f.getNumRuns() * f.getWeight());
            numWins += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
            unitsKilled += (f.getUnitsKilled() * f.getWeight());
        }

        if(options.isBrawlMode) {
            double expectedScore = 1.0 * numWins / numTotal * (50.0 + (2.0 * unitsKilled - 0.5 * turnCount) / numWins) + 5.0 * (numTotal - numWins) / numTotal;

            if (options.isAttack) {
                return (int) (1000 * expectedScore / 60.0);
            } else {
                return (int) (1000 * (1 - expectedScore / 60.0));
            }
        }

        int winRate = 1000 * numWins / numTotal;
        if(options.isAttack) {
            return winRate;
        } else {
            return 1000 - winRate;
        }
    }

    private double calcWinRate(List<Field> fieldList) {
        int numTotal = 0;
        int numWins = 0;
        int turnCount = 0;
        for(Field f : fieldList) {
            numTotal += (f.getNumRuns() * f.getWeight());
            numWins += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
        }

        int roundedRate = 1000 * numWins / numTotal;
        if(options.isAttack) {
            return roundedRate / 10.0;
        } else {
            return 100.0 - roundedRate / 10.0;
        }
    }

    private int calcTurns(List<Field> fieldList) {
        int numTotal = 0;
        int turnCount = 0;
        for(Field f : fieldList) {
            numTotal += (f.getNumWins() * f.getWeight());
            turnCount += (f.getTotalTurns() * f.getWeight());
        }

        if(numTotal == 0) { return MAX_TURNS; }

        return (int)(turnCount / numTotal);
    }
}
