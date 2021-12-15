package com.AK.unilog.controller;

import com.AK.unilog.entity.*;
import com.AK.unilog.repository.CartItemRepository;
import com.AK.unilog.repository.SectionsRepository;
import com.AK.unilog.service.CartItemService;
import com.AK.unilog.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import com.AK.unilog.service.RegistrationService;
import com.AK.unilog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

import java.util.Optional;


/*
Note* Must determine a way to specify that all of these urls as /student/{url}
and specify the files as /student/{htmlFileName}
 */

@Controller
@RequestMapping("/student/*")
public class StudentController {

    private final UserService userService;
    private final RegistrationService registrationService;
    private final CourseService courseService;
    private final CartItemService cartItemService;
    private SectionsRepository sectionsRepository;


    @Autowired
    public StudentController(UserService userService, RegistrationService registrationService,
                             CourseService courseService, CartItemService cartItemService, SectionsRepository sectionsRepository) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.courseService = courseService;
        this.cartItemService = cartItemService;
        this.sectionsRepository = sectionsRepository;
    }



    @GetMapping("")
    public String home(Model model, Principal principal){
        User student = userService.findByEmail(principal.getName());
        model.addAttribute("user", student);
        model.addAttribute("unpaidSum", student.getUnpaidSum());
        return "student/home";
    }

    @GetMapping("/home")
    public String getHome(){
        return "redirect:/";
    }

    @GetMapping("availableCourses")
    public String availableCourses(){
        return "student/availableCourses";
    }

    @GetMapping("availableSections")
    public String availableSections(){
        return "student/availableSections";
    }

    @GetMapping("cart")
    public String showCart(Model model, Principal principal){
        User student = userService.findByEmail(principal.getName());
        Set<CartItem>cart = new HashSet<>(student.getCart());
        model.addAttribute("cart", cart);
        //get the subtotal of all the cart items
        double total = 0;
        for(CartItem item : cart){
            total += item.getSection().getCourse().getPrice();
        }
        model.addAttribute("total", total);
        //figure out what the due date is
        //student and section can be passed from cart item.
        //these cart items have to disappear when the post is made
        return "student/cart";
    }

    @PostMapping("checkout")
    public String checkout(Model model, Principal principal){
        User student = userService.findByEmail(principal.getName());

        HashMap<String, ArrayList<String>> messages = registrationService.registerCart(student.getCart());
        model.addAttribute("errorMsgs", messages.get("errors"));
        model.addAttribute("confMsgs", messages.get("confirmations"));
        return "student/checkout";
    }

    @PostMapping("paymentConfirmation")
    public String confirmPayment() {
        return "student/paymentConfirmation";
    }

    @PostMapping(value = "editRegistration", params = "action=save")
    public String proceedToPayment(@RequestParam(name = "sectionId", required = false) List<String> sectionIdList,
                                   @RequestParam(name = "total")String total){
        if(sectionIdList == null){
            return "redirect:/student/registeredCourses";
        }
        List<Section>sectionList = new ArrayList<>();
        for(String id: sectionIdList){
            sectionList.add(sectionsRepository.getById(Long.parseLong(id)));
        }
        return "makePayment";
    }

    @PostMapping(value = "editRegistration", params = "action=delete")
    public String deleteRegistrations(@RequestParam(name = "sectionId", required = false) List<Long> sectionIdList, RedirectAttributes redirect){
        if(sectionIdList == null){
            return "redirect:/student/registeredCourses";
        }
        StringBuilder message = new StringBuilder("Course registrations deleted: ");
        for(Long id: sectionIdList){
            RegisteredCourse deleted = registrationService.getRegistrationRepo().getById(id);
            message.append(String.format("%s %s %s; ", deleted.getSection().getCourse().getCourseNumber(),
                    deleted.getSection().getSemester().name(), deleted.getSection().getYear()));
            registrationService.getRegistrationRepo().deleteById(id);
        }
        redirect.addFlashAttribute("deleteMsg", message.toString());
        return "redirect:/student/registeredCourses";
    }

    // TODO: check to make sure paid courses show 0 dollars, and that their totals are not added if they're selected
    @GetMapping("registeredCourses")
    public String registeredCourses(Model model, Principal principal){
        HashSet<RegisteredCourse> registeredCourses = new HashSet<>(userService.findByEmail(principal.getName()).getRegisteredCourses());
        model.addAttribute("registeredCourses", registeredCourses);
        return "student/registeredCourses";
    }


    @GetMapping("paymentHistory")
    public String paymentHistory(Model model, Principal principal){
        User student = userService.findByEmail(principal.getName());
        model.addAttribute("student", student);
        return "student/paymentHistory";
    }

    @GetMapping("sections/{courseNumber}")
    public String showSpecificSections(@PathVariable String courseNumber, Model model){
        System.out.println("inside student/sections/courseNumber");
        courseService.getCourseByNumber(courseNumber);
        model.addAttribute("listSections", courseService.getAvailableSectionsByCourse(courseNumber));
        System.out.println(courseService.getAvailableSectionsByCourse(courseNumber));
        return "student/singleCourseSections";
    }

    @GetMapping("addToCart/{id}")
    public String addToCart(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes){
        Section section = courseService.getSectionById(id);
        User user = userService.findByEmail(principal.getName());
        if(section != null && user != null){
            CartItem cartItem = cartItemService.verifyCartItem(section, user);
            if(cartItem != null){
                redirectAttributes.addFlashAttribute("message", "Section added to course cart successfully.");
                return "redirect:/home";
            }
            redirectAttributes.addFlashAttribute("message", "Section cannot be added to cart.");
            return "redirect:/student/sections/" + section.getCourse().getCourseNumber();
        }
        redirectAttributes.addFlashAttribute("message", "Section cannot be added to cart.");
        return "redirect:/student/availableCourses";
    }

    @GetMapping("studentDetails")
    public String viewDetails(Model model, Principal principal){
        User student = userService.findByEmail(principal.getName());
        model.addAttribute("user", student);
        return "student/studentDetails";
    }
}
