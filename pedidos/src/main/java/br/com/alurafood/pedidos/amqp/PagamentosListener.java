package br.com.alurafood.pedidos.amqp;

import br.com.alurafood.pedidos.dto.PagamentoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PagamentosListener {

    @RabbitListener(queues = "pagamentos.detalhes-pedido")
    public void lerMensagem(PagamentoDto pagamento) {
        log.info("Recebi a mensagem: {}", pagamento);
    }

}
