package br.com.alurafood.pedidos.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PagamentosListener {

    @RabbitListener(queues = "pagamento.confirmado")
    public void lerMensagem(Message mensagem) {
        log.info("Recebi a mensagem: {}", new String(mensagem.getBody()));
    }

}
