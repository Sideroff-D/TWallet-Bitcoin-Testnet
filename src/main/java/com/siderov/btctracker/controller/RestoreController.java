package com.siderov.btctracker.controller;

import com.siderov.btctracker.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RestoreController {

    private final WalletService walletService;

    @Autowired
    public RestoreController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Показва формата за възстановяване от мнемонична фраза.
     */
    @GetMapping("/restore")
    public String restoreForm() {
        return "restore";
    }

    /**
     * Обработва подадената мнемонична фраза и възстановява портфейла.
     */
    @PostMapping("/restore")
    public String doRestore(@RequestParam String mnemonic,
                            HttpSession session) {
        try {
            // Възстановяваме портфейла от думите
            walletService.restoreWallet(mnemonic);
            // Маркираме, че потребителят е потвърдил seed-а
            session.setAttribute("seedAccepted", true);
        } catch (Exception ignored) {
            // тук може да добавите flash-съобщение при грешка
        }
        // Пренасочваме към таблото, където ще се зареди балансът
        return "redirect:/home";
    }
}
