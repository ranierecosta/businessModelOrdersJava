package com.raniere.cursomc.domain.enums;

import java.util.stream.Stream;

public enum TipoCliente {

	PESSOAFISICA(1, "Pessoa Física"),
	PESSOAJURIDICA(2, "Pessoa Jurídica");

	private int codigo;
	private String descricao;

	private TipoCliente(int codigo, String descricao) {
		this.codigo = codigo;
		this.descricao = descricao;
	}

	public int getCodigo() {
		return codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public static TipoCliente toEnum(Integer codigo) {

		return Stream.of(values()).filter(tipoCliente -> codigo.equals(tipoCliente.getCodigo())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Código inválido: " + codigo));
		/*
		 * if (codigo == null) { return null; }
		 * 
		 * for (TipoCliente x: TipoCliente.values()) { if (codigo.equals(x.getCodigo()))
		 * { return x; } }
		 * 
		 * throw new IllegalArgumentException("Código inválido: " + codigo); }
		 */
	}
}
