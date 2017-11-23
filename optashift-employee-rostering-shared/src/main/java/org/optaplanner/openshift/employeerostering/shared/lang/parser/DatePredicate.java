package org.optaplanner.openshift.employeerostering.shared.lang.parser;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * A {@link DatePredicate} tests if a date meets its conditions. A DatePredicate Expression
 * is defined by the following structure:
 * <br>
 * If (weekday) is a weekday number, (day) is a day number, (month) is a month number, and (week) is a week number and
 * (p) and (q0)...(qn) are {@link DatePredicate}s, then:
 * <br>
 * 1. E(day)/(month) is a DatePredicate Expression (tests if the date is on the (day)th day of the (month)th month)
 * <br>
 * 2. W(weekday):(week) is a DatePredicate Expression (tests if the date is on the (week)th (weekday) of the month)
 * <br>
 * 3. D(weekday) is a DatePredicate Expression (tests if the date is on a (weekday))
 * <br>
 * 4. d(day) is a DatePredicate Expression (tests if the date is on a (day) day of a month)
 * <br>
 * 5. m(month) is a DatePredicate Expression (tests if the date is on (month))
 * <br>
 * 6. M(p)|(q0)|...|(qn) is a DatePredicate Expression (tests if any of the {@link DatePredicate}s match)
 * <br>
 * 7. !(p) is a DatePredicate Expression (tests if (p) does not match)
 * <br>
 * 8. Nothing else is a DatePredicate Expression
 */
public interface DatePredicate {

    boolean test(LocalDateTime date);

    /**
     * Creates a {@link DatePredicate} from a DatePredicate Expression as described in  {@link DatePredicate}
     * 
     * @param subexpr The DatePredicate expression to create the {@link DatePredicate} from
     * @return The {@link DatePredicate} which tests the DatePredicate Expression
     * @throws ParserException If subexpr is not a valid DatePredicate Expression
     */
    public static DatePredicate parse(String subexpr) throws ParserException {
        String predicate = subexpr.substring(1);
        String[] dateParts;
        int dayOfMonth;
        int month;
        int week;
        DayOfWeek dayOfWeek;
        DatePredicate out;

        switch (subexpr.charAt(0)) {
            //Match exact date (day/month)
            case 'E':
                dateParts = predicate.split("/");
                if (2 != dateParts.length) {
                    throw new ParserException("Badly formatted date");
                }
                dayOfMonth = Integer.parseInt(dateParts[0]);
                month = Integer.parseInt(dateParts[1]);
                return (d) -> d.getDayOfMonth() == dayOfMonth && d.getMonthValue() == month;

            //Match before (n+1)th dayOfWeek of month (dayOfWeek:n) 
            case 'W':
                dateParts = predicate.split(":");
                if (2 != dateParts.length) {
                    throw new ParserException("Badly formatted date");
                }
                DayOfWeek base = DayOfWeek.valueOf(dateParts[0]);
                week = Integer.parseInt(dateParts[1]);

                return (d) -> {
                    int mdayOfMonth = d.getDayOfMonth();
                    LocalDateTime firstDayOfMonth = d.minusDays(mdayOfMonth - 1);
                    int offset = Math.abs((firstDayOfMonth.getDayOfWeek().getValue() - base.getValue() - 2) % 7);
                    return ((mdayOfMonth + offset) / 7) == week - 1;
                };

            //Match day = dayOfWeek
            case 'D':
                dayOfWeek = DayOfWeek.valueOf(predicate);
                return (d) -> dayOfWeek.equals(d.getDayOfWeek());

            //Match day = dayOfMonth
            case 'd':
                dayOfMonth = Integer.parseInt(predicate);
                return (d) -> dayOfMonth == d.getDayOfMonth();

            //Match date.month = month
            case 'm':
                month = Integer.parseInt(predicate);
                return (d) -> month == d.getMonthValue();

            //Logical disjunct of several DatePredicate
            case 'M':
                dateParts = predicate.split("\\|");
                out = (d) -> false;//Identity for OR is false
                for (String disjunct : dateParts) {
                    DatePredicate disjunctPredicate = parse(disjunct);
                    DatePredicate clone = out;
                    out = (d) -> clone.test(d) || disjunctPredicate.test(d);
                }
                return out;

            //Negation
            case '!':
                out = parse(predicate); {
                DatePredicate clone = out;
                return (d) -> !clone.test(d);
            } //To hide scope, since cases don't, which is VERY ANNOYING

            default:
                throw new ParserException("Badly formated predicate");
        }
    }
}