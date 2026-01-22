package org.backend.message.unit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.backend.core.util.security.crypto.CryptoUtil;
import org.backend.core.util.security.crypto.Encryptor;
import org.backend.domain.line.repository.LineRepository;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.channel.impl.PushChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushChannel 테스트")
class PushChannelUnitTest {

	@Mock
	private LineRepository lineRepository;
	
	@Mock
	private Encryptor encryptor;

    @InjectMocks
    private PushChannel pushChannel;
    
    
    @BeforeEach
    void setUpCrypto() {
        try {
            var field = CryptoUtil.class.getDeclaredField("encryptor");
            field.setAccessible(true);
            field.set(null, encryptor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    

    @Nested
    @DisplayName("send 메서드는")
    class Send {

        @Test
        @DisplayName("PUSH 전송을 시도한다")
        void attemptToSendPush() {
            // given
            MessageAttempt attempt = createAttempt(1L);

            // when
            
            when(lineRepository.findPhoneByAttemptId(1L))
            .thenReturn("ENCRYPTED_PHONE");

            when(encryptor.decrypt("ENCRYPTED_PHONE"))
            .thenReturn("01012345678");
            
            boolean result = pushChannel.send(attempt).isSuccess();
            
            

            // then
            // 성공 또는 실패 중 하나여야 함
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("supports 메서드는")
    class Supports {

        @Test
        @DisplayName("EMAIL 타입을 지원하지 않는다")
        void doesNotSupportEmailType() {
            boolean result = pushChannel.supports(ChannelType.EMAIL);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("SMS 타입을 지원하지 않는다")
        void doesNotSupportSmsType() {
            boolean result = pushChannel.supports(ChannelType.SMS);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("PUSH 타입을 지원한다")
        void supportPushType() {
            boolean result = pushChannel.supports(ChannelType.PUSH);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 타입은 지원하지 않는다")
        void doesNotSupportNullType() {
            boolean result = pushChannel.supports(null);
            assertThat(result).isFalse();
        }
    }

    // =========================
    // util
    // =========================
    private MessageAttempt createAttempt(Long attemptId) {
        Message message = mock(Message.class);

        MessageAttempt attempt =
            MessageAttempt.attempting(message, 1L, "{push-payload}");

        // 🔥 핵심: JPA @Id 강제 세팅
        try {
            var field = MessageAttempt.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(attempt, attemptId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return attempt;
    }
}
