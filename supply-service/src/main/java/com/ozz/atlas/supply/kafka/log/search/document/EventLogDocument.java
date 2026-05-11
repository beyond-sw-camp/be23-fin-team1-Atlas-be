package com.ozz.atlas.supply.kafka.log.search.document;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.supply.kafka.log.EventLog;
import com.ozz.atlas.supply.kafka.log.EventLogStatus;
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
    private String documentId;

    @Field(type = FieldType.Long)
    private Long eventLogId;

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

    @Field(type = FieldType.Keyword)
    private AggregateType aggregateType;

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

    @Field(type = FieldType.Keyword)
    private EventLogStatus status;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime publishedAt;

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

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    public static EventLogDocument fromEntity(EventLog eventLog) {
        return EventLogDocument.builder()
                .documentId("supply-" + eventLog.getId())
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
