package com.greenmarket.dao;

import com.greenmarket.model.Article;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ArticleDAO extends GenericDAO<Article, Integer> {

    public ArticleDAO() {
        super(Article.class);
    }

    public List<Article> findAllOrderedByIdDesc() {
        return em.createQuery("SELECT a FROM Article a ORDER BY a.id DESC", Article.class).getResultList();
    }
}
