package de.hhu.propra.sharingplatform.model;


import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String address;
    private String email;
    private String propayId;
    private int rating;
    private boolean ban;
    private boolean deleted;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH}, mappedBy = "borrower")
    private List<Contract> contracts;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH}, mappedBy = "owner")
    private List<Item> items;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH}, mappedBy = "borrower")
    private List<Offer> offers;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH}, mappedBy = "sender")
    private List<Payment> paymentsSend;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST,
        CascadeType.REFRESH}, mappedBy = "recipient")
    private List<Payment> paymentsReceive;

}
