package com.operation.survival;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicAndProtectedApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void stageAndShopItemsShouldBePublicReadOnly() throws Exception {
        mockMvc.perform(get("/api/stage/list"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/shop/items"))
                .andExpect(status().isOk());
    }

    @Test
    void buyAndProgressSaveShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/shop/buy").param("itemCode", "POTION"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/progress/save"))
                .andExpect(status().isForbidden());
    }
}
