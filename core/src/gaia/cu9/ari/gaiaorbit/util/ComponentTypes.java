/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package gaia.cu9.ari.gaiaorbit.util;

import java.util.BitSet;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;

/**
 * BitSet with some added functionality
 *
 * @author Toni Sagrista
 */
public class ComponentTypes extends BitSet {

    public ComponentTypes() {
        super(18);
    }

    public ComponentTypes(int ordinal) {
        super(18);
        set(ordinal);
    }

    public ComponentTypes(ComponentType... cts) {
        super();
        for (ComponentType ct : cts)
            set(ct.ordinal());
    }

    /**
     * Returns the index of the rightmost bit set to 1. If no bits are set to 1, returns -1.
     * @return
     */
    public int getFirstOrdinal() {
        return nextSetBit(0);
    }

    public boolean allSetLike(ComponentTypes other) {
        ComponentTypes clone = ((ComponentTypes) clone());
        clone.and(other);
        return clone.cardinality() == cardinality();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ComponentType[] values = ComponentType.values();
        for (int i = nextSetBit(0); i >= 0; i = nextSetBit(i + 1)) {
            // operate on index i here
            sb.append(values[i]).append(" ");
            if (i == Integer.MAX_VALUE) {
                break; // or (i+1) would overflow
            }
        }
        return sb.toString();
    }

}