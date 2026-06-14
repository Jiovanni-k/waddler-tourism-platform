package org.example.rest.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>,
        JpaSpecificationExecutor<Contact> {

    List<Contact> findByStatus(ContactStatus status);

    List<Contact> findByUserId(Long userId);

    long countByStatus(ContactStatus status);
}