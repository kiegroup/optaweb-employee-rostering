package org.optaweb.employeerostering.service.rotation;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.rotation.Seat;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.roster.RosterService;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.optaweb.employeerostering.service.spot.SpotService;
import org.optaweb.employeerostering.service.tenant.TenantService;

@ApplicationScoped
public class RotationService extends AbstractRestService {

    TimeBucketRepository timeBucketRepository;
    RosterService rosterService;
    TenantService tenantService;
    SpotService spotService;
    SkillService skillService;
    EmployeeService employeeService;

    @Inject
    public RotationService(Validator validator,
            TimeBucketRepository timeBucketRepository, RosterService rosterService,
            TenantService tenantService, SpotService spotService, SkillService skillService,
            EmployeeService employeeService) {
        super(validator);

        this.timeBucketRepository = timeBucketRepository;
        this.tenantService = tenantService;
        this.rosterService = rosterService;
        this.spotService = spotService;
        this.skillService = skillService;
        this.employeeService = employeeService;
    }

    private Set<Skill> getRequiredSkillSet(Integer tenantId, TimeBucketView timeBucketView) {
        return timeBucketView.getAdditionalSkillSetIdList()
                .stream().map(id -> skillService.getSkill(tenantId, id))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public List<TimeBucketView> getTimeBucketList(@Min(0) Integer tenantId) {
        return timeBucketRepository.findAllByTenantId(tenantId)
                .stream()
                .map(TimeBucketView::new)
                .collect(Collectors.toList());
    }

    public TimeBucketView getTimeBucket(@Min(0) Integer tenantId, @Min(0) Long id) {
        TimeBucket timeBucket = timeBucketRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("No TimeBucket entity found with ID (" + id + ")."));

        validateBean(tenantId, timeBucket);
        return new TimeBucketView(timeBucket);
    }

    @Transactional
    public Boolean deleteTimeBucket(@Min(0) Integer tenantId, @Min(0) Long id) {
        Optional<TimeBucket> timeBucketOptional = timeBucketRepository.findByIdOptional(id);

        if (!timeBucketOptional.isPresent()) {
            return false;
        }

        validateBean(tenantId, timeBucketOptional.get());
        timeBucketRepository.deleteById(id);
        return true;
    }

    @Transactional
    public TimeBucketView createTimeBucket(@Min(0) Integer tenantId, @Valid TimeBucketView timeBucketView) {
        Spot spot = spotService.getSpot(tenantId, timeBucketView.getSpotId());
        Set<Skill> additionalSkillSet = getRequiredSkillSet(tenantId, timeBucketView);
        Integer rotationLength = rosterService.getRosterState(tenantId).getRotationLength();

        Set<DayOfWeek> repeatOnDaySet = new HashSet<>(timeBucketView.getRepeatOnDaySetList());
        TimeBucket timeBucket;

        if (timeBucketView.getSeatList() != null) {
            List<Seat> seatList = timeBucketView.getSeatList().stream()
                    .map(seat -> {
                        if (seat.getEmployeeId() != null) {
                            Employee employee = employeeService.getEmployee(tenantId, seat.getEmployeeId());
                            return new Seat(seat.getDayInRotation(), employee);
                        } else {
                            return new Seat(seat.getDayInRotation(), null);
                        }
                    }).collect(Collectors.toList());
            timeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, seatList);
        } else {
            DayOfWeek startOfWeek = tenantService.getRosterConstraintConfiguration(tenantId).getWeekStartDay();
            timeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, startOfWeek, rotationLength);
        }

        validateBean(tenantId, timeBucket);
        timeBucketRepository.persist(timeBucket);
        return new TimeBucketView(timeBucket);
    }

    @Transactional
    public TimeBucketView updateTimeBucket(@Min(0) Integer tenantId, @Valid TimeBucketView timeBucketView) {
        Spot spot = spotService.getSpot(tenantId, timeBucketView.getSpotId());
        Set<Skill> additionalSkillSet = getRequiredSkillSet(tenantId, timeBucketView);
        Integer rotationLength = rosterService.getRosterState(tenantId).getRotationLength();

        Set<DayOfWeek> repeatOnDaySet = new HashSet<>(timeBucketView.getRepeatOnDaySetList());
        TimeBucket newTimeBucket;

        if (timeBucketView.getSeatList() != null) {
            List<Seat> seatList = timeBucketView.getSeatList().stream()
                    .map(seat -> {
                        if (seat.getEmployeeId() != null) {
                            Employee employee = employeeService.getEmployee(tenantId, seat.getEmployeeId());
                            return new Seat(seat.getDayInRotation(), employee);
                        } else {
                            return new Seat(seat.getDayInRotation(), null);
                        }
                    }).collect(Collectors.toList());
            newTimeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, seatList);
        } else {
            DayOfWeek startOfWeek = tenantService.getRosterConstraintConfiguration(tenantId).getWeekStartDay();
            newTimeBucket = new TimeBucket(timeBucketView.getTenantId(),
                    spot, timeBucketView.getStartTime(), timeBucketView.getEndTime(),
                    additionalSkillSet, repeatOnDaySet, startOfWeek, rotationLength);
        }
        newTimeBucket.setId(timeBucketView.getId());
        newTimeBucket.setVersion(timeBucketView.getVersion());

        validateBean(tenantId, newTimeBucket);

        TimeBucket oldTimeBucket = timeBucketRepository
                .findByIdOptional(newTimeBucket.getId())
                .orElseThrow(() -> new EntityNotFoundException("TimeBucket entity with ID (" +
                        newTimeBucket.getId() + ") not found."));

        if (!oldTimeBucket.getTenantId().equals(newTimeBucket.getTenantId())) {
            throw new IllegalStateException("TimeBucket entity with tenantId (" + oldTimeBucket.getTenantId() +
                    ") cannot change tenants.");
        }

        oldTimeBucket.setValuesFromTimeBucket(newTimeBucket);

        // Flush to increase version number before we duplicate it to TimeBucketView
        timeBucketRepository.persistAndFlush(oldTimeBucket);

        return new TimeBucketView(oldTimeBucket);
    }
}
