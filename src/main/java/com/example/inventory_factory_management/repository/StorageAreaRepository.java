package com.example.inventory_factory_management.repository;


import com.example.inventory_factory_management.entity.Factory;
import com.example.inventory_factory_management.entity.StorageArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageAreaRepository extends JpaRepository<StorageArea, Long> {

    List<StorageArea> findByFactory(Factory factory);
    boolean existsByFactoryAndRowNumAndColNumAndStack(
            Factory factory,
            Integer rowNum,
            Integer colNum,
            Integer stack
    );

    boolean existsByFactoryAndRowNum(Factory factory, Integer row);
    boolean existsByFactoryAndRowNumAndColNum(Factory factory, Integer row, Integer col);

    boolean existsByRowNumAndColNumAndStack(Integer rowNum, Integer colNum, Integer stack);
}
