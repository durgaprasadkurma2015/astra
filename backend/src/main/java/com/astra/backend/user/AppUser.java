package com.astra.backend.user;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    

@Column(name = "date_of_birth")
private LocalDate dateOfBirth;

@Column(length = 30)
private String gender;

@Column(name = "phone_number", length = 20)
private String phoneNumber;

@Column(name = "alternate_phone_number", length = 20)
private String alternatePhoneNumber;

@Column(name = "address_line1")
private String addressLine1;

@Column(name = "address_line2")
private String addressLine2;

@Column(length = 150)
private String landmark;

@Column(length = 100)
private String city;

@Column(length = 100)
private String district;

@Column(length = 100)
private String state;

@Column(length = 100)
private String country;

@Column(name = "postal_code", length = 20)
private String postalCode;

@Column(name = "preferred_language", length = 50)
private String preferredLanguage;

    

@Column(name = "email_verified", nullable = false)
private boolean emailVerified = false;


    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

public boolean isEmailVerified() {
    return emailVerified;
}

public void setEmailVerified(boolean emailVerified) {
    this.emailVerified = emailVerified;
}


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

public LocalDate getDateOfBirth() {
    return dateOfBirth;
}

public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
}

public String getGender() {
    return gender;
}

public void setGender(String gender) {
    this.gender = gender;
}

public String getPhoneNumber() {
    return phoneNumber;
}

public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
}

public String getAlternatePhoneNumber() {
    return alternatePhoneNumber;
}

public void setAlternatePhoneNumber(String alternatePhoneNumber) {
    this.alternatePhoneNumber = alternatePhoneNumber;
}

public String getAddressLine1() {
    return addressLine1;
}

public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
}

public String getAddressLine2() {
    return addressLine2;
}

public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
}

public String getLandmark() {
    return landmark;
}

public void setLandmark(String landmark) {
    this.landmark = landmark;
}

public String getCity() {
    return city;
}

public void setCity(String city) {
    this.city = city;
}

public String getDistrict() {
    return district;
}

public void setDistrict(String district) {
    this.district = district;
}

public String getState() {
    return state;
}

public void setState(String state) {
    this.state = state;
}

public String getCountry() {
    return country;
}

public void setCountry(String country) {
    this.country = country;
}

public String getPostalCode() {
    return postalCode;
}

public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
}

public String getPreferredLanguage() {
    return preferredLanguage;
}

public void setPreferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
}

}
