package app.nepaliapp.padhaighar.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import app.nepaliapp.padhaighar.enums.SellerType;
import app.nepaliapp.padhaighar.model.UserModel;
import app.nepaliapp.padhaighar.serviceimp.UserServiceImp;

@Service
public class AuthSuccessHandlerImp implements AuthenticationSuccessHandler {

    @Autowired
    @Lazy
    private UserServiceImp userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // 1. Get the exactly logged-in user's email/phone from the session
        String username = authentication.getName();
        
        // 2. Fetch their real-time profile from the database
        UserModel user = userService.getUserByPhoneorEmail(username);

        if (user != null) {
            String role = user.getRole(); 
            SellerType sellerType = user.getSellerType();

            // 3. Bulletproof Database-Driven Routing Logic
            
            if ("ROLE_ADMIN".equals(role) || "ADMIN".equals(role)) {
                response.sendRedirect("/admin");
                return;
            } 
            else if ("ROLE_TEACHER".equals(role) || "TEACHER".equals(role)) {
                response.sendRedirect("/dashboard/teacher");
                return;
            }
            // Route Affiliates (Checks both Role AND SellerType to be safe)
            else if ("ROLE_AFFILATOR".equals(role) || "AFFILATOR".equals(role) || SellerType.POSTPAID == sellerType) {
                response.sendRedirect("/dashboard/aff");
                return;
            }
            // Route Prepaid Partners directly to their POS Dashboard
            else if (SellerType.PREPAID == sellerType) {
                response.sendRedirect("/prepaid/dashboard");
                return;
            }
        }

        // 4. Default fallback for normal students/users
        response.sendRedirect("/index");
    }
}