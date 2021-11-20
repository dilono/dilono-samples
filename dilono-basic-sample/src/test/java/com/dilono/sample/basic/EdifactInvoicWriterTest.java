package com.dilono.sample.basic;

import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.d96a.D96A;
import com.dilono.edifact.toolkit.DTMUtils;
import com.dilono.test.EdifactAssert;
import com.dilono.test.TestResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EdifactInvoicWriterTest {

    private static final TestResource R = TestResource.forClass(EdifactInvoicWriterTest.class);

    @Autowired
    private EdifactInvoicWriter invoicWriter;

    @Autowired
    private ECSClient client;

    final LocalDateTime orderDate = LocalDateTime.of(2020, 12, 20, 14, 30, 0);

    @Test
    void shouldWritePojoToEdifact() throws Exception {
        final List<Invoice> invoices = invoices();
        final String actual = invoicWriter.toEdifact(invoices);

        assertThat(actual).isEqualTo(expected());

    }

    private List<Invoice> invoices() {
        final Invoice.LineItem lineItem1 = new Invoice.LineItem();
        lineItem1.setPosition(1);
        lineItem1.setSku("9783898307529");
        lineItem1.setInvoicedQty(BigDecimal.valueOf(5));
        lineItem1.setPiecePriceNetUnit("PCE");
        lineItem1.setPiecePriceNetAmount(BigDecimal.valueOf(27.5));
        lineItem1.setVatRate(BigDecimal.valueOf(19.0));

        final Invoice.LineItem lineItem2 = new Invoice.LineItem();
        lineItem2.setPosition(2);
        lineItem2.setSku("9783898307539");
        lineItem2.setInvoicedQty(BigDecimal.valueOf(1));
        lineItem2.setPiecePriceNetUnit("PCE");
        lineItem2.setPiecePriceNetAmount(BigDecimal.valueOf(10.87));
        lineItem2.setVatRate(BigDecimal.valueOf(19.0));

        final Invoice.LineItem lineItem3 = new Invoice.LineItem();
        lineItem3.setPosition(3);
        lineItem3.setSku("97838983938472");
        lineItem3.setInvoicedQty(BigDecimal.valueOf(5));
        lineItem3.setPiecePriceNetUnit("PCE");
        lineItem3.setPiecePriceNetAmount(BigDecimal.valueOf(3.85));
        lineItem3.setVatRate(BigDecimal.valueOf(19.0));

        final Invoice invoice = new Invoice();
        invoice.setInvoiceNr("INV123456");
        invoice.setDeliveryDateActual(Date.from(orderDate.plusDays(3).toInstant(ZoneOffset.UTC)));
        invoice.setInvoiceCreatedAt(Date.from(orderDate.plusDays(7).toInstant(ZoneOffset.UTC)));
        invoice.setOrderNr("PO1234");
        invoice.setOrderCreatedAt(Date.from(orderDate.toInstant(ZoneOffset.UTC)));
        invoice.setSupplierNr("0000000000002");
        invoice.setSupplierVatId("DE000000B");
        invoice.setInvoiceeNr("0000000000004");
        invoice.setInvoiceeVatId("DE000000A");
        invoice.setShipToNr("0000000000004");
        invoice.setLineItems(Arrays.asList(lineItem1, lineItem2, lineItem3));

        return Collections.singletonList(invoice);
    }

    private String expected() {
        return "" +
            "UNA:+.? '" +
            "UNB+UNOB:2+0000000000001:14+0000000000002:14+210101:0020+ABCD123+++++EANCOM'" +
            "UNH+1+INVOIC:D:96A:UN:EAN008'" +
            "BGM+380+INV123456'" +
            "DTM+137:20201227:102'" +
            "DTM+35:20201223:102'" +
            "RFF+ON:PO1234'" +
            "DTM+4:20201220:102'" +
            "NAD+SU+0000000000002::9'" +
            "RFF+VA:DE000000B'" +
            "NAD+DP+0000000000004::9'" +
            "NAD+IV+0000000000004::9'" +
            "RFF+VA:DE000000B'" +
            "CUX+2:EUR'" +
            "PAT+1++5::D:30'" +
            "LIN+1++9783898307529:EN'" +
            "QTY+47:5'" +
            "MOA+203:137.5:EUR:4'" +
            "PRI+AAA:27.5:CT:NTP::PCE'" +
            "TAX+7+VAT+++:::19+S'" +
            "LIN+2++9783898307539:EN'" +
            "QTY+47:1'" +
            "MOA+203:10.87:EUR:4'" +
            "PRI+AAA:10.87:CT:NTP::PCE'" +
            "TAX+7+VAT+++:::19+S'" +
            "LIN+3++97838983938472:EN'" +
            "QTY+47:5'" +
            "MOA+203:19.25:EUR:4'" +
            "PRI+AAA:3.85:CT:NTP::PCE'" +
            "TAX+7+VAT+++:::19+S'" +
            "UNS+S'" +
            "CNT+2:3'" +
            "MOA+77:167.62'" +
            "MOA+125:167.62'" +
            "UNT+33+1'" +
            "UNZ+1+ABCD123'";
    }

    @Test
    void docSnippetsWork() throws Exception {
        EdifactAssert.assertThat(new Snippet1().toEdifact(invoices()))
            .pretty()
            .contentEqualTo(R, "snippet1.edi");

        EdifactAssert.assertThat(new Snippet2().toEdifact(invoices()))
            .pretty()
            .contentEqualTo(R, "snippet2.edi");

        EdifactAssert.assertThat(new Snippet3().toEdifact(invoices()))
            .pretty()
            .contentEqualTo(R, "snippet3.edi");
    }

    class Snippet1 {
        // tag::howto-write-edifact-1[]
        public String toEdifact(final List<Invoice> invoices) throws Exception {
            return D96A.writer(client)
                .una(una -> una.defaults()) // <1>
                .unb(unb -> unb // <2>
                    .unoc()
                    .version3()
                    .sender(sender -> sender
                        .id("0000000000001")
                        .codeQualifier("14"))
                    .recipient(recipient -> recipient
                        .id("0000000000002")
                        .codeQualifier("14"))
                    .interchangeId("ABCD123")
                    .interchangeTimestamp(now())
                    .eancom())
                .invoic(() -> invoices.stream(), (invoice, invoic) -> invoic // <3>
                    .unh(unh -> unh.invoic().d96a().un().ean008()) // <4>
                    .bgm(bgm -> bgm // <5>
                        .data(bgm_ -> bgm_
                            .c002DocumentMessageName(c002 -> c002
                                .e1001DocumentMessageNameCoded("380")) // Commercial invoice
                            .e1004DocumentMessageNumber(invoice.getInvoiceNr()))) // <6>
                    .dtm(dtm -> dtm.data(dtm_ -> dtm_
                        .c507DateTimePeriod(c507 -> c507
                            .e2005DateTimePeriodQualifier("137") // Document/message date/time
                            .e2379DateTimePeriodFormatQualifier("102")
                            .e2380DateTimePeriod(DTMUtils.format(invoice.getInvoiceCreatedAt(), "102"))))))
                //...
                .dumpToString();
        }
        // end::howto-write-edifact-1[]
    }

    class Snippet2 {
        // tag::howto-write-edifact-2[]
        public String toEdifact(final List<Invoice> invoices) throws Exception {
            return D96A.writer(client)
                .invoic(() -> invoices.stream(), (invoice, invoic) -> invoic
                    //...
                    .sg2(sg2 -> sg2.data(sg2_ -> sg2_  // <1>
                        .nad(nad -> nad.data(nad_ -> nad_
                            .e3035PartyQualifier("DP") // Ship To
                            .c082PartyIdentificationDetails(c082 -> c082
                                .e3039PartyIdIdentification(invoice.getShipToNr())
                                .e3055CodeListResponsibleAgencyCoded("9")))))) // GLN
                    .sg2(sg2 -> sg2.data(sg2_ -> sg2_ // <2>
                        .nad(nad -> nad.data(nad_ -> nad_
                            .e3035PartyQualifier("IV") // Invoicee
                            .c082PartyIdentificationDetails(c082 -> c082
                                .e3039PartyIdIdentification(invoice.getInvoiceeNr())
                                .e3055CodeListResponsibleAgencyCoded("9"))))  // GLN
                        .sg3(sg3 -> sg3.data(sg3_ -> sg3_
                            .rff(rff -> rff.data(rff_ -> rff_ // <3>
                                .c506Reference(c506 -> c506
                                    .e1153ReferenceQualifier("VA") // VAT ID
                                    .e1154ReferenceNumber(invoice.getSupplierVatId()))))))))
                    //...
                    .unt())
                .dumpToString();
        }
        // end::howto-write-edifact-2[]
    }

    class Snippet3 {
        // tag::howto-write-edifact-3[]
        public String toEdifact(final List<Invoice> invoices) throws Exception {
            return D96A.writer(client)
                .invoic(() -> invoices.stream(), (invoice, invoic) -> invoic
                    // ...
                    .sg25(() -> invoice.getLineItems().stream(), (lineItem, sg25) -> sg25.data(sg25_ -> sg25_ // <1>
                        .lin(lin -> lin.data(lin_ -> lin_
                            .e1082LineItemNumber(lineItem.getPosition())
                            .c212ItemNumberIdentification(c212 -> c212
                                .e7140ItemNumber(lineItem.getSku())
                                .e7143ItemNumberTypeCoded("EN"))))  // EAN
                        .qty(qty -> qty.data(qty_ -> qty_
                            .c186QuantityDetails(c186 -> c186
                                .e6063QuantityQualifier("47") // Invoiced quantity
                                .e6060Quantity(lineItem.getInvoicedQty().intValue()))))
                        .sg28(sg28 -> sg28.data(sg28_ -> sg28_
                            .pri(pri -> pri.data(pri_ -> pri_
                                .c509PriceInformation(c509 -> c509
                                    .e5125PriceQualifier("AAA") // Calculation net
                                    .e5118Price(lineItem.getPiecePriceNetAmount())
                                    .e6411MeasureUnitQualifier(lineItem.getPiecePriceNetUnit())
                                    .e5375PriceTypeCoded("CT") // Contract
                                    .e5387PriceTypeQualifier("NTP")))))))) // Net unit price
                    // ...
                    .unt())
                .dumpToString();
        }
        // end::howto-write-edifact-3[]
    }

    private Date now() {
        return Date.from(LocalDateTime.of(2020, 12, 31, 23, 20, 0).toInstant(ZoneOffset.UTC));
    }
}
