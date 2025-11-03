package sit.meetroom.meetingroomapi.config;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtils {
    public static String currentEmail(){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null ? a.getName() : null;
    }
}