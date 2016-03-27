package com.cspinformatique.kubik.server.domain.warehouse.service.impl;

import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.cspinformatique.kubik.server.domain.product.service.ProductService;
import com.cspinformatique.kubik.server.domain.warehouse.repository.StocktakingProductRepository;
import com.cspinformatique.kubik.server.domain.warehouse.service.ProductInventoryService;
import com.cspinformatique.kubik.server.domain.warehouse.service.StocktakingCategoryService;
import com.cspinformatique.kubik.server.domain.warehouse.service.StocktakingProductService;
import com.cspinformatique.kubik.server.model.product.Product;
import com.cspinformatique.kubik.server.model.warehouse.ProductInventory;
import com.cspinformatique.kubik.server.model.warehouse.StocktakingCategory;
import com.cspinformatique.kubik.server.model.warehouse.StocktakingProduct;

@Service
public class StocktakingProductServiceImpl implements StocktakingProductService {
	@Resource
	private StocktakingProductRepository stocktakingProductRepository;

	@Resource
	private ProductInventoryService productInventoryService;

	@Resource
	private ProductService productService;

	@Resource
	private StocktakingCategoryService stocktakingCategoryService;

	@Override
	public StocktakingProduct addProductToCategory(int productId, long categoryId) {
		Product product = productService.findOne(productId);
		StocktakingCategory category = stocktakingCategoryService.findOne(categoryId);

		StocktakingProduct stocktakingProduct;
		Optional<StocktakingProduct> optional = stocktakingProductRepository.findByProductAndCategory(product,
				category);

		if (optional.isPresent()) {
			stocktakingProduct = optional.get();
		} else {
			stocktakingProduct = new StocktakingProduct();
			stocktakingProduct.setCategory(category);
			stocktakingProduct.setProduct(product);
			stocktakingProduct.setQuantity(0);
		}

		stocktakingProduct.setQuantity(stocktakingProduct.getQuantity() + 1);

		return save(stocktakingProduct);
	}

	@Override
	public int countCategoriesWithProduct(int productId, long categoryId) {
		return stocktakingProductRepository.countCategoriesWithProduct(productId, categoryId);
	}

	@Override
	public void delete(long id) {
		stocktakingProductRepository.delete(id);
	}

	@Override
	public StocktakingProduct updateQuantity(long id, double quantity) {
		StocktakingProduct stocktakingProduct = stocktakingProductRepository.findOne(id);
		stocktakingProduct.setQuantity(quantity);

		return save(stocktakingProduct);
	}

	@Override
	public StocktakingProduct save(StocktakingProduct stocktakingProduct) {
		ProductInventory productInventory = productInventoryService.findByProduct(stocktakingProduct.getProduct());

		stocktakingProduct
				.setInventoryQuantity(productInventory.getQuantityOnHand() + productInventory.getQuantityOnHold());

		return stocktakingProductRepository.save(stocktakingProduct);
	}
}