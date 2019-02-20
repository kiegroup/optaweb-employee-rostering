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

package org.optaweb.employeerostering.gwtui.client.popups;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;

@ApplicationScoped
public class PopupFactory {
    
    private static FormPopup formPopup = null;
    
    public Optional<FormPopup> getFormPopup(IsElement content) {
        if (formPopup == null || !formPopup.isShowing()) {
            formPopup = new FormPopup(content);
            return Optional.of(formPopup);
        }
        return Optional.empty();
    }
    
    public void showErrorPopup(String message) {
        ErrorPopup.show(message);
    }
}
