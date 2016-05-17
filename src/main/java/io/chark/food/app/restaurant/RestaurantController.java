package io.chark.food.app.restaurant;

import com.fasterxml.jackson.annotation.JsonView;
import io.chark.food.domain.BaseEntity;
import io.chark.food.domain.authentication.account.Account;
import io.chark.food.domain.restaurant.Invitation;
import io.chark.food.domain.restaurant.Restaurant;
import io.chark.food.util.exception.BadInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Get the restaurant template to which the account belongs to.
     *
     * @return restaurant profile template.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String restaurant(Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurant());
        return "restaurant/profile";
    }

    /**
     * Edit restaurant profile.
     *
     * @return restaurant profile template.
     */
    @RequestMapping(method = RequestMethod.POST)
    public String restaurant(Restaurant details, Model model) {

        if (restaurantService.update(details).isPresent()) {
            // Success, redirect to profile page.
            return "redirect:/restaurant";
        }

        // Error, failed to update.
        model.addAttribute("error", "Could not update restaurant details, " +
                "please double check what you've entered");

        model.addAttribute("restaurant", details);
        return "restaurant/profile";
    }

    /**
     * Get the restaurant register template.
     *
     * @return restaurant register template.
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "restaurant/register";
    }

    /**
     * Register a new restaurant.
     *
     * @return same restaurant register template on error or newly created restaurant.
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@Valid Restaurant restaurant,
                           RedirectAttributes attributes,
                           Model model) {

        Optional<Restaurant> optional = restaurantService.register(restaurant);

        // Register was a success.
        if (optional.isPresent()) {
            attributes.addFlashAttribute("success", "Your new restaurant profile is created");
            return "redirect:/restaurant";
        }

        // Failed, try again.
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("error", "Failed to register restaurant, please check if the name" +
                " and email is not taken");

        return "restaurant/register";
    }

    /**
     * Get invitations for the current restaurant.
     *
     * @return list of invitations.
     */
    @ResponseBody
    @RequestMapping(value = "/api/invitations", method = RequestMethod.GET)
    public List<Invitation> getInvitations() {
        return restaurantService.getRestaurant().getInvitations();
    }

    /**
     * Invite a user to restaurant.
     *
     * @param username user to invite.
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/api/invitations/invite", method = RequestMethod.POST)
    public void invite(@RequestParam String username) {
        restaurantService.invite(username)
                .orElseThrow(() -> new BadInputException("Failed to invite this user"));
    }

    /**
     * Delete sent invitation.
     *
     * @param id invitation id.
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/api/invitations/{id}", method = RequestMethod.DELETE)
    public void deleteInvitation(@PathVariable long id) {
        restaurantService.deleteInvitation(id);
    }
}