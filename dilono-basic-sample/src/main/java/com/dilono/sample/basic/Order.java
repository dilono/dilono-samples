package com.dilono.sample.basic;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Order {

    private String orderNr;
    private Date orderCreatedAt;
    private String supplierNr;
    private String buyerNr;
    private List<LineItem> lineItems = new ArrayList<>();

    @Data
    public static class LineItem {
        private Integer position;
        private String sku;
        private BigDecimal qty;
        private BigDecimal piecePriceNet;
        private BigDecimal piecePriceGross;
    }
}
