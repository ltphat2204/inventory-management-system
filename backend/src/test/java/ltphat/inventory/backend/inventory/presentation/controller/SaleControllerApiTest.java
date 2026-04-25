package ltphat.inventory.backend.inventory.presentation.controller;

import ltphat.inventory.backend.inventory.application.service.ISaleService;
import ltphat.inventory.backend.inventory.domain.exception.InsufficientStockException;
import ltphat.inventory.backend.shared.api.exception.GlobalExceptionHandler;
import ltphat.inventory.backend.shared.api.exception.IdempotencyConflictException;
import ltphat.inventory.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
@Import(GlobalExceptionHandler.class)
class SaleControllerApiTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ISaleService saleService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @Test
  @WithMockUser(roles = "CASHIER")
  void createSale_shouldReturn400_whenInsufficientStock() throws Exception {
    when(saleService.createSale(any()))
        .thenThrow(new InsufficientStockException("Insufficient stock for variant ID: 101"));

    mockMvc.perform(post("/sales")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "idempotencyKey": "idem-400",
              "items": [
                {"variantId": 101, "quantity": 2}
              ]
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error_code").value("INSUFFICIENT_STOCK"));
  }

  @Test
  @WithMockUser(roles = "CASHIER")
  void createSale_shouldReturn409_whenIdempotencyConflict() throws Exception {
    when(saleService.createSale(any()))
        .thenThrow(new IdempotencyConflictException("Duplicate idempotency key: idem-409"));

    mockMvc.perform(post("/sales")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "idempotencyKey": "idem-409",
              "items": [
                {"variantId": 101, "quantity": 1}
              ]
            }
            """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error_code").value("IDEMPOTENCY_CONFLICT"));
  }
}
