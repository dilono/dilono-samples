package com.dilono.sample.azure.functions;

import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.client.ECSClientBuilder;
import com.dilono.edifact.client.ECSClientCredentials;
import com.dilono.sample.basic.EdifactOrdersReader;
import com.dilono.sample.basic.Order;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import static com.microsoft.azure.functions.annotation.AuthorizationLevel.ANONYMOUS;

public class OrdersConverterFunction {

    @FunctionName("OrdersConverter")
    public HttpResponseMessage run(@HttpTrigger(
        name = "req",
        dataType = "binary",
        methods = {HttpMethod.POST},
        authLevel = ANONYMOUS) final HttpRequestMessage<byte[]> request,
                                   final ExecutionContext context) {
        final Logger logger = context.getLogger();
        try {
            final byte[] content = request.getBody();
            final URL dilonoServerUrl = new URL(System.getenv("DILONO_SERVER_URL"));
            final String dilonoServerTokenId = System.getenv("DILONO_SERVER_TOKEN_ID");
            final String dilonoServerTokenSecret = System.getenv("DILONO_SERVER_TOKEN_SECRET");
            logger.info("Creating client for '" + dilonoServerUrl + "' with token id '" + dilonoServerTokenId + "'.");

            final ECSClientCredentials credentials = ECSClientCredentials.token(
                dilonoServerTokenId,
                dilonoServerTokenSecret
            );
            final ECSClient client = new ECSClientBuilder()
                .withBaseUrl(dilonoServerUrl)
                .withCredentials(credentials)
                .build();
            final List<Order> orders = new EdifactOrdersReader(client)
                .fromEdifact(new ByteArrayInputStream(content));
            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(orders)
                .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage())
                .build();
        }

    }
}
