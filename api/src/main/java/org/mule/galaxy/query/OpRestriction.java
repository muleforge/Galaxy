package org.mule.galaxy.query;

import java.util.Collection;

public class OpRestriction extends Restriction {
    public enum Operator {
        EQUALS,
        NOT,
        IN,
        LIKE
    }

    private Object value;
    private Object left;
    private Operator operator;
    
    protected OpRestriction(Operator o, Object left, Object right) {
        this.operator = o;
        this.left = left;
        this.value = right;
    }
    
    protected OpRestriction(Operator not, OpRestriction restriction) {
        this.operator = not;
        this.value = restriction;
    }

    public Object getRight() {
        return value;
    }

    public Object getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public static OpRestriction eq(String property, Object value) {
        return new OpRestriction(Operator.EQUALS, property, value);
    }

    public static OpRestriction not(OpRestriction restriction) {
        return new OpRestriction(Operator.NOT, restriction);
    }

    public static OpRestriction like(String property, Object value)
    {
        return new OpRestriction(Operator.LIKE, property, value);
    }

    public static OpRestriction in(String property, Collection<?> values) {
        return new OpRestriction(Operator.IN, property, values);
    }
}

