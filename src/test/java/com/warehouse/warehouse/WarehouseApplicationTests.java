package com.warehouse.warehouse;

import com.warehouse.warehouse.models.Deal;
import com.warehouse.warehouse.utils.CurrencyCsvReader;
import com.warehouse.warehouse.validation.DealValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class WarehouseApplicationTests {


	@Mock
	private CurrencyCsvReader currencyUtil;

	private DealValidator dealValidator;


    @BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		dealValidator = new DealValidator(currencyUtil);

		// By default, all currencies are valid unless overridden in tests
		when(currencyUtil.isValid(anyString())).thenReturn(true);
	}

	@Test
	void shouldReturnNullForValidDeal() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now().minusMinutes(1))
				.amount(100F)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertNull(error);
	}

	@Test
	void shouldReturnErrorWhenDealIdIsMissing() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId(null)
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(100F)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Deal ID is required", error);
	}

	@Test
	void shouldReturnErrorWhenDealIdIsBlank() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(100.0f)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Deal ID is required", error);
	}

	@Test
	void shouldReturnErrorWhenFromCurrencyIsInvalid() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("XXX") // invalid currency
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(100.0f)
				.build();

		// Mock invalid currency
		when(currencyUtil.isValid("XXX")).thenReturn(false);

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Invalid fromCurrency code", error);
	}

	@Test
	void shouldReturnErrorWhenFromCurrencyIsBlank() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(100F)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Invalid fromCurrency code", error);
	}

	@Test
	void shouldReturnErrorWhenToCurrencyIsInvalid() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("ZZZ") // invalid currency
				.timestamp(LocalDateTime.now())
				.amount(100.0f)
				.build();

		// Mock invalid currency
		when(currencyUtil.isValid("ZZZ")).thenReturn(false);

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Invalid toCurrency code", error);
	}

	@Test
	void shouldReturnErrorWhenToCurrencyIsBlank() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("")
				.timestamp(LocalDateTime.now())
				.amount(100.0f)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Invalid toCurrency code", error);
	}

	@Test
	void shouldReturnErrorWhenTimestampIsNull() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(null)
				.amount(100.0f)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		// Assert
		assertEquals("Invalid or future timestamp", error);
	}

	@Test
	void shouldReturnErrorWhenTimestampIsInFuture() {
		// Arrange
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now().plusDays(1))
				.amount(100.0f)
				.build();

		// Act
		String error = dealValidator.validate(deal);

		assertEquals("Invalid or future timestamp", error);
	}

	@Test
	void shouldReturnErrorWhenAmountIsNull() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(null)
				.build();

		String error = dealValidator.validate(deal);
		assertEquals("Amount must be a positive number", error);
	}

	@Test
	void shouldReturnErrorWhenAmountIsZero() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(0.0F)
				.build();

		String error = dealValidator.validate(deal);

		assertEquals("Amount must be a positive number", error);
	}

	@Test
	void shouldReturnErrorWhenAmountIsNegative() {
		Deal deal = Deal.builder()
				.dealId("123")
				.fromCurrency("USD")
				.toCurrency("EUR")
				.timestamp(LocalDateTime.now())
				.amount(-100F)
				.build();

		String error = dealValidator.validate(deal);

		assertEquals("Amount must be a positive number", error);
	}


}
