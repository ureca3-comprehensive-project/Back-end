package org.backend.billing.message.Test3Controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.backend.billing.message.controller.RetryPolicyController;
import org.backend.billing.message.entity.RetryPolicyEntity;
import org.backend.billing.message.repository.RetryPolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RetryPolicyController.class)
class RetryPolicyControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean RetryPolicyRepository retryPolicyRepository;

    @Test
    void getPolicy_ok_when_exists() throws Exception {
        RetryPolicyEntity e = new RetryPolicyEntity();
        e.patch(5, 2000L, 2.5, 3000L, 0.02);

        given(retryPolicyRepository.findById(1L)).willReturn(Optional.of(e));

        mockMvc.perform(get("/messages/retry-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maxAttempts").value(5))
                .andExpect(jsonPath("$.data.baseDelayMillis").value(2000))
                .andExpect(jsonPath("$.data.backoffMultiplier").value(2.5));
    }

    @Test
    void updatePolicy_ok() throws Exception {
        RetryPolicyEntity e = new RetryPolicyEntity();
        given(retryPolicyRepository.findById(1L)).willReturn(Optional.of(e));

        String body = """
                {
                  "maxAttempts": 7,
                  "baseDelayMillis": 1500,
                  "backoffMultiplier": 2.0,
                  "timeoutMillis": 5000,
                  "emailFailRate": 0.05
                }
                """;

        mockMvc.perform(patch("/messages/retry-policy")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maxAttempts").value(7))
                .andExpect(jsonPath("$.data.timeoutMillis").value(5000));
    }
}