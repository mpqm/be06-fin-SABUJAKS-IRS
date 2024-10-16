package com.service.member.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.common.dto.kafka.CreateEstimatorMsg;
import com.service.common.dto.kafka.CreatedEmailVerificationMsg;
import com.service.common.dto.kafka.CreateEmailVerificationMsg;
import com.service.common.dto.kafka.DeleteVerificationMsg;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBroker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Producer
    private <T> ProducerFactory<String, T> createProducerFactory(Class<? extends Serializer<T>> serializerClass) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass);
        return new DefaultKafkaProducerFactory<>(config);
    }

    // Serializer
    public static class GenericKafkaSerializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        @Override
        public byte[] serialize(String topic, T data) {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException(data.getClass().getSimpleName() + " 직렬화 중 오류 발생", e);
            }
        }
    }

    public static class CreateEmailVerificationMsgSerializer extends GenericKafkaSerializer<CreateEmailVerificationMsg> {}
    @Bean(name = "createEmailVerificationMsgKafkaTemplate")
    public KafkaTemplate<String, CreateEmailVerificationMsg> createEmailVerificationMsgKafkaTemplate() {
        return new KafkaTemplate<>(createProducerFactory(CreateEmailVerificationMsgSerializer.class));
    }

    public static class DeleteVerificationMsgSerializer extends GenericKafkaSerializer<DeleteVerificationMsg> {}
    @Bean(name = "deleteVerificationMsgKafkaTemplate")
    public KafkaTemplate<String, DeleteVerificationMsg> deleteVerificationMsgKafkaTemplate() {
        return new KafkaTemplate<>(createProducerFactory(DeleteVerificationMsgSerializer.class));
    }
    // ##########################################################################################################################

    // Consumer
    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<? extends Deserializer<T>> deserializerClass) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "member");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    // Deserializer
    public static class GenericKafkaDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Class<T> targetClass;

        public GenericKafkaDeserializer(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, targetClass);
            } catch (Exception e) {
                throw new RuntimeException(targetClass.getSimpleName() + " 역직렬화 중 오류 발생", e);
            }
        }
    }

    // CreatedEmailVerificationConsumer
    public static class CreatedEmailVerificationMsgDeserializer extends GenericKafkaDeserializer<CreatedEmailVerificationMsg> {
        public CreatedEmailVerificationMsgDeserializer() {
            super(CreatedEmailVerificationMsg.class);
        }
    }
    @Bean
    public ConsumerFactory<String, CreatedEmailVerificationMsg> createdEmailVerificationMsgConsumerFactory() {
        return createConsumerFactory(CreatedEmailVerificationMsgDeserializer.class);
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreatedEmailVerificationMsg> createdEmailVerificationMsgKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CreatedEmailVerificationMsg> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createdEmailVerificationMsgConsumerFactory());
        return factory;
    }

    // createEstimatorMsgConsumer
    public static class CreateEstimatorMsgDeserializer extends GenericKafkaDeserializer<CreateEstimatorMsg> {
        public CreateEstimatorMsgDeserializer() {
            super(CreateEstimatorMsg.class);
        }
    }
    @Bean
    public ConsumerFactory<String, CreateEstimatorMsg> createEstimatorMsgConsumerFactory() {
        return createConsumerFactory(CreateEstimatorMsgDeserializer.class);
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreateEstimatorMsg> createEstimatorMsgKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CreateEstimatorMsg> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createEstimatorMsgConsumerFactory());
        return factory;
    }
}
