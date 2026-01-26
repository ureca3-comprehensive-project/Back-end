package org.backend.message.unit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.backend.core.util.security.crypto.CryptoUtil;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.domain.user.repository.UserRepository; // UserRepository 추가
import org.backend.message.channel.impl.EmailChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailChannel 테스트")
class EmailChannelUnitTest {

    @Mock
    private UserRepository userRepository; // 1. UserRepository 모킹 추가

    @InjectMocks
    private EmailChannel emailChannel;

    @Nested
    @DisplayName("send 메서드는")
    class Send {

        @Test
        @DisplayName("100번 실행 시 성공률이 90% 이상이어야 한다")
        void verifyOverallSuccessRate() {
            // Static Mocking이 필요한 경우 (CryptoUtil 등)
            try (MockedStatic<CryptoUtil> cryptoUtil = mockStatic(CryptoUtil.class)) {
                // given
                int successCount = 0;
                int totalAttempts = 100;
                
                // Static 메서드 및 Repository 기본 동작 정의
                cryptoUtil.when(() -> CryptoUtil.decrypt(anyString())).thenReturn("test@email.com");

                // when
                for (int i = 0; i < totalAttempts; i++) {
                    MessageAttempt attempt = createAttempt((long) i);
                    // 2. ID 강제 주입 (NPE 방지)
                    ReflectionTestUtils.setField(attempt, "id", (long) i);
                    
                    // Repository Mocking
                    given(userRepository.findEmailByAttemptId((long) i)).willReturn("encrypted_email");

                    if (emailChannel.send(attempt).isSuccess()) {
                        successCount++;
                    }
                }

                // then
                double successRate = (double) successCount / totalAttempts;
                assertThat(successRate).isGreaterThanOrEqualTo(0.90);
            }
        }

        // ... 나머지 테스트 메서드들도 동일한 방식으로 UserRepository mocking 필요
    }

    @Nested
    @DisplayName("supports 메서드는")
    class Supports {
        @Test
        @DisplayName("EMAIL 타입을 지원한다")
        void supportEmailType() {
            assertThat(emailChannel.supports(ChannelType.EMAIL)).isTrue();
        }

        @Test
        @DisplayName("SMS 타입을 지원하지 않는다")
        void doesNotSupportSmsType() {
            assertThat(emailChannel.supports(ChannelType.SMS)).isFalse();
        }
    }

    // =========================
    // util
    // =========================
    private MessageAttempt createAttempt(Long attemptNo) {
        Message message = mock(Message.class);
        // 로깅 시 발생하는 NPE 방지
//        lenient().when(message.getCorrelationId()).willReturn("test-id");
        return MessageAttempt.attempting(message, attemptNo, "{json-payload}");
    }
}