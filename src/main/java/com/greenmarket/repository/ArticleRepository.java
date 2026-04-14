package com.greenmarket.repository;

import com.greenmarket.entity.Article;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ArticleRepository extends GenericRepository<Article, Integer> {

    public ArticleRepository() {
        super(Article.class);
    }

    public List<Article> findAllOrderedByIdDesc() {
        return em.createQuery("SELECT a FROM Article a ORDER BY a.id DESC", Article.class).getResultList();
    }
}
