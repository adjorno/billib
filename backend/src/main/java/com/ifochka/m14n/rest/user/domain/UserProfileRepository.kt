package com.ifochka.m14n.rest.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserProfileRepository : JpaRepository<UserProfile, String>
