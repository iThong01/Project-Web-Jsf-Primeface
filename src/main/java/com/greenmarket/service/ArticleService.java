package com.greenmarket.service;

import com.greenmarket.model.Article;
import com.greenmarket.dao.ArticleDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

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
