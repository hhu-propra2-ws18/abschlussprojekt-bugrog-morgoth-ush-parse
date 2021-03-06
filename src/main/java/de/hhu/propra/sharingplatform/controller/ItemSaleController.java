package de.hhu.propra.sharingplatform.controller;

import de.hhu.propra.sharingplatform.model.User;
import de.hhu.propra.sharingplatform.model.items.ItemSale;
import de.hhu.propra.sharingplatform.service.ItemService;
import de.hhu.propra.sharingplatform.service.RecommendationService;
import de.hhu.propra.sharingplatform.service.UserService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ItemSaleController extends BaseController {

    private final ItemService itemService;
    private final RecommendationService recommendationService;


    @Autowired
    public ItemSaleController(ItemService itemService,
                              UserService userService,
                              RecommendationService recommendationService) {
        super(userService);
        this.itemService = itemService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/item/sale/details/{itemId}")
    public String detailPage(Model model, @PathVariable long itemId, Principal principal) {
        ItemSale itemSale = (ItemSale) itemService.findItem(itemId);
        model.addAttribute("itemSale", itemSale);
        model.addAttribute("user", userService.fetchUserByAccountName(principal.getName()));
        boolean ownItem = itemService.userIsOwner(itemSale.getId(),
            userService.fetchUserIdByAccountName(principal.getName()));
        model.addAttribute("ownItem", ownItem);
        model.addAttribute("recItems", recommendationService.findRecommendations(itemId));
        return "itemSaleDetails";
    }

    @GetMapping("/item/sale/new")
    public String newItem(Model model, Principal principal) {
        User user = userService.fetchUserByAccountName(principal.getName());
        model.addAttribute("itemSale", new ItemSale(user));
        model.addAttribute("user", user);
        return "itemSaleForm";
    }

    @PostMapping("/item/sale/new")
    public String inputItemData(Model model, ItemSale itemSale, Principal principal) {
        itemService
            .persistItem(itemSale, userService.fetchUserIdByAccountName(principal.getName()));
        return "redirect:/user/account/";
    }

    @PostMapping("/item/sale/remove/{itemId}")
    public String markItemAsRemoved(Model model, @PathVariable long itemId,
                                    Principal principal) {
        itemService.removeItem(itemId, userService.fetchUserIdByAccountName(principal.getName()));
        return "redirect:/user/account/";
    }

    @GetMapping("/item/sale/edit/{itemId}")
    public String editItem(Model model, @PathVariable long itemId, Principal principal) {
        ItemSale itemSale = (ItemSale) itemService.findItem(itemId);
        model.addAttribute("itemSale", itemSale);
        model.addAttribute("itemId", itemId);
        long userId = userService.fetchUserIdByAccountName(principal.getName());
        model.addAttribute("userId", userId);
        itemService.allowOnlyOwner(itemSale, userId);
        return "itemSaleForm";
    }

    @PostMapping("/item/sale/edit/{itemId}")
    public String editItemData(Model model, ItemSale itemSale,
                               @PathVariable long itemId,
                               Principal principal) {
        long userId = userService.fetchUserIdByAccountName(principal.getName());
        itemService.editItem(itemSale, itemId, userId);
        return "redirect:/user/account";
    }
}
