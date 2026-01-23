//package org.backend.billing.invoice.api;
//
//import org.backend.billingbatch.services.LockService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//import org.springframework.web.filter.CharacterEncodingFilter;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class LockConcurrencyTest {
//    private MockMvc mockMvc;
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @Autowired
//    private LockService lockService; // 가짜(Mock)가 아닌 진짜 서비스 주입
//
//    @BeforeEach
//    void setUp() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
//                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
//                .build();
//        lockService.forceUnlock(); // 테스트 전 lock 초기화
//    }
//
//    @Test
//    @DisplayName("10명이 동시에 락을 요청하면 딱 1명만 성공해야 한다")
//    void testConcurrency() throws InterruptedException {
//        // Given
//        int numberOfThreads = 10;
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads); // 동시 출발
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        // When
//        for (int i = 0; i < numberOfThreads; i++) {
//            executorService.submit(() -> {
//                try {
//                    MvcResult result = mockMvc.perform(post("/billing/locks/acquire"))
//                            .andReturn();
//
//                    String content = result.getResponse().getContentAsString();
//
//                    // "SUCCESS" 응답이 오면 성공 카운트 증가
//                    if ("SUCCESS".equals(content)) {
//                        successCount.incrementAndGet();
//                    } else {
//                        failCount.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    latch.countDown(); // 작업 끝남 알림
//                }
//            });
//        }
//
//        latch.await(); // 10명 다 끝날 때까지 대기
//
//        // Then
//        System.out.println("성공한 요청 수: " + successCount.get());
//        System.out.println("실패한 요청 수: " + failCount.get());
//
//        // 검증: 오직 1명만 성공해야 함
//        assertThat(successCount.get()).isEqualTo(1);
//        assertThat(failCount.get()).isEqualTo(9);
//    }
//}
