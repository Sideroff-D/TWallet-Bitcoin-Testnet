package com.siderov.btctracker.service;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class WalletService {

    private ECKey key;
    private String address;
    private String privateKey;
    private String blindingKey;
    private List<String> mnemonic;

    @PostConstruct
    public void init() throws Exception {
        generateNewWallet();
    }

    public void generateNewWallet() throws Exception {
        byte[] entropy = new byte[16];
        new SecureRandom().nextBytes(entropy);
        mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy);
        deriveFrom(mnemonic);
    }

    public void restoreWallet(String phrase) throws Exception {
        mnemonic = List.of(phrase.trim().split("\\s+"));
        deriveFrom(mnemonic);
    }

    private void deriveFrom(List<String> words) throws Exception {
        byte[] seedBytes = MnemonicCode.INSTANCE.toSeed(words, "");
        TestNet3Params params = TestNet3Params.get();
        DeterministicKey master   = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        DeterministicKey purpose  = HDKeyDerivation.deriveChildKey(master,   84  | 0x80000000);
        DeterministicKey coin     = HDKeyDerivation.deriveChildKey(purpose,  1   | 0x80000000);
        DeterministicKey account  = HDKeyDerivation.deriveChildKey(coin,     0   | 0x80000000);
        DeterministicKey external = HDKeyDerivation.deriveChildKey(account, 0);
        DeterministicKey child    = HDKeyDerivation.deriveChildKey(external, 0);

        this.key        = child;
        this.address    = SegwitAddress.fromKey(params, child).toString();
        this.privateKey = Base64.getEncoder().encodeToString(child.getPrivKeyBytes());

        byte[] blind = new byte[32];
        new SecureRandom().nextBytes(blind);
        this.blindingKey = Base64.getEncoder().encodeToString(blind);
    }

    // **Тук добавяме обратно гетъра за ECKey**
    public ECKey getKey() {
        return key;
    }

    public List<String> getMnemonic()  { return mnemonic; }
    public String       getAddress()   { return address; }
    public String       getPrivateKey(){ return privateKey; }
    public String       getBlindingKey(){ return blindingKey; }
}
