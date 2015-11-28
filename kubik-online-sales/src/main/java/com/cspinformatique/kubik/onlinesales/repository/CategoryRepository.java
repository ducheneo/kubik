package com.cspinformatique.kubik.onlinesales.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cspinformatique.kubik.onlinesales.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	void delete(Category category);

	Category findByKubikId(int kubikId);
}