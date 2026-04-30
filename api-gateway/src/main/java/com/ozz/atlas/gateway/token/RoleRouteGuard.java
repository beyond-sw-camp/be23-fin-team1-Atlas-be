package com.ozz.atlas.gateway.token;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class RoleRouteGuard {

    private static final String PLATFORM_ADMIN_ROLE = "ADMIN";

    private static final List<String> CONTROL_TOWER_READ_GUARD_PATHS = List.of(
            "/api/supply/purchase-order/dashboard",
            "/api/supply/items/managed/dashboard",
            "/api/supply/shipments/map",
            "/api/supply/suppliers/connections/summary",
            "/api/supply/settlements/statistics"
    );

    // 플랫폼 관리자가 FE에서 보지 않는 공급망 운영 메뉴 경로는 gateway에서 1차 차단한다.
    private static final List<String> SUPPLY_WRITE_GUARD_PATHS = List.of(
            "/api/supply/lots",
            "/api/supply/mapping",
            "/api/supply/logistics-nodes",
            "/api/supply/returns",
            "/api/supply/shipments",
            "/api/supply/delivery-exceptions",
            "/api/supply/purchase-order",
            "/api/supply/sub-purchase-orders",
            "/api/supply/production-lines",
            "/api/supply/settlements",
            "/api/supply/certificates",
            "/api/supply/certificate-types",
            // ADMIN 협력사 생성을 위한 주석
            // "/api/supply/suppliers",
            "/api/supply/items",
            // ADMIN 카테고리 생성을 위한 주석
            // "/api/supply/item-category",
            "/esg"
    );

    public void validate(String userRole, HttpMethod method, String path) {
        if (method == null || HttpMethod.OPTIONS.equals(method) || HttpMethod.HEAD.equals(method)) {
            return;
        }

        if (!PLATFORM_ADMIN_ROLE.equalsIgnoreCase(userRole)) {
            return;
        }

        if (isControlTowerReadPath(path)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "플랫폼 관리자는 해당 컨트롤타워 메뉴에 접근할 수 없습니다.");
        }

        if (HttpMethod.GET.equals(method)) {
            return;
        }

        boolean blocked = SUPPLY_WRITE_GUARD_PATHS.stream().anyMatch(path::startsWith);
        if (blocked) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "플랫폼 관리자는 해당 공급망 메뉴에 접근할 수 없습니다.");
        }
    }

    private boolean isControlTowerReadPath(String path) {
        return CONTROL_TOWER_READ_GUARD_PATHS.stream().anyMatch(path::startsWith)
                || (path.startsWith("/api/supply/suppliers/") && path.endsWith("/connections/detail"));
    }
}
