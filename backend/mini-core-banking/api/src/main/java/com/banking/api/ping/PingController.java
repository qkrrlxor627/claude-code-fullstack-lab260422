package com.banking.api.ping;

import com.banking.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ping")
@Tag(name = "Ping", description = "서비스 상태 확인용 엔드포인트")
public class PingController {

    @GetMapping
    @Operation(summary = "핑", description = "서비스가 살아있는지 확인합니다.")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "service", "mini-core-banking",
                "timestamp", Instant.now().toString()
        ));
    }
}
