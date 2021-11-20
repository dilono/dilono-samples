package com.dilono.sample.basic;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
class Invoice {

    private String invoiceNr;
    private Date invoiceCreatedAt;
    private Date deliveryDateActual;

    private String orderNr;
    private Date orderCreatedAt;

    private String supplierNr;
    private String supplierVatId;

    private String shipToNr;

    private String invoiceeNr;
    private String invoiceeVatId;

    private List<LineItem> lineItems = new ArrayList<>();

    public BigDecimal sumAmount() {
        return lineItems.stream()
            .map(LineItem::sumNetAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ONE);

    }

    public BigDecimal sumTaxableAmount() {
        return lineItems.stream()
            .map(LineItem::sumNetAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ONE);
    }

    @Data
    static class LineItem {
        private int position;
        private String sku;
        private BigDecimal invoicedQty;
        private BigDecimal piecePriceNetAmount;
        private String piecePriceNetUnit;
        private BigDecimal vatRate;


        BigDecimal sumNetAmount() {
            return invoicedQty.multiply(piecePriceNetAmount)
                .setScale(2, RoundingMode.HALF_UP);
        }
    }
}
