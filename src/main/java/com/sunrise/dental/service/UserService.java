package com.sunrise.dental.service;

import com.sunrise.dental.dao.UserDAO;
import com.sunrise.dental.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final String UPLOAD_DIR = "uploads/users/";
    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$";

    private final UserDAO userDAO = new UserDAO();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<User> getAllStaffUsers() {
        return userDAO.findAllStaff();
    }

    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    private String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String original = image.getOriginalFilename();
            String extension = "";
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID() + extension;
            Path path = Paths.get(UPLOAD_DIR + filename);
            Files.write(path, image.getBytes());
            return "/uploads/users/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createUser(User user, String rawPassword, String confirmPassword, MultipartFile image) {
        if (user.getUsername() == null || user.getUsername().isBlank())
            return "Username is required.";
        if (user.getEmail() == null || user.getEmail().isBlank())
            return "Email is required.";
        if (!user.getEmail().matches(EMAIL_REGEX))
            return "Enter a valid email address.";
        if (rawPassword == null || rawPassword.isBlank())
            return "Password is required.";
        if (!rawPassword.equals(confirmPassword))
            return "Passwords do not match.";
        if (userDAO.findByUsername(user.getUsername()) != null)
            return "That username is already taken.";
        if (userDAO.findByEmail(user.getEmail()) != null)
            return "That email is already registered.";

        user.setPassword(encoder.encode(rawPassword));
        user.setImagePath(saveImage(image));

        return userDAO.insert(user) ? null : "Failed to create user.";
    }

    public String updateUser(User user, String rawPassword, String confirmPassword, MultipartFile image) {
        if (user.getUsername() == null || user.getUsername().isBlank())
            return "Username is required.";
        if (user.getEmail() == null || user.getEmail().isBlank())
            return "Email is required.";
        if (!user.getEmail().matches(EMAIL_REGEX))
            return "Enter a valid email address.";

        User existing = userDAO.findById(user.getUserId());
        if (existing == null) return "User not found.";

        User byUsername = userDAO.findByUsername(user.getUsername());
        if (byUsername != null && byUsername.getUserId() != user.getUserId())
            return "That username is already taken.";

        User byEmail = userDAO.findByEmail(user.getEmail());
        if (byEmail != null && byEmail.getUserId() != user.getUserId())
            return "That email is already registered.";

        String newImagePath = saveImage(image);
        user.setImagePath(newImagePath != null ? newImagePath : existing.getImagePath());

        boolean ok = userDAO.update(user);
        if (!ok) return "Failed to update user.";

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (!rawPassword.equals(confirmPassword)) return "Passwords do not match.";
            String hashed = encoder.encode(rawPassword);
            userDAO.updatePassword(user.getUserId(), hashed);
        }

        return null;
    }

    public boolean deleteUser(int userId) {
        return userDAO.delete(userId);
    }

    // --- Admin's own profile ---

    public String updateOwnProfile(User admin, String email, String rawPassword,
                                   String confirmPassword, MultipartFile image) {
        if (email == null || email.isBlank())
            return "Email is required.";
        if (!email.matches(EMAIL_REGEX))
            return "Enter a valid email address.";

        User byEmail = userDAO.findByEmail(email);
        if (byEmail != null && byEmail.getUserId() != admin.getUserId())
            return "That email is already in use.";

        admin.setEmail(email);

        String newImagePath = saveImage(image);
        if (newImagePath != null) admin.setImagePath(newImagePath);

        boolean ok = userDAO.update(admin);
        if (!ok) return "Failed to update profile.";

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (!rawPassword.equals(confirmPassword)) return "Passwords do not match.";
            userDAO.updatePassword(admin.getUserId(), encoder.encode(rawPassword));
        }

        return null;
    }
}