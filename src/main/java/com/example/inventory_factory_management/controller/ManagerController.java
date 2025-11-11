//package com.example.inventory_factory_management.controller;
//
//import com.example.inventory_factory_management.DTO.*;
//import com.example.inventory_factory_management.service.ManagerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/manager")
//public class ManagerController {
//
//    @Autowired
//    private ManagerService managerService;
//
//    // Create worker/supervisor
//    @PostMapping("/createEmployee")
//    public BaseResponseDTO<UserDTO> createEmployee(@RequestBody CreateEmployeeDTO employeeDTO,
//                                                   @RequestHeader("X-User-Id") Long managerId) {
//        return managerService.createEmployee(employeeDTO, managerId);
//    }
//
//    // Get all employees for a factory
//    @GetMapping("/employees/{factoryId}")
//    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactory(@PathVariable Long factoryId,
//                                                                      @RequestHeader("X-User-Id") Long managerId) {
//        return managerService.getEmployeesByFactory(factoryId, managerId);
//    }
//
//    // Update employee
//    @PutMapping("/{employeeId}")
//    public BaseResponseDTO<UserDTO> updateEmployee(@PathVariable Long employeeId,
//                                                   @RequestBody CreateEmployeeDTO employeeDTO,
//                                                   @RequestHeader("X-User-Id") Long managerId) {
//        return managerService.updateEmployee(employeeId, employeeDTO, managerId);
//    }
//
//    // Delete employee
//    @DeleteMapping("/{employeeId}")
//    public BaseResponseDTO<Void> deleteEmployee(@PathVariable Long employeeId,
//                                                @RequestHeader("X-User-Id") Long managerId) {
//        return managerService.deleteEmployee(employeeId, managerId);
//    }
//
//    // Get available bays for assigning to workers
//    @GetMapping("/factories/{factoryId}/bays")
//    public BaseResponseDTO<List<BayDTO>> getAvailableBays(@PathVariable Long factoryId) {
//        return managerService.getAvailableBays(factoryId);
//    }
//}