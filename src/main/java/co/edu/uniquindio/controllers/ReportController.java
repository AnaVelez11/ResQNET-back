package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.CreateReportRequest;
import co.edu.uniquindio.dto.UpdateReportRequest;
import co.edu.uniquindio.exceptions.ApiResponse;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.services.interfaces.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

}
