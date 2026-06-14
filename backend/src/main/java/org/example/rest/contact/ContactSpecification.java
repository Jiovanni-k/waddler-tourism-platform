package org.example.rest.contact;

import org.springframework.data.jpa.domain.Specification;

public class ContactSpecification {

    public static Specification<Contact> hasStatus(ContactStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Contact> hasPriority(ContactPriority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Contact> hasCategory(ContactCategory category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Contact> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("userId"), userId);
    }
}