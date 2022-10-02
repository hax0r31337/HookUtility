package test;

import java.lang.*;

public class DynaCompile {

    private final long a = 1000L;

    public static final void main() {
        System.out.println("Hello, world!");
    }

    private static String a(int b) {
        return "method-a";
    }

    private final int b() {
        int b = 1;
        return (int)this.a + b;
    }

    private Object[] test(int a, long b, float c, double d, char e, byte f, short g, boolean h, String i, int[] j) {
        Object[] arr = new Object[] {a, b, c, d, e, f, g, h, i, j};
        if (i == null) {
            System.out.println("L");
        }
        a = (int)arr[0];
        b = (long)arr[1];
        c = (float)arr[2];
        d = (double)arr[3];
        e = (char)arr[4];
        f = (byte)arr[5];
        g = (short)arr[6];
        h = (boolean)arr[7];
        i = (String) arr[8];
        j = (int[])arr[9];
        return arr;
    }

    private static int[] throwed(int a) {
        if (a == 0) {
            throw new NullPointerException();
        }
        a += 2;
        if (a == 1) {
            throw new IllegalArgumentException();
        }
        throw new IllegalStateException();
    }
}