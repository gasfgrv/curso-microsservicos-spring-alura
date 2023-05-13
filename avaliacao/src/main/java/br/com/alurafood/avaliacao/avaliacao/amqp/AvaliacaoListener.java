package br.com.alurafood.avaliacao.avaliacao.amqp;

import br.com.alurafood.avaliacao.avaliacao.dto.PagamentoDto;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AvaliacaoListener {

    @RabbitListener(queues = "pagamentos.detalhes-avaliacao")
    public void receberMensagem(@Payload PagamentoDto dto) {

        if (Objects.equals(dto.getCodigo(), "000")) throw new RuntimeException("Código inválido");

        var mensagem = """
                Necessário criar registro de avaliação para o pedido: {}
                Id do pagamento: {}
                Nome do cliente: {}
                Valor R$: {}
                Status: {}
                """;

        log.info(mensagem, dto.getPedidoId(), dto.getId(), dto.getNome(), dto.getValor(), dto.getStatus());
    }

}
