package XPSystem;

public class GlobalState {
    public static int maxStageCleared = 0;

    // Default: Tron is ALIVE and Playable
    private static boolean tronIsAlive = true;

    public static boolean isTronPlayable() {
        return tronIsAlive;
    }

    public static void killTron() {
        tronIsAlive = false;
        System.out.println(">>> CRITICAL ALERT: SIGNAL LOST... TRON DEREZZED. <<<");
    }
}
