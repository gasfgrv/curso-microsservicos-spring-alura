package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class ServiceStack extends Stack {

    private ApplicationLoadBalancedFargateService service;

    public ServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public ServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> autenticacao = new HashMap<>();
        autenticacao.put("SPRING_DATASOURCE_URL",
                String.format("jdbc:mysql://%s:3306/alurafood-pedidos?createDatabaseIfNotExist=true",
                        Fn.importValue("pedidos-db-endpoint")));
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        autenticacao.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-senha"));

        IRepository repositorio = Repository.fromRepositoryName(this, "repositorio", "pedidos-ms");

        LogGroup logGroup = LogGroup.Builder.create(this, "PedidosMsLog")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        AwsLogDriverProps logDriverProps = AwsLogDriverProps.builder()
                .logGroup(logGroup)
                .streamPrefix("PedidosMs")
                .build();

        ApplicationLoadBalancedTaskImageOptions imageOptions = ApplicationLoadBalancedTaskImageOptions.builder()
                .image(ContainerImage.fromEcrRepository(repositorio))
                .containerPort(8080)
                .containerName("pedidos")
                .environment(autenticacao)
                .logDriver(LogDriver.awsLogs(logDriverProps))
                .build();

        service = ApplicationLoadBalancedFargateService.Builder.create(this, "AluraService")
                .cluster(cluster)
                .cpu(512)
                .desiredCount(1)
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(imageOptions)
                .memoryLimitMiB(2048)
                .publicLoadBalancer(true)
                .build();

        service.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/pedidos")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount scalableTarget = service.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(20)
                .build());

        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());

        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());
    }

}
