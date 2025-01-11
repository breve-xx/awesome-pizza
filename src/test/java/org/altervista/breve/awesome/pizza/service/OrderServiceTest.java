package org.altervista.breve.awesome.pizza.service;

import org.altervista.breve.awesome.pizza.dao.OrderDao;
import org.altervista.breve.awesome.pizza.exception.EmptyOrderException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderPizzaException;
import org.altervista.breve.awesome.pizza.exception.InvalidOrderQtyException;
import org.altervista.breve.awesome.pizza.model.Order;
import org.altervista.breve.awesome.pizza.model.OrderStatus;
import org.altervista.breve.awesome.pizza.model.Pizza;
import org.altervista.breve.awesome.pizza.model.request.OrderEntry;
import org.altervista.breve.awesome.pizza.model.request.SubmitOrderRequest;
import org.altervista.breve.awesome.pizza.utils.UUIDUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private final static UUID AN_UUID = UUID.fromString("21c1bdab-2fa9-424f-84c5-edf207ecba6d");

    @Mock
    private UUIDUtils utils;

    @Mock
    private OrderDao dao;

    @InjectMocks
    private OrderService sut;

    @Test
    public void givenANullRequestThenShouldThrowEmptyOrderException() {
        assertThrows(EmptyOrderException.class, () -> sut.submit(null));

        verifyNoInteractions(utils);
        verifyNoInteractions(dao);
    }

    @Test
    public void givenARequestWithNullOrderThenShouldThrowEmptyOrderException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(null);

        assertThrows(EmptyOrderException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(dao);
    }

    @Test
    public void givenARequestWithEmptyOrderThenShouldThrowEmptyOrderException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.emptyList());

        assertThrows(EmptyOrderException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(dao);
    }

    @Test
    public void givenARequestWithANotSupportedNameThenShouldThrowInvalidOrderPizzaException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-not-supported-pizza-name", 7)));

        assertThrows(InvalidOrderPizzaException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(dao);
    }

    @Test
    public void givenARequestWithANotSupportedQtyThenShouldThrowInvalidOrderPizzaException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("diavola", -7)));

        assertThrows(InvalidOrderQtyException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(dao);
    }

    @Test
    public void givenARequestWithDuplicatedNamesInTheOrderThenShouldSaveAnOrderCollapsingThem() {
        when(utils.get()).thenReturn(AN_UUID);
        final Order expected = new Order(AN_UUID, OrderStatus.READY, Map.of(Pizza.DIAVOLA, 7));
        when(dao.save(any(Order.class))).thenReturn(expected);

        final SubmitOrderRequest req = new SubmitOrderRequest(List.of(
                new OrderEntry("diavola", 2),
                new OrderEntry("Diavola", 3),
                new OrderEntry("diavolA", 2)
        ));
        final UUID actual = sut.submit(req);

        assertEquals(AN_UUID, actual);
        verify(utils).get();
        verify(dao).save(expected);
    }

    @Test
    public void givenARequestThenShouldSaveAnOrder() {
        when(utils.get()).thenReturn(AN_UUID);
        final Order expected = new Order(AN_UUID, OrderStatus.READY, Map.of(
                Pizza.MARGHERITA, 7,
                Pizza.CAPRICCIOSA, 7,
                Pizza.DIAVOLA, 7
        ));
        when(dao.save(any(Order.class))).thenReturn(expected);

        final SubmitOrderRequest req = new SubmitOrderRequest(List.of(
                new OrderEntry("margherita", 7),
                new OrderEntry("capricciosa", 7),
                new OrderEntry("diavola", 7)
        ));
        final UUID actual = sut.submit(req);

        assertEquals(AN_UUID, actual);
        verify(utils).get();
        verify(dao).save(expected);
    }
}