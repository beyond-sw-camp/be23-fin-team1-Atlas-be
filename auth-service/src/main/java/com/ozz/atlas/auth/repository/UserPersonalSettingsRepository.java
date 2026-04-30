package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.UserPersonalSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPersonalSettingsRepository extends JpaRepository<UserPersonalSettings, Long> {

    Optional<UserPersonalSettings> findByUserPublicId(String userPublicId);
}
