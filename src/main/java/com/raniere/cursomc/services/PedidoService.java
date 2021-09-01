package com.raniere.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raniere.cursomc.domain.Cliente;
import com.raniere.cursomc.domain.ItemPedido;
import com.raniere.cursomc.domain.PagamentoComBoleto;
import com.raniere.cursomc.domain.Pedido;
import com.raniere.cursomc.domain.enums.EstadoPagamento;
import com.raniere.cursomc.repositories.ItemPedidoRepository;
import com.raniere.cursomc.repositories.PagamentoRepository;
import com.raniere.cursomc.repositories.PedidoRepository;
import com.raniere.cursomc.security.UserSS;
import com.raniere.cursomc.services.exceptions.AuthorizationException;
import com.raniere.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;	
	
	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Pedido " + id + " não encontrado! " +
				"Local: " + Pedido.class.getName()));
	}	
	
	@Transactional
	public Pedido insert(Pedido obj) {
		
		obj.setId(null);
		obj.setInstante(new Date());
		
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		
		obj.getPagamento().setEstadoPgto(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		
		itemPedidoRepository.saveAll(obj.getItens());
		
		emailService.sendOrderConfirmationHtmlEmail(obj);
		
		return obj;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String directionOrder) {
		
		UserSS user = UserService.authenticated();
		
		if (user == null) {
			throw new AuthorizationException("Acesso negado!");
		}
		
	
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(directionOrder), orderBy);

		Cliente cliente = clienteService.find(user.getId());
		
		return repo.findByCliente(cliente, pageRequest);
	}
}
