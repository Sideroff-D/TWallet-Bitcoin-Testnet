package com.siderov.btctracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.core.TransactionWitness;
import org.bitcoinj.core.SegwitAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class SendService {

    private final WalletService walletService;
    private final RestTemplate http;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NetworkParameters params = TestNet3Params.get();

    // Всички Esplora заявки към mempool.space
    private static final String BASE_URL = "https://mempool.space/testnet/api";

    private static final long DUST_P2WPKH = 294;
    private static final long DUST_P2PKH  = 546;

    @Autowired
    public SendService(WalletService walletService, RestTemplate restTemplate) {
        this.walletService = walletService;
        this.http          = restTemplate;
    }

    public String sendSats(String toAddress, long amount) throws Exception {
        String addr = walletService.getAddress();

        // 1) Вземаме UTXO-та от mempool.space
        JsonNode utxos;
        try {
            String utxoJson = http.getForObject(
                    BASE_URL + "/address/" + addr + "/utxo",
                    String.class
            );
            utxos = objectMapper.readTree(utxoJson);
        } catch (RestClientException e) {
            throw new Exception("Error fetching UTXOs: " + e.getMessage());
        }

        if (!utxos.isArray() || utxos.size() == 0) {
            throw new Exception("No available UTXOs to spend");
        }

        // 2) Подготовка на транзакция
        Transaction tx = new Transaction(params);
        ECKey ecKey = walletService.getKey();

        long sum = 0, fee = 1000;
        List<TransactionOutPoint> outs = new ArrayList<>();
        List<Long> utxoValues = new ArrayList<>();
        for (JsonNode u : utxos) {
            long v = u.path("value").asLong();
            outs.add(new TransactionOutPoint(
                    params,
                    u.path("vout").asInt(),
                    Sha256Hash.wrap(u.path("txid").asText())
            ));
            utxoValues.add(v);
            sum += v;
            if (sum >= amount + fee) break;
        }
        if (sum < amount + fee) {
            throw new Exception("Insufficient balance: you have " + sum + " sats, need " + (amount + fee));
        }

        // 3) Dust проверка
        Address recipient;
        try {
            recipient = Address.fromString(params, toAddress);
        } catch (IllegalArgumentException iae) {
            throw new Exception("Invalid Testnet address: " + toAddress);
        }
        long dustLimitAmount = (recipient instanceof SegwitAddress) ? DUST_P2WPKH : DUST_P2PKH;
        if (amount < dustLimitAmount) {
            throw new Exception("Amount below dust threshold (" + dustLimitAmount + " sats). Increase amount.");
        }
        tx.addOutput(Coin.valueOf(amount), recipient);

        // 4) Change logic
        long change = sum - amount - fee;
        if (change > 0) {
            long dustLimitChange = addr.startsWith("tb1") ? DUST_P2WPKH : DUST_P2PKH;
            if (change < dustLimitChange) {
                fee += change;
                change = 0;
            } else {
                Address changeAddr = Address.fromString(params, addr);
                tx.addOutput(Coin.valueOf(change), changeAddr);
            }
        }

        // 5) Inputs
        for (TransactionOutPoint out : outs) {
            Script scriptPubKey = ScriptBuilder.createOutputScript(
                    Address.fromString(params, addr)
            );
            tx.addInput(new TransactionInput(params, tx, scriptPubKey.getProgram(), out));
        }

        // 6) Подписване
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput input = tx.getInput(i);
            long value = utxoValues.get(i);

            input.clearScriptBytes();
            Script scriptCode = ScriptBuilder.createP2PKHOutputScript(pubKeyHash);
            Sha256Hash sigHash = tx.hashForWitnessSignature(i, scriptCode, Coin.valueOf(value),
                    Transaction.SigHash.ALL, false);
            TransactionSignature txSig = new TransactionSignature(ecKey.sign(sigHash),
                    Transaction.SigHash.ALL, false);
            TransactionWitness witness = new TransactionWitness(2);
            witness.setPush(0, txSig.encodeToBitcoin());
            witness.setPush(1, ecKey.getPubKey());
            input.setWitness(witness);
        }

        // 7) Broadcast
        String rawHex = Utils.HEX.encode(tx.bitcoinSerialize());
        try {
            return http.postForObject(
                    BASE_URL + "/tx",
                    new HttpEntity<>(rawHex, createPlainTextHeaders()),
                    String.class
            );
        } catch (HttpClientErrorException.BadRequest e) {
            String body = e.getResponseBodyAsString();
            if (body.contains("dust")) {
                throw new Exception("Error: dust output – value is too small for the network.");
            }
            throw e;
        }
    }

    private HttpHeaders createPlainTextHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return headers;
    }
}
