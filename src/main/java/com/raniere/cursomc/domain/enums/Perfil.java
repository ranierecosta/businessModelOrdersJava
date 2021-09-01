package com.raniere.cursomc.domain.enums;

import java.util.stream.Stream;

public enum Perfil {

	ADMIN(1, "ROLE_ADMIN"), 
	CLIENTE(2, "ROLE_CLIENT");

	private int codigo;
	private String descricao;

	private Perfil(int codigo, String descricao) {
		this.codigo = codigo;
		this.descricao = descricao;
	}

	public int getCodigo() {
		return codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public static Perfil toEnum(Integer codigo) {
		
		return Stream.of(values()).filter(perfil -> codigo.equals(perfil.getCodigo())).findFirst().
				orElseThrow(() -> new IllegalArgumentException("Código inválido: " + codigo));

	}
}
