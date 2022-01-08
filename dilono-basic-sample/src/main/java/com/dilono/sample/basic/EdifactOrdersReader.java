package com.dilono.sample.basic;


import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.d96a.D96A;
import com.dilono.edifact.toolkit.DTMUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class EdifactOrdersReader {

    private final ECSClient client;

    EdifactOrdersReader(ECSClient client) {
        this.client = client;
    }

    List<Order> fromEdifact(final InputStream edifact) throws Exception {
        return D96A.reader(client, IOUtils.toByteArray(edifact))
            .orders(() -> new Order(), (orders, myOrder) -> orders
                .bgm(bgm -> bgm
                    .data(bgm_ -> bgm_
                        .e1004DocumentMessageNumber(e1004 -> myOrder.setOrderNr(e1004))))
                .dtm(dtm -> dtm
                    .must(dtm_ -> dtm_
                        .c507DateTimePeriod(c507 -> c507
                            .e2005DateTimePeriodQualifier(e2005 -> e2005.isEqualTo("137"))))
                    .data(dtm_ -> dtm_
                        .c507DateTimePeriod(c507 -> myOrder.setOrderCreatedAt(DTMUtils.parse(
                            c507.e2380DateTimePeriod(),
                            c507.e2379DateTimePeriodFormatQualifier())))))
                .sg2(sg2 -> sg2
                    .must(sg2_ -> sg2_
                        .nad(nad -> nad
                            .e3035PartyQualifier(e3035 -> e3035.isEqualTo("SU"))))
                    .data(sg2_ -> sg2_
                        .nad(nad -> nad.data(nad_ -> nad_
                            .c082PartyIdentificationDetails(c082 -> c082
                                .e3039PartyIdIdentification(e3039 -> myOrder.setSupplierNr(e3039)))))))
                .sg25(sg25 -> sg25.data(() -> newLineItem(myOrder), (sg25_, myLineItem) -> sg25_
                    .lin(lin -> lin.data(lin_ -> lin_
                        .e1082LineItemNumber(e1082 -> myLineItem.setPosition(e1082.intValue()))
                        .c212ItemNumberIdentification(c212 -> c212
                            .e7140ItemNumber(e7140 -> myLineItem.setSku(e7140)))))
                    .qty(qty -> qty
                        .must(qty_ -> qty_
                            .c186QuantityDetails(c186 -> c186
                                .e6063QuantityQualifier(e6063 -> e6063.isEqualTo("21"))))
                        .data(qty_ -> qty_.c186QuantityDetails(c186 -> c186
                            .e6060Quantity(e6060 -> myLineItem.setQty(e6060)))))
                    .sg28(sg28 -> sg28.data(sg28_ -> sg28_
                        .pri(pri -> pri
                            .must(pri_ -> pri_
                                .c509PriceInformation(c509 -> c509
                                    .e5125PriceQualifier(e5126 -> e5126.isEqualTo("AAA"))))
                            .data(pri_ -> pri_
                                .c509PriceInformation(c509 -> c509
                                    .e5118Price(e5118 -> myLineItem.setPiecePriceNet(e5118)))))
                        .pri(pri -> pri
                            .can(pri_ -> pri_
                                .c509PriceInformation(c509 -> c509.e5125PriceQualifier(e5126 -> e5126.isEqualTo("AAB"))))
                            .data(pri_ -> pri_
                                .c509PriceInformation(c509 -> c509
                                    .e5118Price(e5118 -> myLineItem.setPiecePriceGross(e5118))))))))));
    }

    private Order.LineItem newLineItem(Order myOrder) {
        final Order.LineItem LineItem = new Order.LineItem();
        myOrder.getLineItems().add(LineItem);
        return LineItem;
    }

}
