package arena;

import characters.Direction;

public class Disc {
    public int r;
    public int c;
    public Direction dir;
    public int distanceTraveled = 0;
    public int maxDistance;
    public boolean isActive = true; // true = flying, false = stopped
    // Store the tile underneath the disc so we can restore it when disc leaves or is picked up
    private char originalTile = '.';

    public Disc(int startR, int startC, Direction dir, int maxDist) {
        this.r = startR;
        this.c = startC;
        this.dir = dir;
        this.maxDistance = maxDist;
    }

    public char getOriginalTile() { return this.originalTile; }
    public void setOriginalTile(char ch) { this.originalTile = ch; }
}