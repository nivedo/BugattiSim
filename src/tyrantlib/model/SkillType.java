/* Enums for skill types */

package tyrantlib.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum SkillType {
    ENFEEBLE, HEAL, PROTECT, RALLY, SIEGE, STRIKE, WEAKEN, JAM, ENHANCE,
    PIERCE, BERSERK, LEECH, POISON, INHIBIT,
    EVADE, ARMORED, COUNTER, CORROSIVE, WALL, FLURRY,
    OVERLOAD, VALOR, PROGENITOR, UNKNOWN;

    // Activated Abilities
    public static final Set<SkillType> ACTIVE_SKILLS = new HashSet<SkillType>(Arrays.asList(
            new SkillType[]{SkillType.ENFEEBLE, SkillType.HEAL, SkillType.PROTECT, SkillType.RALLY,
                    SkillType.SIEGE, SkillType.STRIKE, SkillType.WEAKEN, SkillType.JAM, SkillType.ENHANCE, SkillType.OVERLOAD}
    ));

    // Abilities on Attack Phase
    public static final Set<SkillType> ATTACK_SKILLS = new HashSet<SkillType>(Arrays.asList(

            new SkillType[] {SkillType.PIERCE, SkillType.BERSERK, SkillType.LEECH, SkillType.POISON, SkillType.INHIBIT}
    ));

    // Passive stats
    public static final Set<SkillType> PASSIVE_SKILLS = new HashSet<SkillType>(Arrays.asList(
            new SkillType[] {SkillType.EVADE, SkillType.ARMORED, SkillType.COUNTER, SkillType.CORROSIVE,
                    SkillType.WALL, SkillType.FLURRY, SkillType.VALOR}
    ));

    public static boolean isActiveSkill(SkillType id) { return SkillType.ACTIVE_SKILLS.contains(id); }
    public static boolean isAttackSkill(SkillType id) { return SkillType.ATTACK_SKILLS.contains(id); }
    public static boolean isPassiveSkill(SkillType id) { return SkillType.PASSIVE_SKILLS.contains(id); }

    public static SkillType stringToSkillType(String id) {
        for(SkillType st : SkillType.values()) {
            if(st.toString().equalsIgnoreCase(id)) { return st; }
        }

        System.err.println("Unknown Skill: " + id);
        return UNKNOWN;
    }
}