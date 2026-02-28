package app.nepaliapp.padhaighar.common_controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.nepaliapp.padhaighar.enums.SellerType;
import app.nepaliapp.padhaighar.enums.TransactionType;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.model.WalletTransactionModel;
import app.nepaliapp.padhaighar.repository.WalletTransactionRepository;
import app.nepaliapp.padhaighar.service.UserService;
import app.nepaliapp.padhaighar.serviceimp.CommonServiceImp;

@Controller
@RequestMapping("/admin/wallet")
public class AdminWalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionRepository walletTxRepo;

    @Autowired
    private CommonServiceImp commonServiceImp;

    @GetMapping
    public String walletPage(@RequestParam(required = false, name = "keyword") String keyword, Model model) {
        UserModel user = null;

        if (keyword != null && !keyword.isBlank()) {
            try {
                user = userService.getUserByPhoneorEmail(keyword);
                // Ensure they are actually marked as a PREPAID seller
                if (user != null && user.getSellerType() != SellerType.PREPAID) {
                    model.addAttribute("errorMsg", "User found, but they are NOT a Prepaid Seller. Update their role in User Management first.");
                    user = null; // Hide them if they aren't prepaid
                }
            } catch (Exception ex) {
                user = null;
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        commonServiceImp.modelForAuth(model);
        
        return "admin/admin_wallet";
    }

    @PostMapping("/recharge")
    public String rechargeWallet(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") Double amount,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserModel seller = userService.getUserById(userId);
            if (seller == null || seller.getSellerType() != SellerType.PREPAID) {
                throw new Exception("Invalid seller account.");
            }

            if (amount <= 0) {
                throw new Exception("Amount must be greater than zero.");
            }

            // 1. Update Wallet Balance
            seller.setWalletBalance(seller.getWalletBalance() + amount);
            userService.updateUser(seller);

            // 2. Save Ledger Record
            WalletTransactionModel tx = new WalletTransactionModel();
            tx.setUserId(seller.getId());
            tx.setAmount(amount);
            tx.setTransactionType(TransactionType.CREDIT);
            tx.setDescription("Admin Recharge");
            walletTxRepo.save(tx);

            redirectAttributes.addFlashAttribute("succMsg", "Successfully added Rs." + amount + " to " + seller.getName() + "'s wallet.");
            return "redirect:/admin/wallet?keyword=" + seller.getEmailId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Recharge failed: " + e.getMessage());
            return "redirect:/admin/wallet";
        }
    }
}