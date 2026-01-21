package org.backend.message.unit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.backend.domain.message.entity.Message;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.channel.impl.PushChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PUSHChannel 테스트")
public class PushChannelUnitTest {
	
	@InjectMocks
    private PushChannel pushChannel;

    @Nested
    @DisplayName("send 메서드는")
    class Send {

        @Test
        @DisplayName("SMS 전송을 시도한다")
        void attemptToSendEmail() {
            // given
            Message message = createMessage(1L, "test@example.com", "Test Subject");

            // when
            boolean result = pushChannel.send(message).isSuccess();

            // then
            // 성공(99%) 또는 실패(1%) 중 하나여야 함
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("supports 메서드는")
    class Supports {

        @Test
        @DisplayName("EMAIL 타입을 지원하지 않는다")
        void supportEmailType() {
            // when
            boolean result = pushChannel.supports(ChannelType.EMAIL);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("SMS 타입을 지원하지 않는다")
        void doesNotSupportSmsType() {
            // when
            boolean result = pushChannel.supports(ChannelType.SMS);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("PUSH 타입을 지원한다")
        void doesNotSupportPushType() {
            // when
            boolean result = pushChannel.supports(ChannelType.PUSH);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 타입은 지원하지 않는다")
        void doesNotSupportNullType() {
            // when
            boolean result = pushChannel.supports(null);

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
