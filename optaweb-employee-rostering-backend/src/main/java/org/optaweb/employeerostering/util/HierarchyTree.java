package org.optaweb.employeerostering.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A data structure representing a hierarchy tree. Each node in the tree represent
 * a subclass of the Hierarchy. Can be used to find the most specific class
 * an object is applicable to (example: in a Hierarchy tree containing Throwable and RuntimeException,
 * IllegalStateException should map to RuntimeException, not Throwable).
 * 
 * @param <K> The type of the key element in the Hierarchy
 * @param <V> The type of the value element in the Hierarchy
 */
public class HierarchyTree<K, V> {

    private Collection<HierarchyNode> hierarchyDisjointClasses;
    private HierarchyRelation<K> hierarchyRelation;

    public HierarchyTree(HierarchyRelation<K> hierarchyRelation) {
        this.hierarchyRelation = hierarchyRelation;
        hierarchyDisjointClasses = new ArrayList<>();
    }

    /**
     * Puts the hierarchy class into the Hierarchy tree.
     * 
     * @param key The key of the hierarchy class, determines where it is put into the tree.
     * @param value The value of the hierarchy class.
     */
    public void putInHierarchy(K key, V value) {
        HierarchyNode newChild = new HierarchyNode(key, value);
        Collection<HierarchyNode> subclassesOfNewChild = hierarchyDisjointClasses.stream()
                .filter(c -> hierarchyRelation.getHierarchyRelationshipBetween(newChild.classKey,
                        c.classKey) == HierarchyRelationship.IS_ABOVE)
                .collect(Collectors.toList());
        if (subclassesOfNewChild.isEmpty()) {
            Optional<HierarchyNode> superclassOfNewChild = hierarchyDisjointClasses.stream()
                    .filter(c -> hierarchyRelation.getHierarchyRelationshipBetween(newChild.classKey,
                            c.classKey) == HierarchyRelationship.IS_BELOW)
                    .findAny();
            if (superclassOfNewChild.isPresent()) {
                superclassOfNewChild.get().addChild(newChild);
            } else {
                hierarchyDisjointClasses.add(newChild);
            }
        } else {
            hierarchyDisjointClasses.removeAll(subclassesOfNewChild);
            subclassesOfNewChild.forEach(newChild::addChild);
            hierarchyDisjointClasses.add(newChild);
        }
    }

    /**
     * Finds the most specific hierarchy class of key, and returns its value.
     * If it belongs to multiple hierarchy branches (example: in the divisibility hierarchy tree
     * with classes "2" and "3", "6" belongs to both "2" and "3"), it returns the value of the most specific
     * hierarchy class of a random branch.
     * 
     * @param key What to find the most specific hierarchy class of.
     * @return The value of the most specific hierarchy class of key.
     */
    public Optional<V> getHierarchyClassValue(K key) {
        List<HierarchyNode> superclassesOfItem = hierarchyDisjointClasses.stream()
                .filter(c -> {
                    HierarchyRelationship relationship = hierarchyRelation.getHierarchyRelationshipBetween(key,
                            c.classKey);
                    return relationship == HierarchyRelationship.IS_BELOW
                            || relationship == HierarchyRelationship.IS_THE_SAME_AS;
                })
                .collect(Collectors.toList());

        if (superclassesOfItem.isEmpty()) {
            return Optional.empty();
        } else {
            HierarchyNode mostSpecificInstance = superclassesOfItem.get(0).findHierarchyClassOf(key);
            for (HierarchyNode superclassOfItem : superclassesOfItem.subList(1, superclassesOfItem.size())) {
                HierarchyNode mostSpecificInstanceInSuperclass = superclassOfItem.findHierarchyClassOf(key);
                if (hierarchyRelation.getHierarchyRelationshipBetween(mostSpecificInstanceInSuperclass.classKey,
                        mostSpecificInstance.classKey) == HierarchyRelationship.IS_BELOW) {
                    mostSpecificInstance = mostSpecificInstanceInSuperclass;
                }
            }
            return Optional.of(mostSpecificInstance.classValue);
        }
    }

    private class HierarchyNode {

        private K classKey;
        private V classValue;
        private Collection<HierarchyNode> hierarchySubclasses;

        public HierarchyNode(K classKey, V classValue) {
            this.classKey = classKey;
            this.classValue = classValue;
            hierarchySubclasses = new ArrayList<>();
        }

        public void addChild(HierarchyNode newChild) {
            if (hierarchyRelation.getHierarchyRelationshipBetween(newChild.classKey,
                    classKey) != HierarchyRelationship.IS_BELOW) {
                throw new IllegalArgumentException("The child you tried to add (" + newChild.classKey +
                        ") is not a descendant of root (" + classKey + ").");
            }
            Collection<HierarchyNode> subclassesOfNewChild = hierarchySubclasses.stream()
                    .filter(c -> hierarchyRelation.getHierarchyRelationshipBetween(newChild.classKey,
                            c.classKey) == HierarchyRelationship.IS_ABOVE)
                    .collect(Collectors.toList());
            if (subclassesOfNewChild.isEmpty()) {
                Optional<HierarchyNode> superclassOfNewChild = hierarchySubclasses.stream()
                        .filter(c -> hierarchyRelation.getHierarchyRelationshipBetween(newChild.classKey,
                                c.classKey) == HierarchyRelationship.IS_BELOW)
                        .findAny();
                if (superclassOfNewChild.isPresent()) {
                    superclassOfNewChild.get().addChild(newChild);
                } else {
                    hierarchySubclasses.add(newChild);
                }
            } else {
                hierarchySubclasses.removeAll(subclassesOfNewChild);
                subclassesOfNewChild.forEach(newChild::addChild);
                hierarchySubclasses.add(newChild);
            }
        }

        public HierarchyNode findHierarchyClassOf(K key) {
            HierarchyRelationship classRelationship = hierarchyRelation.getHierarchyRelationshipBetween(key, classKey);
            if (!(classRelationship == HierarchyRelationship.IS_BELOW
                    || classRelationship == HierarchyRelationship.IS_THE_SAME_AS)) {
                throw new IllegalArgumentException("The item (" + key + ") is not a descendant of root (" +
                        classKey + ").");
            }
            List<HierarchyNode> superclassesOfItem = hierarchySubclasses.stream()
                    .filter(c -> {
                        HierarchyRelationship relationship = hierarchyRelation.getHierarchyRelationshipBetween(
                                key, c.classKey);
                        return relationship == HierarchyRelationship.IS_BELOW
                                || relationship == HierarchyRelationship.IS_THE_SAME_AS;
                    })
                    .collect(Collectors.toList());

            if (superclassesOfItem.isEmpty()) {
                return this;
            } else {
                HierarchyNode mostSpecificInstance = superclassesOfItem.get(0).findHierarchyClassOf(key);
                for (HierarchyNode superclassOfItem : superclassesOfItem.subList(1, superclassesOfItem.size())) {
                    HierarchyNode mostSpecificInstanceInSuperclass = superclassOfItem.findHierarchyClassOf(key);
                    if (hierarchyRelation.getHierarchyRelationshipBetween(mostSpecificInstanceInSuperclass.classKey,
                            mostSpecificInstance.classKey) == HierarchyRelationship.IS_BELOW) {
                        mostSpecificInstance = mostSpecificInstanceInSuperclass;
                    }
                }
                return mostSpecificInstance;
            }
        }

        @Override
        public String toString() {
            return classKey.toString();
        }
    }

    public enum HierarchyRelationship {
        // Returned when the first object is a generalization of the second object
        // (ex: Throwable IS_ABOVE RuntimeException)
        IS_ABOVE,

        // Returned when the first object is a specialization of the second object
        // (ex: RuntimeException IS_BELOW Throwable)
        IS_BELOW,

        //Returned when the first object is identical to the second object
        // (ex: Throwable IS_THE_SAME_AS Throwable)
        IS_THE_SAME_AS,

        // Returned when the first object and the second object have no direct relation
        // (ex: RuntimeException HAS_NO_DIRECT_RELATION Exception)
        // (typically, this means they have a common ancestor, but this is not always the case)
        HAS_NO_DIRECT_RELATION
    }

    public interface HierarchyRelation<K> {

        public HierarchyRelationship getHierarchyRelationshipBetween(K first, K second);
    }
}
