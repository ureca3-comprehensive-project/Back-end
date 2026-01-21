package org.backend.message.unit.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.backend.domain.message.entity.Message;
import org.backend.message.policy.RetryPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryPolicy 테스트")
public class RetryPolicyUnitTest {
	
	@InjectMocks
    private RetryPolicy retryPolicy;
	
	@Nested
	@DisplayName("canRetry 메서드는")
	class CanRetry {
		@Test
        @DisplayName("재시도 횟수가 최대 재시도 횟수보다 작으면 true를 반환한다")
        void returnTrueWhenRetryCountLessThanMax() {
            // given
            Message message = createMessageWithRetry(2, 3);

            // when
            boolean result = retryPolicy.canRetry(message);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("재시도 횟수가 최대 재시도 횟수와 같으면 false를 반환한다")
        void returnFalseWhenRetryCountEqualsMax() {
            // given
            Message message = createMessageWithRetry(3, 3);

            // when
            boolean result = retryPolicy.canRetry(message);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("재시도 횟수가 최대 재시도 횟수보다 크면 false를 반환한다")
        void returnFalseWhenRetryCountGreaterThanMax() {
            // given
            Message message = createMessageWithRetry(5, 3);

            // when
            boolean result = retryPolicy.canRetry(message);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("재시도 횟수가 0이면 true를 반환한다")
        void returnTrueWhenRetryCountIsZero() {
            // given
            Message message = createMessageWithRetry(0, 3);

            // when
            boolean result = retryPolicy.canRetry(message);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("최대 재시도 횟수가 0이고 재시도 횟수도 0이면 false를 반환한다")
        void returnFalseWhenBothAreZero() {
            // given
            Message message = createMessageWithRetry(0, 0);

            // when
            boolean result = retryPolicy.canRetry(message);

            // then
            assertThat(result).isFalse();
        }
	}
	
	// Helper method
    private Message createMessageWithRetry(int retryCount, int maxRetry) {
        Message message = mock(Message.class);
        when(message.getRetryCount()).thenReturn(retryCount);
        when(message.getMaxRetry()).thenReturn(maxRetry);
        return message;
    }
	

}
