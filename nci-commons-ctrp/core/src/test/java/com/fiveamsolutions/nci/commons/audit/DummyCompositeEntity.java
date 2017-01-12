package com.fiveamsolutions.nci.commons.audit;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;


/**
 *
 * @author moweis
 */
@Entity
public class DummyCompositeEntity implements Auditable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private DummyCompositeField compositeField;
    private Set<DummyCompositeField> compositeFields;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name= name;
    }

    /**
     * @return the compositeField
     */
    @Type(type = "com.fiveamsolutions.nci.commons.audit.DummyCompositeUserType")
    @Columns(columns = {
            @Column(name = "field1_column"),
            @Column(name = "field2_column"),
            @Column(name = "field3_column")
    })
    public DummyCompositeField getCompositeField() {
        return compositeField;
    }

    /**
     * @param compositeField the compositeField to set
     */
    public void setCompositeField(DummyCompositeField compositeField) {
        this.compositeField = compositeField;
    }

    /**
     * @param compositeFields the compositeFields to set
     */
    public void setCompositeFields(Set<DummyCompositeField> compositeFields) {
        this.compositeFields = compositeFields;
    }

    /**
     * @return the compositeFields
     */
    @CollectionOfElements
    @Type(type = "com.fiveamsolutions.nci.commons.audit.DummyCompositeUserType")
    @JoinTable(
            name = "dummy_composite_table",
            joinColumns = @JoinColumn(name = "dummy_join_id")
    )
    @Columns(columns = {
            @Column(name = "set_field1_column"),
            @Column(name = "set_field2_column"),
            @Column(name = "set_field3_column")
    })
    public Set<DummyCompositeField> getCompositeFields() {
        return compositeFields;
    }
}
