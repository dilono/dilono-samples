package com.dilono.sample.basic;

import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.d96a.D96A;
import com.dilono.edifact.toolkit.DTMUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Component
public class EdifactInvoicWriter {

    private final ECSClient ecsClient;

    public EdifactInvoicWriter(ECSClient ecsClient) {
        this.ecsClient = ecsClient;
    }

    String toEdifact(final List<Invoice> invoices) throws Exception {
        return D96A.writer(ecsClient)
            .unb(unb -> unb
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
            .invoic(() -> invoices.stream(), (invoice, invoic) -> invoic
                .unh(unh -> unh.invoic().d96a().un().ean008())
                .bgm(bgm -> bgm
                    .data(bgm_ -> bgm_
                        .c002DocumentMessageName(c002 -> c002
                            .e1001DocumentMessageNameCoded("380")) // Commercial invoice
                        .e1004DocumentMessageNumber(invoice.getInvoiceNr())))
                .dtm(dtm -> dtm.data(dtm_ -> dtm_
                    .c507DateTimePeriod(c507 -> c507
                        .e2005DateTimePeriodQualifier("137") // Document/message date/time
                        .e2379DateTimePeriodFormatQualifier("102")
                        .e2380DateTimePeriod(DTMUtils.format(invoice.getInvoiceCreatedAt(), "102")))))
                .dtm(dtm -> dtm.data(dtm_ -> dtm_
                    .c507DateTimePeriod(c507 -> c507
                        .e2005DateTimePeriodQualifier("35") // Delivery date/time, actual
                        .e2379DateTimePeriodFormatQualifier("102")
                        .e2380DateTimePeriod(DTMUtils.format(invoice.getDeliveryDateActual(), "102")))))
                .sg1(sg1 -> sg1.data(sg1_ -> sg1_
                    .rff(rff -> rff.data(rff_ -> rff_
                        .c506Reference(c506 -> c506
                            .e1153ReferenceQualifier("ON") // Order number (purchase)
                            .e1154ReferenceNumber(invoice.getOrderNr()))))
                    .dtm(dtm -> dtm.data(dtm_ -> dtm_
                        .c507DateTimePeriod(c507 -> c507
                            .e2005DateTimePeriodQualifier("4") // Order date/time
                            .e2379DateTimePeriodFormatQualifier("102")
                            .e2380DateTimePeriod(DTMUtils.format(invoice.getOrderCreatedAt(), "102")))))))
                .sg2(sg2 -> sg2.data(sg2_ -> sg2_
                    .nad(nad -> nad.data(nad_ -> nad_
                        .e3035PartyQualifier("SU") // Supplier
                        .c082PartyIdentificationDetails(c082 -> c082
                            .e3039PartyIdIdentification(invoice.getSupplierNr())
                            .e3055CodeListResponsibleAgencyCoded("9")))) // GLN
                    .sg3(sg3 -> sg3.data(sg3_ -> sg3_
                        .rff(rff -> rff.data(rff_ -> rff_
                            .c506Reference(c506 -> c506
                                .e1153ReferenceQualifier("VA") // VAT ID
                                .e1154ReferenceNumber(invoice.getSupplierVatId()))))))))
                .sg2(sg2 -> sg2.data(sg2_ -> sg2_
                    .nad(nad -> nad.data(nad_ -> nad_
                        .e3035PartyQualifier("DP") // Ship To
                        .c082PartyIdentificationDetails(c082 -> c082
                            .e3039PartyIdIdentification(invoice.getShipToNr())
                            .e3055CodeListResponsibleAgencyCoded("9")))))) // GLN
                .sg2(sg2 -> sg2.data(sg2_ -> sg2_
                    .nad(nad -> nad.data(nad_ -> nad_
                        .e3035PartyQualifier("IV") // Invoicee
                        .c082PartyIdentificationDetails(c082 -> c082
                            .e3039PartyIdIdentification(invoice.getInvoiceeNr())
                            .e3055CodeListResponsibleAgencyCoded("9"))))  // GLN
                    .sg3(sg3 -> sg3.data(sg3_ -> sg3_
                        .rff(rff -> rff.data(rff_ -> rff_
                            .c506Reference(c506 -> c506
                                .e1153ReferenceQualifier("VA") // VAT ID
                                .e1154ReferenceNumber(invoice.getSupplierVatId()))))))))
                .sg7(sg7 -> sg7.data(sg7_ -> sg7_
                    .cux(cux -> cux.data(cux_ -> cux_
                        .c5041CurrencyDetails(c504 -> c504
                            .e6347CurrencyDetailsQualifier("2") // Reference currency
                            .e6345CurrencyCoded("EUR"))))))
                .sg8(sg8 -> sg8.data(sg8_ -> sg8_
                    .pat(pat -> pat.data(pat_ -> pat_
                        .e4279PaymentTermsTypeQualifier("1") // Basic
                        .c112TermsTimeInformation(c112 -> c112
                            .e2475PaymentTimeReferenceCoded("5") // Date of invoice
                            .e2151TypeOfPeriodCoded("D") // Day
                            .e2152NumberOfPeriods(30)))))) // 30 days
                .sg25(() -> invoice.getLineItems().stream(), (lineItem, sg25) -> sg25.data(sg25_ -> sg25_
                    .lin(lin -> lin.data(lin_ -> lin_
                        .e1082LineItemNumber(lineItem.getPosition())
                        .c212ItemNumberIdentification(c212 -> c212
                            .e7140ItemNumber(lineItem.getSku())
                            .e7143ItemNumberTypeCoded("EN"))))  // EAN
                    .qty(qty -> qty.data(qty_ -> qty_
                        .c186QuantityDetails(c186 -> c186
                            .e6063QuantityQualifier("47") // Invoiced quantity
                            .e6060Quantity(lineItem.getInvoicedQty().intValue()))))
                    .sg26(sg26 -> sg26.data(sg26_ -> sg26_
                        .moa(moa -> moa.data(moa_ -> moa_
                            .c516MonetaryAmount(c516 -> c516
                                .e5025MonetaryAmountTypeQualifier("203") // Line item amount
                                .e5004MonetaryAmount(lineItem.sumNetAmount())
                                .e6345CurrencyCoded("EUR")
                                .e6343CurrencyQualifier("4")))))) // Invoicing currency
                    .sg28(sg28 -> sg28.data(sg28_ -> sg28_
                        .pri(pri -> pri.data(pri_ -> pri_
                            .c509PriceInformation(c509 -> c509
                                .e5125PriceQualifier("AAA") // Calculation net
                                .e5118Price(lineItem.getPiecePriceNetAmount())
                                .e6411MeasureUnitQualifier(lineItem.getPiecePriceNetUnit())
                                .e5375PriceTypeCoded("CT") // Contract
                                .e5387PriceTypeQualifier("NTP")))))) // Net unit price
                    .sg33(sg33 -> sg33.data(sg33_ -> sg33_
                        .tax(tax -> tax.data(tax_ -> tax_
                            .e5283DutyTaxFeeFunctionQualifier("7") // Tax
                            .c241DutyTaxFeeType(c241 -> c241
                                .e5153DutyTaxFeeTypeCoded("VAT")) // Value added tax
                            .c243DutyTaxFeeDetail(c243 -> c243
                                .e5278DutyTaxFeeRate(String.valueOf(lineItem.getVatRate().intValue())))
                            .e5305DutyTaxFeeCategoryCoded("S"))))))) // Standard rate
                .uns(uns -> uns.data(uns_ -> uns_
                    .e0081("S"))) // Detail/summary section separation
                .cnt(cnt -> cnt.data(cnt_ -> cnt_
                    .c270Control(c270 -> c270
                        .e6069ControlQualifier("2") // Number of line items in message
                        .e6066ControlValue(invoice.getLineItems().size()))))
                .sg48(sg48 -> sg48
                    .data(sg48_ -> sg48_
                        .moa(moa -> moa.data(moa_ -> moa_.c516MonetaryAmount(c516 -> c516
                            .e5025MonetaryAmountTypeQualifier("77") // Invoice amount
                            .e5004MonetaryAmount(invoice.sumAmount()))))))
                .sg48(sg48 -> sg48
                    .data(sg48_ -> sg48_
                        .moa(moa -> moa.data(moa_ -> moa_.c516MonetaryAmount(c516 -> c516
                            .e5025MonetaryAmountTypeQualifier("125") // taxable amount
                            .e5004MonetaryAmount(invoice.sumTaxableAmount()))))))
                .unt())
            .unz()
            .validate()
            .writeToString();
    }

    private Date now() {
        return Date.from(LocalDateTime.of(2020, 12, 31, 23, 20, 0).toInstant(ZoneOffset.UTC));
    }
}
