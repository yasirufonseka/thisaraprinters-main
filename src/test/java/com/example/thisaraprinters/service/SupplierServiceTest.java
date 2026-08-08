package com.example.thisaraprinters.service;

import com.example.thisaraprinters.config.EmailService;
import com.example.thisaraprinters.model.Category;
import com.example.thisaraprinters.model.PriceRequest;
import com.example.thisaraprinters.model.Supplier;
import com.example.thisaraprinters.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepo supplierRepo;

    @Mock
    private MaterialRepo materialRepo;

    @Mock
    private PriceRequestRepo priceRequestRepo;

    @Mock
    private PriceRequestReplyRepo priceRequestReplyRepo;

    @Mock
    private PurchaseOrderRepo purchaseOrderRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private SupplierPaymentRepo supplierPaymentRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private MaterialVariantRepo materialVariantRepo;

    @Test
    void shouldReturnSuppliersMatchingPriceRequestCategory() {
        SupplierService service = new SupplierService(
                supplierRepo,
                materialRepo,
                priceRequestRepo,
                priceRequestReplyRepo,
                purchaseOrderRepo,
                categoryRepo,
                supplierPaymentRepo
        );

        PriceRequest priceRequest = new PriceRequest();
        priceRequest.setId(1);
        priceRequest.setMaterialcategory("Paper");

        Category paperCategory = new Category();
        paperCategory.setId(10);
        paperCategory.setName("Paper");

        Category inkCategory = new Category();
        inkCategory.setId(11);
        inkCategory.setName("Ink");

        Supplier matchingSupplier = new Supplier();
        matchingSupplier.setId(1);
        matchingSupplier.setCompanyname("Apex Traders");
        matchingSupplier.setCategory(List.of(paperCategory));

        Supplier nonMatchingSupplier = new Supplier();
        nonMatchingSupplier.setId(2);
        nonMatchingSupplier.setCompanyname("Ink Only");
        nonMatchingSupplier.setCategory(List.of(inkCategory));

        when(priceRequestRepo.findById(1)).thenReturn(Optional.of(priceRequest));
        when(supplierRepo.findAll()).thenReturn(List.of(matchingSupplier, nonMatchingSupplier));

        List<Supplier> result = service.getSuppliersByPriceRequestCategory(1);

        assertEquals(1, result.size());
        assertEquals("Apex Traders", result.get(0).getCompanyname());
    }
}
