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

import '@patternfly/react-core/dist/styles/base.css';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { SpinnerIcon } from '@patternfly/react-icons';
import './index.css';
import { I18nextProvider } from 'react-i18next';
import { configureStore } from 'store';
import App from './ui/App';

// import i18n (needs to be bundled)
import i18n from './i18n';

const path = window.location.pathname;
let windowTenantId = 0;
if (path.indexOf('/', 1) > 0) {
  windowTenantId = parseInt(path.substring(1, path.indexOf('/', 1)), 10);
}

const store = configureStore({
  restBaseURL: `${process.env.REACT_APP_BACKEND_URL}/rest`,
}, {
  tenantData: {
    currentTenantId: windowTenantId,
    tenantList: [],
    timezoneList: [],
  },
});

const LoadingSpinner: React.FC = () => (
  <>
    <SpinnerIcon />
    Loading...
  </>
);

ReactDOM.render(
  <I18nextProvider i18n={i18n}>
    <Provider store={store}>
      <BrowserRouter>
        <React.Suspense fallback={<LoadingSpinner />}>
          <App />
        </React.Suspense>
      </BrowserRouter>
    </Provider>
  </I18nextProvider>,
  document.getElementById('root') as HTMLElement,
);
