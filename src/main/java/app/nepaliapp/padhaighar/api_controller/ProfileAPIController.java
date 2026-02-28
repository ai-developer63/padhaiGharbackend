package app.nepaliapp.padhaighar.api_controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.nepaliapp.padhaighar.api_model.ProfileModelForAPI;
import app.nepaliapp.padhaighar.config.JwtUtil;
import app.nepaliapp.padhaighar.model.PurchasedUserModel;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.service.PurchasedService;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;

@RestController
@RequestMapping("/api/")
public class ProfileAPIController {
    
    @Autowired
    private UserServiceImp userServiceImp;

    @Autowired
    private PurchasedService purchasedService;
    
    @GetMapping("getProfile")
    public ResponseEntity<ProfileModelForAPI> getProfileData(@RequestHeader(value = "Authorization") String token){
        
        token = token.substring(7); 
        String username = JwtUtil.extractUsername(token);

        // ✅ Get User info
        UserModel user = userServiceImp.getUserByPhoneorEmail(username);
        String country = user.getCountry();
        String name = user.getName();
        String profileUrl = user.getProfileImage();
        String email = user.getEmailId();
        
        // ✅ REAL DYNAMIC PURCHASE LOGIC
        List<PurchasedUserModel> myPurchases = purchasedService.getPurchasesByUserId(user.getId());
        
        boolean isPurchased = false;
        long maxRemainingDays = 0;
        LocalDate today = LocalDate.now();

        // Loop through all their purchases to see if they have any active subscriptions
        for (PurchasedUserModel purchase : myPurchases) {
            if (purchase.getPurchaseUpto() != null) {
                LocalDate expiryDate = LocalDate.parse(purchase.getPurchaseUpto());
                
                if (!expiryDate.isBefore(today)) { // If expiry date is today or in the future
                    isPurchased = true; // They are an active premium user
                    
                    long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);
                    if (daysLeft > maxRemainingDays) {
                        maxRemainingDays = daysLeft; // Show them the max days they have left on their longest course
                    }
                }
            }
        }
        
        String remainingDays = String.valueOf(maxRemainingDays);

        ProfileModelForAPI profile = new ProfileModelForAPI(
                name,    
                country, 
                email,
                isPurchased,        
                remainingDays,          
                profileUrl     
        );
        
        return ResponseEntity.ok(profile);
    }
}