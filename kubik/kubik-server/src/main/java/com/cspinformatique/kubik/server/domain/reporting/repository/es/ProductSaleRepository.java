package com.cspinformatique.kubik.server.domain.reporting.repository.es;

import java.util.List;

import com.cspinformatique.kubik.server.domain.reporting.model.ProductSale;

public interface ProductSaleRepository {
	void save(List<ProductSale> productSales);
}
