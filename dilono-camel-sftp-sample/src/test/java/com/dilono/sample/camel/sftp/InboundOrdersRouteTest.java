package com.dilono.sample.camel.sftp;

import com.dilono.sample.basic.Order;
import com.dilono.test.TestResource;
import com.github.stefanbirkner.fakesftpserver.lambda.FakeSftpServer;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

@SpringBootTest
class InboundOrdersRouteTest {

    private static final TestResource R = TestResource.forClass(InboundOrdersRouteTest.class);

    @Autowired
    private RecordingOrdersStore store;

    @Test
    void shouldDownloadOrders() throws Exception {
        FakeSftpServer.withSftpServer(sftp -> {
            sftp.setPort(10022);
            sftp.addUser("ben", "secret");
            sftp.putFile("PO123.edi", R.getBytes("orders.edi"));

            Awaitility.await("wait until orders arrive")
                .pollDelay(Duration.ofSeconds(1))
                .until(() -> !store.isEmpty());

            Assertions.assertThat(store.getOrders())
                .extracting(Order::getOrderNr)
                .containsExactly("1AA1TEST");
        });
    }
}
