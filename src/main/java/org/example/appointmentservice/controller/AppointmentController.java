package org.example.appointmentservice.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.example.appointmentservice.exception.ApiResponseError;
import org.example.appointmentservice.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.example.appointmentservice.service.DoctorService;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    public AppointmentController(
            AppointmentService appointmentService,
            DoctorService doctorService
    ) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatient(
            @PathVariable Long patientId
    ) {
        return appointmentService.getPatient(patientId);
    }

    @GetMapping("/search")
    @RateLimiter(name = "searchDoctorLimit")
    public ResponseEntity<?> searchDoctor(
            @RequestParam String name
    ) {
        return doctorService.searchDoctor(name);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponseError> handleRateLimit(
            RequestNotPermitted e
    ) {

        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                429,
                "Too Many Requests",
                "Bạn đã gửi quá 5 request trong 10 giây."
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(error);
    }
}