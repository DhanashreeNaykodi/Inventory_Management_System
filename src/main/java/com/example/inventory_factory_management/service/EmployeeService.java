package com.example.inventory_factory_management.service;

import com.example.inventory_factory_management.dto.*;
import com.example.inventory_factory_management.exceptions.*;
import com.example.inventory_factory_management.specifications.UserSpecifications;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
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
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser.getRole() != Role.MANAGER) {
                throw new UnauthorizedAccessException("Only managers can create bays");
            }
            List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            if (managerFactories.isEmpty()) {
                throw new ResourceNotFoundException("Manager is not assigned to any factory");
            }

            Long factoryId = managerFactories.get(0).getFactory().getFactoryId();
            Factory factory = factoryRepository.findById(factoryId).orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

            boolean bayExists = bayRepository.existsByNameAndFactoryFactoryId(bayDTO.getName(), factoryId);
            if (bayExists) {
                throw new ResourceAlreadyExistsException("Bay with name '" + bayDTO.getName() + "' already exists in this factory");
            }

            Bay newBay = new Bay();
            newBay.setName(bayDTO.getName());
            newBay.setFactory(factory);
            newBay.setCreatedAt(LocalDateTime.now());
            newBay.setUpdatedAt(LocalDateTime.now());
            Bay savedBay = bayRepository.save(newBay);

            BayDTO responseDTO = convertToBayDTO(savedBay);
            return BaseResponseDTO.success("Bay created successfully", responseDTO);
    }

    // Get all bays for a factory - for Owners
    public BaseResponseDTO<List<BayDTO>> getBaysInFactory(@RequestParam(required = false) Long factoryId) {
            User currentUser = securityUtils.getCurrentUser();
            if (!securityUtils.isManagerOrOwner()) {
                throw new UnauthorizedAccessException("Only managers or owners can access bay information");
            }

            Long targetFactoryId = factoryId;
            if (currentUser.getRole() == Role.MANAGER) {
                List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
                if (managerFactories.isEmpty()) {
                    return BaseResponseDTO.error("Manager is not assigned to any factory");
                }
                targetFactoryId = managerFactories.get(0).getFactory().getFactoryId();
            } else if (currentUser.getRole() == Role.OWNER && factoryId == null) {
                throw new OperationNotPermittedException("Factory ID is required for owner");
            }

            if (!securityUtils.hasAccessToFactory(targetFactoryId)) {
                throw new UnauthorizedAccessException("You don't have access to this factory");
            }

            List<Bay> Bays = bayRepository.findByFactoryFactoryId(targetFactoryId);
            List<BayDTO> bayDTOs = Bays.stream()
                    .map(this::convertToBayDTO)
                    .collect(Collectors.toList());

            return BaseResponseDTO.success("Bays fetched successfully", bayDTOs);
    }


// Get employees by factory ID
public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactoryId(Long factoryId) {
        User currentUser = securityUtils.getCurrentUser();
        Long targetFactoryId;

        if (currentUser.getRole() == Role.MANAGER) {

            Long managerFactoryId = securityUtils.getCurrentUserFactoryId();
            if (managerFactoryId == null) {
                throw new ResourceNotFoundException("Manager is not assigned to any factory");
            }
            if (factoryId == null) {
                targetFactoryId = managerFactoryId;
            } else {
                if (!managerFactoryId.equals(factoryId)) {
                    throw new UnauthorizedAccessException("You don't have access to this factory");
                }
                targetFactoryId = factoryId;
            }
        } else {
            if (!securityUtils.hasAccessToFactory(factoryId)) {
                throw new UnauthorizedAccessException("You don't have access to this factory");
            }
            targetFactoryId = factoryId;
        }

        // Fetch user-factory mappings for the target factory
        List<UserFactory> factoryEmployees = userFactoryRepository.findByFactory_FactoryId(targetFactoryId);

        List<EmployeeDetailDTO> employees = factoryEmployees.stream()
                .map(uf -> uf.getUser())
                .filter(u -> u.getRole() == Role.WORKER || u.getRole() == Role.CHIEF_SUPERVISOR)
                .map(user -> convertToEmployeeDetailDTO(user, findUserFactory(factoryEmployees, user.getUserId())))
                .collect(Collectors.toList());

        // Prepare response
        EmployeeResponseDTO response = new EmployeeResponseDTO();
        response.setEmployees(employees);
        response.setTotalCount(employees.size());
        response.setFactoryId(targetFactoryId);

        factoryRepository.findById(targetFactoryId).ifPresent(f -> response.setFactoryName(f.getName()));

        return BaseResponseDTO.success("Employees fetched successfully", response);

}

    // Helper to get UserFactory object by user ID
    private UserFactory findUserFactory(List<UserFactory> list, Long userId) {
        return list.stream()
                .filter(uf -> uf.getUser().getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }


    // Get employees by factory name
    public BaseResponseDTO<EmployeeResponseDTO> getEmployeesByFactoryName(String factoryName) {
            User currentUser = securityUtils.getCurrentUser();
            if (!securityUtils.isManagerOrOwner()) {
                throw new UnauthorizedAccessException("Only managers or owners can access employee data");
            }

            Factory factory = factoryRepository.findByName(factoryName).orElseThrow(() -> new ResourceNotFoundException("Factory not found with name: " + factoryName));

            if (!securityUtils.hasAccessToFactory(factory.getFactoryId())) {
                throw new UnauthorizedAccessException("You don't have access to this factory");
            }
            return getEmployeesByFactoryId(factory.getFactoryId());

    }


    @Transactional
    public BaseResponseDTO<UserDTO> createEmployee(CreateEmployeeDTO employeeDTO) {
            User currentUser = securityUtils.getCurrentUser();

            if (currentUser.getRole() != Role.MANAGER) {
                throw new UnauthorizedAccessException("Only managers can create employees");
            }

            List<UserFactory> managerFactories = userFactoryRepository.findByUser(currentUser);
            if (managerFactories.isEmpty()) {
                throw new ResourceNotFoundException("Manager is not assigned to any factory");
            }

            Long factoryId = managerFactories.get(0).getFactory().getFactoryId();
            Factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

            if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("User with this email already exists");
            }

            Role employeeRole = employeeDTO.getRole();
            if (employeeRole != Role.WORKER && employeeRole != Role.CHIEF_SUPERVISOR) {
                throw new OperationNotPermittedException("Role must be WORKER or CHIEF_SUPERVISOR");
            }

            if (employeeRole == Role.CHIEF_SUPERVISOR) {
                boolean chiefSupervisorExists = userFactoryRepository.findByFactory_FactoryId(factoryId).stream()
                        .anyMatch(uf -> uf.getUserRole() == Role.CHIEF_SUPERVISOR && uf.getStatus() == AccountStatus.ACTIVE);

                if (chiefSupervisorExists) {
                    throw new UserAlreadyExistsException("Chief supervisor already exists for this factory");
                }
            }

            Bay employeeBay = null;
            if (employeeRole == Role.WORKER) {
                if (employeeDTO.getBayId() == null) {
                    throw new ResourceNotFoundException("Bay ID is required for workers");
                }
                employeeBay = bayRepository.findById(employeeDTO.getBayId()).orElseThrow(() -> new ResourceNotFoundException("Bay not found"));

                if (!employeeBay.getFactory().getFactoryId().equals(factoryId)) {
                    throw new OperationNotPermittedException("Bay does not belong to your factory");
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
    }


    @Transactional
    public BaseResponseDTO<UserDTO> updateEmployee(Long employeeId, CreateEmployeeDTO employeeDTO) {
            User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                throw new UnauthorizedAccessException("Only managers or owners can update employees");
            }
            User employee = userRepository.findById(employeeId).orElseThrow(() -> new UserNotFoundException("Employee not found"));

            if (!securityUtils.canManageEmployee(employeeId)) {
                throw new UnauthorizedAccessException("You don't have access to manage this employee");
            }

            List<UserFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                throw new ResourceNotFoundException("Employee is not assigned to any factory");
            }

            UserFactory employeeFactory = employeeFactories.get(0);
            Long currentFactoryId = employeeFactory.getFactory().getFactoryId();

            // UPDATE BASIC INFO: name, email, phone
            if (employeeDTO.getUsername() != null) {
                employee.setUsername(employeeDTO.getUsername());
            }
            if (employeeDTO.getEmail() != null && !employeeDTO.getEmail().equals(employee.getEmail())) {
                if (userRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
                    throw new UserAlreadyExistsException("Email already exists");
                }
                employee.setEmail(employeeDTO.getEmail());
            }
            if (employeeDTO.getPhone() != null) {
                employee.setPhone(Long.parseLong(employeeDTO.getPhone()));
            }
            employee.setUpdatedAt(LocalDateTime.now());
            User updatedEmployee = userRepository.save(employee);

            // UPDATE BAY (only for workers and if bayId is provided)
            if (employee.getRole() == Role.WORKER && employeeDTO.getBayId() != null) {
                Bay newBay = bayRepository.findById(employeeDTO.getBayId()).orElseThrow(() -> new ResourceNotFoundException("Bay not found"));

                // Validate bay belongs to same factory
                if (!newBay.getFactory().getFactoryId().equals(currentFactoryId)) {
                    throw new ResourceNotFoundException("Bay does not belong to employee's factory");
                }
                employeeFactory.setBay(newBay);
                userFactoryRepository.save(employeeFactory);
            }
            List<UserFactory> updatedFactories = userFactoryRepository.findByUser(updatedEmployee);
            UserFactory updatedUserFactory = updatedFactories.isEmpty() ? null : updatedFactories.get(0);

            UserDTO responseDTO = convertToUserDTOWithFactory(updatedEmployee, updatedUserFactory);
            return BaseResponseDTO.success("Employee updated successfully", responseDTO);

    }


    // Delete employee (soft delete) - Manager or Owner
    @Transactional
    public BaseResponseDTO<Void> deleteEmployee(Long employeeId) {
        User currentUser = securityUtils.getCurrentUser();

            if (!securityUtils.isManagerOrOwner()) {
                throw new UnauthorizedAccessException("Only managers or owners can delete employees");
            }
            // Check if user can manage this employee
            if (!securityUtils.canManageEmployee(employeeId)) {
                throw new UnauthorizedAccessException("You don't have access to manage this employee");
            }

            User employee = userRepository.findById(employeeId).orElseThrow(() -> new UserNotFoundException("Employee not found"));
            List<UserFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                throw new UserNotFoundException("Employee is not assigned to any factory");
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
    }


    // Get all employees with filtering (Owner or Central Officer only)
    public BaseResponseDTO<EmployeeResponseDTO> getAllEmployees(EmployeeFilterDTO filterDTO) {
            // Build specification with filters
            Specification<User> spec = UserSpecifications.withFilters(
                    filterDTO.getSearch(),
                    filterDTO.getRole(),
                    filterDTO.getFactoryId()
            );

            // Get filtered users
            List<User> users = userRepository.findAll(spec);

            List<EmployeeDetailDTO> employeeDetails = users.stream()
                    .map(this::convertToEmployeeDetailDTO)
                    .collect(Collectors.toList());

            EmployeeResponseDTO response = new EmployeeResponseDTO();
            response.setEmployees(employeeDetails);
            response.setTotalCount(employeeDetails.size());

            return BaseResponseDTO.success("Employees fetched successfully", response);
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
        dto.setImg(user.getImg());

        // Add factory information
        if (userFactory != null && userFactory.getFactory() != null) {
            dto.setFactoryId(userFactory.getFactory().getFactoryId());
            dto.setFactoryName(userFactory.getFactory().getName());
            dto.setFactoryRole(userFactory.getUserRole());
        }

        return dto;
    }


    // Helper method to convert factory to FactoryInfoDTO
    private FactoryInfoDTO convertToFactoryInfoDTO(Factory factory) {
        FactoryInfoDTO info = new FactoryInfoDTO();
        info.setFactoryId(factory.getFactoryId());
        info.setFactoryName(factory.getName());
        info.setLocation(factory.getCity());
        return info;
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