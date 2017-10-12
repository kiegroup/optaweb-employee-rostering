package org.optaplanner.openshift.employeerostering.server.shift;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

//CUP maven plugins seems out of date; the format file is simple enough to code by hand
public class ShiftFileParser {
    
    public static List<Shift> parse(Integer tenantId, List<Spot> spots, LocalDateTime start, LocalDateTime end, String input) throws ParserException {
        String[] tokens = input.split("\n");
        DateMode dateMode = DateMode.valueOf(tokens[0]);
        
        int i = 1;
        int repeatDays, repeatWeeks, repeatMonths, repeatYears;
        
        if (DateMode.CUSTOM == dateMode) {
            String[] duration = tokens[i].split(":");
            if (4 != duration.length) {
                throw new ParserException("Badly formatted custom duration");
            }
            repeatDays = Integer.parseInt(duration[0]);
            repeatWeeks = Integer.parseInt(duration[1]);
            repeatMonths = Integer.parseInt(duration[2]);
            repeatYears = Integer.parseInt(duration[3]);
            i++;
        }
        else {
            repeatDays = dateMode.days;
            repeatWeeks = dateMode.weeks;
            repeatMonths = dateMode.months;
            repeatYears = dateMode.years;
        }
        
        List<DateMatcher> exceptions = new ArrayList<DateMatcher>();
        
        try {
            while (!tokens[i].equals(";")) {
                exceptions.add(getDateMatcher(tokens[i]));
                i++;
            }
            i++;
        }
        catch (Exception e) {
            if (e instanceof ParserException) {
                throw e;
            }
            throw new ParserException("Unterminated date exceptions list; expected ';' at end of the list");
        }
        
        List<Shift> out = new ArrayList<>();
        Map<String, Spot> spotMap = spots.stream()
                .collect(Collectors.toMap(Spot::getName, Function.identity()));
        
        for (; i < tokens.length; i++) {
            String[] shiftParts = tokens[i].split(";");
            if (shiftParts.length < 3) {
                throw new ParserException("Each date require at least one spot");
            }
            String[] spotNames = new String[shiftParts.length - 2];
            for (int j = 2; j < shiftParts.length; j++) {
                spotNames[j-2] = shiftParts[j];
            }
            
            for (LocalDateTime startDate = parseDate(start, shiftParts[0]),
                    endDate = parseDate(start, shiftParts[1]);
                    //Cond
                    startDate.isBefore(end);
                    //Post
                    startDate = startDate.plusYears(repeatYears)
                    .plusMonths(repeatMonths)
                    .plusWeeks(repeatWeeks)
                    .plusDays(repeatDays),
                    
                    endDate = endDate.plusYears(repeatYears)
                    .plusMonths(repeatMonths)
                    .plusWeeks(repeatWeeks)
                    .plusDays(repeatDays))
            {
                LocalDateTime clone = startDate;
                if (!exceptions.stream().anyMatch((d) -> d.test(clone))) {
                    for (int j = 0; j < spotNames.length; j++) {
                        TimeSlot timeslot = new TimeSlot(tenantId, startDate, endDate);
                        out.add(new Shift(tenantId, spotMap.get(spotNames[j]), timeslot));
                    }
                }
            }
        }
        return out;
    }
    
    //Date Format:
    //00:00:00
    private static LocalDateTime parseDate(LocalDateTime base, String date) throws ParserException {
        String[] parts = date.split(":");
        if (3 != parts.length) {
            throw new ParserException("Bad date format");
        }
        try {
            int day  = Integer.parseInt(parts[0]);
            int hour = Integer.parseInt(parts[1]);
            int minute = Integer.parseInt(parts[2]);
            
            return base.plusMinutes(minute)
                    .plusHours(hour)
                    .plusDays(day);
        }
        catch (Exception e) {
            throw new ParserException(e.getMessage());
        }
    }
    
    private static DateMatcher getDateMatcher(String expr) throws ParserException {
        String[] subexprs = expr.split("&");
        if (1 < subexprs.length) {
            throw new ParserException("Date Matcher requires at least one predicate");
        }
        DateMatcher out = new DateMatcher();
        for (String subexpr : subexprs) {
            out.add(parseDatePredicate(subexpr));
        }
        return out;
    }
    
    //Format "[type]predicate" where type is a single character
    private static DatePredicate parseDatePredicate(String subexpr) throws ParserException {
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
                    int offset = Math.abs((firstDayOfMonth.getDayOfWeek().getValue() -  base.getValue() - 2) % 7);
                    return ((mdayOfMonth + offset) / 7) == week  - 1;
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
                    DatePredicate disjunctPredicate = parseDatePredicate(disjunct);
                    DatePredicate clone = out;
                    out = (d) -> clone.test(d) || disjunctPredicate.test(d);
                }
                return out;
                
            //Negation
            case '!':
                out = parseDatePredicate(predicate);
                {
                    DatePredicate clone = out;
                    return (d) -> !clone.test(d);
                }//To hide scope, since cases don't, which is VERY ANNOYING
                
            default:
                throw new ParserException("Badly formated predicate");
        }
    }
    
    private static enum DateMode {
        DAY(1,0,0,0),
        WEEK(0,1,0,0),
        MONTH(0,0,1,0),
        YEAR(0,0,0,1),
        CUSTOM(0,0,0,0);
        
        int days,weeks,months,years;
        DateMode(int days, int weeks, int months, int years) {
            this.days = days;
            this.weeks = weeks;
            this.months = months;
            this.years = years;
        }
    }
    
    private static class DateMatcher {
        List<DatePredicate> predicates;
        
        public DateMatcher() {
            predicates = new ArrayList<>();
        }
        
        public void add(DatePredicate p) {
            predicates.add(p);
        }
        
        public boolean test(LocalDateTime date) {
            return predicates.stream().allMatch((p) -> p.test(date));
        }
    }
    
    private interface DatePredicate {
        boolean test(LocalDateTime date);
    }
    
    public static class ParserException extends Exception{
        private static final long serialVersionUID = 1L;

        public ParserException(String string) {
            super(string);
        }
        
    }
}
