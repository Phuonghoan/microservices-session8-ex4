package org.example.appointmentservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.appointmentservice.exception.ApiResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
public class DoctorService {

    private final RestClient restClient;

    public DoctorService(RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(
            name = "doctorServiceCB",
            fallbackMethod = "getDoctorFallback"
    )
    public ResponseEntity<?> getDoctor(Long doctorId) {

        Object doctor = restClient.get()
                .uri("http://localhost:8082/api/v1/doctors/" + doctorId)
                .retrieve()
                .body(Object.class);

        return ResponseEntity.ok(doctor);
    }

    public ResponseEntity<?> getDoctorFallback(
            Long doctorId,
            Throwable throwable
    ) {

        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                503,
                "Doctor Service Error",
                "Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây."
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }

    public ResponseEntity<?> searchDoctor(String name) {

        Object[] doctors = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8082)
                        .path("/api/v1/doctors/search")
                        .queryParam("name", name)
                        .build())
                .retrieve()
                .body(Object[].class);

        return ResponseEntity.ok(doctors);
    }
}
