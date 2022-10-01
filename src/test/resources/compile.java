package test;

public class DynaCompile {

    private final long a = 1000L;

    public static final void main() {
        System.out.println("Hello, world!");
    }

    private String a() {
        return "method-a";
    }

    private final int b() {
        int b = 1;
        return (int)this.a + b;
    }

    private Object test(int a, long b, float c, double d, char e, byte f, short g, boolean h, String i, int[] j) {
        return new Object[] {a, b, c, d, e, f, g, h, i, j};
    }
}