package com.fiveamsolutions.nci.commons.audit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

/**
 * Similar object to DummLineItems with addition of the updatable = false on the invoice getter.
 * @author max
 */
@Entity
public class DummyInvertedLineItem implements Auditable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private DummyInvertedInvoice invoice;
    private String item;
    private Double quantity;
    private Double unitPrice;

    public DummyInvertedLineItem() {

    }

    public DummyInvertedLineItem(String item, Double quantity, Double unitPrice) {
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotEmpty
    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    @ManyToOne
    @JoinColumn(name = "join_column", updatable = false)
    @NotNull
    public DummyInvertedInvoice getInvoice() {
        return invoice;
    }

    public void setInvoice(DummyInvertedInvoice invoice) {
        this.invoice = invoice;
    }

    @NotNull
    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    @NotNull
    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }


}
