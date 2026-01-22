package org.backend.message.unit.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.backend.domain.message.entity.Message;
import org.backend.domain.user.entity.BanTime;
import org.backend.domain.user.repository.BanTimeRepository;
import org.backend.message.policy.DndPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("DndPolicy 테스트")
public class DndPolicyUnitTest {
	
	@Mock
	private BanTimeRepository banTimeRepository;
	
	@InjectMocks
	private DndPolicy dndPolicy;

    @Nested
    @DisplayName("isDndNow 메서드는")
    class IsDndNow {

        @Test
        @DisplayName("DND 시간대에 있으면 true를 반환한다 - 시작 시간 이후")
        void returnTrueWhenAfterStartTime() {
        	
            // given
            Message message = createMessage(1L);
            
            LocalTime startTime = LocalTime.now().minusHours(1);
            LocalTime endTime = LocalTime.now().plusHours(1);
            
            BanTime banTime = createBanTime(startTime, endTime);
            
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.of(banTime));

            // when 
            boolean result = dndPolicy.isDndNow(message);

            // then
            verify(banTimeRepository).findBanTimeByMessageId(1L);
        }
        
        
        @Test
        @DisplayName("DND 시간대에 없으면 false를 반환한다 - 시작 시간 이전")
        void returnFalseWhenBeforeStartTime() {
        	
            // given
            Message message = createMessage(1L);
            
            LocalTime startTime = LocalTime.now().plusHours(1);
            LocalTime endTime = LocalTime.now().plusHours(2);
            
            BanTime banTime = createBanTime(startTime, endTime);
            
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.of(banTime));

            // when 
            boolean result = dndPolicy.isDndNow(message);

            // then
            verify(banTimeRepository).findBanTimeByMessageId(1L);
        }
        
        @Test
        @DisplayName("DND 시간대에 없으면 false를 반환한다 - 종료 시간 이후")
        void returnFalseWhenAfterEndTime() {
        	
        	
            // given
            Message message = createMessage(1L);

            LocalTime startTime = LocalTime.now().minusHours(2);
            LocalTime endTime = LocalTime.now().minusHours(1);
            
            BanTime banTime = createBanTime(startTime, endTime);
            
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.of(banTime));

            // when 
            boolean result = dndPolicy.isDndNow(message);

            // then
            verify(banTimeRepository).findBanTimeByMessageId(1L);
        }

        @Test
        @DisplayName("BanTime을 찾을 수 없으면 예외를 발생시킨다")
        void throwExceptionWhenBanTimeNotFound() {
            // given
            Message message = createMessage(1L);
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dndPolicy.isDndNow(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BenTime not found : 1");
        }
        
        
    }
    
    
    @Nested
    @DisplayName("nextAvailableTime 메서드는")
    class NextAvailableTime {

        @Test
        @DisplayName("DND 시간이 아니면 현재 시간을 반환한다")
        void returnCurrentTimeWhenNotDnd() {
            // given
            Message message = createMessage(1L);
            BanTime banTime = createBanTime(LocalTime.of(22, 0), LocalTime.of(8, 0));
            
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.of(banTime));

            // when
            LocalDateTime result = dndPolicy.nextAvailableTime(message);

            // then
            assertThat(result).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
        }

        @Test
        @DisplayName("DND 시간대이면 종료 시간을 반환한다 - 당일")
        void returnEndTimeWhenDndSameDay() {
            // given
            Message message = createMessage(1L);
            
            
            LocalTime startTime = LocalTime.now().minusHours(1);
            LocalTime endTime = LocalTime.of(23,59);
            BanTime banTime = createBanTime(startTime, endTime);
            
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.of(banTime));

            // when
            LocalDateTime result = dndPolicy.nextAvailableTime(message);

            // then
            assertThat(result.toLocalTime()).isEqualTo(endTime);
        }

        @Test
        @DisplayName("DND 시간대이면 종료 시간을 반환한다 - 다음날")
        void returnEndTimeWhenDndNextDay() {
            // given
            Message message = createMessage(1L);
            LocalTime startTime = LocalTime.now().minusHours(1);
            LocalTime endTime = LocalTime.of(8, 0);
            BanTime banTime = createBanTime(startTime, endTime);
            
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.of(banTime));

            // when
            LocalDateTime result = dndPolicy.nextAvailableTime(message);

            // then
            assertThat(result.toLocalTime()).isEqualTo(endTime);
        }

        @Test
        @DisplayName("BanTime을 찾을 수 없으면 예외를 발생시킨다")
        void throwExceptionWhenBanTimeNotFound() {
            // given
            Message message = createMessage(1L);
            given(banTimeRepository.findBanTimeByMessageId(1L))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dndPolicy.nextAvailableTime(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BenTime not found : 1");
        }
    }
    
    
    
    
    
	/**
	 *  Util Method
	 */
    private Message createMessage(Long id) {
        Message message = mock(Message.class);
        when(message.getId()).thenReturn(id);
        return message;
    }

    private BanTime createBanTime(LocalTime startTime, LocalTime endTime) {
    	return BanTime.builder()
    				  .startTime(startTime)
    				  .endTime(endTime)
    				  .build();
    }

}
