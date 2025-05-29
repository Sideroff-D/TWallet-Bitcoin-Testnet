package com.siderov.btctracker.dto;

public class BalanceDTO {
    private final long funded;
    private final long spent;

    public BalanceDTO(long funded, long spent) {
        this.funded = funded;
        this.spent  = spent;
    }

    public long getFunded()  { return funded; }
    public long getSpent()   { return spent; }
    public long getBalance() { return funded - spent; }
}
