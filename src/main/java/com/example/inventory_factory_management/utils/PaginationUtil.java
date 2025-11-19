package com.example.inventory_factory_management.utils;

import com.example.inventory_factory_management.dto.BaseRequestDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

    private PaginationUtil() {}

    public static Pageable toPageable(BaseRequestDTO request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        String sortBy = (request.getSortBy() == null || request.getSortBy().isBlank()) ? "createdAt" : request.getSortBy();
        Sort.Direction dir = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, dir, sortBy);
    }




    // ADD THIS OVERLOADED METHOD
    public static Pageable toPageable(BaseRequestDTO request, String defaultSortField) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int size = request.getSize() == null ? 20 : request.getSize();
        String sortBy = (request.getSortBy() == null || request.getSortBy().isBlank())
                ? defaultSortField  // Using default instead of hardcoded "createdAt"
                : request.getSortBy();
        Sort.Direction dir = "ASC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, dir, sortBy);
    }

}



















//package com.example.inventory_factory_management.utils;
//
//import com.example.inventory_factory_management.dto.BaseRequestDTO;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//public final class PaginationUtil {
//
//    private PaginationUtil() {}
//
//    public static Pageable toPageable(BaseRequestDTO request) {
//        int page = request.getPage() == null ? 0 : request.getPage();
//        int size = request.getSize() == null ? 20 : request.getSize();
//        String sortBy = (request.getSortBy() == null || request.getSortBy().isBlank()) ? "createdAt" : request.getSortBy();
//        Sort.Direction dir = "ASC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
//        return PageRequest.of(page, size, dir, sortBy);
//    }
//}
