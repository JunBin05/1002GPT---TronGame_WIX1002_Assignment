package designenemies;

/**
 * Clu enemy class.
 * Subclass of Enemy representing Clu type with very high speed and brilliant intelligence level.
 */
public class Clu extends Enemy {

    /**
     * Constructor that initializes a Clu object using data from enemies.txt
     * @param data - String array containing enemy attributes in order:
     *             [name, color, difficulty, XP, speed, handling, intelligence, description]
     */
    public Clu(String[] data) {
        super(data[0], data[1], data[2], Integer.parseInt(data[3]),
              data[4], data[5], data[6], data[7]);
    }

     /**
     * Implement AI movement logic for Clu.
     * For now, just prints a description.
     * In full game, this method would determine the next move on the grid.
     */
    @Override
    public void decideMove() {
        System.out.println(name + " uses unpredictable and strategic movement.");
    }
}

