package io.chark.food.app.restaurant;

import io.chark.food.app.restaurant.offer.OfferService;
import io.chark.food.app.restaurant.details.BankService;
import io.chark.food.app.restaurant.location.CityService;
import io.chark.food.domain.audit.RestaurantAuditMessage;
import io.chark.food.domain.offer.Offer;
import io.chark.food.domain.restaurant.Bank;
import io.chark.food.domain.restaurant.Invitation;
import io.chark.food.domain.restaurant.Restaurant;
import io.chark.food.domain.restaurant.location.Location;
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
    private final RestaurantAuditService auditService;
    private final BankService bankService;
    private final OfferService offerService;
    private final CityService cityService;

    @Autowired
    public RestaurantController(RestaurantService restaurantService,
                                RestaurantAuditService auditService,
                                OfferService offerService,
                                BankService bankService,
                                CityService cityService) {

        this.restaurantService = restaurantService;
        this.auditService = auditService;
        this.bankService = bankService;
        this.offerService = offerService;
        this.cityService = cityService;
    }

    /**
     * Get restaurant page that doesn't belong to the current user - view details.
     *
     * @param id restaurant id.
     * @return restaurant page template.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String restaurant(@PathVariable long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurant(id));
        return "restaurant/restaurant";
    }

    /**
     * Get the restaurant template to which the account belongs to.
     *
     * @return restaurant profile template.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String restaurant(Model model) {
        Restaurant rest = restaurantService.getRestaurant();
        List<Bank> banks = bankService.getBanks();
        if (rest.getLocation() == null) {
            rest.setLocation(new Location());
        }
        model.addAttribute("restaurant", rest);
        model.addAttribute("banks", banks);
        model.addAttribute("cities", cityService.getCities());
        model.addAttribute("offer", new Offer());

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


    @RequestMapping(value = "/offer", method = RequestMethod.POST)
    public String addOffer(Offer offer, Model model) {
        offerService.register(offer).isPresent();
//        if (offerService.register(offer).isPresent()) {
        // Success, redirect to profile page.
//            return "redirect:/restaurant";
//        }
        return "redirect:/restaurant";
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

    /**
     * Get audit messages assigned to currently authenticated users restaurant.
     *
     * @return list of audit messages.
     */
    @ResponseBody
    @RequestMapping(value = "/api/audit", method = RequestMethod.GET)
    public List<RestaurantAuditMessage> getAuditMessages() {
        return auditService.getRestaurantAuditMessages();
    }


    @ResponseBody
    @RequestMapping(value = "/api/offers", method = RequestMethod.GET)
    public List<Offer> getOffers() {
        return offerService.getOffers();
    }


    @RequestMapping(value = "/offer/edit/{id}", method = RequestMethod.GET)
    public String getAccount(@PathVariable long id, Model model) {
        Offer offer;

        if (id <= 0) {
            // Id below or equals to zero means this is a new account.
            offer = new Offer();
        } else {
            // Id is above zero, existing account.
            offer = offerService.getOffer(id);
        }
        model.addAttribute("offer", offer);
        return "restaurant/offer";
    }


    @RequestMapping(value = "/offer/edit/{id}", method = RequestMethod.POST)
    public String saveOffer(Offer details, @PathVariable long id, Model model) {


        if (offerService.update(id, details).isPresent()) {
            // Success, redirect to profile page.
            return "redirect:/restaurant";
        }

        // Error, failed to update.
        model.addAttribute("error", "Could not update restaurant details, " +
                "please double check what you've entered");

        model.addAttribute("restaurant", details);
        return "restaurant/profile";
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/api/offers/{id}", method = RequestMethod.DELETE)
    public void deleteOffer(@PathVariable long id) {
        offerService.deleteOffer(id);
    }
}