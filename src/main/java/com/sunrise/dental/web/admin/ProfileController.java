package com.sunrise.dental.web.admin;

import com.sunrise.dental.model.User;
import com.sunrise.dental.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String view(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        model.addAttribute("user", admin);
        return "admin/profile";
    }

    @PostMapping
    public String update(HttpSession session,
                         @RequestParam String email,
                         @RequestParam(required = false) String password,
                         @RequestParam(required = false) String confirmPassword,
                         @RequestParam(required = false) MultipartFile image,
                         Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return "redirect:/login";

        String error = userService.updateOwnProfile(admin, email, password, confirmPassword, image);
        if (error != null) {
            model.addAttribute("user", admin);
            model.addAttribute("error", error);
            return "admin/profile";
        }

        // refresh session copy so navbar/profile reflect changes immediately
        session.setAttribute("loggedInUser", admin);
        model.addAttribute("user", admin);
        model.addAttribute("success", "Profile updated successfully.");
        return "admin/profile";
    }
}