package com.siderov.btctracker.dto;

public class TransactionDTO {
    private final String txid;
    private final boolean incoming;
    private final boolean confirmed;  // статус на потвърждение
    private final long sent;
    private final long change;
    private final long fee;

    public TransactionDTO(String txid,
                          boolean incoming,
                          boolean confirmed,
                          long sent,
                          long change,
                          long fee) {
        this.txid      = txid;
        this.incoming  = incoming;
        this.confirmed = confirmed;
        this.sent      = sent;
        this.change    = change;
        this.fee       = fee;
    }

    public String getTxid()       { return txid; }
    public boolean isIncoming()   { return incoming; }
    public boolean isConfirmed()  { return confirmed; }
    public long getSent()         { return sent; }
    public long getChange()       { return change; }
    public long getFee()          { return fee; }
}
