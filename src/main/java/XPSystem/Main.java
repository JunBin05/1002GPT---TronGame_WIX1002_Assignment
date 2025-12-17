package XPSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameEngine game = new GameEngine();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("==========================================");
        System.out.println("   WELCOME TO THE GRID: TRON LEGACY RPG   ");
        System.out.println("==========================================");

        while (running) {
            CharacterProfile current = game.getActiveCharacter();

            System.out.println("\n------------------------------------------");
            System.out.println("ACTIVE USER: " + current.getName().toUpperCase());
            System.out.println("STATS: Level " + current.getLevel() + " | Discs: " + current.getDiscs());
            System.out.println("STORY PROGRESS: Stage " + GlobalState.maxStageCleared + "/22");
            System.out.println("------------------------------------------");

            System.out.println("1. Switch Character (Kevin <-> Tron)");
            System.out.println("2. Play Next Story Stage");
            System.out.println("3. Play Endless Mode (Grind XP)");
            System.out.println("4. Exit");
            System.out.print("> ");

            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    // Toggle Logic
                    String target = current.getName().equals("Kevin") ? "Tron" : "Kevin";
                    game.switchCharacter(target);
                    break;

                case "2":
                    int nextStage = GlobalState.maxStageCleared + 1;
                    System.out.println("\nLoading Stage " + nextStage + "...");

                    // Define Stage Logic (Simplified for simulation)
                    TronRules.StageType type = TronRules.StageType.NORMAL;

                    if (nextStage == 13)
                        type = TronRules.StageType.STORY_CLIMAX;
                    else if (nextStage == 22)
                        type = TronRules.StageType.BOSS_KILL;
                    else if (nextStage <= 1)
                        type = TronRules.StageType.TUTORIAL;

                    game.setGameMode("STORY");

                    // Simulate Combat (Kill 3 minions)
                    game.enemyKilled(TronRules.EnemyType.MINION);
                    game.enemyKilled(TronRules.EnemyType.MINION);
                    game.enemyKilled(TronRules.EnemyType.MINION);

                    // Clear Stage
                    game.stageCleared(nextStage, type);
                    break;

                case "3":
                    System.out.println("\n--- ENDLESS MODE INITIATED ---");
                    game.setGameMode("ENDLESS");

                    // Simulate a wave of enemies
                    System.out.println("Fighting endless wave...");
                    game.enemyKilled(TronRules.EnemyType.MINION);
                    game.enemyKilled(TronRules.EnemyType.MINION);
                    game.enemyKilled(TronRules.EnemyType.RINZLER); // Elite kill

                    // Try to clear stage (Should yield 0 XP in Endless)
                    game.stageCleared(999, TronRules.StageType.NORMAL);
                    break;

                case "4":
                    running = false;
                    break;
            }
        }
        scanner.close();
    }
}