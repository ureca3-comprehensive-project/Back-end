package org.backend.billing.message.type;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MessageTypeConverter implements AttributeConverter<MessageType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MessageType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public MessageType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : MessageType.fromCode(dbData);
    }
}