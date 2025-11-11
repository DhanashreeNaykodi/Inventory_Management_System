package com.example.inventory_factory_management.service;


import com.example.inventory_factory_management.repository.toolCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ToolCategoryService {

    @Autowired
    private toolCategoryRepository toolCategoryRepository;
}
