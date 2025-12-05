package arena;

import java.util.HashMap;
import java.util.Map;

public class LevelConfig {
    public int enemyCount;
    // Map of Enemy Type -> Count (e.g., "Clu" -> 4)
    public Map<String, Integer> enemyTypes = new HashMap<>();
    public boolean hasBoss;
    public String bossName;

    public LevelConfig() {}

    public void addEnemyType(String type, int count) {
        enemyTypes.put(type, count);
    }
}