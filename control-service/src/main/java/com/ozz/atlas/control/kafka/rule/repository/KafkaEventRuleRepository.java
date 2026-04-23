package com.ozz.atlas.control.kafka.rule.repository;

import com.ozz.atlas.control.kafka.rule.domain.KafkaEventRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaEventRuleRepository extends JpaRepository<KafkaEventRule, Long> {

    Optional<KafkaEventRule> findByRuleCode(String ruleCode);

    List<KafkaEventRule> findAllByEventType(String eventType);

    Page<KafkaEventRule> findAllByOrderByRuleCodeAsc(Pageable pageable);

    boolean existsByRuleCode(String ruleCode);

    boolean existsByRuleCodeAndRuleCodeNot(String ruleCode, String excludeRuleCode);

    boolean existsByEventTypeAndEnabledTrue(String eventType);
}
