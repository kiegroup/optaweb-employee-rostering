package org.optaplanner.openshift.employeerostering.server.lang.parser;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.lang.parser.DateMatcher;
import org.optaplanner.openshift.employeerostering.shared.lang.parser.ParserException;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.BaseDateDefinitions;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.EmployeeTimeSlotInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.IdOrGroup;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.RepeatMode;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

//CUP maven plugins seems out of date; the format file is simple enough to code by hand
public class ShiftFileParser {
    
    public static ParserOut parse(Integer tenantId, List<Spot> spots,
            List<Employee> employees, Map<Long, List<Spot>> spotGroupMap,
            Map<Long, List<Employee>> employeeGroupMap,
            LocalDateTime start, LocalDateTime end,
            String input) throws ParserException {
        ObjectMapper jsonParser = new ObjectMapper();
        ShiftTemplate template;
        try {
            template = jsonParser.readValue(input, ShiftTemplate.class);
        } catch (JsonParseException e1) {
            throw new ParserException("Input is not a JSON Object");
        } catch (JsonMappingException e1) {
            throw new ParserException("Input is not a ShiftTemplate");
        } catch (IOException e1) {
            throw new ParserException("An IO problem occured");
        }
        
        ParserState state = new ParserState();
        state.tenantId = tenantId;
        state.startDate = start;
        state.endDate = end;

        BaseDateDefinitions baseDateType;
        if (!template.getBaseDateType().getIsCustom()) {
            baseDateType = BaseDateDefinitions.valueOf(template.getBaseDateType().getValue());
        } else {
            baseDateType = null;
        }

        if (null == baseDateType) {

        } else {
            switch (baseDateType) {
                case SAME_AS_START_DATE:
                    state.baseDate = start;
                    break;
                case WEEK_OF_START_DATE:
                    state.baseDate = start
                            .minusDays(start.getDayOfWeek().getValue() - 1)
                            .toLocalDate().atStartOfDay();
                    break;
                default:
                    break;

            }
        }

        RepeatMode dateMode;
        if (!template.getRepeatType().getIsCustom()) {
            dateMode = RepeatMode.valueOf(template.getRepeatType().getValue());
        } else {
            dateMode = null;
        }
        
        if (null == dateMode) {
            String[] duration = template.getRepeatType().getValue().split(":");
            if (4 != duration.length) {
                throw new ParserException("Badly formatted custom duration");
            }
            state.repeatDays = Long.parseLong(duration[0]);
            state.repeatWeeks = Long.parseLong(duration[1]);
            state.repeatMonths = Long.parseLong(duration[2]);
            state.repeatYears = Long.parseLong(duration[3]);
        } else if (RepeatMode.NONE == dateMode) {
            state.repeatDays = Math.abs(Duration.between(state.baseDate, end).toDays()) + 1;
            state.repeatWeeks = 0;
            state.repeatMonths = 0;
            state.repeatYears = 0;
        }
        else {
            state.repeatDays = dateMode.days;
            state.repeatWeeks = dateMode.weeks;
            state.repeatMonths = dateMode.months;
            state.repeatYears = dateMode.years;
        }

        state.universalExceptions = template.getUniversalExceptions().stream()
                .map((e) -> {
                    try {
                        return DateMatcher.getDateMatcher(e);
                    } catch (Exception bad) {
                        return null;
                    }
                }).collect(Collectors.toList());
        
        if (state.universalExceptions.contains(null)) {
            throw new ParserException("Badly formatted date exception string");
        }

        state.shiftsOut = new ArrayList<>();
        state.employeeAvailabilityOut = new ArrayList<>();
        state.spotMap = spots.stream()
                .collect(Collectors.toMap(Spot::getId, Function.identity()));
        state.employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, Function.identity()));
        state.spotGroupMap = spotGroupMap;
        state.employeeGroupMap = employeeGroupMap;
        
        addShiftsFrom(state, template.getShifts());
        ParserOut out = new ParserOut();
        out.shiftsOut = state.shiftsOut;
        out.employeeAvailabilityOut = state.employeeAvailabilityOut;

        return out;
    }
    
    private static void addShiftsFrom(ParserState state, List<ShiftInfo> shifts)
            throws ParserException {
        for (ShiftInfo shiftInfo : shifts) {
            List<DateMatcher<ShiftInfo>> exceptions = (null != shiftInfo.getExceptions()) ? shiftInfo.getExceptions()
                    .stream()
                    .map((e) -> {
                        try {
                            return DateMatcher.getDateMatcher(e);
                        } catch (Exception bad) {
                            return null;
                        }
                    }).collect(Collectors.toList()) : Collections.emptyList();
            if (exceptions.contains(null)) {
                throw new ParserException("Badly formatted date exception string");
            }
            for (LocalDateTime startDate = state.baseDate.plus(Duration.between(LocalDateTime.ofEpochSecond(0, 0,
                    ZoneOffset.UTC),
                    shiftInfo.getStartTime())), endDate = state.baseDate.plus(Duration.between(LocalDateTime
                            .ofEpochSecond(0, 0, ZoneOffset.UTC),
                            shiftInfo.getEndTime()));
                    //Cond
                    startDate.isBefore(state.endDate);
                    //Post
                    startDate = startDate.plusYears(state.repeatYears)
                            .plusMonths(state.repeatMonths)
                            .plusWeeks(state.repeatWeeks)
                            .plusDays(state.repeatDays),

                    endDate = endDate.plusYears(state.repeatYears)
                            .plusMonths(state.repeatMonths)
                            .plusWeeks(state.repeatWeeks)
                            .plusDays(state.repeatDays)) {
                LocalDateTime clone = startDate;
                Optional<DateMatcher<ShiftInfo>> shiftException = exceptions.stream().filter((dm) -> dm.test(clone))
                        .findFirst();
                if (shiftException.isPresent()) {
                    DateMatcher<ShiftInfo> dateMatcher = shiftException.get();
                    if (null != dateMatcher.getReplacement()) {
                        long oldRepeatDays = state.repeatDays;
                        state.repeatDays = Duration.between(state.startDate, state.endDate).toDays() + 1;
                        addShiftsFrom(state, Arrays.asList(dateMatcher
                                .getReplacement()));
                        state.repeatDays = oldRepeatDays;
                    }
                } else {
                    shiftException = state.universalExceptions.stream().filter((dm) -> dm.test(clone)).findFirst();
                    if (shiftException.isPresent()) {
                        DateMatcher<ShiftInfo> dateMatcher = shiftException.get();
                        if (null != dateMatcher.getReplacement()) {
                            long oldRepeatDays = state.repeatDays;
                            state.repeatDays = Duration.between(state.startDate, state.endDate).toDays() + 1;
                            addShiftsFrom(state, Arrays.asList(dateMatcher
                                    .getReplacement()));
                            state.repeatDays = oldRepeatDays;
                        }
                    } else {
                        addShift(state, shiftInfo, startDate, endDate);
                    }
                }
            }
        }
    }

    private static void addShift(ParserState state, ShiftInfo shiftInfo, LocalDateTime startDate, LocalDateTime endDate)
            throws ParserException {
        TimeSlot timeslot = new TimeSlot(state.tenantId, startDate, endDate);
        for (IdOrGroup id : shiftInfo.getSpots()) {
            if (id.getIsGroup()) {
                for (Spot spot : state.spotGroupMap.get(id.getId())) {
                    state.shiftsOut.add(new Shift(state.tenantId, spot, timeslot));
                }
            }
            else {
                Spot spot = state.spotMap.get(id.getId());
                if (null == spot) {
                    throw new ParserException("spot is null! id: " + id.getId());
                }
                state.shiftsOut.add(new Shift(state.tenantId, spot, timeslot));
            }
        }
        
        for (EmployeeTimeSlotInfo employeeInfo : shiftInfo.getEmployees()) {
            List<DateMatcher<EmployeeAvailabilityState>> exceptions = (null != employeeInfo.getAvailabilityConditions())
                    ? employeeInfo.getAvailabilityConditions().stream()
                    .map((e) -> {
                        try {
                            return DateMatcher.getDateMatcher(e);
                        } catch (Exception bad) {
                            return null;
                        }
                            }).collect(Collectors.toList()) : Collections.emptyList();
            if (exceptions.contains(null)) {
                throw new ParserException("Badly formatted date exception string");
            }
            Optional<DateMatcher<EmployeeAvailabilityState>> employeeStateCond = exceptions.stream()
                    .filter((cond) -> cond.test(state.startDate)).findFirst();
            EmployeeAvailabilityState employeeState = employeeInfo.getDefaultAvailability();

            if (employeeStateCond.isPresent()) {
                employeeState = employeeStateCond.get().getReplacement();
            }

            if (employeeInfo.getId().getIsGroup()) {
                for (Employee employee : state.employeeGroupMap.get(employeeInfo.getId().getId())) {
                    EmployeeAvailability employeeAvailability = new EmployeeAvailability(state.tenantId,
                            employee, timeslot);
                    employeeAvailability.setState(employeeState);
                    state.employeeAvailabilityOut.add(employeeAvailability);
                }
            } else {
                EmployeeAvailability employeeAvailability = new EmployeeAvailability(state.tenantId,
                        state.employeeMap.get(employeeInfo.getId().getId()), timeslot);
                employeeAvailability.setState(employeeState);
                state.employeeAvailabilityOut.add(employeeAvailability);
            }

        }
    }

    private static class ParserState {

        Integer tenantId;
        List<Shift> shiftsOut;
        List<EmployeeAvailability> employeeAvailabilityOut;
        List<DateMatcher<ShiftInfo>> universalExceptions;
        Map<Long, Spot> spotMap;
        Map<Long, Employee> employeeMap;
        Map<Long, List<Spot>> spotGroupMap;
        Map<Long, List<Employee>> employeeGroupMap;
        LocalDateTime baseDate;
        LocalDateTime startDate;
        LocalDateTime endDate;
        long repeatDays, repeatWeeks, repeatMonths, repeatYears;
    }

    public static class ParserOut {

        List<Shift> shiftsOut;
        List<EmployeeAvailability> employeeAvailabilityOut;

        public List<Shift> getShiftsOut() {
            return shiftsOut;
        }

        public List<EmployeeAvailability> getEmployeeAvailabilityOut() {
            return employeeAvailabilityOut;
        }
    }
}
