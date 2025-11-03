package sit.meetroom.meetingroomapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret:ZmFrZS1iYXNlNjQtc2VjcmV0LW1pbi0zMmJ5dGVzLWF0LWxlYXN0}")
    private String secretBase64;

    @Value("${app.jwt.expMinutes:120}")
    private long expMinutes;

    private SecretKey key() {
        byte[] k = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(k);
    }

    // ===== Generate =====
    public String generateToken(UserDetails user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expMinutes * 60_000))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    // ===== Validate & Extract =====
    public boolean isTokenValid(String token, UserDetails user) {
        String email = extractEmail(token);
        return email.equals(user.getUsername()) && !isExpired(token);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private boolean isExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }
}
