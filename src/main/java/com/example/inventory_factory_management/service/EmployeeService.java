package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.DTO.*;
import com.example.inventory_factory_management.Specifications.UserSpecifications;
import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.constants.AccountStatus;
import com.example.inventory_factory_management.entity.*;
import com.example.inventory_factory_management.repository.*;
import com.example.inventory_factory_management.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFactoryRepository userFactoryRepository;

    @Autowired
    private FactoryRepository factoryRepository;

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

    @Transactional
    public BaseResponseDTO<BayDTO> createBay(CreateBayDTO bayDTO) {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (currentUser.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("Only managers can create bays");
            }

            List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            if (managerFactories.isEmpty()) {
                return BaseResponseDTO.error("Manager is not assigned to any factory");
            }

            Long factoryId = managerFactories.get(0).getFactory().getFactoryId();
            Factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            if (bayDTO.getName() == null || bayDTO.getName().trim().isEmpty()) {
                return BaseResponseDTO.error("Bay name is required");
            }

            boolean bayExists = bayRepository.existsByNameAndFactoryFactoryId(bayDTO.getName(), factoryId);
            if (bayExists) {
                return BaseResponseDTO.error("Bay with name '" + bayDTO.getName() + "' already exists in this factory");
            }

            Bay newBay = new Bay();
            newBay.setName(bayDTO.getName());
            newBay.setFactory(factory);
            newBay.setCreatedAt(LocalDateTime.now());
            newBay.setUpdatedAt(LocalDateTime.now());

            Bay savedBay = bayRepository.save(newBay);

            BayDTO responseDTO = convertToBayDTO(savedBay);
            return BaseResponseDTO.success("Bay created successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create bay: " + e.getMessage());
        }
    }

    // Get all bays for a factory - FIXED for Owners
    public BaseResponseDTO<List<BayDTO>> getBaysInFactory(@RequestParam(required = false) Long factoryId) {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access bay information");
            }

            Long targetFactoryId = factoryId;

            if (currentUser.getRole() == Role.MANAGER) {
                List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
                if (managerFactories.isEmpty()) {
                    return BaseResponseDTO.error("Manager is not assigned to any factory");
                }
                targetFactoryId = managerFactories.get(0).getFactory().getFactoryId();
            } else if (currentUser.getRole() == Role.OWNER && factoryId == null) {
                return BaseResponseDTO.error("Factory ID is required for owner");
            }

            if (!securityUtils.hasAccessToFactory(targetFactoryId)) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            List<Bay> Bays = bayRepository.findByFactoryFactoryId(targetFactoryId);
            List<BayDTO> bayDTOs = Bays.stream()
                    .map(this::convertToBayDTO)
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Bays fetched successfully", bayDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch bays: " + e.getMessage());
        }
    }

    // Get employees by factory ID
    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactoryId(Long factoryId) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access employee data");
            }

            Long targetFactoryId = factoryId;

            if (currentUser.getRole() == Role.MANAGER && factoryId == null) {
                targetFactoryId = getCurrentUserFactoryId();
                if (targetFactoryId == null) {
                    return BaseResponseDTO.error("Manager is not assigned to any factory");
                }
            }
            else if (currentUser.getRole() == Role.MANAGER && factoryId != null) {
                if (!securityUtils.hasAccessToFactory(factoryId)) {
                    return BaseResponseDTO.error("You don't have access to this factory");
                }
                targetFactoryId = factoryId;
            }
            else if (currentUser.getRole() == Role.OWNER && factoryId == null) {
                return BaseResponseDTO.error("Factory ID is required for owner");
            }

            List<UserFactory> factoryEmployees = userFactoryRepository.findByFactoryId(targetFactoryId);

            List<EmployeeDetailDTO> employees = factoryEmployees.stream()
                    .map(uf -> convertToEmployeeDetailDTO(uf.getUser(), uf))
                    .collect(Collectors.toList());

            EmployeeResponseDTO response = new EmployeeResponseDTO();
            response.setEmployees(employees);
            response.setTotalCount(employees.size());
            response.setFactoryId(targetFactoryId);

            factoryRepository.findById(targetFactoryId)
                    .ifPresent(f -> response.setFactoryName(f.getName()));

            return BaseResponseDTO.success("Employees fetched successfully", response);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch employees: " + e.getMessage());
        }
    }

    // Get employees by factory name
    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactoryName(String factoryName) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access employee data");
            }

            Factory factory = factoryRepository.findByName(factoryName)
                    .orElseThrow(() -> new RuntimeException("Factory not found with name: " + factoryName));

            if (!securityUtils.hasAccessToFactory(factory.getFactoryId())) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            return getEmployeesByFactoryId(factory.getFactoryId());

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch employees: " + e.getMessage());
        }
    }

    // Helper method
    private Long getCurrentUserFactoryId() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.MANAGER) {
            List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            return managerFactories.isEmpty() ? null : managerFactories.get(0).getFactory().getFactoryId();
        }
        return null;
    }

    // ✅ FIXED: Manager creates employee
    @Transactional
    public BaseResponseDTO<UserDTO> createEmployee(CreateEmployeeDTO employeeDTO) {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (currentUser.getRole() != Role.MANAGER) {
                return BaseResponseDTO.error("Only managers can create employees");
            }

            List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            if (managerFactories.isEmpty()) {
                return BaseResponseDTO.error("Manager is not assigned to any factory");
            }

            Long factoryId = managerFactories.get(0).getFactory().getFactoryId();
            Factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
                return BaseResponseDTO.error("User with this email already exists");
            }

            Role employeeRole = employeeDTO.getRole();
            if (employeeRole != Role.WORKER && employeeRole != Role.CHIEF_SUPERVISOR) {
                return BaseResponseDTO.error("Role must be WORKER or CHIEF_SUPERVISOR");
            }

            if (employeeRole == Role.CHIEF_SUPERVISOR) {
                boolean chiefSupervisorExists = userFactoryRepository.findByFactoryId(factoryId).stream()
                        .anyMatch(uf -> uf.getUserRole() == Role.CHIEF_SUPERVISOR && uf.getStatus() == AccountStatus.ACTIVE);

                if (chiefSupervisorExists) {
                    return BaseResponseDTO.error("Chief supervisor already exists for this factory");
                }
            }

            Bay employeeBay = null;
            if (employeeRole == Role.WORKER) {
                if (employeeDTO.getBayId() == null) {
                    return BaseResponseDTO.error("Bay ID is required for workers");
                }
                employeeBay = bayRepository.findById(employeeDTO.getBayId())
                        .orElseThrow(() -> new RuntimeException("Bay not found"));

                if (!employeeBay.getFactory().getFactoryId().equals(factoryId)) {
                    return BaseResponseDTO.error("Bay does not belong to your factory");
                }
            }

            User newEmployee = new User();
            newEmployee.setUsername(employeeDTO.getUsername());
            newEmployee.setEmail(employeeDTO.getEmail());
            newEmployee.setPhone(Long.parseLong(employeeDTO.getPhone()));
            newEmployee.setRole(employeeRole);
            newEmployee.setStatus(AccountStatus.ACTIVE);
            newEmployee.setImg("src/main/resources/static/images/user-profile-icon.jpg");

            String generatedPassword = employeeDTO.getUsername().substring(0, 3) + "@" + employeeDTO.getPhone().substring(0, 7);
            newEmployee.setPassword(passwordEncoder.encode(generatedPassword));

            newEmployee.setCreatedAt(LocalDateTime.now());
            newEmployee.setUpdatedAt(LocalDateTime.now());

            User savedEmployee = userRepository.save(newEmployee);

            UserFactory userFactoryRelation = new UserFactory();
            userFactoryRelation.setUser(savedEmployee);
            userFactoryRelation.setFactory(factory);
            userFactoryRelation.setUserRole(employeeRole);
            userFactoryRelation.setStatus(AccountStatus.ACTIVE);
            userFactoryRelation.setBay(employeeBay);

            UserFactory savedUserFactory = userFactoryRepository.save(userFactoryRelation);

            sendEmployeeWelcomeEmail(savedEmployee, generatedPassword, factory.getName(), employeeRole);

            UserDTO responseDTO = convertToUserDTOWithFactory(savedEmployee, savedUserFactory);
            return BaseResponseDTO.success("Employee created successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to create employee: " + e.getMessage());
        }
    }

    // ✅ FIXED: Update employee - only name, email, phone, bay (NO factory transfer)
    @Transactional
    public BaseResponseDTO<UserDTO> updateEmployee(Long employeeId, CreateEmployeeDTO employeeDTO) {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can update employees");
            }

            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            if (!securityUtils.canManageEmployee(employeeId)) {
                return BaseResponseDTO.error("You don't have access to manage this employee");
            }

            List<UserFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                return BaseResponseDTO.error("Employee is not assigned to any factory");
            }

            UserFactory employeeFactory = employeeFactories.get(0);
            Long currentFactoryId = employeeFactory.getFactory().getFactoryId();

            // ✅ UPDATE BASIC INFO: name, email, phone
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
            User updatedEmployee = userRepository.save(employee);

            // ✅ UPDATE BAY (only for workers and if bayId is provided)
            if (employee.getRole() == Role.WORKER && employeeDTO.getBayId() != null) {
                Bay newBay = bayRepository.findById(employeeDTO.getBayId())
                        .orElseThrow(() -> new RuntimeException("Bay not found"));

                // Validate bay belongs to same factory
                if (!newBay.getFactory().getFactoryId().equals(currentFactoryId)) {
                    return BaseResponseDTO.error("Bay does not belong to employee's factory");
                }

                employeeFactory.setBay(newBay);
                userFactoryRepository.save(employeeFactory);
            }

            // ✅ NO FACTORY TRANSFER LOGIC - removed as requested

            List<UserFactory> updatedFactories = userFactoryRepository.findByUser(updatedEmployee);
            UserFactory updatedUserFactory = updatedFactories.isEmpty() ? null : updatedFactories.get(0);

            UserDTO responseDTO = convertToUserDTOWithFactory(updatedEmployee, updatedUserFactory);
            return BaseResponseDTO.success("Employee updated successfully", responseDTO);

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to update employee: " + e.getMessage());
        }
    }



    // Get all bays for a factory - FIXED: Use current user's factory
    public BaseResponseDTO<List<BayDTO>> getBaysInFactory() {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access bay information");
            }

            // Get user's factory ID
            Long factoryId;
            if (currentUser.getRole() == Role.MANAGER) {
                List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
                if (managerFactories.isEmpty()) {
                    return BaseResponseDTO.error("Manager is not assigned to any factory");
                }
                factoryId = managerFactories.get(0).getFactory().getFactoryId();
            } else {
                // For owners, you might want to accept factoryId as parameter
                return BaseResponseDTO.error("Factory specification needed for owner");
            }

            if (!securityUtils.hasAccessToFactory(factoryId)) {
                return BaseResponseDTO.error("You don't have access to this factory");
            }

            List<Bay> Bays = bayRepository.findByFactoryFactoryId(factoryId);
            List<BayDTO> bayDTOs = Bays.stream()
                    .map(this::convertToBayDTO)
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Bays fetched successfully", bayDTOs);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to fetch bays: " + e.getMessage());
        }
    }

    // Update the convertToBayDTO method to include factory info
    private BayDTO convertToBayDTO(Bay bay) {
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


    // Add this new method to include factory information in UserDTO
    private UserDTO convertToUserDTOWithFactory(User user, UserFactory userFactory) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone().toString());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setImg(user.getImg()); // This will get the default image you set during creation

        // Add factory information
        if (userFactory != null && userFactory.getFactory() != null) {
            dto.setFactoryId(userFactory.getFactory().getFactoryId());
            dto.setFactoryName(userFactory.getFactory().getName());
            dto.setFactoryRole(userFactory.getUserRole());
        }

        return dto;
    }


    //making accessible to chief officers too...
    // Get factories accessible to current user (Manager or Owner)
    public BaseResponseDTO<List<FactoryInfoDTO>> getMyFactories() {
        try {
            User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can access this endpoint");
            }

            // OWNER gets all factories
            if (currentUser.getRole() == Role.OWNER) {
                List<Factory> allFactories = factoryRepository.findAll();
                List<FactoryInfoDTO> factoryInfo = allFactories.stream()
                        .map(this::convertToFactoryInfoDTO)
                        .collect(Collectors.toList());
                return BaseResponseDTO.success("All factories (OWNER access)", factoryInfo);
            }

            // MANAGER gets only assigned factories
            List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            List<FactoryInfoDTO> factoryInfo = managerFactories.stream()
                    .map(uf -> convertToFactoryInfoDTO(uf.getFactory()))
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Your assigned factories", factoryInfo);
        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to get factories: " + e.getMessage());
        }
    }

    // Helper method to convert factory to FactoryInfoDTO
    private FactoryInfoDTO convertToFactoryInfoDTO(Factory factory) {
        FactoryInfoDTO info = new FactoryInfoDTO();
        info.setFactoryId(factory.getFactoryId());
        info.setFactoryName(factory.getName());
        info.setLocation(factory.getCity());
        return info;
    }


    // Delete employee (soft delete) - Manager or Owner
    @Transactional
    public BaseResponseDTO<Void> deleteEmployee(Long employeeId) {
        try {
            // Get current authenticated user from security context
            User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                return BaseResponseDTO.error("Only managers or owners can delete employees");
            }

            // Check if user can manage this employee
            if (!securityUtils.canManageEmployee(employeeId)) {
                return BaseResponseDTO.error("You don't have access to manage this employee");
            }

            // Validate employee exists
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            List<UserFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                return BaseResponseDTO.error("Employee is not assigned to any factory");
            }

            // Soft delete employee
            employee.setStatus(AccountStatus.INACTIVE);
            employee.setUpdatedAt(LocalDateTime.now());
            userRepository.save(employee);

            // Soft delete userFactory relationship
            UserFactory relation = employeeFactories.get(0);
            relation.setStatus(AccountStatus.INACTIVE);
            userFactoryRepository.save(relation);

            return BaseResponseDTO.success("Employee deleted successfully");

        } catch (Exception e) {
            return BaseResponseDTO.error("Failed to delete employee: " + e.getMessage());
        }
    }


    // Get all employees with filtering (Owner or Central Officer only)
    public BaseResponseDTO<EmployeeResponseDTO> getAllEmployees(EmployeeFilterDTO filterDTO) {
        try {
            // Build specification with filters
            Specification<User> spec = UserSpecifications.withFilters(
                    filterDTO.getSearch(),
                    filterDTO.getRole(),
                    filterDTO.getFactoryId()
            );

            // Get filtered users
            List<User> users = userRepository.findAll(spec);

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
    private UserDTO convertToUserDTO(User user) {
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

    private EmployeeDetailDTO convertToEmployeeDetailDTO(User user, UserFactory userFactory) {
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

    private EmployeeDetailDTO convertToEmployeeDetailDTO(User userEntity) {
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

    private List<FactoryInfoDTO> getFactoryInfoForUser(User userEntity) {
        List<FactoryInfoDTO> factoryInfo = new ArrayList<>();

        switch (userEntity.getRole()) {
            case CENTRAL_OFFICER:
                // Central officers belong to central office
                Optional<UserCentralOffice> centralMappings = userCentralOfficeRepository.findByUser(userEntity);
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
                List<UserFactory> managerMappings = userFactoryRepository.findByUser(userEntity);
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
                List<UserFactory> workerMappings = userFactoryRepository.findByUser(userEntity);
                if (!workerMappings.isEmpty()) {
                    UserFactory mapping = workerMappings.get(0);
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

    private void sendEmployeeWelcomeEmail(User employee, String password, String factoryName, Role role) {
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