package com.mysite.sbb.user;

import java.security.Principal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "signup_form";
	}

	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "signup_form";
		}

		if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "signup_form";
		}

		try {
			userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			return "signup_form";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			return "signup_form";
		}

		return "redirect:/";
	}

	@GetMapping("/login")
	public String login() {
		return "login_form";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mypage")
	public String myPage(Model model, Principal principal) {
		SiteUser user = this.userService.getUser(principal.getName());
		model.addAttribute("user", user);
		return "mypage";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/edit")
	public String editUser(UserForm userForm, Principal principal, Model model) {
		SiteUser user = this.userService.getUser(principal.getName());
		userForm.setEmail(user.getEmail());
		model.addAttribute("userForm", userForm);
		model.addAttribute("username", user.getUsername()); // Add username to model
		return "user_edit";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/edit")
	public String editUser(@Valid UserForm userForm, BindingResult bindingResult, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "user_edit";
		}
		SiteUser user = this.userService.getUser(principal.getName());
		this.userService.updateUser(user, userForm.getEmail(), userForm.getPassword1());
		return "redirect:/user/mypage";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/delete")
	public String deleteUser(Principal principal) {
		SiteUser user = this.userService.getUser(principal.getName());
		this.userService.deleteUser(user);
		return "redirect:/";
	}
}