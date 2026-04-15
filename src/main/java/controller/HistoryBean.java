package controller;

import com.greenmarket.entity.OrderItem;
import com.greenmarket.entity.Transaction;
import com.greenmarket.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named(value = "historyBean")
@ViewScoped
public class HistoryBean implements Serializable {

    @PersistenceContext(unitName = "GreenMarketDB")
    private EntityManager em;

    @Inject
    private AuthBean authBean;

    private List<Transaction> transactions;
    
    private Transaction selectedTransaction;
    private List<OrderItem> selectedOrderItems;

    @PostConstruct
    public void init() {
        loadHistory();
    }

    public void loadHistory() {
        User currentUser = authBean.getCurrentUser();
        
        if (currentUser == null) {
            this.transactions = new ArrayList<>();
            return;
        }

        try {
            String jpql = "SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createAt DESC";
            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("userId", currentUser.getId());
            
            this.transactions = query.getResultList();
            
        } catch (Exception e) {
            System.err.println("Error loading history: " + e.getMessage());
            this.transactions = new ArrayList<>();
        }
    }

    public void prepareViewDetails(Transaction tx) {
        this.selectedTransaction = tx;
        this.selectedOrderItems = fetchOrderItems(tx.getId());
    }

    private List<OrderItem> fetchOrderItems(Integer transactionId) {
        if (transactionId == null) {
            return new ArrayList<>();
        }
        
        try {
            String jpql = "SELECT o FROM OrderItem o JOIN FETCH o.product WHERE o.transaction.id = :txId";
            TypedQuery<OrderItem> query = em.createQuery(jpql, OrderItem.class);
            query.setParameter("txId", transactionId);
            
            return query.getResultList();
            
        } catch (Exception e) {
            System.err.println("Error loading order items for tx " + transactionId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Transaction getSelectedTransaction() {
        return selectedTransaction;
    }

    public void setSelectedTransaction(Transaction selectedTransaction) {
        this.selectedTransaction = selectedTransaction;
    }

    public List<OrderItem> getSelectedOrderItems() {
        return selectedOrderItems;
    }
}