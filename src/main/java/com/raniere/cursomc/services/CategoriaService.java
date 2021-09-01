package com.raniere.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.raniere.cursomc.domain.Categoria;
import com.raniere.cursomc.dto.CategoriaDTO;
import com.raniere.cursomc.repositories.CategoriaRepository;
import com.raniere.cursomc.services.exceptions.DataIntegrityException;
import com.raniere.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class CategoriaService {

	@Autowired
	public CategoriaRepository repo;
	
	public Categoria find(Integer id) {
		Optional<Categoria> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Categoria " + id + " não encontrada! " +
				"Local: " + Categoria.class.getName()));
	}	
	
	public Categoria insert(Categoria obj) {
		obj.setId(null);
		return repo.save(obj);
	}
	
	public Categoria update(Categoria obj) {
		Categoria newObj = find(obj.getId());
		updateData(newObj, obj);
		
		return repo.save(newObj);
	}
	
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir uma categoria que possui produtos!");
		}
	}
	
	public List<Categoria> findAll() {
		return repo.findAll();
	}
	
	public Page<Categoria> findPageCategoria(Integer page, Integer linesPerPage, String orderBy, String directionOrder) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(directionOrder), orderBy);		
		return repo.findAll(pageRequest);
	}
	
	public Categoria fromDTO(CategoriaDTO objDto) {
		return new Categoria(objDto.getId(), objDto.getNome());
	}
	
	private void updateData(Categoria newOjb, Categoria obj) {
		newOjb.setNome(obj.getNome());
	}
}
