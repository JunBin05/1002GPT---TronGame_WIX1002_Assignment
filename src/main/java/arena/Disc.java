package arena;

import characters.Direction;
import characters.Character;

public class Disc {
    public int r;
    public int c;
    public Direction dir;
    public int distanceTraveled = 0;
    public int maxDistance;
    public boolean isActive = true; // true = flying, false = stopped
    public Character owner; // who threw it (player or enemy)
    public boolean enemyOwned = false; // true if thrown by enemy
    // Store the tile underneath the disc so we can restore it when disc leaves or is picked up
    private char originalTile = '.';

    public Disc(Character owner, int startR, int startC, Direction dir, int maxDist) {
        this.owner = owner;
        this.enemyOwned = (owner != null && !owner.isPlayer);
        this.r = startR;
        this.c = startC;
        this.dir = dir;
        this.maxDistance = maxDist;
    }

    public char getOriginalTile() { return this.originalTile; }
    public void setOriginalTile(char ch) { this.originalTile = ch; }
}