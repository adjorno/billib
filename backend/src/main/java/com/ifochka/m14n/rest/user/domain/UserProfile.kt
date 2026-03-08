package com.ifochka.m14n.rest.user.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "profiles", schema = "users")
data class UserProfile(
    @Id
    @Column(name = "firebase_uid")
    val firebaseUid: String,
    @Column(name = "is_anonymous")
    val isAnonymous: Boolean = true,
    @Column(name = "plan_type")
    val planType: String = "free",
    @Column(name = "email")
    val email: String? = null,
    @Column(name = "display_name")
    val displayName: String? = null,
    @Column(name = "last_login_at")
    val lastLoginAt: Instant = Instant.now(),
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant = Instant.now(),
)
