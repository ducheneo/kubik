package com.cspinformatique.kubik.kos.domain.product.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cspinformatique.kubik.kos.domain.product.service.CategoryService;
import com.cspinformatique.kubik.kos.model.product.Category;

@Controller
@RequestMapping("/category")
public class CategoryController {
	@Resource
	private CategoryService categoryService;

	@RequestMapping(method = RequestMethod.GET, params = "name", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Category findByName(@RequestParam String name) {
		return categoryService.findByName(name);
	}

	@RequestMapping(method = RequestMethod.GET, params = "rootCategory", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Category> findbyRootCategory(@RequestParam boolean rootCategory) {
		return categoryService.findByRootCategory(rootCategory);
	}
}
