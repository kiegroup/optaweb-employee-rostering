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

import { DomainObject } from './DomainObject';

/**
 * DomainObjectView is a mapped type that converts any
 * DomainObject into their corresponding view. In particular:
 *
 * - If a field is a DomainObject, that field is mapped to a number
 *   (the DomainObject's id).
 *
 * - If a field is a nullable DomainObject, that field is mapped to a number|null
 *   (the DomainObject's id, if present, or null, if not present).
 *
 * - If a field is a collection of DomainObjects, that field is mapped to a number[]
 *   (an array of the DomainObjects' ids).
 *
 * Since TypeScript does not support field renaming as of yet
 * (https://github.com/Microsoft/TypeScript/issues/12754),
 * if P is a property that is a DomainObject, then its corresponding
 * property has the same name. Below is an example of a DomainObject and its
 * corresponding DomainObjectView:
 *
 * @example
 * DomainObject (Employee)
 * {
 *   id: 1,
 *   version: 0,
 *   name: "Amy",
 *   contract: { id: 10, version: 0, name: "A contract", ... },
 *   skillProficiencySet: [
 *     { id: 12, version: 0, name: "Skill A" },
 *     { id: 20, version: 0, name: "Skill B" }
 *   ]
 * }
 *
 * DomainObjectView (DomainObjectView<Employee>)
 * {
 *   id: 1,
 *   version: 0,
 *   name: "Amy",
 *   contract: 10,
 *   skillProficiencySet: [12, 20]
 * }
 *
 * @see {DomainObject}
 * @exports
*/

type DomainObjectView<T extends DomainObject> = {
  [K in keyof T]:
  T[K] extends DomainObject[]? number[] :
    T[K] extends DomainObject? number :
      T[K] extends (DomainObject | null)? number | null :
        T[K];
}

// eslint-disable-next-line no-undef
export default DomainObjectView;
