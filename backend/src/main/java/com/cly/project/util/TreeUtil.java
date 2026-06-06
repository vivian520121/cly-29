package com.cly.project.util;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TreeUtil {

    public static <T> List<T> buildTree(List<T> list, Long rootId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> roots = new ArrayList<>();
        for (T item : list) {
            Long parentId = getParentId(item);
            if (Objects.equals(parentId, rootId)) {
                roots.add(item);
            }
        }

        for (T root : roots) {
            buildChildren(root, list);
        }

        return roots;
    }

    private static <T> void buildChildren(T parent, List<T> list) {
        Long parentId = getId(parent);
        List<T> children = new ArrayList<>();
        for (T item : list) {
            Long itemParentId = getParentId(item);
            if (Objects.equals(itemParentId, parentId)) {
                children.add(item);
            }
        }

        if (!children.isEmpty()) {
            setChildren(parent, children);
            for (T child : children) {
                buildChildren(child, list);
            }
        }
    }

    private static <T> Long getId(T item) {
        try {
            Method method = item.getClass().getMethod("getId");
            return (Long) method.invoke(item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getId() method on " + item.getClass().getName(), e);
        }
    }

    private static <T> Long getParentId(T item) {
        try {
            Method method = item.getClass().getMethod("getParentId");
            return (Long) method.invoke(item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getParentId() method on " + item.getClass().getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void setChildren(T item, List<T> children) {
        try {
            Method method = item.getClass().getMethod("setChildren", List.class);
            method.invoke(item, children);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke setChildren() method on " + item.getClass().getName(), e);
        }
    }
}
