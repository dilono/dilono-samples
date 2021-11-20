package com.dilono.sample.basic;

import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.d96a.D96A;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EdifactOrdersReaderTest {

    @Autowired
    private EdifactOrdersReader ordersReader;

    @Autowired
    private ECSClient client;

    @Test
    void shouldReadEdifactToPojo() throws Exception {
        final String edifact = edifact();
        final ByteArrayInputStream edifactStream = new ByteArrayInputStream(edifact.getBytes());
        final List<Order> orders = ordersReader.fromEdifact(edifactStream);
        assertThat(orders)
            .as("one order pojo has been created")
            .hasSize(1);

        final Order order = orders.get(0);
        assertThat(order.getOrderNr())
            .as("order number mapped correctly.")
            .isEqualTo("1AA1TEST");

        assertThat(order.getLineItems()).extracting(Order.LineItem::getSku)
            .as("loop over segment group 25 creates 2 line items")
            .containsExactly("9783898307529", "9783898307539", "97838983938472");

        assertThat(order.getLineItems().get(0).getPiecePriceNet())
            .as("net price mapped, because PRI segment matches condition")
            .isEqualTo("27.5");

        assertThat(order.getLineItems().get(0).getPiecePriceGross())
            .as("gross price not found, because no PRI segments match condition")
            .isNull();

        assertThat(order.getLineItems().get(2).getPiecePriceNet())
            .as("net price mapped, because PRI segment matches condition")
            .isEqualTo("3.85");

        assertThat(order.getLineItems().get(2).getPiecePriceGross())
            .as("gross price mapped, because PRI segment matches condition")
            .isEqualTo("4.51");
    }

    private String edifact() {
        final String edifact = "UNA:+.? '" +
            "UNB+UNOA:2+0000000000001:14+0000000000002:14+140407:0910+5++++1+EANCOM'" +
            "UNH+1+ORDERS:D:96A:UN:EAN008'" +
            "BGM+220+1AA1TEST+9'" +
            "DTM+137:20140407:102'" +
            "DTM+63:20140421:102'" +
            "DTM+64:20140414:102'" +
            "RFF+ON:20140407-1/1'" +
            "RFF+PD:1704'" +
            "RFF+CR:ABCD5'" +
            "NAD+BY+0000000000005::9'" +
            "NAD+SU+0000000000002::9'" +
            "NAD+DP+0000000000004::9+++++++DE'" +
            "NAD+IV+0000000000004::9++Muster GmbH+Berlin++Muster str. 23+DE'" +
            "RFF+VA:DE123456789'" +
            "CUX+2:EUR:9'" +
            "LIN+1++9783898307529:EN'" +
            "QTY+21:5'" +
            "PRI+AAA:27.5'" +
            "LIN+2++9783898307539:EN'" +
            "QTY+21:1'" +
            "PRI+AAA:10.87'" +
            "LIN+3++97838983938472:EN'" +
            "QTY+21:3'" +
            "PRI+AAA:3.85'" +
            "PRI+AAB:4.51'" +
            "UNS+S'" +
            "CNT+2:3'" +
            "UNT+27+1'" +
            "UNZ+1+5'";
        return edifact;
    }


    @Test
    void docSnippetsWork() {
        final byte[] edifact = edifact().getBytes(StandardCharsets.UTF_8);
        Assertions.assertThatCode(() -> new Snippet1().fromEdifact(edifact)).doesNotThrowAnyException();
        Assertions.assertThatCode(() -> new Snippet2().fromEdifact(edifact)).doesNotThrowAnyException();
        Assertions.assertThatCode(() -> new Snippet3().fromEdifact(edifact)).doesNotThrowAnyException();
    }

    private static Order.LineItem newLineItem(Order myOrder) {
        final Order.LineItem LineItem = new Order.LineItem();
        myOrder.getLineItems().add(LineItem);
        return LineItem;
    }


    class Snippet1 {
        // tag::howto-read-edifact-1[]
        public List<Order> fromEdifact(byte[] edifact) throws Exception {
            final List<Order> orderPojos = D96A.reader(client, edifact)
                .orders(() -> new Order(), (orders, myOrder) -> orders // <1>
                        .bgm(bgm -> bgm // <2>
                            .data(bgm_ -> bgm_ // <3>
                                .e1004DocumentMessageNumber(e1004 -> myOrder.setOrderNr(e1004)))) // <4>
                    // ...
                );
            return orderPojos;
        }
        // end::howto-read-edifact-1[]
    }

    class Snippet2 {
        // tag::howto-read-edifact-2[]
        public List<Order> fromEdifact(byte[] edifact) throws Exception {
            final List<Order> orderPojos = D96A.reader(client, edifact)
                .orders(() -> new Order(), (orders, myOrder) -> orders //
                    // ...
                    .sg2(sg2 -> sg2 // <1>
                        .must(sg2_ -> sg2_ // <2>
                            .nad(nad -> nad // <3>
                                .e3035PartyQualifier(e3035 -> e3035.isEqualTo("BY")))) // <4>
                        .data(sg2_ -> sg2_
                            .nad(nad -> nad.data(nad_ -> nad_
                                .c082PartyIdentificationDetails(c082 -> c082
                                    .e3039PartyIdIdentification(e3039 -> myOrder.setBuyerNr(e3039))))))) // <5>
                    .sg2(sg2 -> sg2
                        .can(sg2_ -> sg2_ // <6>
                            .nad(nad -> nad
                                .e3035PartyQualifier(e3035 -> e3035.isEqualTo("SU"))))
                        .data(sg2_ -> sg2_
                            .nad(nad -> nad.data(nad_ -> nad_
                                .c082PartyIdentificationDetails(c082 -> c082
                                    .e3039PartyIdIdentification(e3039 -> myOrder.setSupplierNr(e3039))))))));
            //...
            return orderPojos;
        }
        // end::howto-read-edifact-2[]
    }

    class Snippet3 {
        // tag::howto-read-edifact-3[]
        public List<Order> fromEdifact(byte[] edifact) throws Exception {
            final List<Order> orderPojos = D96A.reader(client, edifact)
                .orders(() -> new Order(), (orders, myOrder) -> orders
                    // ...
                    .sg25(sg25 -> sg25.data(() -> newLineItem(myOrder), (sg25_, myLineItem) -> sg25_ // <1>
                        .lin(lin -> lin.data(lin_ -> lin_ // <2>
                            .e1082LineItemNumber(e1082 -> myLineItem.setPosition(e1082.intValue()))
                            .c212ItemNumberIdentification(c212 -> c212
                                .e7140ItemNumber(e7140 -> myLineItem.setSku(e7140)))))))); // <3>
            // ...
            return orderPojos;
        }
        // end::howto-read-edifact-3[]
    }
}
