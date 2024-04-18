package com.primecheck;

import java.math.BigInteger;

public class PrimeCandidate {
    private BigInteger integer;

    public BigInteger getInteger() {
        return integer;
    }

    public void setInteger(BigInteger integer) {
        this.integer = integer;
    }

    @Override
    public String toString() {
        return integer.toString();
    }
}
