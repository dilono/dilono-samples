package com.dilono.sample.camel.sftp;

import com.dilono.sample.basic.EdifactOrdersReader;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
class InboundOrdersRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("sftp://ben@localhost:10022?password=secret")
            .bean(EdifactOrdersReader.class)
            .log("${body}")
            .bean(RecordingOrdersStore.class);
    }
}
