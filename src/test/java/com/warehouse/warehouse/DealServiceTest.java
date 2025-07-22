package com.warehouse.warehouse;

import com.warehouse.warehouse.Dto.DealsDto;
import com.warehouse.warehouse.Dto.ResultDto;
import com.warehouse.warehouse.Repository.DealRepository;
import com.warehouse.warehouse.models.Deal;
import com.warehouse.warehouse.services.DealService;
import com.warehouse.warehouse.validation.DealValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealValidator validator;

    @InjectMocks
    private DealService dealService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void importCsv_shouldCountDuplicate_whenDealExistsInRepository() throws IOException {
        String csv = "dealId,fromCurrency,toCurrency,timestamp,amount\n" +
                "D1,USD,EUR,2025-07-22T10:15:30,100.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                csv.getBytes()
        );

        when(validator.validate(any(Deal.class))).thenReturn(null);
        when(dealRepository.existsByDealId("D1")).thenReturn(true);  // simulate duplicate

        ResultDto result = dealService.importCsv(file);

        assertEquals(0, result.getSaved());
        assertEquals(1, result.getDuplicates());
        assertEquals(0, result.getInvalid());
        assertTrue(result.getErrors().stream().anyMatch(msg -> msg.contains("Duplicate deal")));
    }

    @Test
    void importCsv_shouldThrow_whenCsvIsMalformed() {
        String malformedCsv = "bad,data,without,header\ninvalid,line\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.csv",
                "text/csv",
                malformedCsv.getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            dealService.importCsv(file);
        });

        assertTrue(ex.getMessage().startsWith("Invalid CSV format"));
        // Optionally check cause, message, or log output with Mockito + Appender for logs
    }


    @Test
    void importCsv_shouldThrow_whenFileTypeIsInvalid() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "some content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            dealService.importCsv(invalidFile);
        });

        assertEquals("Invalid file type. Only CSV files are allowed", ex.getMessage());
    }

    @Test
    void testImportCsv_success() throws IOException {
        // Prepare a simple CSV content matching your DealsDto fields
        String csv = "dealId,fromCurrency,toCurrency,timestamp,amount\n" +
                "D1,USD,EUR,2023-07-22T10:15:30,100.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                new ByteArrayInputStream(csv.getBytes())
        );

        // Validator returns null (valid)
        when(validator.validate(any(Deal.class))).thenReturn(null);

        // DealRepository says deal doesn't exist
        when(dealRepository.existsByDealId("D1")).thenReturn(false);

        // Capture saved Deal
        ArgumentCaptor<Deal> captor = ArgumentCaptor.forClass(Deal.class);

        ResultDto result = dealService.importCsv(file);

        // Verify save was called once with the right deal
        verify(dealRepository, times(1)).save(captor.capture());

        Deal savedDeal = captor.getValue();
        assertEquals("D1", savedDeal.getDealId());
        assertEquals("USD", savedDeal.getFromCurrency());
        assertEquals("EUR", savedDeal.getToCurrency());
        assertEquals(100.0f, savedDeal.getAmount());

        // Assert results counts
        assertEquals(1, result.getSaved());
        assertEquals(0, result.getDuplicates());
        assertEquals(0, result.getInvalid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testImportCsv_duplicateInFile() throws IOException {
        String csv = "dealId,fromCurrency,toCurrency,timestamp,amount\n" +
                "D1,USD,EUR,2023-07-22T10:15:30,100.0\n" +
                "D1,USD,EUR,2023-07-22T10:15:30,100.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                new ByteArrayInputStream(csv.getBytes())
        );

        when(validator.validate(any(Deal.class))).thenReturn(null);
        when(dealRepository.existsByDealId("D1")).thenReturn(false);

        ResultDto result = dealService.importCsv(file);

        assertEquals(1, result.getSaved());
        assertEquals(1, result.getDuplicates());
        assertEquals(0, result.getInvalid());
        assertTrue(result.getErrors().stream().anyMatch(msg -> msg.contains("Duplicate deal in file")));
    }

    @Test
    void testImportCsv_invalidDeal() throws IOException {
        String csv = "dealId,fromCurrency,toCurrency,timestamp,amount\n" +
                "D1,USD,EUR,2023-07-22T10:15:30,100.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                new ByteArrayInputStream(csv.getBytes())
        );

        // Mark the deal as invalid via validator
        when(validator.validate(any(Deal.class))).thenReturn("Amount must be a positive number");

        ResultDto result = dealService.importCsv(file);

        assertEquals(0, result.getSaved());
        assertEquals(0, result.getDuplicates());
        assertEquals(1, result.getInvalid());
        assertTrue(result.getErrors().stream().anyMatch(msg -> msg.contains("Invalid deal")));
    }
}

