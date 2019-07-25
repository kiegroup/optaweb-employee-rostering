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
import App from './ui/App';

// import i18n (needs to be bundled)
import './i18n';
import { configureStore } from 'store';

const store = configureStore({
  restBaseURL: '/rest'
});

const LoadingSpinner: React.FC = () => (
  <>
    <SpinnerIcon />
    Loading...
  </>
);

ReactDOM.render(
  <Provider store={store}>
    <BrowserRouter>
      <React.Suspense fallback={<LoadingSpinner />}> 
        <App />
      </React.Suspense>
    </BrowserRouter>
  </Provider>,
  document.getElementById('root') as HTMLElement,
);