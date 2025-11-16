package app.nepaliapp.padhaighar.config;

import java.util.Calendar;
import java.util.Date;
import java.security.Key;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

	private static final String SECRET_KEY = ConfigConstants.SECRET_KEY;

	public static String generateToken(String identifier) {

		Calendar calender = Calendar.getInstance();
		calender.add(Calendar.DATE, 30);
		Date expirationDate = calender.getTime();
		Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
		return Jwts.builder().setSubject(identifier).setIssuedAt(new Date()).setExpiration(expirationDate).signWith(key)
				.compact();
	}

	public static Claims extractClaims(String token) {

		try {
			Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		} catch (Exception e) {
			throw new RuntimeException("Invalid or expired JWT token", e);
		}

	}

	public static String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}

	public static boolean isTokenExpired(String token) {

		Date expiration = extractClaims(token).getExpiration();
		return expiration.before(expiration);

	}

	public static boolean validateToken(String token, UserDetails userDetails) {
		return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
	}

}