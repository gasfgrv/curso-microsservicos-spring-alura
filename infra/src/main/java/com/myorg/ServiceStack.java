package com.myorg;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.constructs.Construct;

public class ServiceStack extends Stack {

    private ApplicationLoadBalancedFargateService service;

    public ServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public ServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> autenticacao = new HashMap<>();
        autenticacao.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" + Fn.importValue("pedidos-db-endpoint")
                + ":3306/alurafood-pedidos?createDatabaseIfNotExists=true");
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        autenticacao.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-senha"));

        service = ApplicationLoadBalancedFargateService.Builder.create(this, "AluraService")
                .cluster(cluster)
                .cpu(512)
                .desiredCount(1)
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromRegistry("gustosilva/pedidos"))
                        .containerPort(8080)
                        .containerName("pedidos")
                        .environment(autenticacao)
                        .build())
                .memoryLimitMiB(2048)
                .publicLoadBalancer(true)
                .build();

        service.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/v3/api-docs")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

    }

}
