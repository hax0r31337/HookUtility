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
        return 2099;
    }
}