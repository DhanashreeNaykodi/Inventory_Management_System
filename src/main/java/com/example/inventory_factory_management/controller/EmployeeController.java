package com.example.inventory_factory_management.controller;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    // Bay Management - MANAGER or OWNER

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/bays")
    public BaseResponseDTO<BayDTO> createBay(@RequestBody CreateBayDTO bayDTO) {
        return employeeService.createBay(bayDTO);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @GetMapping("/factories/bays")
    public BaseResponseDTO<List<BayDTO>> getBaysInFactory() {
        return employeeService.getBaysInFactory();
    }

    // Create worker/supervisor - MANAGER or OWNER
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @PostMapping("/createEmployee")
    public BaseResponseDTO<UserDTO> createEmployee(@RequestBody CreateEmployeeDTO employeeDTO) {
        return employeeService.createEmployee(employeeDTO);
    }

    // Get all employees for a factory - MANAGER or OWNER
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @GetMapping("/employees/factories/{factoryId}")
    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactory(@PathVariable Long factoryId) {
        return employeeService.getEmployeesByFactory(factoryId);
    }

    // Update employee - MANAGER or OWNER
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/employee/{employeeId}")
    public BaseResponseDTO<UserDTO> updateEmployee(@PathVariable Long employeeId,
                                                   @RequestBody CreateEmployeeDTO employeeDTO) {
        return employeeService.updateEmployee(employeeId, employeeDTO);
    }

    // Delete employee - MANAGER or OWNER
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @DeleteMapping("/employee/{employeeId}")
    public BaseResponseDTO<Void> deleteEmployee(@PathVariable Long employeeId) {
        return employeeService.deleteEmployee(employeeId);
    }

    // Get available bays for assigning to workers - MANAGER or OWNER
//    @GetMapping("/factories/{factoryId}/bays")
//    public BaseResponseDTO<List<BayDTO>> getAvailableBays(@PathVariable Long factoryId) {
//        return employeeService.getAvailableBays(factoryId);
//    }

    // Get my factories - MANAGER or OWNER
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @GetMapping("/my-factories")
    public BaseResponseDTO<List<FactoryInfoDTO>> getMyFactories() {
        return employeeService.getMyFactories();
    }

    // Get all employees with filters - OWNER or CENTRAL_OFFICER only
    @PreAuthorize("hasAnyRole('OWNER', 'CENTRAL_OFFICER')")
    @GetMapping("/employees")
    public BaseResponseDTO<EmployeeResponseDTO> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {

        EmployeeFilterDTO filterDTO = new EmployeeFilterDTO(search, role, factoryId, page, size);
        return employeeService.getAllEmployees(filterDTO);
    }


}