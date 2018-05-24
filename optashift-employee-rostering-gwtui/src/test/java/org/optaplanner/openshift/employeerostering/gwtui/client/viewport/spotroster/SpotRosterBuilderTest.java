package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.spotroster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.EventManager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.AbstractViewportTest;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@RunWith(GwtMockitoTestRunner.class)
public class SpotRosterBuilderTest extends AbstractViewportTest {

    @InjectMocks
    private SpotRosterPageViewportBuilder builder;

    @Mock
    private SpotRosterPageViewport viewport;

    @Mock
    private PromiseUtils promiseUtils;

    @Mock
    private CommonUtils commonUtils;

    @Mock
    private TenantStore tenantStore;

    @Mock
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    @Mock
    private EventManager eventManager;
    
    @Mock
    private Lockable<Map<Long, Lane<LocalDateTime, SpotRosterMetadata>>> lockableLaneMap;
    
    private Map<Long, Lane<LocalDateTime, SpotRosterMetadata>> laneMap;

    private SpotRosterView spotRosterView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(lockableLaneMap.acquireIfPossible(Mockito.any())).thenAnswer( new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Consumer arg = invocation.getArgument(0);
                arg.accept(laneMap);
                return true;
                }
            });
        Mockito.when(tenantStore.getCurrentTenantId()).thenReturn(0);
        Mockito.when(commonUtils.flatten(Mockito.any())).thenCallRealMethod();
        Mockito.when(promiseUtils.promise(Mockito.any())).thenAnswer(new Answer<Promise>() {

            @Override
            public Promise answer(InvocationOnMock invocation) throws Throwable {
                PromiseExecutorCallbackFn arg = invocation.getArgument(0);
                return promise(arg);
            }

        });
        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SpotRosterView spv = invocation.getArgument(0);
                RepeatingCommand rc = builder.getWorkerCommand(spv, lockableLaneMap, 0);
                while (rc.execute()) {
                    // Wait for worker to finish
                }
                return null;
            }
        }).when(viewport).refresh(Mockito.any());

        builder = Mockito.spy(builder);
        Mockito.doAnswer(new Answer() {

            @SuppressWarnings("unchecked")
            public Promise<SpotRosterView> answer(InvocationOnMock invocation) {
                return promise((res, rej) -> {
                    res.onInvoke(resolveValue(spotRosterView));
                });
            }
        }).when(builder).getSpotRosterView();
         
    }

    @Test
    public void testBuildingSimpleRoster() {
        spotRosterView = new SpotRosterView();
        Spot spotA = new Spot(0, "A", Collections.emptySet());
        spotA.setId(0L);
        Spot spotB = new Spot(0, "B", Collections.emptySet());
        spotA.setId(1L);

        spotRosterView.setSpotList(Arrays.asList(spotA, spotB));
        spotRosterView.setEmployeeList(Collections.emptyList());
        spotRosterView.setStartDate(LocalDate.of(2000, 1, 1));
        spotRosterView.setEndDate(LocalDate.of(2000, 1, 8));
        spotRosterView.setTenantId(0);

        RosterState rosterState = new RosterState();
        rosterState.setDraftLength(7);
        rosterState.setFirstDraftDate(LocalDate.of(2000, 1, 4));
        rosterState.setLastHistoricDate(LocalDate.of(2000, 1, 1));
        rosterState.setPublishLength(7);
        rosterState.setPublishNotice(7);
        rosterState.setRotationLength(7);
        rosterState.setUnplannedRotationOffset(0);
        spotRosterView.setRosterState(rosterState);

        Map<Long, List<ShiftView>> spotIdToShiftViewMap = new HashMap<>();
        ShiftView spotAShiftView = new ShiftView(0, spotA,
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT),
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT.plusHours(8)));
        ShiftView spotBShiftView = new ShiftView(0, spotB,
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.MIDNIGHT),
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.MIDNIGHT.plusHours(8)));
        spotIdToShiftViewMap.put(spotA.getId(), Collections.singletonList(spotAShiftView));
        spotIdToShiftViewMap.put(spotB.getId(), Collections.singletonList(spotBShiftView));
        spotRosterView.setSpotIdToShiftViewListMap(spotIdToShiftViewMap);
        
        laneMap = new HashMap<>();
        Lane<LocalDateTime, SpotRosterMetadata> laneAMock = Mockito.mock(Lane.class);
        Lane<LocalDateTime, SpotRosterMetadata> laneBMock = Mockito.mock(Lane.class);

        laneMap.put(spotA.getId(), laneAMock);
        laneMap.put(spotB.getId(), laneBMock);

        builder.buildSpotRosterViewport(viewport).then((v) -> {
            Mockito.verify(laneAMock).addOrUpdateGridObject(Mockito.eq(ShiftGridObject.class), Mockito.isNull(), Mockito.any(), Mockito.any());
            Mockito.verify(laneBMock).addOrUpdateGridObject(Mockito.eq(ShiftGridObject.class), Mockito.isNull(), Mockito.any(), Mockito.any());
            // TODO: Add more verification tests
            return null;
        });
    }

}
