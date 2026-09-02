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
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    private User requireAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"ADMIN".equals(user.getRole())) return null;
        return user;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User admin = requireAdmin(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("user", admin);
        model.addAttribute("users", userService.getAllStaffUsers());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        User admin = requireAdmin(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("user", admin);
        model.addAttribute("formUser", new User());
        return "admin/users/form";
    }

    @PostMapping("/new")
    public String create(HttpSession session,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam String role,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) MultipartFile image,
                         Model model) {
        User admin = requireAdmin(session);
        if (admin == null) return "redirect:/login";

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setAddress(address);
        newUser.setPhone(phone);

        String error = userService.createUser(newUser, password, confirmPassword, image);
        if (error != null) {
            model.addAttribute("user", admin);
            model.addAttribute("formUser", newUser);
            model.addAttribute("error", error);
            return "admin/users/form";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable int id, Model model) {
        User admin = requireAdmin(session);
        if (admin == null) return "redirect:/login";

        User target = userService.getUserById(id);
        if (target == null || "ADMIN".equals(target.getRole())) return "redirect:/admin/users";

        model.addAttribute("user", admin);
        model.addAttribute("formUser", target);
        return "admin/users/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(HttpSession session, @PathVariable int id,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam(required = false) String password,
                         @RequestParam(required = false) String confirmPassword,
                         @RequestParam String role,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) MultipartFile image,
                         Model model) {
        User admin = requireAdmin(session);
        if (admin == null) return "redirect:/login";

        User target = new User();
        target.setUserId(id);
        target.setUsername(username);
        target.setEmail(email);
        target.setRole(role);
        target.setAddress(address);
        target.setPhone(phone);

        String error = userService.updateUser(target, password, confirmPassword, image);
        if (error != null) {
            model.addAttribute("user", admin);
            model.addAttribute("formUser", target);
            model.addAttribute("error", error);
            return "admin/users/edit";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(HttpSession session, @PathVariable int id) {
        User admin = requireAdmin(session);
        if (admin == null) return "redirect:/login";

        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}