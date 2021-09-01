package com.raniere.cursomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raniere.cursomc.domain.Cidade;
import com.raniere.cursomc.repositories.CidadeRepository;

@Service
public class CidadeService {

	@Autowired
	private CidadeRepository repo;
	
	public List<Cidade> findByEstado(Integer estadoID) {
		
		return repo.findCidades(estadoID);
		
	}
	
}
