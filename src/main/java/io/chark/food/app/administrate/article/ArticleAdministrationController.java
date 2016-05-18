package io.chark.food.app.administrate.article;

import io.chark.food.domain.article.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/administrate")
public class ArticleAdministrationController {

    private final ArticleAdministrationService administrationService;

    @Autowired
    public ArticleAdministrationController(ArticleAdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    /**
     * Article administration page.
     *
     * @return template for administrating articles.
     */
    @RequestMapping(value = "/articles", method = RequestMethod.GET)
    public String articleCategoryAdministration() {
        return "administrate/articles";
    }

    /**
     * Get a single article administration page, if negative id is provided, a empty article template is returned.
     *
     * @return single article administration template.
     */
    @RequestMapping(value = "/articles/{id}", method = RequestMethod.GET)
    public String getArticle(@PathVariable long id, Model model) {
        Article article;

        if (id <= 0) {
            // Id below or equals to zero means this is a new article cagegory.
            article = new Article();
        } else {
            // Id is above zero, existing account.
            article = administrationService.getArticle(id);
        }
        model.addAttribute("article", article);
        return "administrate/article";
    }

    /**
     * Create a new user article or update an existing one based on provided id.
     *
     * @return article administration page or the same page if an error occurred.
     */
    @RequestMapping(value = "/articles/{id}", method = RequestMethod.POST)
    public String saveCategory(@PathVariable long id,
                               Article article,
                               Model model) {

        if (!administrationService.saveArticle(id, article).isPresent()) {
            model.addAttribute("error", "Failed to create article," +
                    " please double check the details you've entered.");

            model.addAttribute("article", article);
            return "administrate/article";
        }
        return "redirect:/administrate/articles";
    }

    /**
     * Get list of articles.
     *
     * @return list of articles.
     */
    @ResponseBody
    @RequestMapping(value = "/api/articles", method = RequestMethod.GET)
    public List<Article> getArticles() {
        return administrationService.getArticles();
    }

    /**
     * Delete specified article category by id.
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/api/articles/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable long id) {
        administrationService.delete(id);
    }
}