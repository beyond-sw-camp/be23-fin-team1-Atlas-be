package com.ozz.atlas.control.kafka.log.search.document;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.control.kafka.log.EventLog;
import com.ozz.atlas.control.kafka.log.EventLogStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "event-logs")
@Setting(settingPath = "/elasticsearch/event-log-settings.json")
public class EventLogDocument {

    @Id
    private Long eventLogId;

    // Kafka 이벤트 고유 ID
    @Field(type = FieldType.Keyword)
    private String eventId;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "event_log_ngram_analyzer",
                            searchAnalyzer = "event_log_search_analyzer"
                    )
            }
    )
    private String topic;

    // 이벤트 타입 예: shipment.created, recommendation.generated
    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "event_log_ngram_analyzer",
                            searchAnalyzer = "event_log_search_analyzer"
                    )
            }
    )
    private String eventType;

    // 이벤트가 가리키는 도메인 종류
    @Field(type = FieldType.Keyword)
    private AggregateType aggregateType;

    // 이벤트가 가리키는 도메인 publicId
    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "event_log_ngram_analyzer",
                            searchAnalyzer = "event_log_search_analyzer"
                    )
            }
    )
    private String aggregatePublicId;

    // Kafka로 발행한 원본 이벤트 JSON
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "event_log_ngram_analyzer",
                            searchAnalyzer = "event_log_search_analyzer"
                    )
            }
    )
    private String eventJson;

    // Kafka 발행 결과 상태 PUBLISHED 또는 FAILED
    @Field(type = FieldType.Keyword)
    private EventLogStatus status;

    // Kafka 발행 성공 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime publishedAt;

    // 발행 실패 시 마지막 에러 메시지
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "event_log_ngram_analyzer",
                            searchAnalyzer = "event_log_search_analyzer"
                    )
            }
    )
    private String lastError;

    // DB event_log 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // DB event_log 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static EventLogDocument fromEntity(EventLog eventLog) {
        return EventLogDocument.builder()
                .eventLogId(eventLog.getId())
                .eventId(eventLog.getEventId())
                .topic(eventLog.getTopic())
                .eventType(eventLog.getEventType())
                .aggregateType(eventLog.getAggregateType())
                .aggregatePublicId(eventLog.getAggregatePublicId())
                .eventJson(eventLog.getEventJson())
                .status(eventLog.getStatus())
                .publishedAt(eventLog.getPublishedAt())
                .lastError(eventLog.getLastError())
                .createdAt(eventLog.getCreatedAt())
                .updatedAt(eventLog.getUpdatedAt())
                .build();
    }
}
