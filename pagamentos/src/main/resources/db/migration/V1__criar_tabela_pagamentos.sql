create table pagamentos(
    id bigint(20) not null auto_increment,
    valor decimal(19, 2) not null,
    nome varchar(100) default null,
    numero varchar(19) default null,
    expiracao varchar(7) default null,
    codigo varchar(3) default null,
    status varchar(255) not null,
    forma_de_pagamento_id bigint(20) not null,
    pedido_id bigint(20) not null,
    primary key (id)
);