package org.backend.message.unit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.backend.core.util.security.crypto.CryptoUtil;
import org.backend.domain.line.repository.LineRepository;
import org.backend.domain.message.entity.Message;
import org.backend.domain.message.entity.MessageAttempt;
import org.backend.domain.message.type.ChannelType;
import org.backend.message.channel.impl.SMSChannel;
import org.backend.message.common.dto.ChannelSendResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SMSChannel 단위 테스트")
class SMSChannelUnitTest {

    @Mock
    private LineRepository lineRepository;

    @InjectMocks
    private SMSChannel smsChannel;

    @Test
    @DisplayName("SMS 전송 시 전화번호를 복호화하고 결과를 반환한다")
    void attemptToSendSms() {
        // Static Mocking 설정 (CryptoUtil)
        try (MockedStatic<CryptoUtil> cryptoUtil = mockStatic(CryptoUtil.class)) {
            
            // given
            MessageAttempt attempt = createAttempt(1L);
            ReflectionTestUtils.setField(attempt, "id", 100L); // ID NPE 방지

            given(lineRepository.findPhoneByAttemptId(100L))
                .willReturn("encrypted_data");

            // Static 메서드 동작 정의
            cryptoUtil.when(() -> CryptoUtil.decrypt("encrypted_data"))
                      .thenReturn("01012345678");

            // when
            ChannelSendResult result = smsChannel.send(attempt);

            // then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getProviderMessageId()).startsWith("SMS-");
            
            // 검증
            verify(lineRepository).findPhoneByAttemptId(100L);
            cryptoUtil.verify(() -> CryptoUtil.decrypt("encrypted_data"));
        }
    }

    @Test
    @DisplayName("SMS 타입만 지원한다")
    void supportsTest() {
        assertThat(smsChannel.supports(ChannelType.SMS)).isTrue();
        assertThat(smsChannel.supports(ChannelType.EMAIL)).isFalse();
    }

    private MessageAttempt createAttempt(Long attemptNo) {
        Message message = mock(Message.class);
        return MessageAttempt.attempting(message, attemptNo, "{payload}");
    }
}