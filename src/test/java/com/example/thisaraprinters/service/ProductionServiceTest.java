package com.example.thisaraprinters.service;

import com.example.thisaraprinters.model.EmployeeModel;
import com.example.thisaraprinters.model.ProductionModel;
import com.example.thisaraprinters.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {
    @Mock private ProductionRepo productionRepo;
    @Mock private ProductionStatusHistoryRepo historyRepo;
    @Mock private ProductionStockReservationRepo reservationRepo;
    @Mock private StockLotsRepo stockLotsRepo;
    @Mock private EmployeeRepo employeeRepo;
    private ProductionService service;

    @BeforeEach void setUp() {
        service = new ProductionService(productionRepo, historyRepo, reservationRepo, stockLotsRepo, employeeRepo);
    }

    @Test void readyToDeliverSetsCompletionDateAndRecordsHistory() {
        ProductionModel job = new ProductionModel(); job.setOrderId("ORD-1"); job.setStatus("Finishing");
        when(productionRepo.findByOrderId("ORD-1")).thenReturn(Optional.of(job));

        service.updateJobStatus("ORD-1", "Ready to Deliver", "admin");

        assertEquals("Ready to Deliver", job.getStatus());
        assertNotNull(job.getCompletedAt());
        verify(historyRepo).save(any());
    }

    @Test void assignmentPersistsSelectedEmployee() {
        ProductionModel job = new ProductionModel(); job.setOrderId("ORD-2");
        EmployeeModel employee = new EmployeeModel(); employee.setId(4L);
        when(productionRepo.findByOrderId("ORD-2")).thenReturn(Optional.of(job));
        when(employeeRepo.findById(4L)).thenReturn(Optional.of(employee));

        service.assignEmployee("ORD-2", 4L, "admin");

        assertSame(employee, job.getAssignedEmployee());
        verify(productionRepo).save(job);
    }
}
