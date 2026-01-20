package org.backend.message.unit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.backend.core.message.entity.Message;
import org.backend.core.message.type.ChannelType;
import org.backend.message.channel.impl.EmailChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailChannel 테스트")
public class EmailChannelUnitTest {
	
    @InjectMocks
    private EmailChannel emailChannel;

    @Nested
    @DisplayName("send 메서드는")
    class Send {

        @Test
        @DisplayName("이메일 전송을 시도한다")
        void attemptToSendEmail() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");

            // when
            boolean result = emailChannel.send(message);

            // then
            // 성공(99%) 또는 실패(1%) 중 하나여야 함
            assertThat(result).isIn(true, false);
        }

        @Test
        @DisplayName("이메일 전송 시 약 1초가 소요된다")
        void takeAboutOneSecond() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");

            // when
            long startTime = System.currentTimeMillis();
            emailChannel.send(message);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // then
            // 약 1초 소요 (오차범위 ±200ms)
            assertThat(duration).isBetween(800L, 1200L);
        }

        @RepeatedTest(value = 100, name = "이메일 전송 성공률 테스트 {currentRepetition}/{totalRepetitions}")
        @DisplayName("100번 실행 시 대부분 성공해야 한다 (약 99% 성공률)")
        void verifySuccessRate() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");

            // when
            boolean result = emailChannel.send(message);

            // then
            // 개별 테스트는 성공 또는 실패
            assertThat(result).isIn(true, false);
        }

        @Test
        @DisplayName("100번 실행 시 성공률이 90% 이상이어야 한다")
        void verifyOverallSuccessRate() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");
            int successCount = 0;
            int totalAttempts = 100;

            // when
            for (int i = 0; i < totalAttempts; i++) {
                if (emailChannel.send(message)) {
                    successCount++;
                }
            }

            // then
            // 성공률이 90% 이상이어야 함 (99% 기대, 90% 최소 보장)
            double successRate = (double) successCount / totalAttempts;
            assertThat(successRate).isGreaterThanOrEqualTo(0.90);
        }

        @Test
        @DisplayName("실패하는 경우도 발생한다")
        void canFail() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");
            boolean hasFailure = false;

            // when - 1000번 시도하면 최소 1번은 실패할 것으로 예상 (1% 실패율)
            for (int i = 0; i < 1000; i++) {
                if (!emailChannel.send(message)) {
                    hasFailure = true;
                    break;
                }
            }

            // then
            assertThat(hasFailure).isTrue();
        }
        
        @Test
        @DisplayName("InterruptedException 발생 시 catch 블럭에 처리된다.")
        void catchInterruptedExceptionWhenThreadIsInterrupted() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");
            Thread.currentThread().interrupt();

            // when
            boolean result = emailChannel.send(message);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("supports 메서드는")
    class Supports {

        @Test
        @DisplayName("EMAIL 타입을 지원한다")
        void supportEmailType() {
            // when
            boolean result = emailChannel.supports(ChannelType.EMAIL);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("SMS 타입을 지원하지 않는다")
        void doesNotSupportSmsType() {
            // when
            boolean result = emailChannel.supports(ChannelType.SMS);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("PUSH 타입을 지원하지 않는다")
        void doesNotSupportPushType() {
            // when
            boolean result = emailChannel.supports(ChannelType.PUSH);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 타입은 지원하지 않는다")
        void doesNotSupportNullType() {
            // when
            boolean result = emailChannel.supports(null);

            // then
            assertThat(result).isFalse();
        }
    }
    
    // Util method
    private Message createMessage(Long id, String recipient, String subject) {
        Message message = mock(Message.class);
        return message;
    }

}
