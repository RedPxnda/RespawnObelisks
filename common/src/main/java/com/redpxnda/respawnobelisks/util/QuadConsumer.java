package com.redpxnda.respawnobelisks.util;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void call(A a, B b, C c, D d);
}
