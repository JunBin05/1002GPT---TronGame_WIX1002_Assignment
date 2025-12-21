package XPSystem;

public class XpSimulator {
    public static void main(String[] args) {
        int[] testLevels = {5, 10, 20, 30, 50};
        int[][] stages = { {1,1}, {1,2}, {2,3}, {5,4} };
        System.out.println("Simulation with current tuning:\n");
        for (int[] st : stages) {
            int ch = st[0], sg = st[1];
            long stageXp = TronRules.calculateStageClearXp(ch, sg);
            int expected = TronRules.getExpectedLevelForStage(ch, sg);
            System.out.println(String.format("Stage C%dS%d: stageXp=%d expectedLevel=%d", ch, sg, stageXp, expected));
            for (int pl : testLevels) {
                double mult = TronRules.calculateReplayMultiplier(pl, ch, sg);
                long finalXp = Math.max(1, Math.round(stageXp * mult));
                System.out.println(String.format("  Player L%d -> mult=%.4f finalXp=%d", pl, mult, finalXp));
            }
            System.out.println();
        }
    }
}