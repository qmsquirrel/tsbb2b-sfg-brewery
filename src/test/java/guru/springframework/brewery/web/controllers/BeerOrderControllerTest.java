package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import guru.springframework.brewery.web.model.OrderStatusEnum;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerOrderDto validOrder;

    @BeforeEach
    void setUp() {
        validOrder = BeerOrderDto.builder().id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .orderStatus(OrderStatusEnum.NEW)
                .version(1)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @Test
    void getOrder() throws Exception {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        given(beerOrderService.getOrderById(any(),any())).willReturn(validOrder);

        MvcResult result = mockMvc.perform(
                    get("/api/v1/customers/{customerId}/orders/{orderId}",validOrder.getCustomerId(), validOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(validOrder.getId().toString())))
                .andExpect(jsonPath("$.customerId", is(validOrder.getCustomerId().toString())))
                .andExpect(jsonPath("$.createdDate",
                        is(dateTimeFormatter.format(validOrder.getCreatedDate()))))
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @DisplayName("List Ops - ")
    @Nested
    public class TestListOperations {

        BeerOrderPagedList beerOrderPagedList;

        @BeforeEach
        void setUp() {
            List<BeerOrderDto> orders = new ArrayList<>();
            orders.add(validOrder);
            orders.add(BeerOrderDto.builder().id(UUID.randomUUID())
                    .customerId(UUID.randomUUID())
                    .orderStatus(OrderStatusEnum.NEW)
                    .version(1)
                    .createdDate(OffsetDateTime.now())
                    .lastModifiedDate(OffsetDateTime.now())
                    .build());

            beerOrderPagedList = new BeerOrderPagedList(orders, PageRequest.of(1,1), 2L);

            given(beerOrderService.listOrders(any(), any())).willReturn(beerOrderPagedList);

        }

        @DisplayName("Test list orders")
        @Test
        void listOrders() throws Exception {
            mockMvc.perform(
                        get("/api/v1/customers/{customerId}/orders", validOrder.getCustomerId())
                        .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(validOrder.getId().toString())));

        }

    }
}