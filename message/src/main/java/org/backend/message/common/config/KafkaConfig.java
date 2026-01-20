package org.backend.message.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

	@Bean
	public NewTopic messageTopic() {
		return TopicBuilder.name("message.send.request")
				.partitions(12)
				.replicas(1)
				.build();
	}
}
