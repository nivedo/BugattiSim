package tyrantlib.model;

public class Field implements Runnable {

    private ActiveDeck playerDeck;
    private ActiveDeck enemyDeck;
    private boolean isSurge = false;
    private int numWins = 0;
    private int numRuns;
    private int totalTurns = 0;
    private int unitsKilled = 0;

    private double weight = 1.0;
    public void setWeight(double w) { weight = w; }
    public double getWeight() { return weight; }

    public Field(Deck playerDeck, Deck enemyDeck, boolean isSurge, int numRuns) {
        this.playerDeck = new ActiveDeck(playerDeck);
        this.enemyDeck  = new ActiveDeck(enemyDeck);
        this.isSurge = isSurge;
        this.numRuns = numRuns;

        this.playerDeck.setMode(ActiveDeck.PlayMode.PRIORITY);
        this.enemyDeck.setMode(ActiveDeck.PlayMode.AUTO);
    }

    public void setBGOptions(BGOptions options, boolean isAttack) {
        if(options != null) {
            this.playerDeck.setBGEffect(options);
            this.enemyDeck.setBGEffect(options);

            if(isAttack) {
                this.playerDeck.setFortress(options.playerSiege[0], options.playerSiege[1]);
                this.enemyDeck.setFortress(options.enemyDefense[0], options.enemyDefense[1]);
            } else {
                // HACK: Yes, this is on purpose because the roles are swapped for defense.
                this.playerDeck.setFortress(options.enemySiege[0], options.enemySiege[1]);
                this.enemyDeck.setFortress(options.playerDefense[0], options.playerDefense[1]);
            }
        }
    }

    // Returns number of wins
    public void run() {
        totalTurns = 0;
        for(int i = 0; i < numRuns; i++) {
            int numTurns = 0;
            boolean isPlayerTurn = !isSurge;

            while (!playerDeck.isDead() && !enemyDeck.isDead() && numTurns < 50) {
                if (isPlayerTurn) {
                    playerDeck.doTurn(enemyDeck);
                } else {
                    enemyDeck.doTurn(playerDeck);
                }
                isPlayerTurn = !isPlayerTurn;
                numTurns++;
            }

            if (enemyDeck.isDead()) {
                numWins++;
                totalTurns += numTurns;
                unitsKilled += enemyDeck.getUnitsKilled();
            }

            playerDeck.reset();
            enemyDeck.reset();
        }
    }

    public int getNumWins() {
        return numWins;
    }

    public int getNumRuns() {
        return numRuns;
    }

    public int getTotalTurns() { return totalTurns; }

    public int getUnitsKilled() { return unitsKilled; }
}
