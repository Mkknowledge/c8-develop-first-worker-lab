package com.camunda.academy;


import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PaymentApplication {

    private static final String ZEEBE_CLIENT_ID = "bVg-uphaaCmETTHEMADeBCme7f7yQ6zv";
    private static final String ZEEBE_CLIENT_SECRET = "tv6BbwR_Oc8gN8SzTJ~Rqg2KLJ_qf9zJha2v~niCUhtY25DXqC4sQG~jyd-qcWT1";
    private static final String ZEEBE_AUTHORIZATION_SERVER_URL = "https://login.cloud.camunda.io/oauth/token";
    private static final String ZEEBE_TOKEN_AUDIENCE = "zeebe.camunda.io";
    private static final String ZEEBE_REST_ADDRESS = "https://ric-1.zeebe.camunda.io/1afe92b6-cbb5-4d1a-b56b-6d0466313765";
    private static final String ZEEBE_GRPC_ADDRESS = "grpcs://1afe92b6-cbb5-4d1a-b56b-6d0466313765.ric-1.zeebe.camunda.io:443";

    public static void main(String[] args) {
        final OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
                .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
                .audience(ZEEBE_TOKEN_AUDIENCE)
                .clientId(ZEEBE_CLIENT_ID)
                .clientSecret(ZEEBE_CLIENT_SECRET)
                .build();

        try (final ZeebeClient client = ZeebeClient.newClientBuilder()
                .grpcAddress(URI.create(ZEEBE_GRPC_ADDRESS))
                .restAddress(URI.create(ZEEBE_REST_ADDRESS))
                .credentialsProvider(credentialsProvider)
                .build()) {

            final Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("reference", "C8_15");
            variables.put("amount", Double.valueOf(100.00));
            variables.put("cardNumber", "7812345678");
            variables.put("cardExpiry", "01/2027");
            variables.put("cardCVC", "777");

            client.newCreateInstanceCommand()
                    .bpmnProcessId("paymentProcess")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            final JobWorker creditCardWorker =
                    client.newWorker()
                            .jobType("chargeCreditCard")
                            .handler(new CreditCardServiceHandler())
                            .timeout(Duration.ofSeconds(10).toMillis())
                            .open();
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
