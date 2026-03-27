package ltphat.inventory.backend.catalog.presentation.controller;

import ltphat.inventory.backend.catalog.application.dto.VariantDto;
import ltphat.inventory.backend.catalog.application.dto.VariantResponse;
import ltphat.inventory.backend.catalog.application.service.ProductVariantService;
import ltphat.inventory.backend.shared.api.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantControllerTest {

    @Mock
    private ProductVariantService variantService;

    @InjectMocks
    private ProductVariantController controller;

    @Test
    void getProductVariants_shouldReturn200() {
        VariantResponse variant = new VariantResponse();
        variant.setId(1L);
        variant.setCurrentQuantity(5);
        when(variantService.getVariantsByProductId(10L, true)).thenReturn(List.of(variant));

        ResponseEntity<ApiResponse<List<VariantResponse>>> result = controller.getProductVariants(10L, true);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).hasSize(1);
    }

    @Test
    void addVariant_shouldReturn201() {
        VariantDto request = new VariantDto();
        request.setSize("S");
        request.setColor("Red");
        VariantResponse response = new VariantResponse();
        response.setId(2L);

        when(variantService.addVariantToProduct(10L, request)).thenReturn(response);

        ResponseEntity<ApiResponse<VariantResponse>> result = controller.addVariant(10L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(2L);
    }

    @Test
    void getVariant_shouldReturn200() {
        VariantResponse response = new VariantResponse();
        response.setId(3L);
        when(variantService.getVariant(10L, 3L)).thenReturn(response);

        ResponseEntity<ApiResponse<VariantResponse>> result = controller.getVariant(10L, 3L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(3L);
    }

    @Test
    void updateVariant_shouldReturn200() {
        VariantDto request = new VariantDto();
        request.setSize("M");
        request.setColor("Blue");
        VariantResponse response = new VariantResponse();
        response.setId(4L);
        when(variantService.updateVariant(10L, 4L, request)).thenReturn(response);

        ResponseEntity<ApiResponse<VariantResponse>> result = controller.updateVariant(10L, 4L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getId()).isEqualTo(4L);
    }

    @Test
    void deleteVariant_shouldReturn200() {
        doNothing().when(variantService).deleteVariant(10L, 5L);

        ResponseEntity<ApiResponse<Void>> result = controller.deleteVariant(10L, 5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
    }

    @Test
    void getVariantByBarcode_shouldReturn200() {
        VariantResponse response = new VariantResponse();
        response.setId(6L);
        response.setProductNameVn("Ao thun");
        response.setCurrentQuantity(5);
        when(variantService.getVariantByBarcode("1234567890")).thenReturn(response);

        ResponseEntity<ApiResponse<VariantResponse>> result = controller.getVariantByBarcode("1234567890");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getProductNameVn()).isEqualTo("Ao thun");
        assertThat(result.getBody().getData().getCurrentQuantity()).isEqualTo(5);
    }
}