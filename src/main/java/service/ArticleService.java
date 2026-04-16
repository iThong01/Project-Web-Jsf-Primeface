package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.Article;

import java.util.List;

import dao.ArticleDAO;

@ApplicationScoped
public class ArticleService {

    @Inject
    private ArticleDAO articleRepo;

    public List<Article> getAllArticles() {
        return articleRepo.findAllOrderedByIdDesc();
    }

    @Transactional
    public void saveOrUpdateArticle(Article article) {
        if (article.getId() == null) {
            articleRepo.save(article);
        } else {
            articleRepo.update(article);
        }
    }

    @Transactional
    public void deleteArticle(Integer id) {
        articleRepo.delete(id);
    }
}
