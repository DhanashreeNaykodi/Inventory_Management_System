package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.Specifications.UserSpecifications;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.account_status;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private userFactoryRepository userFactoryRepository;

    @Autowired
    private factoryRepository factoryRepository;

    @Autowired
    private BayRepository bayRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    UserCentralOfficeRepository userCentralOfficeRepository;

    @Autowired
    private SecurityUtil securityUtils;


    // Create a new bay in a factory
    @Transactional
    public BaseResponseDTO<BayDTO> createBay(CreateBayDTO bayDTO) {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can create bays");
            }

            // Validate factory exists and user has access to it
            factory factory = factoryRepository.findById(bayDTO.getFactoryId())
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            // Check if user has access to this factory using security context
            if (!securityUtils.hasAccessToFactory(bayDTO.getFactoryId())) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            // Validate bay name
            if (bayDTO.getName() == null || bayDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Bay name is required");
            }

            // Check if bay with same name already exists in this factory
            boolean bayExists = bayRepository.findByFactoryFactoryId(bayDTO.getFactoryId()).stream()
                    .anyMatch(b -> b.getName().equalsIgnoreCase(bayDTO.getName()));

            if (bayExists) {
                return BaseResponseDTO.error("Bay with name '" + bayDTO.getName() + "' already exists in this factory");
            }

            // Create new bay
            bay newBay = new bay();
            newBay.setName(bayDTO.getName());
            newBay.setFactory(factory);
            newBay.setCreatedAt(LocalDateTime.now());
            newBay.setUpdatedAt(LocalDateTime.now());

            bay savedBay = bayRepository.save(newBay);

            BayDTO responseDTO = convertToBayDTO(savedBay);
            return BaseResponseDTO.success("Bay created successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create bay: " + e.getMessage());
        }
    }

    // Get all bays for a factory
    public BaseResponseDTO<List<BayDTO>> getBaysInFactory() {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access bay information");
            }

            // Check if user has access to this factory using security context
            if (!securityUtils.hasAccessToFactory(currentUser.getUserId())) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            List<bay> bays = bayRepository.findByFactoryFactoryId(currentUser.getUserId());
            List<BayDTO> bayDTOs = bays.stream()
                    .map(this::convertToBayDTO)
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Bays fetched successfully", bayDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch bays: " + e.getMessage());
        }
    }

    // Update the convertToBayDTO method to include factory info
    private BayDTO convertToBayDTO(bay bay) {
        BayDTO dto = new BayDTO();
        dto.setBayId(bay.getBay_id());
        dto.setName(bay.getName());
        dto.setDescription(bay.getName()); // Using name as description since description field doesn't exist
        if (bay.getFactory() != null) {
            dto.setFactoryId(bay.getFactory().getFactoryId());
            dto.setFactoryName(bay.getFactory().getName());
        }
        return dto;
    }

    // Manager or Owner creates worker/supervisor for factory
    @Transactional
    public BaseResponseDTO<UserDTO> createEmployee(CreateEmployeeDTO employeeDTO) {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can create employees");
            }

            Long factoryId;

            // Determine factory ID based on user role
            if (currentUser.getRole() == Role.OWNER) {
                // OWNER must provide factoryId
                if (employeeDTO.getFactoryId() == null) {
                    return BaseResponseDTO.error("Factory ID is required when creating employees as OWNER");
                }
                factoryId = employeeDTO.getFactoryId();
            } else if (currentUser.getRole() == Role.MANAGER) {
                // MANAGER can only create employees in their own factory
                // Get manager's factory from their userFactory relationship
                List<userFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
                if (managerFactories.isEmpty()) {
                    return BaseResponseDTO.error("Manager is not assigned to any factory");
                }

                // Use the first factory (assuming manager has one primary factory)
                factoryId = managerFactories.get(0).getFactory().getFactoryId();

                // If manager provides factoryId, validate it matches their factory
                if (employeeDTO.getFactoryId() != null && !employeeDTO.getFactoryId().equals(factoryId)) {
                    return BaseResponseDTO.error("Manager can only create employees in their assigned factory");
                }
            } else {
                return BaseResponseDTO.error("Unauthorized to create employees");
            }

            // Validate factory exists and user has access to it
            factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            // Check if user has access to this factory using security context
            if (!securityUtils.hasAccessToFactory(factoryId)) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            // Validate email doesn't exist
            if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
                return BaseResponseDTO.error("User with this email already exists");
            }

            // Validate role
            Role employeeRole;
            try {
                employeeRole = Role.valueOf(employeeDTO.getRole());
            } catch (IllegalArgumentException e) {
                return BaseResponseDTO.error("Invalid role. Must be WORKER or CHIEF_SUPERVISOR");
            }

            if (employeeRole != Role.WORKER && employeeRole != Role.CHIEF_SUPERVISOR) {
                return BaseResponseDTO.error("Role must be WORKER or CHIEF_SUPERVISOR");
            }

            // Validate bay for workers
            bay employeeBay = null;
            if (employeeRole == Role.WORKER) {
                if (employeeDTO.getBayId() == null) {
                    return BaseResponseDTO.error("Bay ID is required for workers");
                }
                employeeBay = bayRepository.findById(employeeDTO.getBayId())
                        .orElseThrow(() -> new RuntimeException("Bay not found"));

                // Check if bay belongs to the same factory
                if (!employeeBay.getFactory().getFactoryId().equals(factoryId)) {
                    return BaseResponseDTO.error("Bay does not belong to the specified factory");
                }
            }

            // Create new employee user
            user newEmployee = new user();
            newEmployee.setUsername(employeeDTO.getUsername());
            newEmployee.setEmail(employeeDTO.getEmail());
            newEmployee.setPhone(Long.parseLong(employeeDTO.getPhone()));
            newEmployee.setRole(employeeRole);
            newEmployee.setStatus(account_status.ACTIVE);

            // Generate password (first 3 chars of username + @ + first 7 digits of phone)
            String generatedPassword = employeeDTO.getUsername().substring(0, 3) + "@" + employeeDTO.getPhone().substring(0, 7);
            newEmployee.setPassword(passwordEncoder.encode(generatedPassword));

            newEmployee.setCreatedAt(LocalDateTime.now());
            newEmployee.setUpdatedAt(LocalDateTime.now());

            user savedEmployee = userRepository.save(newEmployee);

            // Create userFactory relationship
            userFactory userFactoryRelation = new userFactory();
            userFactoryRelation.setUser(savedEmployee);
            userFactoryRelation.setFactory(factory);
            userFactoryRelation.setUserRole(employeeRole);
            userFactoryRelation.setStatus(account_status.ACTIVE);
            userFactoryRelation.setBay(employeeBay); // Set bay only for workers

            userFactory savedUserFactory = userFactoryRepository.save(userFactoryRelation);

            // Send welcome email
            sendEmployeeWelcomeEmail(savedEmployee, generatedPassword, factory.getName(), employeeRole);

            // FIXED: Use the method that includes factory information
            UserDTO responseDTO = convertToUserDTOWithFactory(savedEmployee, savedUserFactory);
            return BaseResponseDTO.success("Employee created successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create employee: " + e.getMessage());
        }
    }

    // Add this new method to include factory information in UserDTO
    private UserDTO convertToUserDTOWithFactory(user user, userFactory userFactory) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone().toString());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Add factory information
        if (userFactory != null && userFactory.getFactory() != null) {
            dto.setFactoryId(userFactory.getFactory().getFactoryId());
            dto.setFactoryName(userFactory.getFactory().getName());
            dto.setFactoryRole(userFactory.getUserRole());
        }

        return dto;
    }

    // Get all employees for a factory (Manager's or Owner's)
    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactory(Long factoryId) {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access employee data");
            }

            // Check if user has access to this factory using security context
            if (!securityUtils.hasAccessToFactory(factoryId)) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            // Get all userFactory mappings for this factory
            List<userFactory> factoryEmployees = userFactoryRepository.findByFactoryId(factoryId);

            List<EmployeeDetailDTO> employees = factoryEmployees.stream()
                    .map(uf -> convertToEmployeeDetailDTO(uf.getUser(), uf))
                    .collect(Collectors.toList());

            EmployeeResponseDTO response = new EmployeeResponseDTO();
            response.setEmployees(employees);
            response.setTotalCount(employees.size());

            return BaseResponseDTO.success("Employees fetched successfully", response);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch employees: " + e.getMessage());
        }
    }

    //making accessible to chief officers too...
    // Get factories accessible to current user (Manager or Owner)
    public BaseResponseDTO<List<FactoryInfoDTO>> getMyFactories() {
        try {
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access this endpoint");
            }

            // OWNER gets all factories
            if (currentUser.getRole() == Role.OWNER) {
                List<factory> allFactories = factoryRepository.findAll();
                List<FactoryInfoDTO> factoryInfo = allFactories.stream()
                        .map(this::convertToFactoryInfoDTO)
                        .collect(Collectors.toList());
                return BaseResponseDTO.success("All factories (OWNER access)", factoryInfo);
            }

            // MANAGER gets only assigned factories
            List<userFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            List<FactoryInfoDTO> factoryInfo = managerFactories.stream()
                    .map(uf -> convertToFactoryInfoDTO(uf.getFactory()))
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Your assigned factories", factoryInfo);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get factories: " + e.getMessage());
        }
    }

    // Helper method to convert factory to FactoryInfoDTO
    private FactoryInfoDTO convertToFactoryInfoDTO(factory factory) {
        FactoryInfoDTO info = new FactoryInfoDTO();
        info.setFactoryId(factory.getFactoryId());
        info.setFactoryName(factory.getName());
        info.setLocation(factory.getCity());
        return info;
    }

    // Update employee (Manager or Owner)
    @Transactional
    public BaseResponseDTO<UserDTO> updateEmployee(Long employeeId, CreateEmployeeDTO employeeDTO) {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can update employees");
            }

            // Validate employee exists
            user employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Check if user can manage this employee
            if (!securityUtils.canManageEmployee(employeeId)) {
                return BaseResponseDTO.error("You don't have access to manage this employee");
            }

            List<userFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                return BaseResponseDTO.error("Employee is not assigned to any factory");
            }

            Long employeeFactoryId = employeeFactories.get(0).getFactory().getFactoryId();

            // Update employee details
            if (employeeDTO.getUsername() != null) {
                employee.setUsername(employeeDTO.getUsername());
            }
            if (employeeDTO.getEmail() != null && !employeeDTO.getEmail().equals(employee.getEmail())) {
                if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
                    return BaseResponseDTO.error("Email already exists");
                }
                employee.setEmail(employeeDTO.getEmail());
            }
            if (employeeDTO.getPhone() != null) {
                employee.setPhone(Long.parseLong(employeeDTO.getPhone()));
            }

            employee.setUpdatedAt(LocalDateTime.now());
            user updatedEmployee = userRepository.save(employee);

            // Update userFactory relationship if factory changed
            if (employeeDTO.getFactoryId() != null &&
                    !employeeDTO.getFactoryId().equals(employeeFactoryId)) {

                // Check if user has access to the new factory
                if (!securityUtils.hasAccessToFactory(employeeDTO.getFactoryId())) {
                    return BaseResponseDTO.error("You don't have access to the target factory");
                }

                factory newFactory = factoryRepository.findById(employeeDTO.getFactoryId())
                        .orElseThrow(() -> new RuntimeException("Factory not found"));

                userFactory relation = employeeFactories.get(0);
                relation.setFactory(newFactory);

                // Update bay if provided and employee is a worker
                if (employee.getRole() == Role.WORKER && employeeDTO.getBayId() != null) {
                    bay newBay = bayRepository.findById(employeeDTO.getBayId())
                            .orElseThrow(() -> new RuntimeException("Bay not found"));
                    relation.setBay(newBay);
                }

                userFactoryRepository.save(relation);
            }

// Get the updated userFactory relationship
            List<userFactory> updatedFactories = userFactoryRepository.findByUser(updatedEmployee);
            userFactory updatedUserFactory = updatedFactories.isEmpty() ? null : updatedFactories.get(0);

            UserDTO responseDTO = convertToUserDTOWithFactory(updatedEmployee, updatedUserFactory);
            return BaseResponseDTO.success("Employee updated successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update employee: " + e.getMessage());
        }
    }

    // Delete employee (soft delete) - Manager or Owner
    @Transactional
    public BaseResponseDTO<Void> deleteEmployee(Long employeeId) {
        try {
            // Get current authenticated user from security context
            user currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can delete employees");
            }

            // Check if user can manage this employee
            if (!securityUtils.canManageEmployee(employeeId)) {
                return BaseResponseDTO.error("You don't have access to manage this employee");
            }

            // Validate employee exists
            user employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            List<userFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                return BaseResponseDTO.error("Employee is not assigned to any factory");
            }

            // Soft delete employee
            employee.setStatus(account_status.INACTIVE);
            employee.setUpdatedAt(LocalDateTime.now());
            userRepository.save(employee);

            // Soft delete userFactory relationship
            userFactory relation = employeeFactories.get(0);
            relation.setStatus(account_status.INACTIVE);
            userFactoryRepository.save(relation);

            return BaseResponseDTO.success("Employee deleted successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete employee: " + e.getMessage());
        }
    }

    //duplicate (getBayByFactory) so causing error
//    // Get available bays for a factory (for assigning to workers) - Manager or Owner
//    public BaseResponseDTO<List<BayDTO>> getAvailableBays(Long factoryId) {
//        try {
//            // Get current authenticated user from security context
//            user currentUser = securityUtils.getCurrentUser();
//
//            if (!securityUtils.isManagerOrOwner()) {
//                return BaseResponseDTO.error("Only managers or owners can access bay information");
//            }
//
//            // Check if user has access to this factory using security context
//            if (!securityUtils.hasAccessToFactory(factoryId)) {
//                return BaseResponseDTO.error("You don't have access to this factory");
//            }
//
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

    // Get all employees with filtering (Owner or Central Officer only)
    public BaseResponseDTO<EmployeeResponseDTO> getAllEmployees(EmployeeFilterDTO filterDTO) {
        try {
            // Build specification with filters
            Specification<user> spec = UserSpecifications.withFilters(
                    filterDTO.getSearch(),
                    filterDTO.getRole(),
                    filterDTO.getFactoryId()
            );

            // Get filtered users
            List<user> users = userRepository.findAll(spec);

            // Convert to DTO with proper factory information
            List<EmployeeDetailDTO> employeeDetails = users.stream()
                    .map(this::convertToEmployeeDetailDTO)
                    .collect(Collectors.toList());

            EmployeeResponseDTO response = new EmployeeResponseDTO();
            response.setEmployees(employeeDetails);
            response.setTotalCount(employeeDetails.size());

            return BaseResponseDTO.success("Employees fetched successfully", response);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch employees: " + e.getMessage());
        }
    }

    // Private helper methods
    private UserDTO convertToUserDTO(user user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone().toString());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setPassword("Password sent to gmail.");
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private EmployeeDetailDTO convertToEmployeeDetailDTO(user user, userFactory userFactory) {
        EmployeeDetailDTO dto = new EmployeeDetailDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setPhone(user.getPhone() != null ? user.getPhone().toString() : null);
        dto.setImg(user.getImg());
        dto.setCreatedAt(user.getCreatedAt());

        // Factory information
        List<FactoryInfoDTO> factoryInfo = new ArrayList<>();
        FactoryInfoDTO info = new FactoryInfoDTO();
        info.setFactoryId(userFactory.getFactory().getFactoryId());
        info.setFactoryName(userFactory.getFactory().getName());
        info.setLocation(userFactory.getFactory().getCity());
        factoryInfo.add(info);
        dto.setFactories(factoryInfo);

        return dto;
    }

    private EmployeeDetailDTO convertToEmployeeDetailDTO(user userEntity) {
        EmployeeDetailDTO dto = new EmployeeDetailDTO();

        // Basic user information
        dto.setUserId(userEntity.getUserId());
        dto.setUsername(userEntity.getUsername());
        dto.setEmail(userEntity.getEmail());
        dto.setRole(userEntity.getRole());
        dto.setStatus(userEntity.getStatus());
        dto.setPhone(userEntity.getPhone() != null ? userEntity.getPhone().toString() : null);
        dto.setImg(userEntity.getImg());
        dto.setCreatedAt(userEntity.getCreatedAt());

        // Factory information based on role
        List<FactoryInfoDTO> factoryInfo = getFactoryInfoForUser(userEntity);
        dto.setFactories(factoryInfo);

        return dto;
    }

    private List<FactoryInfoDTO> getFactoryInfoForUser(user userEntity) {
        List<FactoryInfoDTO> factoryInfo = new ArrayList<>();

        switch (userEntity.getRole()) {
            case CENTRAL_OFFICER:
                // Central officers belong to central office
                Optional<users_centralOffice> centralMappings = userCentralOfficeRepository.findByUser(userEntity);
                if (centralMappings.isPresent()) {
                    FactoryInfoDTO centralOfficeInfo = new FactoryInfoDTO();
                    centralOfficeInfo.setFactoryId(null); // No factory ID for central office
                    centralOfficeInfo.setFactoryName("Central Office");
                    centralOfficeInfo.setLocation(centralMappings.get().getOffice().getLocation());
                    factoryInfo.add(centralOfficeInfo);
                }
                break;

            case MANAGER:
                // Managers can belong to multiple factories
                List<userFactory> managerMappings = userFactoryRepository.findByUser(userEntity);
                factoryInfo.addAll(managerMappings.stream()
                        .map(mapping -> {
                            FactoryInfoDTO info = new FactoryInfoDTO();
                            info.setFactoryId(mapping.getFactory().getFactoryId());
                            info.setFactoryName(mapping.getFactory().getName());
                            info.setLocation(mapping.getFactory().getCity());
                            return info;
                        })
                        .collect(Collectors.toList()));
                break;

            case CHIEF_SUPERVISOR:
            case WORKER:
                // Supervisors and workers belong to one factory
                List<userFactory> workerMappings = userFactoryRepository.findByUser(userEntity);
                if (!workerMappings.isEmpty()) {
                    userFactory mapping = workerMappings.get(0);
                    FactoryInfoDTO info = new FactoryInfoDTO();
                    info.setFactoryId(mapping.getFactory().getFactoryId());
                    info.setFactoryName(mapping.getFactory().getName());
                    info.setLocation(mapping.getFactory().getCity());
                    factoryInfo.add(info);
                }
                break;
        }

        return factoryInfo;
    }

    private void sendEmployeeWelcomeEmail(user employee, String password, String factoryName, Role role) {
        try {
            String subject = "Welcome to " + factoryName + " - Inventory System";
            String message = "Dear " + employee.getUsername() + ",\n\n" +
                    "Welcome to Inventory Factory Management System!\n\n" +
                    "Your account has been created successfully as a " + role + " at " + factoryName + ".\n\n" +
                    "Your Login Credentials:\n" +
                    "Email: " + employee.getEmail() + "\n" +
                    "Password: " + password + "\n\n" +
                    "Please login and change your password immediately.\n\n" +
                    "Login URL: http://localhost:8080/auth/login\n\n" +
                    "Best regards,\n" +
                    factoryName + " Management Team";

            emailService.sendEmail(employee.getEmail(), subject, message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
}