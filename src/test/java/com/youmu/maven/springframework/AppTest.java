package com.youmu.maven.springframework;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @org.junit.Test
    public void test() {
        System.out.println(TimeUnit.SECONDS.convert(1000, TimeUnit.MILLISECONDS));
    }

    public static void main(String[] args) {
        System.out.println(new Bint().getI());
        ;
    }
}

class Int{
    private int i=1;
    private int getInt(){
        return 1;
    }

    public int getI() {
        return i;
    }
}
class Bint extends Int{
    private int i=2;
    public String getInt(){
        return "";
    }
}
