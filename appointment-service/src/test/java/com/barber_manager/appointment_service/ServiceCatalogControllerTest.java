package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.catalog.port.in.IServiceCatalogController;
import com.barber_manager.appointment_service.controller.ServiceCatalogController;
import com.barber_manager.appointment_service.dto.ServiceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IServiceCatalogController serviceCatalogController;

    @Test
    void shouldListServices() throws Exception {
        when(serviceCatalogController.list()).thenReturn(List.of(
                new ServiceResponse(1L, "Haircut", new BigDecimal("60.00"), 2)
        ));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Haircut"))
                .andExpect(jsonPath("$[0].price").value(60.00))
                .andExpect(jsonPath("$[0].slotCount").value(2));
    }
}
