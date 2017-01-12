package com.fiveamsolutions.nci.commons.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


/**
 * Similar object to DummyInvoice with the difference being the mappedBy list of DummyInvertedLineItems.
 * In addition the list can be null.
 * @author max
 */
@Entity
public class DummyInvertedInvoice implements Auditable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Date orderDate;
    private List<DummyInvertedLineItem> items = new ArrayList<DummyInvertedLineItem>();

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

    @OneToMany(mappedBy="invoice", cascade=CascadeType.ALL)
    public List<DummyInvertedLineItem> getItems() {
        return items;
    }

    public void setItems(List<DummyInvertedLineItem> items) {
        this.items = items;
    }



}
