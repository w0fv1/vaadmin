package dev.w0fv1.vaadmin.test;

public class Test {
    public static void main(String[] args) {
        EchoT echo = new EchoT();
        System.out.println("EchoT"+echo.getEntityClass().getName());

        EchoF echoF = new EchoF();
        System.out.println("EchoF"+echoF.getEntityClass().getName());
    }
}
