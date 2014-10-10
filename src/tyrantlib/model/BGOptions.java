package tyrantlib.model;

import java.util.Arrays;

/**
 * Created by Jay on 7/21/2014.
 */
public class BGOptions {

    public Card[] playerSiege = new Card[2];
    public Card[] playerDefense = new Card[2];
    public Card[] enemySiege = new Card[2];
    public Card[] enemyDefense = new Card[2];
    public SkillType bgEffect = SkillType.UNKNOWN;
    public int bgX = 0;
    public boolean surge = true;
    public boolean isAttack = true;
    public boolean isEnhance = false;
    public boolean isBrawlMode = false;

    @Override
    public String toString() {
        return "BGOptions{" +
                "playerSiege=" + Arrays.toString(playerSiege) +
                ", playerDefense=" + Arrays.toString(playerDefense) +
                ", enemySiege=" + Arrays.toString(enemySiege) +
                ", enemyDefense=" + Arrays.toString(enemyDefense) +
                ", bgEffect=" + bgEffect +
                ", bgX=" + bgX +
                ", surge=" + surge +
                ", isAttack=" + isAttack +
                ", isEnhance=" + isEnhance +
                ", isBrawlMode=" + isBrawlMode +
                '}';
    }
}
