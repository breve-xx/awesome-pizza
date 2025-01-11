package org.altervista.breve.awesome.pizza.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderPizzaException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderQtyException;
import org.altervista.breve.awesome.pizza.model.request.OrderEntry;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.model.response.SubmitOrderResponse;
import org.altervista.breve.awesome.pizza.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {OrderController.class})
class OrderControllerTest {

    private final static UUID AN_UUID = UUID.fromString("21c1bdab-2fa9-424f-84c5-edf207ecba6d");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenReturn(AN_UUID);
    }

    @Test
    public void givenARequestWithMissingBodyWhenSubmittingAnOrderThenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/order"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenARequestWithMissingOrderWhenSubmittingAnOrderThenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenARequestWithEmptyOrderWhenSubmittingAnOrderThenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new SubmitOrderRequest(Collections.emptyList()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderAnInvalidOrderPizzaExceptionIsThrownThenShouldReturnBadRequest() throws Exception {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenThrow(InvalidOrderPizzaException.class);

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-not-supported-pizza-name", 7))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderAnInvalidOrderQtyExceptionIsThrownThenShouldReturnBadRequest() throws Exception {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenThrow(InvalidOrderQtyException.class);

        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-pizza-name", -7))))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderThenShouldReturnTheOrderCode() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-pizza-name", 7))))))
                .andExpect(status().isOk())
                .andExpect(content().string(om.writeValueAsString(new SubmitOrderResponse(AN_UUID))));
    }
}