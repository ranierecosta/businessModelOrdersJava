package com.raniere.cursomc.domain.enums;

import java.util.stream.Stream;

public enum EstadoPagamento {

	PENDENTE(1, "Pendente"), 
	QUITADO(2, "Quitado"), 
	CANCELADO(3, "Cancelado");

	private int codigo;
	private String descricao;

	private EstadoPagamento(int codigo, String descricao) {
		this.codigo = codigo;
		this.descricao = descricao;
	}

	public int getCodigo() {
		return codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public static EstadoPagamento toEnum(Integer codigo) {
		
		return Stream.of(values()).filter(estadoPagamento -> codigo.equals(estadoPagamento.getCodigo())).findFirst().
				orElseThrow(() -> new IllegalArgumentException("Código inválido: " + codigo));

	}
}
