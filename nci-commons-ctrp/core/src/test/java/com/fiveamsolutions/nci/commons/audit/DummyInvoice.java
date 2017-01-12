package com.fiveamsolutions.nci.commons.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.validator.NotEmpty;


/**
 *
 * @author gax
 */
@Entity
public class DummyInvoice implements Auditable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Date orderDate;
    private List<DummyLineItem> items = new ArrayList<DummyLineItem>();

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name = "invoice_items",
               joinColumns = @JoinColumn(name = "invoice_id"),
               inverseJoinColumns = @JoinColumn(name = "item_id"))
    @NotEmpty
    public List<DummyLineItem> getItems() {
        return items;
    }

    public void setItems(List<DummyLineItem> items) {
        this.items = items;
    }



}
