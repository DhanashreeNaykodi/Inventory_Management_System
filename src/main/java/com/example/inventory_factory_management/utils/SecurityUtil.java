package com.example.inventory_factory_management.utils;

import com.example.inventory_factory_management.constants.Role;
import com.example.inventory_factory_management.entity.User;
import com.example.inventory_factory_management.entity.UserFactory;
import com.example.inventory_factory_management.repository.UserFactoryRepository;
import com.example.inventory_factory_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFactoryRepository userFactoryRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public boolean isManagerOrOwner() {
        User currentUser = getCurrentUser();
        return currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.OWNER;
    }

    public boolean hasAccessToFactory(Long factoryId) {
        User currentUser = getCurrentUser();

        // OWNER has access to all factories
        if (currentUser.getRole() == Role.OWNER) {
            return true;
        }

        // MANAGER needs to be assigned to the factory
        if (currentUser.getRole() == Role.MANAGER) {
            return userFactoryRepository.findByUser(currentUser).stream()
                    .anyMatch(uf -> uf.getFactory().getFactoryId().equals(factoryId));
        }

        return false;
    }

    public boolean canManageEmployee(Long employeeId) {
        User currentUser = getCurrentUser();

        // OWNER can manage any employee
        if (currentUser.getRole() == Role.OWNER) {
            return true;
        }

        // MANAGER can only manage employees in their factories
        if (currentUser.getRole() == Role.MANAGER) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            List<UserFactory> employeeFactories = userFactoryRepository.findByUser(employee);
            if (employeeFactories.isEmpty()) {
                return false;
            }

            Long employeeFactoryId = employeeFactories.get(0).getFactory().getFactoryId();
            return hasAccessToFactory(employeeFactoryId);
        }

        return false;
    }

    public Long getCurrentUserFactoryId() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.MANAGER) {
            // Get manager's assigned factory
//            List<userFactory> userFactories = userFactoryRepository.findByUserId(currentUser.getId());
            List<UserFactory> userFactories = userFactoryRepository.findByUser(currentUser);
            if (!userFactories.isEmpty()) {
//                return userFactories.get(0).getFactory().getId(); // Assuming one factory per manager
                return userFactories.get(0).getFactory().getFactoryId();
            }
        }

        return null;
    }
}