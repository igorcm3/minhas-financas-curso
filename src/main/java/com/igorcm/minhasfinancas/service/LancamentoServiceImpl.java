/**
 * 
 */
package com.igorcm.minhasfinancas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.igorcm.minhasfinancas.exception.RegraNegocioException;
import com.igorcm.minhasfinancas.model.entity.Lancamento;
import com.igorcm.minhasfinancas.model.enums.StatusLancamento;
import com.igorcm.minhasfinancas.model.enums.TipoLancamento;
import com.igorcm.minhasfinancas.model.repository.LancamentoRepository;

/**
 * @author <a href="coronaigor@gmail.com">Igor Corona de Matos</a>
 *
 */
@Service
public class LancamentoServiceImpl implements LancamentoService{

	private LancamentoRepository lancamentoRepository;
	
	public LancamentoServiceImpl(LancamentoRepository lancamentoRepository) {
		this.lancamentoRepository = lancamentoRepository;
	} 
	
	
	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		this.validar(lancamento);
		lancamento.setStatusLancamento(StatusLancamento.PENDENTE);
		return this.lancamentoRepository.save(lancamento);
	}

	@Override
	@Transactional
	public Lancamento atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		this.validar(lancamento);
		return this.lancamentoRepository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		this.lancamentoRepository.delete(lancamento);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
		Example example = Example.of(lancamentoFiltro, 
				ExampleMatcher.matching()
				.withIgnoreCase()
				.withStringMatcher(StringMatcher.CONTAINING));
		
		return this.lancamentoRepository.findAll(example);
	}

	@Override
	public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
		lancamento.setStatusLancamento(status);
		this.atualizar(lancamento);
		
	}


	@Override
	public void validar(Lancamento lancamento) {
		
		if(lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")) {
			throw new RegraNegocioException("Informe uma descrição válida");
		}
		
		if(lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
			throw new RegraNegocioException("Informe um Mês válido");
		}
		
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Informe um Ano válido");
		}	
		
		if(lancamento.getUsuario()== null || lancamento.getUsuario().getId()== null) {
			throw new RegraNegocioException("Informe um Usuário");
		}	
		
		if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
			throw new RegraNegocioException("Informe um Valor válido");
		}
		
		if(lancamento.getTipoLancamento() == null) {
			throw new RegraNegocioException("Informe um Tipo de lançamento");
		}
	}


	@Override
	public Optional<Lancamento> findById(Long id) {
		return this.lancamentoRepository.findById(id);
	}


	@Override
	@Transactional
	public BigDecimal getSaldoByUsuario(Long idUsuario) {
		BigDecimal receitas = this.lancamentoRepository.getSaldoByTipoLancamentoAndUsuario(idUsuario, TipoLancamento.RECEITA);
		BigDecimal despesas = this.lancamentoRepository.getSaldoByTipoLancamentoAndUsuario(idUsuario, TipoLancamento.DESPESA);
		
		if (receitas == null) {
			receitas = BigDecimal.ZERO;
		}
		
		if (despesas == null) {
			despesas = BigDecimal.ZERO;
			
		}
		return receitas.subtract(despesas);
	}

}
