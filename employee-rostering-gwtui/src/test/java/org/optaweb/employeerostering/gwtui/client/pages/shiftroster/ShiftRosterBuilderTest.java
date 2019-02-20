/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

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
import elemental2.promise.Promise.PromiseExecutorCallbackFn;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.optaweb.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.Lockable;
import org.optaweb.employeerostering.gwtui.client.pages.AbstractViewportTest;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ShiftRosterBuilderTest extends AbstractViewportTest {

    @InjectMocks
    private ShiftRosterPageViewportBuilder builder;

    @Mock
    private ShiftRosterPageViewport viewport;

    @Mock
    private PromiseUtils promiseUtils;

    @Mock
    private TenantStore tenantStore;

    @Mock
    private LoadingSpinner loadingSpinner;

    @Mock
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    @Mock
    private EventManager eventManager;

    @Mock
    private Lockable<Map<Long, Lane<LocalDateTime, ShiftRosterMetadata>>> lockableLaneMap;

    private Map<Long, Lane<LocalDateTime, ShiftRosterMetadata>> laneMap;

    private ShiftRosterView shiftRosterView;

    @Before
    public void setup() {
        //MockitoAnnotations.initMocks(this);
        when(lockableLaneMap.acquireIfPossible(any())).thenAnswer(invocation -> {
            Consumer arg = invocation.getArgument(0);
            arg.accept(laneMap);
            return true;
        });
        when(tenantStore.getCurrentTenantId()).thenReturn(0);
        when(promiseUtils.promise(any())).thenAnswer(invocation -> {
            PromiseExecutorCallbackFn arg = invocation.getArgument(0);
            return promise(arg);
        });
        doAnswer(invocation -> {
            ShiftRosterView spv = invocation.getArgument(0);
            RepeatingCommand rc = builder.getWorkerCommand(spv, lockableLaneMap, 0);
            while (rc.execute()) {
                // Wait for worker to finish
            }
            return null;
        }).when(viewport).refresh(any());

        builder = spy(builder);

        // Mockito disallows when(spy.method()).thenAnswer(answer)
        // But allows doAnswer(answer).when(spy).method()
        doAnswer(invocation -> {
            return promise((res, rej) -> {
                res.onInvoke(resolveValue(shiftRosterView));
            });
        }).when(builder).getShiftRosterView();
    }

    @Test
    public void testBuildingSimpleRoster() {
        shiftRosterView = new ShiftRosterView();
        Spot spotA = new Spot(0, "A", Collections.emptySet());
        spotA.setId(0L);
        Spot spotB = new Spot(0, "B", Collections.emptySet());
        spotA.setId(1L);

        shiftRosterView.setSpotList(Arrays.asList(spotA, spotB));
        shiftRosterView.setEmployeeList(Collections.emptyList());
        shiftRosterView.setStartDate(LocalDate.of(2000, 1, 1));
        shiftRosterView.setEndDate(LocalDate.of(2000, 1, 8));
        shiftRosterView.setTenantId(0);

        RosterState rosterState = new RosterState();
        rosterState.setDraftLength(7);
        rosterState.setFirstDraftDate(LocalDate.of(2000, 1, 4));
        rosterState.setLastHistoricDate(LocalDate.of(2000, 1, 1));
        rosterState.setPublishNotice(7);
        rosterState.setRotationLength(7);
        rosterState.setUnplannedRotationOffset(0);
        shiftRosterView.setRosterState(rosterState);

        Map<Long, List<ShiftView>> spotIdToShiftViewMap = new HashMap<>();
        ShiftView spotAShiftView = new ShiftView(0, spotA,
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT),
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIDNIGHT.plusHours(8)));
        ShiftView spotBShiftView = new ShiftView(0, spotB,
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.MIDNIGHT),
                                                 LocalDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.MIDNIGHT.plusHours(8)));
        spotIdToShiftViewMap.put(spotA.getId(), Collections.singletonList(spotAShiftView));
        spotIdToShiftViewMap.put(spotB.getId(), Collections.singletonList(spotBShiftView));
        shiftRosterView.setSpotIdToShiftViewListMap(spotIdToShiftViewMap);

        laneMap = new HashMap<>();
        Lane<LocalDateTime, ShiftRosterMetadata> laneAMock = mock(Lane.class);
        Lane<LocalDateTime, ShiftRosterMetadata> laneBMock = mock(Lane.class);

        laneMap.put(spotA.getId(), laneAMock);
        laneMap.put(spotB.getId(), laneBMock);

        builder.buildShiftRosterViewport(viewport).then((v) -> {
            verify(laneAMock).addOrUpdateGridObject(eq(ShiftGridObject.class), isNull(), any(), any());
            verify(laneBMock).addOrUpdateGridObject(eq(ShiftGridObject.class), isNull(), any(), any());
            // TODO: Add more verification tests
            return null;
        });
    }
}
