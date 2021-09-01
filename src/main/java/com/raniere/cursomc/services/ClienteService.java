package com.raniere.cursomc.services;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.raniere.cursomc.domain.Cidade;
import com.raniere.cursomc.domain.Cliente;
import com.raniere.cursomc.domain.Endereco;
import com.raniere.cursomc.domain.enums.Perfil;
import com.raniere.cursomc.domain.enums.TipoCliente;
import com.raniere.cursomc.dto.ClienteDTO;
import com.raniere.cursomc.dto.ClienteNewDTO;
import com.raniere.cursomc.repositories.CidadeRepository;
import com.raniere.cursomc.repositories.ClienteRepository;
import com.raniere.cursomc.repositories.EnderecoRepository;
import com.raniere.cursomc.security.UserSS;
import com.raniere.cursomc.services.exceptions.AuthorizationException;
import com.raniere.cursomc.services.exceptions.DataIntegrityException;
import com.raniere.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private BCryptPasswordEncoder pw;
	
	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired
	private CidadeRepository cidadeRepository;
	
	@Autowired
	private S3Service s3Service;
	
	@Autowired
	private ImageService imageService;
	
	@Value("${img.prefix.client.profile}")
	private String prefixImage;
	
	@Value("${img.profile.size}")
	private Integer size;
	
	public Cliente find(Integer id) {
		
		UserSS user = UserService.authenticated();
		
		if (user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado!");
		}
		
		Optional<Cliente> obj = repo.findById(id);
		
		return obj.orElseThrow(() -> new ObjectNotFoundException("Cliente " + id + " não encontrado! " +
				"Local: " + Cliente.class.getName()));
	}	
	
	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}
	
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		
		return repo.save(newObj);
	}
	
	public void delete(Integer id) {
		find(id);
		
		try {
			repo.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir um cliente que possui pedidos!");
		}
	}
	
	public List<Cliente> findAll() {
		return repo.findAll();
	}
	
	public Cliente findByEmail(String email) {
		
		UserSS user = UserService.authenticated();
				
		if (user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Cliente obj = repo.findByEmail(email);
		
		if (obj == null) {
			throw new ObjectNotFoundException("Cliente não encontrado! ID: " + user.getId());
		}
		
		return obj;
		
	}
	
	public Page<Cliente> findPageCliente(Integer page, Integer linesPerPage, String orderBy, String directionOrder) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(directionOrder), orderBy);
		
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}
	
	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cliente = new Cliente(null, 
									  objDto.getNome(), 
									  objDto.getEmail(), 
									  objDto.getCpfOuCnpj(), 
									  TipoCliente.toEnum(objDto.getTipoCliente()),
									  pw.encode(objDto.getSenha()));
		
		Cidade cidade = cidadeRepository.findById(objDto.getCidadeId()).get();
		
		Endereco endereco = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cliente, cidade);
		
		cliente.getEnderecos().add(endereco);
		cliente.getTelefones().add(objDto.getTelefone1());
		
		if (objDto.getTelefone2() != null) {
			cliente.getTelefones().add(objDto.getTelefone2());
		}
		
		if (objDto.getTelefone3() != null) {
			cliente.getTelefones().add(objDto.getTelefone3());
		}
		
		return cliente;
	}
	
	private void updateData(Cliente newOjb, Cliente obj) {
		
		newOjb.setNome(obj.getNome());
		newOjb.setEmail(obj.getEmail());
		
	}
	
	public URI uploadProfilePicture(MultipartFile multiPartFile) {
		
		UserSS user = UserService.authenticated();
		
		if (user == null) {
			throw new AuthorizationException("Acesso negado");
		}
		
		BufferedImage jpgImage = imageService.getJpgImageFromFile(multiPartFile);
		
		jpgImage = imageService.cropSquare(jpgImage);
		jpgImage = imageService.resize(jpgImage, size);
		
		String fileName = prefixImage + user.getId() + ".jpg";
		
		return s3Service.uploadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");
		
	}
	
}
