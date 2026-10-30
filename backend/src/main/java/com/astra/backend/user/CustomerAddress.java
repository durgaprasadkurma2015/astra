package com.astra.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_addresses", indexes = {
        @Index(name = "idx_customer_addresses_owner", columnList = "user_email, id"),
        @Index(name = "idx_customer_addresses_default", columnList = "user_email, is_default")
})
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(nullable = false, length = 40)
    private String label;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 150)
    private String landmark;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected CustomerAddress() { }

    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String value) { userEmail = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getFullName() { return fullName; }
    public void setFullName(String value) { fullName = value; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String value) { phoneNumber = value; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String value) { addressLine1 = value; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String value) { addressLine2 = value; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String value) { landmark = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getDistrict() { return district; }
    public void setDistrict(String value) { district = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String value) { postalCode = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean value) { defaultAddress = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
