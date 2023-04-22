package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class RdsStack extends Stack {

    public RdsStack(final Construct scope, final String id, final Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public RdsStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) {
        super(scope, id, props);

        CfnParameter senha = CfnParameter.Builder.create(this, "senha")
                .type("String")
                .description("Senha do database")
                .build();

        ISecurityGroup securityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306));

        IInstanceEngine mysql = DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder()
                .version(MysqlEngineVersion.VER_8_0)
                .build());

        CredentialsFromUsernameOptions credentials = CredentialsFromUsernameOptions.builder()
                .password(SecretValue.unsafePlainText(senha.getValueAsString()))
                .build();

        SubnetSelection subnets = SubnetSelection.builder()
                .subnets(vpc.getPrivateSubnets())
                .build();

        DatabaseInstance database = DatabaseInstance.Builder.create(this, "RDS-Pedidos")
                .instanceIdentifier("alura-aws-pedido-db")
                .engine(mysql)
                .vpc(vpc)
                .credentials(Credentials.fromUsername("admin", credentials))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .multiAz(false)
                .allocatedStorage(10)
                .securityGroups(Collections.singletonList(securityGroup))
                .vpcSubnets(subnets)
                .build();

        CfnOutput.Builder.create(this, "pedidos-db-endpoint")
                .exportName("pedidos-db-endpoint")
                .value(database.getDbInstanceEndpointAddress())
                .build();

        CfnOutput.Builder.create(this, "pedidos-db-senha")
                .exportName("pedidos-db-senha")
                .value(senha.getValueAsString())
                .build();
    }

}