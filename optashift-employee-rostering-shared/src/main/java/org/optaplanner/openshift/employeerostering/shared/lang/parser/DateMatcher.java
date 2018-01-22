package org.optaplanner.openshift.employeerostering.shared.lang.parser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.openshift.employeerostering.shared.lang.parser.ParserException;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.EmployeeConditional;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftConditional;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;

/**
 * A {@link DateMatcher} is a conjunction of {@link DatePredicate}s, matching only if all
 * the {@link DatePredicate}s match. A DateMatcher expression is defined by the following structure:
 * 
 * if p is a {@link DatePredicate} expression and m is a {@link DateMatcher} expression, then:
 * <br>
 * 1. p is a {@link DateMatcher} expression<br>
 * 2. p&m is a {@link DateMatcher} expression<br>
 * 3. Nothing else is a {@link DateMatcher} expression<br>
 * 
 * @param <T> Type of replacement if matched
 */
public class DateMatcher<T> {

    List<DatePredicate> predicateList;
    T replacement;

    private DateMatcher(T replacement) {
        predicateList = new ArrayList<>();
        this.replacement = replacement;
    }

    private void add(DatePredicate p) {
        predicateList.add(p);
    }

    public T getReplacement() {
        return replacement;
    }

    public boolean test(LocalDateTime date) {
        return predicateList.stream().allMatch((p) -> p.test(date));
    }

    /**
     * Constructs a DateMatcher from the expression
     * 
     * @param expr A DateMatcher expression as described in {@link DateMatcher}
     * @return A new DateMatcher generated from {@code expr} with a null replacement.
     * @throws ParserException If {@code expr} is not a valid DateMatcher expression
     */
    public static DateMatcher<Object> getDateMatcher(String expr) throws ParserException {
        String[] subexprs = expr.split("&");
        if (1 < subexprs.length) {
            throw new ParserException("Date Matcher requires at least one predicate");
        }
        DateMatcher<Object> out = new DateMatcher<>(null);
        for (String subexpr : subexprs) {
            out.add(DatePredicate.parse(subexpr));
        }
        return out;
    }

    /**
     * Constructs a DateMatcher from the ShiftConditional
     * 
     * @param expr A ShiftConditional to generate the DateMatcher from
     * @return A new DateMatcher generated from {@code ShiftConditional#condition} using {@code ShiftConditional#shift}
     * as the replacement
     * @throws ParserException If {@link ShiftConditional#condition} is not a valid DateMatcher expression
     */
    public static DateMatcher<ShiftInfo> getDateMatcher(ShiftConditional expr) throws ParserException {
        String[] subexprs = expr.getCondition().split("&");
        if (1 < subexprs.length) {
            throw new ParserException("Date Matcher requires at least one predicate");
        }
        DateMatcher<ShiftInfo> out = new DateMatcher<>(expr.getShift());
        for (String subexpr : subexprs) {
            out.add(DatePredicate.parse(subexpr));
        }
        return out;
    }

    /**
     * Constructs a DateMatcher from the EmployeeConditional
     * 
     * @param expr A EmployeeConditional to generate the DateMatcher from
     * @return A new DateMatcher generated from {@code EmployeeConditional#condition} using {@code EmployeeConditional#avaliability}
     * as the replacement
     * @throws ParserException If {@link EmployeeConditional#condition} is not a valid DateMatcher expression
     */
    public static DateMatcher<EmployeeAvailabilityState> getDateMatcher(EmployeeConditional expr)
            throws ParserException {
        String[] subexprs = expr.getCondition().split("&");
        if (1 < subexprs.length) {
            throw new ParserException("Date Matcher requires at least one predicate");
        }
        DateMatcher<EmployeeAvailabilityState> out = new DateMatcher<>(expr.getAvaliability());
        for (String subexpr : subexprs) {
            out.add(DatePredicate.parse(subexpr));
        }
        return out;
    }

}