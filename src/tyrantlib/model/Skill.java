/*
 * Skill wrapper for cards.  Variables correspond to values in XML file.
 */

package tyrantlib.model;

public class Skill {
    protected SkillType id;
    protected int x; // e.g. Rally X
    protected Faction y; // e.g. Rally Faction
    protected SkillType s; // e.g. Enhance Skill
    protected int c; // e.g. Jam counter
    protected int n; // number of units targeted e.g. Overload N
    protected boolean all;

    public Skill() {
        this.y = Faction.NOTYPE;
    }

    public Skill(Skill s) {
        this.id = s.id;
        this.x  = s.x;
        this.y  = s.y;
        this.s  = s.s;
        this.c  = s.c;
        this.n  = s.n;
        this.all = s.all;
        this.category = s.category;
    }

    public void setId(SkillType type) {
        this.id = type;

        if(SkillType.isPassiveSkill(type)) { category = Category.PASSIVE; }
        else if(SkillType.isAttackSkill(type)) { category = Category.ATTACK; }
        else { category = Category.ACTIVE; }
    }

    private enum Category {
        ACTIVE, ATTACK, PASSIVE;
    }
    private Category category;

    public boolean isActiveSkill() { return category == Category.ACTIVE; }
    public boolean isAttackSkill() { return category == Category.ATTACK; }
    public boolean isPassiveSkill() { return category == Category.PASSIVE; }

    @Override
    public String toString() {
        return "Skill {" +
                id + " " +
                (all ? "ALL " : "") +
                (s!=null ? s + " " : "") +
                (y!=null ? y + " " : "") +
                (c > 0 ? c + " " : "") +
                (x > 0 ? x + " " : "") +
                (n > 0 ? n + " " : "") +
                "}";
    }
}
