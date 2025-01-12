package org.altervista.breve.awesome.pizza.service;

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
import org.altervista.breve.awesome.pizza.repository.OrderRepository;
import org.altervista.breve.awesome.pizza.utils.UUIDUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private final static UUID AN_UUID = UUID.fromString("21c1bdab-2fa9-424f-84c5-edf207ecba6d");
    private final static LocalDateTime SOMEWHERE_IN_TIME = LocalDateTime.of(1969, 7, 20, 20, 17);

    private final Order inProgressOrder = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.IN_PROGRESS, Map.of(Pizza.DIAVOLA, 7));
    private final Order readyOrder1 = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.MARGHERITA, 7));
    private final Order readyOrder2 = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.CAPRICCIOSA, 7));

    @Mock
    private UUIDUtils utils;

    @Mock
    private OrderRepository repository;

    @InjectMocks
    private OrderService sut;

    @Test
    public void givenANullRequestThenShouldThrowEmptyOrderException() {
        assertThrows(EmptyOrderException.class, () -> sut.submit(null));

        verifyNoInteractions(utils);
        verifyNoInteractions(repository);
    }

    @Test
    public void givenARequestWithNullOrderThenShouldThrowEmptyOrderException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(null);

        assertThrows(EmptyOrderException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(repository);
    }

    @Test
    public void givenARequestWithEmptyOrderThenShouldThrowEmptyOrderException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.emptyList());

        assertThrows(EmptyOrderException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(repository);
    }

    @Test
    public void givenARequestWithANotSupportedNameThenShouldThrowInvalidOrderPizzaException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("a-not-supported-pizza-name", 7)));

        assertThrows(InvalidOrderPizzaException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(repository);
    }

    @Test
    public void givenARequestWithANotSupportedQtyThenShouldThrowInvalidOrderPizzaException() {
        final SubmitOrderRequest req = new SubmitOrderRequest(Collections.singletonList(new OrderEntry("diavola", -7)));

        assertThrows(InvalidOrderQtyException.class, () -> sut.submit(req));

        verifyNoInteractions(utils);
        verifyNoInteractions(repository);
    }

    @Test
    public void givenARequestWithDuplicatedNamesInTheOrderThenShouldSaveAnOrderCollapsingThem() {
        when(utils.get()).thenReturn(AN_UUID);
        final Order expected = new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.DIAVOLA, 7));
        when(repository.save(any(Order.class))).thenReturn(expected);

        final SubmitOrderRequest req = new SubmitOrderRequest(List.of(
                new OrderEntry("diavola", 2),
                new OrderEntry("Diavola", 3),
                new OrderEntry("diavolA", 2)
        ));
        final UUID actual = sut.submit(req);

        assertEquals(AN_UUID, actual);
        verify(utils).get();
        verify(repository).save(expected);
    }

    @Test
    public void givenARequestThenShouldSaveAnOrder() {
        when(utils.get()).thenReturn(AN_UUID);
        final Order expected = new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(
                Pizza.MARGHERITA, 7,
                Pizza.CAPRICCIOSA, 7,
                Pizza.DIAVOLA, 7
        ));
        when(repository.save(any(Order.class))).thenReturn(expected);

        final SubmitOrderRequest req = new SubmitOrderRequest(List.of(
                new OrderEntry("margherita", 7),
                new OrderEntry("capricciosa", 7),
                new OrderEntry("diavola", 7)
        ));
        final UUID actual = sut.submit(req);

        assertEquals(AN_UUID, actual);
        verify(utils).get();
        verify(repository).save(expected);
    }

    @Test
    public void whenNoInProgressAndNoReadyOrdersArePresentThenShouldReturnEmptyList() {
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.READY)).thenReturn(Collections.emptyList());

        final List<Order> actual = sut.findNotCompletedOrders();

        assertEquals(Collections.emptyList(), actual);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.READY);
    }

    @Test
    public void whenNoInProgressAndSomeReadyOrdersArePresentThenShouldReturnOnlyReadyOrders() {
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.READY)).thenReturn(List.of(readyOrder1, readyOrder2));

        final List<Order> actual = sut.findNotCompletedOrders();

        assertEquals(List.of(readyOrder1, readyOrder2), actual);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.READY);
    }

    @Test
    public void whenAnInProgressAndNoReadyOrdersArePresentThenShouldReturnOnlyTheInProgressOrder() {
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS)).thenReturn(List.of(inProgressOrder));
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.READY)).thenReturn(Collections.emptyList());

        final List<Order> actual = sut.findNotCompletedOrders();

        assertEquals(List.of(inProgressOrder), actual);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.READY);
    }

    @Test
    public void whenAnInProgressAndSomeReadyOrdersArePresentThenShouldReturnAllTheOrders() {
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS)).thenReturn(List.of(inProgressOrder));
        when(repository.findByStatusOrderBySubmittedAtAsc(OrderStatus.READY)).thenReturn(List.of(readyOrder2, readyOrder1));

        final List<Order> actual = sut.findNotCompletedOrders();

        assertEquals(List.of(inProgressOrder, readyOrder2, readyOrder1), actual);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS);
        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.READY);
    }

    @Test
    public void givenAnInvalidOrderCodeThenShouldThrowInvalidOrderCodeException() {
        assertThrows(InvalidOrderCodeException.class, () -> sut.getOrder("an-invalid-order-code"));

        verifyNoInteractions(repository);
    }

    @Test
    public void givenANotPresentOrderCodeThenShouldReturnAnEmptyOptional() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        final Optional<Order> actual = sut.getOrder(AN_UUID.toString());

        assertEquals(Optional.empty(), actual);
        verify(repository).findById(AN_UUID);
    }

    @Test
    public void givenAPresentOrderCodeThenShouldReturnTheOrder() {
        final Order expected = new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(
                Pizza.MARGHERITA, 7,
                Pizza.CAPRICCIOSA, 7,
                Pizza.DIAVOLA, 7
        ));
        when(repository.findById(any(UUID.class))).thenReturn(Optional.of(expected));

        final Optional<Order> actual = sut.getOrder(AN_UUID.toString());

        assertEquals(Optional.of(expected), actual);
        verify(repository).findById(AN_UUID);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    public void givenAnOrderAndAStatusWhenTheOrderIsAlreadyInThatStatusThenShouldDoNothing(final OrderStatus status) {
        final Order order = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, status, Map.of(Pizza.MARGHERITA, 7));

        sut.updateStatus(order, status);

        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"IN_PROGRESS", "DELIVERED"})
    public void givenAnOrderNotInReadyStatusWhenTryingToSetReadyStatusThenShouldThrowInvalidStatusUpdateException(final OrderStatus status) {
        final Order order = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, status, Map.of(Pizza.MARGHERITA, 7));

        assertThrows(InvalidStatusUpdateException.class, () -> sut.updateStatus(order, OrderStatus.READY));

        verifyNoInteractions(repository);
    }

    @Test
    public void givenADeliveredOrderWhenTryingToSetInProgressStatusThenShouldThrowInvalidStatusUpdateException() {
        final Order order = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.DELIVERED, Map.of(Pizza.MARGHERITA, 7));

        assertThrows(InvalidStatusUpdateException.class, () -> sut.updateStatus(order, OrderStatus.IN_PROGRESS));

        verifyNoInteractions(repository);
    }

    @Test
    public void givenAReadyOrderWhenTryingToSetInProgressStatusAndAnotherOrderHasInProgressStatusThenShouldThrowInvalidStatusUpdateException() {
        final Order order = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.MARGHERITA, 7));
        when(repository.findByStatusOrderBySubmittedAtAsc(any(OrderStatus.class))).thenReturn(Collections.singletonList(inProgressOrder));

        assertThrows(InvalidStatusUpdateException.class, () -> sut.updateStatus(order, OrderStatus.IN_PROGRESS));

        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void givenAReadyOrderWhenTryingToSetInProgressStatusAndNoOrderHasInProgressStatusThenShouldSaveTheOrderWithInProgressStatus() {
        final Order order = new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.MARGHERITA, 7));
        when(repository.findByStatusOrderBySubmittedAtAsc(any(OrderStatus.class))).thenReturn(Collections.emptyList());

        sut.updateStatus(order, OrderStatus.IN_PROGRESS);

        verify(repository).findByStatusOrderBySubmittedAtAsc(OrderStatus.IN_PROGRESS);
        verify(repository).save(new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.IN_PROGRESS, Map.of(Pizza.MARGHERITA, 7)));
    }

    @Test
    public void givenAReadyOrderWhenTryingToSetDeliveredStatusThenShouldThrowInvalidStatusUpdateException() {
        final Order order = new Order(UUID.randomUUID(), SOMEWHERE_IN_TIME, OrderStatus.READY, Map.of(Pizza.MARGHERITA, 7));

        assertThrows(InvalidStatusUpdateException.class, () -> sut.updateStatus(order, OrderStatus.DELIVERED));

        verifyNoInteractions(repository);
    }

    @Test
    public void givenAnInProgressOrderWhenTryingToSetDeliveredStatusThenShouldSaveTheOrderWithDeliveredStatus() {
        final Order order = new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.IN_PROGRESS, Map.of(Pizza.MARGHERITA, 7));

        sut.updateStatus(order, OrderStatus.DELIVERED);

        verify(repository).save(new Order(AN_UUID, SOMEWHERE_IN_TIME, OrderStatus.DELIVERED, Map.of(Pizza.MARGHERITA, 7)));
    }
}