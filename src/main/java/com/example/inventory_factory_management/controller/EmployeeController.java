package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/createBay")
    public BaseResponseDTO<BayDTO> createBay(@Valid @RequestBody CreateBayDTO bayDTO) {
        return employeeService.createBay(bayDTO);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/createEmployee")
    public BaseResponseDTO<UserDTO> createEmployee(@Valid @RequestBody CreateEmployeeDTO employeeDTO) {
        return employeeService.createEmployee(employeeDTO);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @GetMapping("/factories/bays")
    public BaseResponseDTO<List<BayDTO>> getBaysInFactory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Long factoryId) {
        return employeeService.getBaysInFactory(factoryId);
    }

    // Get employees by factory ID
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @GetMapping("/employees/factories/{factoryId}")
    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactoryId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @PathVariable Long factoryId) {
        return employeeService.getEmployeesByFactoryId(factoryId);
    }

    // Get employees by factory name
//    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
//    @GetMapping("/employees/factories/name/{factoryName}")
//    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactoryName(@PathVariable String factoryName) {
//        return employeeService.getEmployeesByFactoryName(factoryName);
//    }

    // Get manager's own factory employees
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/employees/my-factory")
    public BaseResponseDTO<EmployeeResponseDTO> getMyFactoryEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return employeeService.getEmployeesByFactoryId(null);
    }

    // Get all employees with filters (for owners/central officers)
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/employees")
    public BaseResponseDTO<EmployeeResponseDTO> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        EmployeeFilterDTO filterDTO = new EmployeeFilterDTO(search, role, factoryId, page, size);
        return employeeService.getAllEmployees(filterDTO);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @PutMapping("/employees/{employeeId}")
    public BaseResponseDTO<UserDTO> updateEmployee(@PathVariable Long employeeId,
                                                   @Valid @RequestBody CreateEmployeeDTO employeeDTO) {
        return employeeService.updateEmployee(employeeId, employeeDTO);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @DeleteMapping("/employees/{employeeId}")
    public BaseResponseDTO<Void> deleteEmployee(@PathVariable Long employeeId) {
        return employeeService.deleteEmployee(employeeId);
    }

//    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
//    @GetMapping("/my-factories")
//    public BaseResponseDTO<List<FactoryInfoDTO>> getMyFactories() {
//        return employeeService.getMyFactories();
//    }
}