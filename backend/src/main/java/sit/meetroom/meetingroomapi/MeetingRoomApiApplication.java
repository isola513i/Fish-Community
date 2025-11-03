package sit.meetroom.meetingroomapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class MeetingRoomApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomApiApplication.class, args);
    }

}
