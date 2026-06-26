package com.jscm.yxtyg.controller;

import com.jscm.yxtyg.security.JwtTokenProvider;
import com.jscm.yxtyg.service.RequirementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RequirementControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RequirementService requirementService;

    private String tokenFor(String role) {
        return "Bearer " + jwtTokenProvider.generateToken(1L, "user", role);
    }

    @Test
    public void create_productManager_forbidden() throws Exception {
        mockMvc.perform(post("/api/requirement")
                        .header("Authorization", tokenFor("PRODUCT_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"需求\",\"productManagerId\":1,\"systemName\":\"系统\",\"initialWorkload\":1,\"initialAmount\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void create_devAdmin_ok() throws Exception {
        mockMvc.perform(post("/api/requirement")
                        .header("Authorization", tokenFor("DEV_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"需求\",\"productManagerId\":1,\"systemName\":\"系统\",\"initialWorkload\":1,\"initialAmount\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    public void fill_devAdmin_forbidden() throws Exception {
        mockMvc.perform(post("/api/requirement/1/fill")
                        .header("Authorization", tokenFor("DEV_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalWorkload\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void fill_productManager_ok() throws Exception {
        mockMvc.perform(post("/api/requirement/1/fill")
                        .header("Authorization", tokenFor("PRODUCT_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalWorkload\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    public void approve_productManager_forbidden() throws Exception {
        mockMvc.perform(post("/api/requirement/1/approve")
                        .header("Authorization", tokenFor("PRODUCT_MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void approve_devAdmin_ok() throws Exception {
        mockMvc.perform(post("/api/requirement/1/approve")
                        .header("Authorization", tokenFor("DEV_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    public void update_productManager_ok() throws Exception {
        mockMvc.perform(put("/api/requirement/1")
                        .header("Authorization", tokenFor("PRODUCT_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"需求\",\"productManagerId\":1,\"systemName\":\"系统\",\"initialWorkload\":1,\"initialAmount\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_productManager_forbidden() throws Exception {
        mockMvc.perform(delete("/api/requirement/1")
                        .header("Authorization", tokenFor("PRODUCT_MANAGER")))
                .andExpect(status().isForbidden());
    }
}
