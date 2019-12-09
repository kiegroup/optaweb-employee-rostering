/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.standalone;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Arcane Spring Boot Magic from https://stackoverflow.com/a/42998817
        // From what I can tell:
        // registry.addViewController(String) add a new handler for URLs,
        // and controller.setViewName(String) tell the handler what to do.
        // In this case, controller.setViewName("forward:/") tell the handler
        // to redirect any requests it handles to "/" (which is index.html,
        // where our frontend exists.
        
        // probably handles requests with no trailing paths (/app, /admin,
        // but not /employees/0)
        registry.addViewController("/{spring:\\w+}") 
              .setViewName("forward:/");
        
        // probably handles requests with trailing paths (/employees/0 but
        // not /app)
        registry.addViewController("/**/{spring:\\w+}")
              .setViewName("forward:/");
        
        // I have no idea what this does; does react have javascript files
        // with a $ suffix?
        registry.addViewController("/{spring:\\w+}/**{spring:?!(\\.js|\\.css)$}") 
              .setViewName("forward:/");
    }

}