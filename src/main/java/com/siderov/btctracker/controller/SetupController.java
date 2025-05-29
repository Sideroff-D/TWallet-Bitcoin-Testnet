package com.siderov.btctracker.controller;

import com.siderov.btctracker.service.WalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SetupController {

    private final WalletService walletService;

    @Autowired
    public SetupController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Показва (и генерира) нова мнемонична фраза.
     */
    @GetMapping("/setup")
    public String showMnemonic(Model model) {
        try {
            walletService.generateNewWallet();
            List<String> m = walletService.getMnemonic();
            model.addAttribute("mnemonic", String.join(" ", m));
        } catch (Exception e) {
            model.addAttribute("mnemonic", List.of("Error: " + e.getMessage()));
        }
        return "setup";
    }

    /**
     * Потвърждение от потребителя, че е запазил фразата → пренасочваме към таблото.
     */
    @PostMapping("/setup")
    public String acceptMnemonic(HttpSession session) {
        session.setAttribute("seedAccepted", true);
        return "redirect:/home";
    }
}
