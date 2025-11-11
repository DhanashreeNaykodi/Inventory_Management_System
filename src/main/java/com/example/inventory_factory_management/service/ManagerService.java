//package com.example.inventory_factory_management.service;
//
//
//import com.example.inventory_factory_management.DTO.*;
//import com.example.inventory_factory_management.constants.Role;
//import com.example.inventory_factory_management.constants.account_status;
//import com.example.inventory_factory_management.entity.bay;
//import com.example.inventory_factory_management.entity.factory;
//import com.example.inventory_factory_management.entity.user;
//import com.example.inventory_factory_management.entity.userFactory;
//import com.example.inventory_factory_management.repository.BayRepository;
//import com.example.inventory_factory_management.repository.factoryRepository;
//import com.example.inventory_factory_management.repository.userFactoryRepository;
//import com.example.inventory_factory_management.repository.userRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class ManagerService {
//
//    @Autowired
//    private userRepository userRepository;
//
//    @Autowired
//    private userFactoryRepository userFactoryRepository;
//
//    @Autowired
//    private factoryRepository factoryRepository;
//
//    @Autowired
//    private BayRepository bayRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private EmailService emailService;
//
//    // Manager creates worker/supervisor for their factory
//    @Transactional
//    public BaseResponseDTO<UserDTO> createEmployee(CreateEmployeeDTO employeeDTO, Long managerId) {
//        try {
//            // Validate manager exists and is a manager
//            user manager = userRepository.findById(managerId)
//                    .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//            if (manager.getRole() != Role.MANAGER) {
//                return BaseResponseDTO.error("User is not a manager");
//            }
//
//            // Validate factory exists and manager has access to it
//            factory factory = factoryRepository.findById(employeeDTO.getFactoryId())
//                    .orElseThrow(() -> new RuntimeException("Factory not found"));
//
//            // Check if manager is assigned to this factory
//            boolean managerHasAccess = userFactoryRepository.findByUser(manager).stream()
//                    .anyMatch(uf -> uf.getFactory().getFactoryId().equals(employeeDTO.getFactoryId()));
//
//            if (!managerHasAccess) {
//                return BaseResponseDTO.error("Manager does not have access to this factory");
//            }
//
//            // Validate email doesn't exist
//            if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
//                return BaseResponseDTO.error("User with this email already exists");
//            }
//
//            // Validate role
//            Role employeeRole;
//            try {
//                employeeRole = Role.valueOf(employeeDTO.getRole());
//            } catch (IllegalArgumentException e) {
//                return BaseResponseDTO.error("Invalid role. Must be WORKER or CHIEF_SUPERVISOR");
//            }
//
//            if (employeeRole != Role.WORKER && employeeRole != Role.CHIEF_SUPERVISOR) {
//                return BaseResponseDTO.error("Role must be WORKER or CHIEF_SUPERVISOR");
//            }
//
//            // Validate bay for workers
//            bay employeeBay = null;
//            if (employeeRole == Role.WORKER) {
//                if (employeeDTO.getBayId() == null) {
//                    return BaseResponseDTO.error("Bay ID is required for workers");
//                }
//                employeeBay = bayRepository.findById(employeeDTO.getBayId())
//                        .orElseThrow(() -> new RuntimeException("Bay not found"));
//
//                // Check if bay belongs to the same factory
//                if (!employeeBay.getFactory().getFactoryId().equals(employeeDTO.getFactoryId())) {
//                    return BaseResponseDTO.error("Bay does not belong to the specified factory");
//                }
//            }
//
//            // Create new employee user
//            user newEmployee = new user();
//            newEmployee.setUsername(employeeDTO.getUsername());
//            newEmployee.setEmail(employeeDTO.getEmail());
//            newEmployee.setPhone(Long.parseLong(employeeDTO.getPhone()));
//            newEmployee.setRole(employeeRole);
//            newEmployee.setStatus(account_status.ACTIVE);
//
//            // Generate password (first 3 chars of username + @ + first 7 digits of phone)
//            String generatedPassword = employeeDTO.getUsername().substring(0, 3) + "@" + employeeDTO.getPhone().substring(0, 7);
//            newEmployee.setPassword(passwordEncoder.encode(generatedPassword));
//
//            newEmployee.setCreatedAt(LocalDateTime.now());
//            newEmployee.setUpdatedAt(LocalDateTime.now());
//
//            user savedEmployee = userRepository.save(newEmployee);
//
//            // Create userFactory relationship
//            userFactory userFactoryRelation = new userFactory();
//            userFactoryRelation.setUser(savedEmployee);
//            userFactoryRelation.setFactory(factory);
//            userFactoryRelation.setUserRole(employeeRole);
//            userFactoryRelation.setStatus(account_status.ACTIVE);
//            userFactoryRelation.setBay(employeeBay); // Set bay only for workers
//
//            userFactoryRepository.save(userFactoryRelation);
//
//            // Send welcome email
//            sendEmployeeWelcomeEmail(savedEmployee, generatedPassword, factory.getName(), employeeRole);
//
//            UserDTO responseDTO = convertToUserDTO(savedEmployee);
//            return BaseResponseDTO.success("Employee created successfully", responseDTO);
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to create employee: " + e.getMessage());
//        }
//    }
//
//    // Get all employees for a manager's factory
//    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactory(Long factoryId, Long managerId) {
//        try {
//            // Validate manager and factory access
//            user manager = userRepository.findById(managerId)
//                    .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//            boolean managerHasAccess = userFactoryRepository.findByUser(manager).stream()
//                    .anyMatch(uf -> uf.getFactory().getFactoryId().equals(factoryId));
//
//            if (!managerHasAccess) {
//                return BaseResponseDTO.error("Manager does not have access to this factory");
//            }
//
//            // Get all userFactory mappings for this factory
//            List<userFactory> factoryEmployees = userFactoryRepository.findByFactoryId(factoryId);
//
//            List<EmployeeDetailDTO> employees = factoryEmployees.stream()
//                    .map(uf -> convertToEmployeeDetailDTO(uf.getUser(), uf))
//                    .collect(Collectors.toList());
//
//            EmployeeResponseDTO response = new EmployeeResponseDTO();
//            response.setEmployees(employees);
//            response.setTotalCount(employees.size());
//
//            return BaseResponseDTO.success("Employees fetched successfully", response);
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to fetch employees: " + e.getMessage());
//        }
//    }
//
//    // Update employee
//    @Transactional
//    public BaseResponseDTO<UserDTO> updateEmployee(Long employeeId, CreateEmployeeDTO employeeDTO, Long managerId) {
//        try {
//            // Validate manager
//            user manager = userRepository.findById(managerId)
//                    .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//            // Validate employee exists and belongs to manager's factory
//            user employee = userRepository.findById(employeeId)
//                    .orElseThrow(() -> new RuntimeException("Employee not found"));
//
//            List<userFactory> employeeFactories = userFactoryRepository.findByUser(employee);
//            if (employeeFactories.isEmpty()) {
//                return BaseResponseDTO.error("Employee is not assigned to any factory");
//            }
//
//            // Check if manager has access to employee's factory
//            boolean managerHasAccess = userFactoryRepository.findByUser(manager).stream()
//                    .anyMatch(uf -> uf.getFactory().getFactoryId().equals(employeeFactories.get(0).getFactory().getFactoryId()));
//
//            if (!managerHasAccess) {
//                return BaseResponseDTO.error("Manager does not have access to this employee");
//            }
//
//            // Update employee details
//            if (employeeDTO.getUsername() != null) {
//                employee.setUsername(employeeDTO.getUsername());
//            }
//            if (employeeDTO.getEmail() != null && !employeeDTO.getEmail().equals(employee.getEmail())) {
//                if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
//                    return BaseResponseDTO.error("Email already exists");
//                }
//                employee.setEmail(employeeDTO.getEmail());
//            }
//            if (employeeDTO.getPhone() != null) {
//                employee.setPhone(Long.parseLong(employeeDTO.getPhone()));
//            }
//
//            employee.setUpdatedAt(LocalDateTime.now());
//            user updatedEmployee = userRepository.save(employee);
//
//            // Update userFactory relationship if factory changed
//            if (employeeDTO.getFactoryId() != null &&
//                    !employeeDTO.getFactoryId().equals(employeeFactories.get(0).getFactory().getFactoryId())) {
//
//                factory newFactory = factoryRepository.findById(employeeDTO.getFactoryId())
//                        .orElseThrow(() -> new RuntimeException("Factory not found"));
//
//                userFactory relation = employeeFactories.get(0);
//                relation.setFactory(newFactory);
//
//                // Update bay if provided and employee is a worker
//                if (employee.getRole() == Role.WORKER && employeeDTO.getBayId() != null) {
//                    bay newBay = bayRepository.findById(employeeDTO.getBayId())
//                            .orElseThrow(() -> new RuntimeException("Bay not found"));
//                    relation.setBay(newBay);
//                }
//
//                userFactoryRepository.save(relation);
//            }
//
//            return BaseResponseDTO.success("Employee updated successfully", convertToUserDTO(updatedEmployee));
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to update employee: " + e.getMessage());
//        }
//    }
//
//    // Delete employee (soft delete)
//    @Transactional
//    public BaseResponseDTO<Void> deleteEmployee(Long employeeId, Long managerId) {
//        try {
//            // Validate manager
//            user manager = userRepository.findById(managerId)
//                    .orElseThrow(() -> new RuntimeException("Manager not found"));
//
//            // Validate employee exists
//            user employee = userRepository.findById(employeeId)
//                    .orElseThrow(() -> new RuntimeException("Employee not found"));
//
//            // Check if manager has access to employee's factory
//            List<userFactory> employeeFactories = userFactoryRepository.findByUser(employee);
//            if (employeeFactories.isEmpty()) {
//                return BaseResponseDTO.error("Employee is not assigned to any factory");
//            }
//
//            boolean managerHasAccess = userFactoryRepository.findByUser(manager).stream()
//                    .anyMatch(uf -> uf.getFactory().getFactoryId().equals(employeeFactories.get(0).getFactory().getFactoryId()));
//
//            if (!managerHasAccess) {
//                return BaseResponseDTO.error("Manager does not have access to this employee");
//            }
//
//            // Soft delete employee
//            employee.setStatus(account_status.INACTIVE);
//            employee.setUpdatedAt(LocalDateTime.now());
//            userRepository.save(employee);
//
//            // Soft delete userFactory relationship
//            userFactory relation = employeeFactories.get(0);
//            relation.setStatus(account_status.INACTIVE);
//            userFactoryRepository.save(relation);
//
//            return BaseResponseDTO.success("Employee deleted successfully");
//
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to delete employee: " + e.getMessage());
//        }
//    }
//
//    // Get available bays for a factory (for assigning to workers)
//    public BaseResponseDTO<List<BayDTO>> getAvailableBays(Long factoryId) {
//        try {
//            List<bay> bays = bayRepository.findByFactoryFactoryId(factoryId);
//            List<BayDTO> bayDTOs = bays.stream()
//                    .map(this::convertToBayDTO)
//                    .collect(Collectors.toList());
//
//            return BaseResponseDTO.success("Bays fetched successfully", bayDTOs);
//        } catch (Exception e) {
//            return BaseResponseDTO.error("Failed to fetch bays: " + e.getMessage());
//        }
//    }
//
//    private UserDTO convertToUserDTO(user user) {
//        UserDTO dto = new UserDTO();
//        dto.setUserId(user.getUserId());
//        dto.setUsername(user.getUsername());
//        dto.setEmail(user.getEmail());
//        dto.setPhone(user.getPhone().toString());
//        dto.setRole(user.getRole());
//        dto.setStatus(user.getStatus());
//        dto.setCreatedAt(user.getCreatedAt());
//        dto.setUpdatedAt(user.getUpdatedAt());
//        return dto;
//    }
//
//    private EmployeeDetailDTO convertToEmployeeDetailDTO(user user, userFactory userFactory) {
//        EmployeeDetailDTO dto = new EmployeeDetailDTO();
//        dto.setUserId(user.getUserId());
//        dto.setUsername(user.getUsername());
//        dto.setEmail(user.getEmail());
//        dto.setRole(user.getRole());
//        dto.setStatus(user.getStatus());
//        dto.setPhone(user.getPhone() != null ? user.getPhone().toString() : null);
//        dto.setImg(user.getImg());
//        dto.setCreatedAt(user.getCreatedAt());
//
//        // Factory information
//        List<FactoryInfoDTO> factoryInfo = new ArrayList<>();
//        FactoryInfoDTO info = new FactoryInfoDTO();
//        info.setFactoryId(userFactory.getFactory().getFactoryId());
//        info.setFactoryName(userFactory.getFactory().getName());
//        info.setLocation(userFactory.getFactory().getCity());
//        factoryInfo.add(info);
//        dto.setFactories(factoryInfo);
//
//        return dto;
//    }
//
//    private BayDTO convertToBayDTO(bay bay) {
//        BayDTO dto = new BayDTO();
//        dto.setBayId(bay.getBayId());
//        dto.setName(bay.getName());
//        dto.setDescription(bay.getDescription());
//        return dto;
//    }
//
//    private void sendEmployeeWelcomeEmail(user employee, String password, String factoryName, Role role) {
//        try {
//            String subject = "Welcome to " + factoryName + " - Inventory System";
//            String message = "Dear " + employee.getUsername() + ",\n\n" +
//                    "Welcome to Inventory Factory Management System!\n\n" +
//                    "Your account has been created successfully as a " + role + " at " + factoryName + ".\n\n" +
//                    "Your Login Credentials:\n" +
//                    "Email: " + employee.getEmail() + "\n" +
//                    "Password: " + password + "\n\n" +
//                    "Please login and change your password immediately.\n\n" +
//                    "Login URL: http://localhost:8080/auth/login\n\n" +
//                    "Best regards,\n" +
//                    factoryName + " Management Team";
//
//            emailService.sendEmail(employee.getEmail(), subject, message);
//        } catch (Exception e) {
//            System.err.println("Failed to send welcome email: " + e.getMessage());
//        }
//    }
//
//
//}
