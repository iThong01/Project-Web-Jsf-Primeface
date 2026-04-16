package controller;

import com.greenmarket.model.Article;
import com.greenmarket.service.ArticleService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named(value = "articleBean")
@ViewScoped
public class ArticleBean implements Serializable {

    @Inject
    private ArticleService articleService;

    private List<Article> articleList;
    private Article newArticle = new Article();

    @PostConstruct
    public void init() {
        loadArticles();
    }

    public void loadArticles() {
        articleList = articleService.getAllArticles();
    }

    public void saveArticle() {
        try {
            articleService.saveOrUpdateArticle(newArticle);

            String msg = (newArticle.getId() == null) ? "เพิ่มบทความเรียบร้อย" : "แก้ไขบทความเรียบร้อย";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(msg));

            newArticle = new Article();
            loadArticles();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาด", e.getMessage()));
        }
    }

    public void editArticle(Article article) {
        this.newArticle = article;
    }

    public void prepareNewArticle() {
        this.newArticle = new Article();
    }

    public void deleteArticle(Article article) {
        try {
            articleService.deleteArticle(article.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("ลบบทความเรียบร้อย"));
            loadArticles();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "เกิดข้อผิดพลาด", e.getMessage()));
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
