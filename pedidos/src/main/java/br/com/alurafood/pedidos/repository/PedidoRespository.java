package br.com.alurafood.pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import br.com.alurafood.pedidos.model.Pedido;
import br.com.alurafood.pedidos.model.Status;

public interface PedidoRespository extends JpaRepository<Pedido, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Pedido p set p.status = :status where p = :pedido")
    void atualizarStatus(Status status, Pedido pedido);

    @Query("select p from Pedido p left join fetch p.itens where p.id = :id")
    Pedido porIdComItens(Long id);

}
