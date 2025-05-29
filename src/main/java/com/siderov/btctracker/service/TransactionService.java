package com.siderov.btctracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siderov.btctracker.dto.TransactionDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionService {
    // За всички мемпул и потвърдени транзакции ползваме mempool.space
    private static final String BASE_URL     = "https://mempool.space/testnet/api";
    private static final int    PAGE_SIZE    = 25;

    private final RestTemplate http;
    private final ObjectMapper om = new ObjectMapper();

    public TransactionService(RestTemplate http) {
        this.http = http;
    }

    public List<TransactionDTO> getAllTransactions(String address) {
        List<TransactionDTO> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 1) Pending
        try {
            String memJson = http.getForObject(
                    BASE_URL + "/address/" + address + "/txs/mempool",
                    String.class
            );
            List<TransactionDTO> pendings = parseTxs(memJson, address, false);
            for (TransactionDTO tx : pendings) {
                all.add(tx);
                seen.add(tx.getTxid());
            }
        } catch (Exception e) {
            System.err.println("⚠Skipping mempool: " + e.getMessage());
        }

        // 2) Confirmed с пагинация, но без дубликати
        String lastSeen = null;
        while (true) {
            String url = BASE_URL + "/address/" + address + "/txs"
                    + (lastSeen != null ? "/" + lastSeen : "");
            List<TransactionDTO> batch;
            try {
                String json = http.getForObject(url, String.class);
                batch = parseTxs(json, address, true);
            } catch (Exception e) {
                System.err.println("Failed confirmed batch: " + e.getMessage());
                break;
            }
            if (batch.isEmpty()) break;

            for (TransactionDTO tx : batch) {
                if (!seen.contains(tx.getTxid())) {
                    all.add(tx);
                    seen.add(tx.getTxid());
                }
            }

            if (batch.size() < PAGE_SIZE) break;
            lastSeen = batch.get(batch.size() - 1).getTxid();
        }

        return all;
    }

    private List<TransactionDTO> parseTxs(String json,
                                          String address,
                                          boolean confirmed) throws Exception {
        List<TransactionDTO> list = new ArrayList<>();
        JsonNode arr = om.readTree(json);
        if (!arr.isArray()) return list;

        for (JsonNode tx : arr) {
            String txid = tx.path("txid").asText();
            long inputsSum = 0;
            boolean outgoing = false;
            for (JsonNode vin : tx.path("vin")) {
                JsonNode prev = vin.path("prevout");
                if (!prev.isMissingNode()) {
                    inputsSum += prev.path("value").asLong(0);
                    if (address.equals(prev.path("scriptpubkey_address").asText(""))) {
                        outgoing = true;
                    }
                }
            }

            long sent = 0, change = 0;
            for (JsonNode vout : tx.path("vout")) {
                long v = vout.path("value").asLong(0);
                String outAddr = vout.path("scriptpubkey_address").asText("");
                if (address.equals(outAddr)) change += v;
                else sent += v;
            }

            long fee = inputsSum - (sent + change);
            boolean incoming = !outgoing && change > 0;
            list.add(new TransactionDTO(txid, incoming, confirmed, sent, change, fee));
        }
        return list;
    }

    public long calculateBalanceFromTransactions(String address) {
        long balance = 0;
        for (TransactionDTO tx : getAllTransactions(address)) {
            if (tx.isIncoming()) balance += tx.getChange();
            else balance -= (tx.getSent() + tx.getFee());
        }
        return balance;
    }
}
