package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.demo.model.Product;

import jakarta.annotation.PostConstruct;

@Service
public class ProductService {
    private List<Product> products=new ArrayList<>(Arrays.asList(new Product(1l, "Сок", 130)));
    private AtomicLong idGeneration = new AtomicLong(1l);
    public Product create(Product product){
        Long id = idGeneration.getAndIncrement();
        product.setId(id);
        products.add(product);
        return product;
    }

    public List<Product> getAll(){
        return products;
    }

    @PostConstruct
    public void init(){
        create(new Product(null, "Кумыс", 123));
        
    }
}