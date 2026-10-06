package br.com.uninter.baozistore.controller;

import br.com.uninter.baozistore.model.Pedido;
import br.com.uninter.baozistore.repository.ClienteRepository;
import br.com.uninter.baozistore.repository.PedidoRepository;
import br.com.uninter.baozistore.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(PedidoRepository pedidoRepository,
                            ClienteRepository clienteRepository,
                            ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Pedido pedido) {
        if (!dadosDoPedidoValidos(pedido)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Cliente, produto ou quantidade inválidos."));
        }

        Pedido salvo = pedidoRepository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Pedido dados) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (!dadosDoPedidoValidos(dados)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Cliente, produto ou quantidade inválidos."));
        }

        Pedido pedido = pedidoRepository.findById(id).orElseThrow();
        pedido.setClienteId(dados.getClienteId());
        pedido.setProdutoId(dados.getProdutoId());
        pedido.setQuantidade(dados.getQuantidade());
        return ResponseEntity.ok(pedidoRepository.save(pedido));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean dadosDoPedidoValidos(Pedido pedido) {
        if (pedido.getClienteId() == null || pedido.getProdutoId() == null || pedido.getQuantidade() == null) {
            return false;
        }

        if (pedido.getQuantidade() <= 0) {
            return false;
        }

        return clienteRepository.existsById(pedido.getClienteId())
                && produtoRepository.existsById(pedido.getProdutoId());
    }
}
