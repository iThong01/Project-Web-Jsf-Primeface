package controller;

import com.greenmarket.entity.Article;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

@Named(value = "articleBean")
@ViewScoped
public class ArticleBean implements Serializable {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;

    private List<Article> articleList;
    private Article newArticle = new Article();

    @PostConstruct
    public void init() {
        loadArticles();
    }

    public void loadArticles() {
        articleList = em.createQuery("SELECT a FROM Article a ORDER BY a.id DESC", Article.class).getResultList();
    }

    @Transactional
    public void saveArticle() {
        try {
            if (newArticle.getId() == null) {
                em.persist(newArticle);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("เพิ่มบทความเรียบร้อย"));
            } else {
                em.merge(newArticle);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("แก้ไขบทความเรียบร้อย"));
            }
            newArticle = new Article();
            loadArticles();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาด", e.getMessage()));
        }
    }

    public void editArticle(Article article) {
        this.newArticle = article;
    }

    @Transactional
    public void deleteArticle(Article article) {
        try {
            Article a = em.find(Article.class, article.getId());
            if (a != null) {
                em.remove(a);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("ลบบทความเรียบร้อย"));
                loadArticles();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาด", e.getMessage()));
        }
    }

    public List<Article> getArticleList() {
        return articleList;
    }

    public void setArticleList(List<Article> articleList) {
        this.articleList = articleList;
    }

    public Article getNewArticle() {
        return newArticle;
    }

    public void setNewArticle(Article newArticle) {
        this.newArticle = newArticle;
    }
}
