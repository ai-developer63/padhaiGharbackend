package app.nepaliapp.padhaighar.config;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	
	@SuppressWarnings("unused")
	private final AuthenticationManager authenticationManager;
	
	
	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
		

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = getTokenFromRequest(request);
		if (token != null && validateToken(token)) {
			String username = getUsernameFromToken(token);
		Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,new ArrayList<>());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response); //this help to move chain forward
		
	}

	
	 // Extracts the JWT token from the "Authorization" header
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
        	return header.substring(7).trim();  // Extract JWT token from Authorization header
        }
        return null;
    }
    
    
    //Helps to validate token
    private boolean validateToken(String token) {
        try {
            Jwts.parserBuilder() // Use parserBuilder to avoid deprecated method
                .setSigningKey(Keys.hmacShaKeyFor(ConfigConstants.SECRET_KEY.getBytes())) // Use HMAC SHA key
                .build()
                .parseClaimsJws(token); // Validate JWT token
            return true;
        } catch (Exception e) {
            return false; // If parsing fails, return false
        }
    }
    
    
    // Extract the username from the JWT token
    private String getUsernameFromToken(String token) {
    	
        Claims claims = Jwts.parserBuilder() // Use parserBuilder for JWT parsing
                .setSigningKey(Keys.hmacShaKeyFor(ConfigConstants.SECRET_KEY.getBytes())) // Use HMAC SHA key
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // Extract the subject (username) from the token
    }

}