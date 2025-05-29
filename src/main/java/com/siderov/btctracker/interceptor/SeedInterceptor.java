package com.siderov.btctracker.interceptor;

import com.siderov.btctracker.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Интерсептор, който гарантира, че потребителят е генерирал/възстановил портфейл
 * и е потвърдил seed-а преди достъп до таблото.
 */
@Component
public class SeedInterceptor implements HandlerInterceptor {

    private final WalletService walletService;

    public SeedInterceptor(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) throws Exception {
        // 1) Ако все още няма мнемонична фраза, пренасочваме към началната страница
        if (walletService.getMnemonic() == null) {
            res.sendRedirect("/");
            return false;
        }
        // 2) Ако фразата е генерирана, проверяваме дали потребителят е натиснал Enter Wallet
        HttpSession session = req.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("seedAccepted"))) {
            return true;
        }
        // ако не е потвърдил → връщаме към началото
        res.sendRedirect("/");
        return false;
    }
}
