package com.siderov.btctracker.controller;

import com.google.zxing.WriterException;
import com.siderov.btctracker.dto.TransactionDTO;
import com.siderov.btctracker.service.SendService;
import com.siderov.btctracker.service.TransactionService;
import com.siderov.btctracker.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final WalletService      walletService;
    private final TransactionService txService;
    private final SendService        sendService;

    @Autowired
    public HomeController(WalletService ws,
                          TransactionService ts,
                          SendService ss) {
        this.walletService = ws;
        this.txService     = ts;
        this.sendService   = ss;
    }

    /**
     * Табло на потребителя.
     * View файл: src/main/resources/templates/home.html
     */
    @GetMapping("/home")
    public String home(Model model,
                       @ModelAttribute("message") String message) {
        String address = walletService.getAddress();

        long balance = txService.calculateBalanceFromTransactions(address);
        List<TransactionDTO> txs = txService.getAllTransactions(address);

        model.addAttribute("balance",      balance);
        model.addAttribute("transactions", txs);
        model.addAttribute("address",      address);
        model.addAttribute("privateKey",   walletService.getPrivateKey());
        model.addAttribute("blindingKey",  walletService.getBlindingKey());
        model.addAttribute("message",      message);

        return "home";
    }

    /**
     * JSON endpoint за баланс.
     */
    @GetMapping(path = "/api/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Long> apiBalance() {
        long bal = txService.calculateBalanceFromTransactions(walletService.getAddress());
        return Map.of("balance", bal);
    }

    /**
     * JSON endpoint за история на транзакции.
     */
    @GetMapping(path = "/api/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<TransactionDTO> apiTransactions() {
        return txService.getAllTransactions(walletService.getAddress());
    }

    /**
     * Сервира QR код като PNG.
     */
    @GetMapping("/qr-code")
    public ResponseEntity<byte[]> getQRCode() throws WriterException, IOException {
        byte[] qr = com.siderov.btctracker.service.QRCodeGenerator
                .generateQRCodeImage(walletService.getAddress(), 250, 250);
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(qr);
    }

    /**
     * Пренасочва GET /send към /home.
     */
    @GetMapping("/send")
    public String sendRedirect() {
        return "redirect:/home";
    }

    /**
     * Обработва изпращане на sats.
     */
    @PostMapping("/send")
    public String doSend(@RequestParam String toAddress,
                         @RequestParam long   amount,
                         RedirectAttributes ra) {
        try {
            String txid = sendService.sendSats(toAddress, amount);
            ra.addFlashAttribute("message",
                    "Sent " + amount + " sats! TXID: " + txid);
        } catch (Exception e) {
            String err = e.getMessage() != null ? e.getMessage() : e.toString();
            ra.addFlashAttribute("message",
                    "Send failed: " + err);
        }
        return "redirect:/home";
    }
}
