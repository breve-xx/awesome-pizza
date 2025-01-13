package org.altervista.breve.awesome.pizza.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.altervista.breve.awesome.pizza.exception.EmptyOrderException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderCodeException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderPizzaException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderQtyException;
import org.altervista.breve.awesome.pizza.exception.InvalidStatusUpdateException;
import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.OrderStatus;
import org.altervista.breve.awesome.pizza.model.Pizza;
import org.altervista.breve.awesome.pizza.model.request.OrderEntry;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.model.response.SubmitOrderResponse;
import org.altervista.breve.awesome.pizza.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {OrderController.class})
class OrderControllerTest {

    private final static UUID AN_UUID = UUID.fromString("21c1bdab-2fa9-424f-84c5-edf207ecba6d");
    private final static LocalDateTime SOMEWHERE_IN_TIME = LocalDateTime.of(1969, 7, 20, 20, 17);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private OrderService orderService;

    @Test
    public void givenARequestWithMissingBodyWhenSubmittingAnOrderThenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/orders"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderAnEmptyOrderExceptionIsThrownThenShouldReturnBadRequest() throws Exception {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenThrow(EmptyOrderException.class);

        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.emptyList());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(new SubmitOrderRequest(Collections.emptyList()))))
                .andExpect(status().isBadRequest());

        verify(orderService).submit(req);
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderAnInvalidOrderPizzaExceptionIsThrownThenShouldReturnBadRequest() throws Exception {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenThrow(InvalidOrderPizzaException.class);

        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-not-supported-pizza-name", 7)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest());

        verify(orderService).submit(req);
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderAnInvalidOrderQtyExceptionIsThrownThenShouldReturnBadRequest() throws Exception {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenThrow(InvalidOrderQtyException.class);

        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-pizza-name", -7)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest());

        verify(orderService).submit(req);
    }

    @Test
    public void givenARequestWhenSubmittingAnOrderThenShouldReturnTheOrderCode() throws Exception {
        when(orderService.submit(any(SubmitOrderRequest.class))).thenReturn(AN_UUID);

        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-pizza-name", 7)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/orders/%s".formatted(AN_UUID)))
                .andExpect(content().string(om.writeValueAsString(new SubmitOrderResponse(AN_UUID))));

        verify(orderService).submit(req);
    }

    @Test
    public void whenSomeNotCompletedOrdersAreFoundThenShouldReturnTheFoundOrders() throws Exception {
        final List<Order> expected = List.of(
                new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.IN_PROGRESS, Map.of(Pizza.MARGHERITA, 7)),
                new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.DIAVOLA, 7))
        );
        when(orderService.findNotCompletedOrders()).thenReturn(expected);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string(om.writeValueAsString(expected)));

        verify(orderService).findNotCompletedOrders();
    }

    @Test
    public void givenAnInvalidOrderCodeThenShouldReturnBadRequest() throws Exception {
        when(orderService.getOrder(anyString())).thenThrow(InvalidOrderCodeException.class);

        mockMvc.perform(get("/api/v1/orders/an-invalid-order-code"))
                .andExpect(status().isBadRequest());

        verify(orderService).getOrder("an-invalid-order-code");
    }

    @Test
    public void givenANotPresentOrderCodeThenShouldReturnNotFound() throws Exception {
        when(orderService.getOrder(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/a-not-present-order-code"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrder("a-not-present-order-code");
    }

    @Test
    public void givenAPresentOrderCodeThenShouldReturnTheOrder() throws Exception {
        final Order expected = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.DIAVOLA, 7));
        when(orderService.getOrder(anyString())).thenReturn(Optional.of(expected));

        mockMvc.perform(get("/api/v1/orders/a-present-order-code"))
                .andExpect(status().isOk())
                .andExpect(content().string(om.writeValueAsString(expected)));

        verify(orderService).getOrder("a-present-order-code");
    }

    @Test
    public void givenAnOrderCodeAndAnInvalidStatusThenShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/a-valid-order-code?status=an-invalid-status"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    public void givenAnInvalidOrderCodeAndAStatusThenShouldReturnBadRequest() throws Exception {
        when(orderService.getOrder(anyString())).thenThrow(InvalidOrderCodeException.class);

        mockMvc.perform(patch("/api/v1/orders/an-invalid-order-code?status=IN_PROGRESS"))
                .andExpect(status().isBadRequest());

        verify(orderService).getOrder("an-invalid-order-code");
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void givenANotPresentOrderCodeAndAStatusThenShouldReturnNotFound() throws Exception {
        when(orderService.getOrder(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/orders/a-not-present-order-code?status=IN_PROGRESS"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrder("a-not-present-order-code");
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void givenAPresentOrderCodeAndAStatusWhenTheStatusCannotBeSetThenShouldReturnUnprocessableEntity() throws Exception {
        final Order expected = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.IN_PROGRESS, Map.of(Pizza.DIAVOLA, 7));
        when(orderService.getOrder(anyString())).thenReturn(Optional.of(expected));
        doThrow(InvalidStatusUpdateException.class)
                .when(orderService)
                .updateStatus(any(Order.class), any(OrderStatus.class));

        mockMvc.perform(patch("/api/v1/orders/a-present-order-code?status=IN_PROGRESS"))
                .andExpect(status().isUnprocessableEntity());

        verify(orderService).getOrder("a-present-order-code");
        verify(orderService).updateStatus(expected, OrderStatus.IN_PROGRESS);
    }

    @Test
    public void givenAPresentOrderCodeAndAStatusThenShouldUpdateTheStatusAndReturnOk() throws Exception {
        final Order expected = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.DIAVOLA, 7));
        when(orderService.getOrder(anyString())).thenReturn(Optional.of(expected));
        doNothing()
                .when(orderService)
                .updateStatus(any(Order.class), any(OrderStatus.class));

        mockMvc.perform(patch("/api/v1/orders/a-present-order-code?status=IN_PROGRESS"))
                .andExpect(status().isOk());

        verify(orderService).getOrder("a-present-order-code");
        verify(orderService).updateStatus(expected, OrderStatus.IN_PROGRESS);
    }
}