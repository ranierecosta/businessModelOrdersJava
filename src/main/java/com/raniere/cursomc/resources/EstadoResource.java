package com.raniere.cursomc.resources;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raniere.cursomc.domain.Cidade;
import com.raniere.cursomc.domain.Estado;
import com.raniere.cursomc.dto.CidadeDTO;
import com.raniere.cursomc.dto.EstadoDTO;
import com.raniere.cursomc.services.CidadeService;
import com.raniere.cursomc.services.EstadoService;

@RestController
@RequestMapping(value = "/estados")
public class EstadoResource {

	@Autowired
	private EstadoService service;
	
	@Autowired
	private CidadeService cidadeService;
	
	@GetMapping
	public ResponseEntity<List<EstadoDTO>> findAll() {
		
		List<Estado> list = service.findAll();
		
		List<EstadoDTO> listDto = list.stream().map(obj -> new EstadoDTO(obj)).collect(Collectors.toList());
		
		return ResponseEntity.ok().body(listDto);
		
	}
	
	@GetMapping(value = "/{estadoID}/cidades")
	public ResponseEntity<List<CidadeDTO>> findCidades(@PathVariable Integer estadoID) {
		
		List<Cidade> list = cidadeService.findByEstado(estadoID);
		
		List<CidadeDTO> listDto = list.stream().map(obj -> new CidadeDTO(obj)).collect(Collectors.toList());
		
		return ResponseEntity.ok().body(listDto);
		
	}
	
}
