package sit.meetroom.meetingroomapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.dto.LoginRequest;
import sit.meetroom.meetingroomapi.dto.RegisterRequest;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.dto.TokenResponse;
import sit.meetroom.meetingroomapi.service.AuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() {
        RegisterRequest adminReq = new RegisterRequest("admin_test@example.com", "Pass1234!", "Admin Test", true);
        authService.register(adminReq);
        TokenResponse adminLogin = authService.login(new LoginRequest(adminReq.email(), adminReq.password()));
        this.adminToken = adminLogin.accessToken();

        RegisterRequest memberReq = new RegisterRequest("member_test@example.com", "Pass1234!", "Member Test", false);
        authService.register(memberReq);
        TokenResponse memberLogin = authService.login(new LoginRequest(memberReq.email(), memberReq.password()));
        this.memberToken = memberLogin.accessToken();
    }

    @Test
    void testCreateRoom_AsAdmin_Success() throws Exception {
        // Arrange
        RoomDto newRoom = new RoomDto(null, "Admin Room", 10, "Floor 1", "{}", true, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + this.adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRoom)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateRoom_AsMember_Forbidden() throws Exception {
        // Arrange
        RoomDto newRoom = new RoomDto(null, "Member Room", 5, "Floor 2", "{}", true, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + this.memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRoom)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateRoom_NoToken_Unauthorized() throws Exception {
        // Arrange
        RoomDto newRoom = new RoomDto(null, "No Token Room", 5, "Floor 3", "{}", true, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRoom)))
                .andExpect(status().isUnauthorized());
    }
}