package io.github.evancolewright.royaleftop.utils;

import java.util.*;

/**
 * A class for sorting Map implementations.
 *
 * @param <K> The key
 * @param <T> The value (Must be comparable)
 */

public class WorthSorter<K, T extends Comparable<T>>
{
    private final Map<K, T> map;
    private final Order order;

    public WorthSorter(Map<K, T> map)
    {
        this.map = map;
        this.order = Order.GREATEST_TO_LEAST;
    }

    public Map<K, T> getSortedMap()
    {
        final List<Map.Entry<K, T>> entries = new LinkedList<>(this.map.entrySet());
        entries.sort(this.comparator());
        final Map<K, T> sortedMap = new LinkedHashMap<>();
        entries.forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }

    private Comparator<Map.Entry<K, T>> comparator()
    {
        return (o1, o2) ->
        {
            if (order == Order.GREATEST_TO_LEAST)
            {
                return o2.getValue().compareTo(o1.getValue());
            }
            return o1.getValue().compareTo(o2.getValue());
        };
    }

    public enum Order
    {
        GREATEST_TO_LEAST,
        LEAST_TO_GREATEST;
    }
}
